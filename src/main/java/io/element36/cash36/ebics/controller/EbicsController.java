package io.element36.cash36.ebics.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.xml.datatype.DatatypeConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.element36.cash36.ebics.config.AppConfig;
import io.element36.cash36.ebics.config.EbicsMode;
import io.element36.cash36.ebics.dto.Payment;
import io.element36.cash36.ebics.dto.StatementDTO;
import io.element36.cash36.ebics.dto.TxResponse;
import io.element36.cash36.ebics.dto.TxStatusEnum;
import io.element36.cash36.ebics.dto.UnpegPayment;
import io.element36.cash36.ebics.service.EbicsPaymentService;
import io.element36.cash36.ebics.service.EbicsPaymentStatusService;
import io.element36.cash36.ebics.service.EbicsStatementService;
import io.element36.cash36.ebics.strategy.GeneratePaymentIds;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * Rest-Wrapper for the Ebics client.
 *
 * <p>http://localhost:8093/ebics/swagger-ui/#/ebics-controller/unpegUsingPOST
 *
 * <p>- See https://www.ebics.org/en/home . See
 * https://www.iso20022.org/iso-20022-message-definitions, https://wiki.xmldation.com/Support,
 * https://www.iso20022.org/payments_messages.page
 *
 * <p>Google for local support of Ebics -
 * https://www.six-group.com/en/products-services/banking-services/standardization/iso-payments.html
 * https://www.credit-suisse.com/media/assets/microsite/docs/zv-migration/pain-001-001-03-six.pdf
 *
 * @author w-element36
 */
@CrossOrigin
@RestController
@RequestMapping("/" + AppConfig.API_PATH + "/")
@Slf4j
public class EbicsController {

  @Autowired GeneratePaymentIds generatePaymentIds;

  @Autowired EbicsStatementService ebicsStatementService;

  @Autowired EbicsPaymentService ebicsPaymentService;

  @Autowired EbicsPaymentStatusService ebicsPaymentStatusService;

  @Value("${ebics.peggingAccount.iban}")
  private String peggingSourceIban;

  @Value("${ebics.peggingAccount.bic}")
  private String peggingSourceBic;

  @Autowired EbicsMode ebicsMode;

  @ApiOperation("Create a tarnsaction with your pegging account configured in `ebics.pegging.account`. "
  + "The service call will create a XML document (pain-file) which will be sent to the bank using "
  + "Ebics protocoll. For development purposes you can use the dev-mode (spring.profiles.active) "
  + "to generate a pain-file. Most banks also offer a service where you can manually"
  + "upload (and debug) pain-files - you find them in the configured ebics.outputDir folder. "
  + "HINT: In prod mode it sends real money - in dev mode it shows the command and the Ebics document in the result. "
  + "The prod-mode only makes sense, if you have set up the connectivity with your bank successfully as described in HOWTO.md."
  + "See https://wiki.xmldation.com/General_Information/Payment_Standards/ISO_20022/pain.001 "
  + "for more information about the standard. ")  
  @PostMapping("/unpeg")
  public ResponseEntity<TxResponse> createUnpegOrder(
      @RequestBody @Valid UnpegPayment payment, HttpServletRequest servletRequest) {
    log.debug("unpeg {} from {} ", payment, peggingSourceIban);

    TxResponse result;

    String msgId =
        this.generatePaymentIds.getMsgId(payment, servletRequest); // Maximal 35 der SEPA Datei.
    String pmtInfId =
        this.generatePaymentIds.getPmtInfId(payment, servletRequest); // Max 35, Id of Sammler

    log.info(
        " createUnpegOrder {}, {},{},{},{},{}",
        peggingSourceBic,
        peggingSourceIban,
        msgId,
        pmtInfId,
        payment.getAmount(),
        payment.getReceipientIban());

    try {
       result =
          ebicsPaymentService.makePayment(
              msgId,
              pmtInfId,
              peggingSourceIban,
              peggingSourceBic,
              payment.getAmount(),
              payment.getCurrency(),
              payment.getReceipientIban(),
              payment.getReceipientBankName(),
              payment.getRecipientBankPostAccount(),
              payment.getReceipientName(),
              payment.getPurpose(),
              payment.getOurReference(),
              payment.getReceipientStreet(),
              payment.getReceipientStreetNr(),
              payment.getReceipientZip(),
              payment.getReceipientCity(),
              payment.getReceipientCountry(),
              payment.getClearingSystemMemberId(),
              payment.isNationalPayment());
      return ResponseEntity.ok(result);
    } catch (DatatypeConfigurationException e) {
      log.error("ERROR in makePayment ", e);
      return ResponseEntity.badRequest().body(
          TxResponse.builder()
              .message(e.getMessage())
              .status(TxStatusEnum.OK)
              .msgId(msgId)
              .ebicsMode(ebicsMode)
              .build()
          );
    } catch (IOException e) {
      log.error("ERROR in makePayment ", e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Error makePayment - IOException: " + e.toString(), e);
    } catch (Exception e) {
      log.error("ERROR in makePayment ", e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Error makePayment - Exception: " + e.toString(), e);
    }
  }

  /**
   * Refer to standards
   * https://www.hettwer-beratung.de/sepa-spezialwissen/sepa-technische-anforderungen/pain-format-sepa-pain-001-sct/
   *
   * @param payment
   * @return
   */
  @ApiOperation("Initiate a payment - works with multiple source accounts in case you have more than one account at your bank. "
  + "The service call will create a XML document (pain-file) which will be sent to the bank using "
  + "Ebics protocoll. For development purposes you can use the dev-mode (spring.profiles.active) "
  + "to generate a pain-file. Most banks also offer a service where you can manually"
  + "upload (and debug) pain-files - you find them in the configured ebics.outputDir folder. "
  + "HINT: In prod mode it sends real money - in dev mode it shows the command and the Ebics document in the result. "
  + "The prod-mode only makes sense, if you have set up the connectivity with your bank successfully as described in HOWTO.md."
  + "See https://wiki.xmldation.com/General_Information/Payment_Standards/ISO_20022/pain.001 "
  + "for more information about the standard. ")  
  @PostMapping("/createOrder")
  public ResponseEntity<TxResponse> createPaymentOrder(@RequestBody @Valid Payment payment) {
    log.debug("makePayment {}", payment);
    log.info(
        " createUnpegOrder {}, {},{},{},{},{}",
        payment.getSourceBic(),
        payment.getSourceIban(),
        payment.getMsgId(),
        payment.getPmtInfId(),
        payment.getAmount(),
        payment.getReceipientIban());

    TxResponse result;
    try {
      result =
          ebicsPaymentService.makePayment(
              payment.getMsgId(),
              payment.getPmtInfId(),
              payment.getSourceIban(),
              payment.getSourceBic(),
              payment.getAmount(),
              payment.getCurrency(),
              payment.getReceipientIban(),
              payment.getReceipientBankName(),
              payment.getRecipientBankPostAccount(),
              payment.getReceipientName(),
              payment.getPurpose(),
              payment.getOurReference(),
              payment.getReceipientStreet(),
              payment.getReceipientStreetNr(),
              payment.getReceipientZip(),
              payment.getReceipientCity(),
              payment.getReceipientCountry(),
              payment.getClearingSystemMemberId(),
              payment.isNationalPayment());

      return ResponseEntity.ok(result);
    } catch (DatatypeConfigurationException e) {
      log.error("ERROR in makePayment ", e);
      return ResponseEntity.badRequest().body(
        TxResponse.builder()
            .message(e.getMessage())
            .status(TxStatusEnum.OK)
            .msgId(payment.getMsgId())
            .ebicsMode(ebicsMode)
            .build()
        );
    } catch (IOException e) {
      log.error("ERROR in makePayment ", e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Error makePayment - IOException: " + e.toString(), e);
    } catch (Exception e) {
      log.error("ERROR in makePayment ", e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Error makePayment - Exception: " + e.toString(), e);
    }
  }

  @ApiOperation("Create a test transaction - send funds form your bank account to somebody else. "
  + "The result shows the Ebics file generated by the request. "
  + "You may test this file with your bank prior to activating Ebics: Many "
  + "banks offer buld-upload function which read Camt.053 format via web-interface.")
  @PostMapping("/simulate")
  public ResponseEntity<TxResponse> simulate(@RequestBody @Valid Payment request) {
    log.debug("makePayment {}", request);
    TxResponse result;
    try {
      result =
          ebicsPaymentService.simulatePayment(
              request.getMsgId(),
              request.getPmtInfId(),
              request.getSourceIban(),
              request.getSourceBic(),
              request.getAmount(),
              request.getCurrency(),
              request.getReceipientIban(),
              request.getReceipientBankName(),
              request.getRecipientBankPostAccount(),
              request.getReceipientName(),
              request.getPurpose(),
              request.getOurReference(),
              request.getReceipientStreet(),
              request.getReceipientStreetNr(),
              request.getReceipientZip(),
              request.getReceipientCity(),
              request.getReceipientCountry(),
              request.getClearingSystemMemberId(),
              request.isNationalPayment());

    } catch (DatatypeConfigurationException e) {
      log.error("ERROR in makePayment ", e);
      
      return ResponseEntity.badRequest().body(
        TxResponse.builder()
            .message(e.getMessage())
            .status(TxStatusEnum.OK)
            .msgId(request.getMsgId())
            .ebicsMode(ebicsMode)
            .build()
        );
    } catch (IOException e) {
      log.error("ERROR in makePayment ", e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Error makePayment - IOException: " + e.toString(), e);
    } catch (Exception e) {
      log.error("ERROR in makePayment ", e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Error makePayment - Exception: " + e.toString(), e);
    }
    return ResponseEntity.ok(result);
  }

  @ApiOperation("Retrieves bank statement and converts Camt.053 format to a simpler JSON-format. "
  + "CAMT is an ISO 20022 Payment Message definition that stands for Cash Management and "
  + "specifically covers Bank to Customer Cash Management reporting. "
  + "In prod-mode with a real bank account you will get any statement ONLY ONCE: "
  + "A bank statement is a legal document, not a query result.  "
  + "With this call you retrieve all Camt.053 documents which have not been retrieved yet."
  + "This also means, that if you already have received (downloaded) all statements,  "
  + "then you will get an error, similar to: 'can not download request file'. E.g. if you issue the command "
  + "without new transactions inbetween then you will see the error as well. "
  + "Anyway you see all Camt.053 files in the configured out folder (ebics.outputDir). "
  + "In dev mode you see the command which is issued to query the daily statement. "
  + "See https://wiki.xmldation.com/General_Information/Payment_Standards/ISO_20022/Bank-to-Customer_Cash_Management "
  + "for more information about the standard. ")
  @GetMapping("/bankstatements")
  public ResponseEntity<List<StatementDTO>> getPayments() {
    log.debug("getStatement ");

    List<StatementDTO> result;
    try {
      result = ebicsStatementService.getBankStatement();
    } catch (Exception e) {
      log.error("ERROR in getStatement ", e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Error getStatement - Exception: " + e.toString(), e);
    }
    return ResponseEntity.ok(result);
  }

}
