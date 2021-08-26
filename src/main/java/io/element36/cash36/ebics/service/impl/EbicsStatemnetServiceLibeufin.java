package io.element36.cash36.ebics.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.element36.cash36.ebics.config.AppConfig;
import io.element36.cash36.ebics.config.AppConfigLibeufin;
import io.element36.cash36.ebics.dto.StatementDTO;
import io.element36.cash36.ebics.dto.libeufin.FetchTranactionsResponse;
import io.element36.cash36.ebics.dto.libeufin.Transaction;
import io.element36.cash36.ebics.dto.libeufin.TransactionsResponse;
import io.element36.cash36.ebics.service.EbicsStatementService;
import lombok.extern.slf4j.Slf4j;

@Service
@Profile({"sandbox"})
@Slf4j
public class EbicsStatemnetServiceLibeufin implements EbicsStatementService {

  @Autowired AppConfig appConfig;

  @Autowired AppConfigLibeufin libeufinConfig;

  @Override
  public List<StatementDTO> getBankStatement() throws IOException {
    List<StatementDTO> result = null;

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
      this.log.debug(" respose transactions --> " + transaction.getBody());
      if (resp.getStatusCode() != HttpStatus.OK)
        throw new RuntimeException("Error fetching transactions from nexus");
      
      int totalTxns=transaction.getBody().getTransactions().length;
      
      Transaction[] newTransactions=Arrays.copyOfRange(transaction.getBody().getTransactions(), totalTxns-newTxns, totalTxns);
      if (newTransactions!=null && newTransactions.length>0) {
	  
	  StatementDTO envelope= StatementDTO.builder().balanceCL(libeufinConfig.accountBalance).build();
          for (Transaction tx:newTransactions) {
    	  // copy to StatementDTO
    	  
    	  
          }
      }
      
      
      

    } catch (URISyntaxException e) { // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return result;
  }
}
