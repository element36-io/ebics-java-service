package io.element36.cash36.ebics.service.impl;

import io.element36.cash36.ebics.config.AppConfig;
import io.element36.cash36.ebics.config.AppConfigLibeufin;
import io.element36.cash36.ebics.dto.StatementDTO;
import io.element36.cash36.ebics.dto.TransactionDTO;
import io.element36.cash36.ebics.dto.libeufin.*;
import io.element36.cash36.ebics.service.EbicsStatementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Profile({"sandbox"})
@Slf4j
public class EbicsStatemnetServiceLibeufin implements EbicsStatementService {

  @Autowired AppConfig appConfig;

  @Autowired AppConfigLibeufin libeufinConfig;

  @Override
  public List<StatementDTO> getBankStatement() throws IOException  {
    List<StatementDTO> result = new ArrayList<StatementDTO>();

    RestTemplate restTemplate = new RestTemplate();
    restTemplate
        .getInterceptors()
        .add(new BasicAuthorizationInterceptor(libeufinConfig.username, libeufinConfig.password));

    // first fetch transactions from sandbox to nexus
    this.log.debug(" fetch txns from sandbox to nexus ");

    try {

      ResponseEntity<FetchTranactionsResponse> resp;
      resp =
          restTemplate.postForEntity(
              new URI(
                  libeufinConfig.nexus_url
                      + "/bank-accounts/"
                      + libeufinConfig.accountName
                      + "/fetch-transactions"),
              null,
              FetchTranactionsResponse.class);

      this.log.debug(" respose --> " + resp.getBody().toString());
      if (resp.getStatusCode() != HttpStatus.OK)
        throw new RuntimeException("Error fetching transactions from sandbox");
      
      int newTxns=resp.getBody().getNewTransactions();
      if (newTxns==0) {
	  return new ArrayList<StatementDTO>(); 
      }
      
      ResponseEntity<TransactionsResponse> transaction;
      transaction =
          restTemplate.getForEntity(
              new URI(
                  libeufinConfig.nexus_url
                      + "/bank-accounts/"
                      + libeufinConfig.accountName
                      + "/transactions"),
              TransactionsResponse.class);
      this.log.debug(" response transactions --> " + transaction.getBody());
      if (resp.getStatusCode() != HttpStatus.OK)
        throw new RuntimeException("Error fetching transactions from nexus");
      
      int totalTxns=transaction.getBody().getTransactions().length;

      
      Transaction[] newTransactions=Arrays.copyOfRange(transaction.getBody().getTransactions(), totalTxns-newTxns, totalTxns);
      if (newTransactions==null || newTransactions.length==0) return result;


	  StatementDTO.StatementDTOBuilder smtResult= StatementDTO.builder().balanceCL(libeufinConfig.accountBalance);
	    for (Transaction txContainer:newTransactions) {

          LocalDate now=LocalDate.now();

          // build incoming transactions
          List<TransactionDTO> incoming= new ArrayList<TransactionDTO>();
          List<TransactionDTO> outgoing= new ArrayList<TransactionDTO>();

          for (Batches batch:txContainer.getBatches()) {
            for (BatchedTransaction tx:batch.getBatchTransactions()) {
              TransactionDTO.TransactionDTOBuilder txTo =
                      TransactionDTO.builder()
                              .msgId("not set")
                              .instrId("not set")
                              .amount(new BigDecimal(tx.getAmount()))
                              .currency(libeufinConfig.accountCurrency)
                              .endToEndId(tx.getEndToEndId())
                              .reference(tx.getUnstructuredRemittanceInformation()); // we dont have this

              if ("CRDT".equals(tx.getCreditDebitIndicator())) {
                txTo.iban(tx.getDetails().getCreditorAccount().getIban());
                incoming.add(txTo.build());
              } else if ("DBT".equals(tx.getCreditDebitIndicator())) {
                txTo.iban(tx.getDetails().getDectorAccount().getIban());
                outgoing.add(txTo.build());
              } else throw new RuntimeException(" not expected this getCreditDevitIndicator value of: "+tx.getCreditDebitIndicator());


            }
          }

          smtResult.balanceCL(libeufinConfig.accountBalance)
                  .balanceCLCurrency(libeufinConfig.accountCurrency)
                  .balanceCLDate(now)
                  .balanceOP(libeufinConfig.accountBalance)
                  .bookingDate(now)
                  .validationDate(now)
                  .balanceOPCurrency(libeufinConfig.accountCurrency)
                  .iban(appConfig.peggingIban)
                  .incomingTransactions(incoming)
                  .outgoingTransactions(outgoing);

          result.add(smtResult.build()); //only one result an pegging account
          this.log.debug(" --> json: "+result.get(0));
      }
      
      
      

    } catch (URISyntaxException e) { // TODO Auto-generated catch block
      this.log.error("wrong URI for sandbox ",e);
      e.printStackTrace();
    }

    return result;
  }
}
