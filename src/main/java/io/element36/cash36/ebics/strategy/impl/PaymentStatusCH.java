package io.element36.cash36.ebics.strategy.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;

import io.element36.cash36.EbicsTools;
import io.element36.cash36.ebics.config.AppConfig;
import io.element36.cash36.ebics.dto.PaymentStatusReportDTO;
import io.element36.cash36.ebics.generated.pain_002_001_03_ch_02.Document;
import io.element36.cash36.ebics.generated.pain_002_001_03_ch_02.ObjectFactory;
import io.element36.cash36.ebics.generated.pain_002_001_03_ch_02.OriginalPaymentInformation1CH;
import io.element36.cash36.ebics.generated.pain_002_001_03_ch_02.StatusReasonInformation8CH;
import io.element36.cash36.ebics.generated.pain_002_001_03_ch_02.TransactionGroupStatus3Code;
import io.element36.cash36.ebics.strategy.PaymentStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentStatusCH implements PaymentStatus {

  @Autowired AppConfig appConfig; //

  public List<PaymentStatusReportDTO> process(File z01OutFile) throws IOException {
    log.debug("processZipFile {}", z01OutFile);
    // Unzip Output File
    List<File> reports =
        new EbicsTools()
            .unzip(
                z01OutFile,
                appConfig.outputDir + "/" + FilenameUtils.removeExtension(z01OutFile.getName()));

    Map<String, List<Document>> processedFiles = new HashMap<>();
    log.trace(" unmarshalling xml");
    reports
        .stream()
        .forEach(
            report -> {
              // Captain Unmarshall into XML
              try {
                JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                Document document =
                    (Document) JAXBIntrospector.getValue(unmarshaller.unmarshal(report));

                List<Document> documents =
                    processedFiles.get(
                        document.getCstmrPmtStsRpt().getOrgnlGrpInfAndSts().getOrgnlMsgId());
                if (documents == null) {
                  documents = new ArrayList<>();
                }
                log.trace(
                    " add doc , msgid:"
                        + document.getCstmrPmtStsRpt().getOrgnlGrpInfAndSts().getOrgnlMsgId());
                documents.add(document);
                processedFiles.put(
                    document.getCstmrPmtStsRpt().getOrgnlGrpInfAndSts().getOrgnlMsgId(), documents);
              } catch (JAXBException e) {
                log.error("ERROR processZipFile", e);
              }
            });
    log.trace(" done; make DTOs " + processedFiles.size());
    List<PaymentStatusReportDTO> result = new ArrayList<>();

    processedFiles
        .keySet()
        .stream()
        .forEach(
            msgId -> {
              PaymentStatusReportDTO.PaymentStatusReportDTOBuilder builder =
                  PaymentStatusReportDTO.builder();
              List<Document> statusReports = processedFiles.get(msgId);

              if (statusReports != null) {
                statusReports
                    .stream()
                    .forEach(
                        statusReport -> {
                          String orgnlMsgId =
                              statusReport
                                  .getCstmrPmtStsRpt()
                                  .getOrgnlGrpInfAndSts()
                                  .getOrgnlMsgId();
                          builder.msgId(orgnlMsgId);
                          log.trace(" make DTO and add to result " + orgnlMsgId);

                          TransactionGroupStatus3Code grpSts =
                              statusReport.getCstmrPmtStsRpt().getOrgnlGrpInfAndSts().getGrpSts();
                          if (grpSts.equals(TransactionGroupStatus3Code.ACTC)) {
                            builder.paymentValid(true);
                          }
                          if (grpSts.equals(TransactionGroupStatus3Code.ACCP)) {
                            builder.paymentAccepted(true);
                          }

                          if (grpSts.equals(TransactionGroupStatus3Code.ACWC)
                              || grpSts.equals(TransactionGroupStatus3Code.PART)
                              || grpSts.equals(TransactionGroupStatus3Code.RJCT)) {

                            builder.paymentAccepted(false);
                            // Level A errors
                            List<StatusReasonInformation8CH> stsRsnInf =
                                statusReport
                                    .getCstmrPmtStsRpt()
                                    .getOrgnlGrpInfAndSts()
                                    .getStsRsnInf();
                            builder.errorCodesALevel(
                                stsRsnInf
                                    .stream()
                                    .map(s -> s.getRsn().getCd())
                                    .collect(Collectors.toList()));

                            List<OriginalPaymentInformation1CH> orgnlPmtInfAndSts =
                                statusReport.getCstmrPmtStsRpt().getOrgnlPmtInfAndSts();
                            List<String> errorBLevel =
                                orgnlPmtInfAndSts
                                    .stream()
                                    .flatMap(
                                        o -> o.getStsRsnInf().stream().map(s -> s.getRsn().getCd()))
                                    .collect(Collectors.toList());
                            builder.errorCodesBLevel(errorBLevel);

                            List<String> errorCLevel =
                                orgnlPmtInfAndSts
                                    .stream()
                                    .flatMap(
                                        o ->
                                            o.getTxInfAndSts()
                                                .stream()
                                                .flatMap(
                                                    s ->
                                                        s.getStsRsnInf()
                                                            .stream()
                                                            .map(r -> r.getRsn().getCd())))
                                    .collect(Collectors.toList());
                            builder.errorCodesCLevel(errorCLevel);
                          }
                        });
                log.trace("build and add dto ");
                result.add(builder.build());
              }
            });
    log.debug("result size " + result.size());
    return result;
  }
}
