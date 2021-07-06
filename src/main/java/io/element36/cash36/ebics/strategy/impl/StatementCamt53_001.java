package io.element36.cash36.ebics.strategy.impl;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.element36.cash36.EbicsTools;
import io.element36.cash36.ebics.config.AppConfig;
import io.element36.cash36.ebics.config.EbicsMode;
import io.element36.cash36.ebics.dto.StatementDTO;
import io.element36.cash36.ebics.dto.TransactionDTO;
import io.element36.cash36.ebics.generated.camt_053_001.AccountStatement4;
import io.element36.cash36.ebics.generated.camt_053_001.BalanceType12Code;
import io.element36.cash36.ebics.generated.camt_053_001.CashBalance3;
import io.element36.cash36.ebics.generated.camt_053_001.CreditDebitCode;
import io.element36.cash36.ebics.generated.camt_053_001.DateAndDateTimeChoice;
import io.element36.cash36.ebics.generated.camt_053_001.Document;
import io.element36.cash36.ebics.generated.camt_053_001.EntryTransaction4;
import io.element36.cash36.ebics.strategy.Statement;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatementCamt53_001 implements Statement {

    @Autowired
	EbicsMode ebicsMode;

    @Autowired
	AppConfig appConfig;

	@Value("${ebics.proxy.statement}")
	String proxyStatementFile;

    public List<StatementDTO> process(String z53OutRessource) throws IOException {
        return this.process(new File(z53OutRessource));
    }

    public List<StatementDTO> process(File z53OutFile) throws IOException {
        // TODO: check for zipped and non-zipped files
        // Unzip Output File
    	log.debug(" unzipping "+z53OutFile);
        List<File> statements;
        if (z53OutFile.exists() && z53OutFile.getParentFile().getName().equals("error")) {
            statements = new EbicsTools().unzip(z53OutFile, appConfig.outputDir + "/error/" + FilenameUtils.removeExtension(z53OutFile.getName()));
        } else {
        	String unzipDir=appConfig.outputDir + "/" + FilenameUtils.removeExtension(z53OutFile.getName());
            statements = new EbicsTools().unzip(z53OutFile, unzipDir);
        }

        Map<String, List<Document>> processedFiles = new HashMap<>();
        statements.stream().forEach(statement -> {
            // Captain Unmarshall into XML
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(Document.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                Document document = (Document) JAXBIntrospector.getValue(unmarshaller.unmarshal(statement));

                List<Document> documents = processedFiles.get(document.getBkToCstmrStmt().getStmt().get(0).getAcct().getId().getIBAN());
                if (documents == null) {
                    documents = new ArrayList<>();
                }
                documents.add(document);
                processedFiles.put(document.getBkToCstmrStmt().getStmt().get(0).getAcct().getId().getIBAN(), documents);
            } catch (JAXBException e) {
            	log.error("JAXBException",e);
            }
        });

        List<StatementDTO> result = new ArrayList<>();

        // IBAN
        // Company account CH4308307000289537312 - will not be processed
        // CHF36 account CH9008307000289537339 - will be processed
        // EUR36 account CH2108307000289537320 - will be processed
        //List<String> accounts = Arrays.asList("CH9008307000289537339", "CH2108307000289537320");
        //accounts.stream().forEach(account -> {
        for (String acc:processedFiles.keySet()) {
            List<Document> accountDocument = processedFiles.get(acc);
            if (appConfig.getIgnoreAccounts().contains(acc)) {
            	log.info(" ignore account "+acc);
            	continue;
            } else log.info(" process account "+acc);
            
            if (accountDocument != null) {
                accountDocument.stream().forEach(document -> {
                    List<AccountStatement4> stmts = document.getBkToCstmrStmt().getStmt();
                    stmts.stream().forEach(stmt -> {
                        StatementDTO.StatementDTOBuilder builder = StatementDTO.builder();

                        // Set IBAN
                        builder.iban(stmt.getAcct().getId().getIBAN());

                        // Set Balances
                        for (int i = 0; i < stmt.getBal().size(); i++) {
                            CashBalance3 balance = stmt.getBal().get(i);
                            BalanceType12Code code = balance.getTp().getCdOrPrtry().getCd();
                            if (code == BalanceType12Code.CLBD) {
                                builder.balanceCL(balance.getAmt().getValue());
                                builder.balanceCLCurrency(balance.getAmt().getCcy());
                                DateAndDateTimeChoice date = balance.getDt();
                                builder.balanceCLDate(LocalDate.of(date.getDt().getYear(), date.getDt().getMonth(),
                                        date.getDt().getDay()));
                            }
                            if (code == BalanceType12Code.OPBD) {
                                builder.balanceOP(balance.getAmt().getValue());
                                builder.balanceOPCurrency(balance.getAmt().getCcy());
                            }
                        }

                        // Set Dates
                        XMLGregorianCalendar bookgDt=stmt.getNtry().get(0).getBookgDt().getDt();
                        XMLGregorianCalendar valDt=stmt.getNtry().get(0).getValDt().getDt();
                        builder .bookingDate(LocalDate.of(bookgDt.getYear(),bookgDt.getMonth(),bookgDt.getDay()))
                                .validationDate(LocalDate.of(valDt.getYear(),valDt.getMonth(),valDt.getDay()));

                        // Process Transactions
                        List<TransactionDTO> incoming = new ArrayList<>();
                        List<TransactionDTO> outgoing = new ArrayList<>();

                        stmt.getNtry().stream().forEach(entry -> {
                            CreditDebitCode code = entry.getCdtDbtInd();
                            if (code == CreditDebitCode.CRDT) {
                                entry.getNtryDtls().stream().forEach(details -> {
                                    EntryTransaction4 transaction = details.getTxDtls().get(0);

                                    TransactionDTO.TransactionDTOBuilder builderIn = TransactionDTO.builder();
                                    // Prepare addrLine as ArrayList
                                    ArrayList<String> addrLine = new ArrayList<>();
                                    if (transaction.getRltdPties() != null) {
                                        if (transaction.getRltdPties().getDbtr() != null) {
                                            if (transaction.getRltdPties().getDbtr().getPstlAdr().getAdrLine().size() > 0) {
                                                addrLine.addAll(transaction.getRltdPties().getDbtr().getPstlAdr().getAdrLine());
                                            }
                                        }
                                    }
                                    builderIn.addrLine(addrLine);
                                    builderIn.amount(transaction.getAmt().getValue());
                                    builderIn.currency(transaction.getAmt().getCcy());
                                    builderIn.iban(transaction.getRltdPties().getDbtrAcct().getId().getIBAN());
                                    if (transaction.getRefs() != null) {
                                        builderIn.endToEndId(transaction.getRefs().getEndToEndId());
                                        builderIn.instrId(transaction.getRefs().getInstrId());
                                        builderIn.msgId(transaction.getRefs().getMsgId());
                                        builderIn.pmtInfId(transaction.getRefs().getPmtInfId());
                                    }
                                    if (transaction.getRmtInf() != null) {
                                        if (transaction.getRmtInf().getUstrd() != null) {
                                            builderIn.reference(transaction.getRmtInf().getUstrd().get(0));
                                        }
                                    }
                                    incoming.add(builderIn.build());
                                });
                            }
                            if (code == CreditDebitCode.DBIT) {
                                entry.getNtryDtls().stream().forEach(details -> {
                                    EntryTransaction4 transaction = details.getTxDtls().get(0);
                                    TransactionDTO.TransactionDTOBuilder builderOut = TransactionDTO.builder();

                                    if (transaction.getRltdPties() != null) {
                                        if (transaction.getRltdPties().getCdtr() != null) {
                                            builderOut.name(transaction.getRltdPties().getCdtr().getNm());

                                            ArrayList<String> addrLine = new ArrayList<>();
                                            if (transaction.getRltdPties().getCdtr().getPstlAdr().getAdrLine().size() > 0) {
                                                addrLine.addAll(transaction.getRltdPties().getCdtr().getPstlAdr().getAdrLine());
                                            } else {
                                                addrLine.add(transaction.getRltdPties().getCdtr().getPstlAdr().getStrtNm());
                                                addrLine.add(transaction.getRltdPties().getCdtr().getPstlAdr().getBldgNb());
                                                addrLine.add(transaction.getRltdPties().getCdtr().getPstlAdr().getTwnNm());
                                                addrLine.add(transaction.getRltdPties().getCdtr().getPstlAdr().getPstCd());
                                                addrLine.add(transaction.getRltdPties().getCdtr().getPstlAdr().getCtry());
                                            }
                                            builderOut.addrLine(addrLine);
                                        }
                                        if (transaction.getRltdPties().getCdtrAcct() != null) {
                                            builderOut.iban(transaction.getRltdPties().getCdtrAcct().getId().getIBAN());
                                        }
                                    }
                                    builderOut.amount(transaction.getAmt().getValue());
                                    builderOut.currency(transaction.getAmt().getCcy());
                                    if (transaction.getRmtInf() != null) {
                                        if (transaction.getRmtInf().getUstrd() != null &&  transaction.getRmtInf().getUstrd().size()>0 ) {
                                            builderOut.reference(transaction.getRmtInf().getUstrd().get(0));
                                        }
                                    }
                                    outgoing.add(builderOut.build());
                                });
                            }
                        });
                        builder.incomingTransactions(incoming);
                        builder.outgoingTransactions(outgoing);

                        result.add(builder.build());
                    });
                });
            }
        }
        //});
        return result;
    }
    
}
