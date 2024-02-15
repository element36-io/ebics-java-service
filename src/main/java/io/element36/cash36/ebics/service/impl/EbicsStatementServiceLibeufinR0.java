package io.element36.cash36.ebics.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import io.element36.cash36.ebics.config.AppConfig;
import io.element36.cash36.ebics.config.AppConfigLibeufin;
import io.element36.cash36.ebics.dto.StatementDTO;
import io.element36.cash36.ebics.dto.TransactionDTO;
import io.element36.cash36.ebics.dto.libeufin.BatchedTransaction;
import io.element36.cash36.ebics.dto.libeufin.Batches;
import io.element36.cash36.ebics.dto.libeufin.FetchTransactionsResponse;
import io.element36.cash36.ebics.dto.libeufin.Transaction;
import io.element36.cash36.ebics.dto.libeufin.TransactionsResponse;
import io.element36.cash36.ebics.service.EbicsStatementService;
import lombok.extern.slf4j.Slf4j;

@Service
@Profile({"sandbox"})
@Slf4j
public class EbicsStatementServiceLibeufinR0 implements EbicsStatementService {

  @Autowired AppConfig appConfig;

  @Autowired AppConfigLibeufin libeufinConfig;

  // https://docs.taler.net/libeufin/api-nexus.html#bank-accounts

  private static HttpEntity<String> entity;
  static {
    String requestJson = "{\"rangeType\":\"since-last\",\"level\":\"all\" }";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    entity = new HttpEntity<String>(requestJson,headers);
    // detail" `tech.libeufin.nexus.server.FetchSpecJson`: known type ids = [all, latest, previous-days, since-last]\n at [Source: (InputStreamReader); line: 1, column: 14]"
  }
  

  @Override
  public List<StatementDTO> getBankStatement() throws IOException {

    RestTemplate restTemplate = new RestTemplate();
    restTemplate
        .getInterceptors()
        .add(new BasicAuthorizationInterceptor(libeufinConfig.username, libeufinConfig.password));

    // first fetch transactions from sandbox to nexus
    this.log.debug(" fetch txns from sandbox to nexus ");
    List<StatementDTO> result = new ArrayList<StatementDTO>();

    try {

      ResponseEntity<FetchTransactionsResponse> resp;
      URI uri=new URI(
        libeufinConfig.nexus_url
            + "/bank-accounts/"
            + appConfig.peggingIban
            + "/fetch-transactions");
      resp =
          restTemplate.postForEntity(
              uri,
              entity,
              FetchTransactionsResponse.class);

      this.log.debug(" call --> " + uri);
      this.log.debug(" response --> " + resp.getBody().toString());
      if (resp.getStatusCode() != HttpStatus.OK)
        throw new RuntimeException("Error fetching transactions from sandbox");

      int newTxns = resp.getBody().getNewTransactions();
      if (newTxns == 0) {
        return new ArrayList<StatementDTO>();
      }

      ResponseEntity<TransactionsResponse> transaction;
      transaction =
          restTemplate.getForEntity(
              new URI(
                  libeufinConfig.nexus_url
                      + "/bank-accounts/"
                      + appConfig.peggingIban
                      + "/transactions"),
              TransactionsResponse.class);
      this.log.debug(" response transactions from nexus --> " + transaction.getBody());
      if (resp.getStatusCode() != HttpStatus.OK)
        throw new RuntimeException("Error fetching transactions from nexus");

      int totalTxns = transaction.getBody().getTransactions().length;

      Transaction[] newTransactions =
          Arrays.copyOfRange(
              transaction.getBody().getTransactions(), totalTxns - newTxns, totalTxns);
      if (newTransactions == null || newTransactions.length == 0) return result;

      final StatementDTO.StatementDTOBuilder smtResult = StatementDTO.builder();

      // build incoming transactions
      final List<TransactionDTO> incoming = new ArrayList<TransactionDTO>();
      final List<TransactionDTO> outgoing = new ArrayList<TransactionDTO>();

      for (Transaction txContainer : newTransactions) {

        for (Batches batch : txContainer.getBatches()) {
          for (BatchedTransaction tx : batch.getBatchTransactions()) {
            log.debug("  processing tx from backend: " + tx +" tx-detail: "+tx.getDetails());

            if (tx.getDetails()==null) {
              log.debug(" detail null - ignore tx ");
              continue;
            } 
            
            TransactionDTO.TransactionDTOBuilder txTo =
                TransactionDTO.builder()
                    .msgId("not set")
                    .instrId("not set")
                    .amount(new BigDecimal(tx.getAmount().substring(4))) // e.g. EUR:
                    .currency(libeufinConfig.accountCurrency)
                    .endToEndId(tx.getDetails().getEndToEndId())
                    .pmtInfId(tx.getDetails().getPaymentInformationId())
                    .reference(tx.getDetails().getUnstructuredRemittanceInformation());

            if (TxType.DBIT.name().equals(tx.getCreditDebitIndicator())) {
              txTo.iban(
                  tx.getDetails().getCreditorAccount() != null
                      ? tx.getDetails().getCreditorAccount().getIban()
                      : "empty creditor iban");
              txTo.name(
                  tx.getDetails().getCreditor() != null
                      ? tx.getDetails().getCreditor().getName()
                      : "empty creditor name");


              log.debug(" add outgoing tx ");
              outgoing.add(txTo.build());
            } else if (TxType.CRDT.name().equals(tx.getCreditDebitIndicator())) {
                txTo.iban(
                tx.getDetails().getDebtorAccount() != null
                      ? tx.getDetails().getDebtorAccount().getIban()
                      : "empty debtor iban");
                txTo.name(
                  tx.getDetails().getDebtor() != null
                      ? tx.getDetails().getDebtor().getName()
                      : "empty debtor name");
         
              log.debug(" add incoming tx ");
              incoming.add(txTo.build());
            } else {
              log.error( " not expected this getCreditDevitIndicator value of: "
                      + tx.getCreditDebitIndicator());
              throw new RuntimeException(
                  " not expected this getCreditDevitIndicator value of: "
                      + tx.getCreditDebitIndicator());
            }
          }
        }
      }

      LocalDate now = LocalDate.now();

      BigDecimal updateBalance =
          incoming.stream().map(x -> x.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);

      updateBalance =
          updateBalance.subtract(
              outgoing.stream().map(x -> x.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add));

      log.debug(" updateBalance: " + updateBalance);

      libeufinConfig.accountBalance = libeufinConfig.accountBalance.add(updateBalance);

      smtResult
          .balanceCL(libeufinConfig.accountBalance)
          .balanceCLCurrency(libeufinConfig.accountCurrency)
          .balanceCLDate(now)
          .balanceOP(libeufinConfig.accountBalance)
          .bookingDate(now)
          .validationDate(now)
          .balanceOPCurrency(libeufinConfig.accountCurrency)
          .iban(appConfig.peggingIban)
          .incomingTransactions(incoming)
          .outgoingTransactions(outgoing);

      result.add(smtResult.build()); // only one result an pegging account

    } catch (URISyntaxException e) {
      this.log.error("wrong URI for sandbox ", e);
      e.printStackTrace();
    }

    return result;
  }
}
