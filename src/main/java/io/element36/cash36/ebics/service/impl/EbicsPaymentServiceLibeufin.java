package io.element36.cash36.ebics.service.impl;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.element36.cash36.ebics.config.AppConfig;
import io.element36.cash36.ebics.config.AppConfigLibeufin;
import io.element36.cash36.ebics.dto.TxResponse;
import io.element36.cash36.ebics.dto.libeufin.CreatePaymentInitiationRequest;
import io.element36.cash36.ebics.dto.libeufin.PaymentInitiationResponse;
import io.element36.cash36.ebics.dto.libeufin.SandboxPayment;
import io.element36.cash36.ebics.service.EbicsPaymentService;
import io.element36.cash36.ebics.service.EbicsStatementService;
import io.element36.cash36.ebics.strategy.GeneratePaymentIds;
import lombok.extern.slf4j.Slf4j;

@Service
@Profile("sandbox")
@Slf4j
public class EbicsPaymentServiceLibeufin implements EbicsPaymentService {

  @Autowired AppConfig appConfig;

  @Autowired AppConfigLibeufin libeufinConfig;

  @Autowired GeneratePaymentIds generatePaymentIds;

  @Override
  public TxResponse makePayment(
      String msgId,
      String pmtInfId,
      String sourceIban,
      String sourceBic,
      BigDecimal amount,
      String currency,
      String receipientIban,
      String receipientBankName,
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

    if (msgId.isEmpty() || msgId.equals("empty"))
      msgId = this.generatePaymentIds.getMsgId(null, null);
    if (pmtInfId.isEmpty() || msgId.equals("empty"))
      pmtInfId = this.generatePaymentIds.getPmtInfId(null, null);
    if (ourReference.isEmpty() || ourReference.equals("empty"))
      ourReference = "ref " + java.time.LocalDateTime.now();
    TxResponse result = null;

    RestTemplate restTemplate = new RestTemplate();
    restTemplate
        .getInterceptors()
        .add(new BasicAuthorizationInterceptor(libeufinConfig.username, libeufinConfig.password));

    CreatePaymentInitiationRequest input =
        CreatePaymentInitiationRequest.builder()
            .amount(libeufinConfig.accountCurrency + ":" + amount.toString())
            .bic(clearingSystemMemberId)
            .iban(receipientIban)
            .name(receipientName)
            .subject("Purp:"+purpose + "; ourRef:" + ourReference)
            .build();

    this.log.debug(" json payload--> " + input.toString());
    ResponseEntity<PaymentInitiationResponse> resp =
        restTemplate.postForEntity(
            new URI(
                libeufinConfig.nexus_url + "/bank-accounts/" + sourceIban + "/payment-initiations"),
            input,
            PaymentInitiationResponse.class);
    this.log.debug(" respose --> " + resp.toString());

    if (resp.getStatusCode() == HttpStatus.OK) {
      this.log.debug(" sending payment to backend ");

      ResponseEntity<String> submit =
          restTemplate.postForEntity(
              new URI(
                  libeufinConfig.nexus_url
                      + "/bank-accounts/"
                      + sourceIban
                      + "/payment-initiations/"
                      + resp.getBody().getUuid()
                      + "/submit"),
              null,
              String.class);

      this.log.debug(" result " + submit.toString());
      if (submit.getStatusCode() != HttpStatus.OK) {
        throw new RuntimeException(
            "ERROR - payment prepared, but could not be submitted to sandbox: " + input.toString());
      }
    } else {
      throw new RuntimeException("ERROR - could not prepare payment " + input.toString());
    }
    return result;
  }

  @Override
  public TxResponse simulatePayment(
      String msgId,
      String pmtInfId,
      String sourceIban,
      String sourceBic,
      BigDecimal amount,
      String currency,
      String receipientIban,
      String receipientBankName,
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

    throw new RuntimeException("not supported by sandbox");
  }

  /**
   * curl -d
   * '{"creditorIban":"DE18500105172929531881","creditorBic":"INGDDEFFXXX","creditorName":"element36
   * account1","debtorIban": "DE18500105172929531882","debtorBic":"INGDDEFFXXX",
   * "debtorName":"test", "amount":"200", "currency":"EUR", "subject":"test", "direction":
   * "CRDT","uid":"abc"}' -H "Content-Type: application/json" -X POST
   * $LIBEUFIN_SANDBOX_URL"admin/payments" # Can take the values: "CRDT" or "DBIT"
   */
  @SuppressWarnings("unused")
  private TxResponse simulatePaymentSandbox(
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

    RestTemplate restTemplate = new RestTemplate();

    SandboxPayment sandboxPayment =
        SandboxPayment.builder()
            .amount(amount.toString())
            .direction(EbicsStatementService.TxType.DBIT.name())
            .debtorBic(sourceBic)
            .debtorIban(sourceIban.replaceAll("\\W", ""))
            .debtorName("external name")
            .creditorName(receipientName)
            .currency(currency)
            .creditorBic(appConfig.peggingBic)
            .creditorIban(receipientIban.replaceAll("\\W", ""))
            .creditorName("pegging account")
            .subject("subject:" + ourReference)
            .uid("t:" + new Date().getTime())
            .build();

    this.log.debug(" json payload from payload --> " + sandboxPayment.toString());
    ResponseEntity<String> resp =
        restTemplate.postForEntity(
            new URI(libeufinConfig.sandbox_url + "/admin/payments"), sandboxPayment, String.class);
    this.log.debug(" respose from sandbox --> " + resp.toString());
    if (resp.getStatusCode() != HttpStatus.OK)
      throw new RuntimeException(
          "ERROR - payment prepared, but could not be submitted to sandbox: "
              + sandboxPayment.toString());

    libeufinConfig.accountBalance = libeufinConfig.accountBalance.add(amount);

    return TxResponse.builder()
        .command("sandbox payment:" + sandboxPayment.toString())
        .ebicsMode(appConfig.ebicsMode())
        .build();
  }
}
