package io.element36.cash36.ebics.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.element36.cash36.EbicsTools;
import io.element36.cash36.ebics.config.AppConfig;
import io.element36.cash36.ebics.config.EbicsMode;
import io.element36.cash36.ebics.dto.StatementDTO;
import io.element36.cash36.ebics.service.EbicsStatementService;
import io.element36.cash36.ebics.strategy.Statement;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EbicsStatementServiceImpl implements EbicsStatementService {
  @Autowired EbicsMode ebicsMode;

  @Autowired Statement statementStrategy;

  @Autowired AppConfig appConfig;

  @Value("${ebics.proxy.statement}")
  String proxyStatementFile;

  @Override
  public List<StatementDTO> getBankStatement() throws IOException {
    log.debug("getBankStatement - mode " + this.ebicsMode);
    List<StatementDTO> statements = new ArrayList<>();
    if (this.ebicsMode == EbicsMode.disabled) return statements;

    // See if there are any error statements from last time to be re-run
    List<StatementDTO> errorStatements = new ArrayList<>();
    log.debug(" check files in error dir");
    if (Files.exists(Paths.get(appConfig.outputDir + "/error"))) {
      Files.list(Paths.get(appConfig.outputDir + "/error"))
          .filter(Files::isRegularFile)
          .forEach(
              file -> {
                try {
                  log.debug(
                      " files in error dir found, move it to processing folder and add processing array {} ",
                      file);
                  errorStatements.addAll(statementStrategy.process(file.toFile()));
                  new EbicsTools()
                      .moveFile(
                          file.toFile().getAbsolutePath(),
                          appConfig.outputDir + "/" + file.toFile().getName());
                } catch (Exception e) {
                  log.error("ERROR reworking on failed files", e);
                }
              });
    }

    statements.addAll(errorStatements);

    // Now define output file and try to get new Statements
    File z53OutFile =
        new File(
            String.format(
                "%s%s%s%s",
                appConfig.outputDir, "/z53-", new Date().toInstant().getEpochSecond(), ".zip"));
    String command = appConfig.entryPoint + " --z53 -o " + z53OutFile.getAbsolutePath();
    log.debug(" execute cmd: {}", command);
    CommandLine commandLine = CommandLine.parse(command);

    if (this.ebicsMode == EbicsMode.enabled) {
      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(120 * 1000);
        Executor executor = new DefaultExecutor();
        executor.setWatchdog(watchdog);
        executor.setStreamHandler(streamHandler);
        Exception innerException = null;
        try {
          executor.execute(commandLine);
        } catch (Exception e) {
          innerException = e;
        }

        String outputAsString = outputStream.toString("UTF-8");
        log.trace(" z53 output of cmd: {}", outputAsString);

        if (outputAsString != null
            && !outputAsString.contains("No download data available")
            && !outputAsString.contains("ERROR")) {
          statements.addAll(statementStrategy.process(z53OutFile));
        } else if (outputAsString != null
            && outputAsString.contains("ebics.exception.NoDownloadDataAvailableException")) {
          log.warn("Warn - no z53 data available");
        } else {
          log.error(" Error downloading data, throwing exception");
          throw innerException;
        }
      } catch (Exception e) {
        log.error("Unhandled exception in getBankStatement, move z53 file if present", e);
        if (z53OutFile != null && z53OutFile.exists()) {
          new EbicsTools()
              .moveFile(
                  z53OutFile.getAbsolutePath(),
                  appConfig.outputDir + "/error/" + z53OutFile.getName());
        }
      } finally {
        // FileUtils.deleteDirectory(new File(OUTPUT_DIR + "/" +
        // FilenameUtils.removeExtension(z53OutFile.getName())));
      }
    } else if (ebicsMode == EbicsMode.proxy) {
      log.info(" ebics proxy mode, taking static file {}", this.proxyStatementFile);
      statements.addAll(statementStrategy.process(proxyStatementFile));
    } else {
      log.debug(" ebics disabled, cmd not executed " + this.ebicsMode);
    }
    log.debug(" statements returned " + statements);
    return statements;
  }
}
