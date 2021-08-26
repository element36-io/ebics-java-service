package io.element36.cash36.ebics.service.impl;

import java.math.BigDecimal;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.element36.cash36.ebics.config.AppConfig;
import io.element36.cash36.ebics.config.AppConfigLibeufin;
import io.element36.cash36.ebics.config.EbicsMode;
import io.element36.cash36.ebics.dto.libeufin.CreatePaymentInitiationRequest;
import io.element36.cash36.ebics.dto.libeufin.PaymentInitiationResponse;
import io.element36.cash36.ebics.service.EbicsPaymentService;
import io.element36.cash36.ebics.service.GeneratePainService;
import lombok.extern.slf4j.Slf4j;

@Service
@Profile("sandbox")
@Slf4j
public class EbicsPaymentServiceLibeufin implements EbicsPaymentService {

  @Autowired AppConfig appConfig;

  @Autowired AppConfigLibeufin libeufinConfig;



  @Override
  public String makePayment(
      String msgId,
      String pmtInfId,
      String sourceIban,
      String sourceBic,
      BigDecimal amount,
      String currency,
      String receipientIban,
      String receipientBankName,
      String recipientBankPostAccount,
      String receipientName,
      String purpose,
      String ourReference,
      String receipientStreet,
      String receipientStreetNr,
      String receipientZip,
      String receipientCity,
      String receipientCountry,
      String clearingSystemMemberId,
      boolean nationalPayment)
      throws Exception {

    String result = "OK";

    RestTemplate restTemplate = new RestTemplate();
    restTemplate
        .getInterceptors()
        .add(new BasicAuthorizationInterceptor(libeufinConfig.username, libeufinConfig.password));

    CreatePaymentInitiationRequest input =
        CreatePaymentInitiationRequest.builder()
            .amount(libeufinConfig.accountCurrency+":"+amount.toString())
            .bic(sourceBic)
            .iban(receipientIban)
            .name(receipientName)
            .subject(purpose)
            .build();

    this.log.debug(" json payload--> " + input.toString());
    ResponseEntity<PaymentInitiationResponse> resp =
        restTemplate.postForEntity(
            new URI(
                libeufinConfig.nexus_url
                    + "/bank-accounts/"
                    + libeufinConfig.accountName
                    + "/payment-initiations"),
            input,
            PaymentInitiationResponse.class); 
    this.log.debug(" respose --> " + resp.toString());
    if (resp.getStatusCode()==HttpStatus.OK)  {
	this.log.debug(" sending payment to backend ");
	
	 ResponseEntity<String> submit =
		        restTemplate.getForEntity(
		            new URI(
		                libeufinConfig.nexus_url
		                    + "/bank-accounts/"
		                    + libeufinConfig.accountName
		                    + "/payment-initiations/"+resp.getBody().getUuid()),
		            String.class); 
	 this.log.debug(" result "+submit.toString());
	if (submit.getStatusCode()!=HttpStatus.OK) {
	    throw new RuntimeException("ERROR - payment prepared, but could not be submitted to sandbox: "+input.toString());
	}
    } else {
	throw new RuntimeException("ERROR - could not prepare payment "+input.toString());
    }
   
    
    return result;
  }

  @Override
  public String simulatePayment(
      String msgId,
      String pmtInfId,
      String sourceIban,
      String sourceBic,
      BigDecimal amount,
      String currency,
      String receipientIban,
      String receipientBankName,
      String recipientBankPostAccount,
      String receipientName,
      String purpose,
      String ourReference,
      String receipientStreet,
      String receipientStreetNr,
      String receipientZip,
      String receipientCity,
      String receipientCountry,
      String clearingSystemMemberId,
      boolean nationalPayment)
      throws Exception {
    throw new RuntimeException("not supported by sandbox mode");
  }
}
