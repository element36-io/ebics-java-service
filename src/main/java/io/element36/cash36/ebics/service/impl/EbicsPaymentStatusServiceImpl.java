package io.element36.cash36.ebics.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.element36.cash36.EbicsTools;
import io.element36.cash36.ebics.config.AppConfig;
import io.element36.cash36.ebics.config.EbicsMode;
import io.element36.cash36.ebics.dto.PaymentStatusReportDTO;
import io.element36.cash36.ebics.service.EbicsPaymentStatusService;
import io.element36.cash36.ebics.service.GeneratePainService;
import io.element36.cash36.ebics.strategy.PaymentStatus;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EbicsPaymentStatusServiceImpl implements EbicsPaymentStatusService {

	@Autowired
	AppConfig appConfig;
	
	@Autowired
	GeneratePainService painService;
	
	@Autowired
	EbicsMode ebicsMode;

    @Autowired
	PaymentStatus ebicsStrategy;

    
	
    @Override
    public List<PaymentStatusReportDTO> getStatusReport() {
    	log.debug("getStatusReport");
    	
        File z01OutFile = new File(String.format("%s%s%s%s", appConfig.outputDir, "/z01-", new Date().toInstant().getEpochSecond(), ".zip"));
        new EbicsTools().printContent(z01OutFile);
        
        String command= appConfig.entryPoint+" --z01 -o " + z01OutFile.getAbsolutePath(); 
        log.debug("ebics exec command {}",command);
        CommandLine commandLine = CommandLine.parse(command);

        if (ebicsMode==EbicsMode.enabled) {
	        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
	            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
	            ExecuteWatchdog watchdog = new ExecuteWatchdog(120 * 1000);
	            Executor executor = new DefaultExecutor();
	            executor.setWatchdog(watchdog);
	            executor.setStreamHandler(streamHandler);
	
	            executor.execute(commandLine);
	            //TODO: wasa public String toString(String enc) Gets the curent contents of this byte stream as a string using the specified encoding.
	            String output = outputStream.toString("UTF-8");
	            log.debug("z01 outpout {}",output);
	
	            if (!output.contains("No download data available") && !output.contains("ERROR")) {
	                return ebicsStrategy.process(z01OutFile);
	            } else {
	            	log.warn(" ebics no data available");
	                return Collections.emptyList();
	            }
	        } catch (IOException e) {
	            log.error("ERROR ebics ",e );
	            return Collections.emptyList();
	        }
        } else {
        	log.debug("ebics not enabled, command not executed "+ebicsMode);
        	return Collections.emptyList();
        }
    }

    

 
}
