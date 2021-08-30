package io.element36.cash36.ebics.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.element36.cash36.EbicsTools;
import io.element36.cash36.ebics.config.AppConfig;
import io.element36.cash36.ebics.config.EbicsMode;
import io.element36.cash36.ebics.dto.TxResponse;
import io.element36.cash36.ebics.dto.TxStatusEnum;
import io.element36.cash36.ebics.service.GeneratePainService;
import lombok.extern.slf4j.Slf4j;

@Service
@Profile("prod")
@Slf4j
public class EbicsPaymentServiceProdImpl extends EbicsPaymentServiceImpl {

  @Autowired AppConfig appConfig;

  @Autowired GeneratePainService painService;

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

    new EbicsTools().printContent(painFile);

    String command = appConfig.entryPoint + " --xe2 -i " + painFile.getAbsolutePath();
    log.debug("calling ebics via cmd {} ", command);
    CommandLine commandLine = CommandLine.parse(command);
    String message="not set";
    TxStatusEnum status=TxStatusEnum.ERROR;

    if (ebicsMode == EbicsMode.enabled) {

      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(120 * 1000);
        Executor executor = new DefaultExecutor();
        executor.setWatchdog(watchdog);
        executor.setStreamHandler(streamHandler);

        Exception innerException = null;
        try {
          executor.execute(commandLine);
          status=TxStatusEnum.OK;
        } catch (Exception e) {
          innerException = e;
        }

        String outputAsString = outputStream.toString("UTF-8");
        log.trace(" xe2 output of cmd: {}", outputAsString);

        if (innerException != null) throw innerException;

        message= "PROD: Payment generated and triggered - " + outputAsString;
      } catch (IOException e) {

        log.error("IOException", e);
        throw new Exception("Something went wrong: " + e.getMessage());
      }
    } else {
      log.debug("makePayment ignored - ebics is not enabled " + ebicsMode);
      message= "Payment not enabled. Ebics-File:"+painFile.getAbsolutePath()+"; command:"+command;
    }

    return TxResponse.builder()
            .command("makePayment")
            .message(message)
            .ebicsMode(ebicsMode)
            .ebicsDocumentPath(painFile==null?"no file":painFile.getAbsolutePath())
            .msgId(msgId)
            .status(status)
            .build();
  }
}
