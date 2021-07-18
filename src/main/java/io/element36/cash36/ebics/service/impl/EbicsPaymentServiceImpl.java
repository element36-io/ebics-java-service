package io.element36.cash36.ebics.service.impl;

import java.io.File;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.element36.cash36.EbicsTools;
import io.element36.cash36.ebics.config.AppConfig;
import io.element36.cash36.ebics.config.EbicsMode;
import io.element36.cash36.ebics.dto.TxResponse;
import io.element36.cash36.ebics.dto.TxStatusEnum;
import io.element36.cash36.ebics.service.EbicsPaymentService;
import io.element36.cash36.ebics.service.GeneratePainService;
import lombok.extern.slf4j.Slf4j;

@Service
@Profile("!prod")
@Slf4j
public class EbicsPaymentServiceImpl implements EbicsPaymentService {

  @Autowired GeneratePainService painService;

  @Autowired AppConfig appConfig;

  @Autowired EbicsMode ebicsMode;

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
    {
      log.debug("makePayment - dev mode");

      File painFile =
          painService.generatePainFile(
              msgId,
              pmtInfId,
              sourceIban,
              sourceBic,
              amount,
              currency,
              receipientIban,
              receipientBankName,
              recipientBankPostAccount,
              receipientName,
              purpose,
              ourReference,
              receipientStreet,
              receipientStreetNr,
              receipientZip,
              receipientCity,
              receipientCountry,
              clearingSystemMemberId,
              nationalPayment);

      String command = appConfig.entryPoint + " --xe2 -i " + painFile.getAbsolutePath();
      log.debug(" makePayment - command {}, file {} ", command, painFile);

      return TxResponse.builder()
        .command("makePayment")
        .ebicsDocumentPath(painFile.getAbsolutePath())
        .ebicsMode(ebicsMode)
        .status(TxStatusEnum.OK)
        .message("DEV -  Payment generated but not sent")
        .msgId(msgId).build();
    }
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

    String message = "simulatePayment - generate pain file:\n";
    File painFile =
        painService.generatePainFile(
            msgId,
            pmtInfId,
            sourceIban,
            sourceBic,
            amount,
            currency,
            receipientIban,
            receipientBankName,
            recipientBankPostAccount,
            receipientName,
            purpose,
            ourReference,
            receipientStreet,
            receipientStreetNr,
            receipientZip,
            receipientCity,
            receipientCountry,
            clearingSystemMemberId,
            nationalPayment);

    String pain = new EbicsTools().getContent(painFile);
    String command = appConfig.entryPoint + " --xe2 -i " + painFile.getAbsolutePath();
    

    if (ebicsMode == EbicsMode.enabled) {
      message += "would issue following command: " + command;
    } else {
      message += "would NOT issue this command because EbicsMode is not set to enabled: " + command;
    }
    message += "\nEbics file generated:" + painFile.getAbsolutePath();
    log.debug(message);
    message += "\n;Content of file which will be sent to the bank:\n" + pain.toString();

    return TxResponse.builder()
            .command("simulatePayment")
            .message(message)
            .ebicsDocumentPath(painFile.getAbsolutePath())
            .ebicsMode(ebicsMode)
            .status(TxStatusEnum.OK)
            .msgId(msgId)
            .build();
  }
}
