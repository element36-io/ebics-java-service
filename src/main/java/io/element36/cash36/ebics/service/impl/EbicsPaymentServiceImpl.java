package io.element36.cash36.ebics.service.impl;

import java.io.File;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.element36.cash36.EbicsTools;
import io.element36.cash36.ebics.config.AppConfig;
import io.element36.cash36.ebics.config.EbicsMode;
import io.element36.cash36.ebics.service.EbicsPaymentService;
import io.element36.cash36.ebics.service.GeneratePainService;
import lombok.extern.slf4j.Slf4j;

@Service
@Profile("!prod")
@Slf4j
public class EbicsPaymentServiceImpl implements EbicsPaymentService {
	
	@Autowired 
	GeneratePainService painService; 
	
	@Autowired
	AppConfig appConfig;
	
	@Autowired
	EbicsMode ebicsMode;


    @Override
    public String makePayment(String msgId, String pmtInfId, String sourceIban, String sourceBic, BigDecimal amount,
                              String currency, String receipientIban, String receipientBankName, String recipientBankPostAccount,
                              String receipientName, String purpose, String ourReference, String receipientStreet, String receipientStreetNr,
                              String receipientZip, String receipientCity, String receipientCountry, String clearingSystemMemberId,
                              boolean nationalPayment) throws Exception {
        {
        	log.debug("makePayment - dev mode");
        	
            File painFile = painService.generatePainFile(msgId, pmtInfId, sourceIban, sourceBic, amount, currency, receipientIban,
                    receipientBankName, recipientBankPostAccount, receipientName, purpose, ourReference, receipientStreet,
                    receipientStreetNr, receipientZip, receipientCity, receipientCountry, clearingSystemMemberId, nationalPayment);
            
           
            String command=appConfig.entryPoint+" --xe2 -i "+ painFile.getAbsolutePath();
            log.debug(" makePayment - command {}, file {} ",command,painFile);

            return "DEV -  Payment generated but not sent:"+painFile.getCanonicalPath()+"; command: "+command;
        }
    }
    

	@Override
    public String simulatePayment(String msgId, String pmtInfId, String sourceIban, String sourceBic, BigDecimal amount,
                              String currency, String receipientIban, String receipientBankName, String recipientBankPostAccount,
                              String receipientName, String purpose, String ourReference, String receipientStreet,  String receipientStreetNr, 
                              String receipientZip, String receipientCity, String receipientCountry, String clearingSystemMemberId,
                              boolean nationalPayment) throws Exception {

		String result="simulatePayment - generate pain file:\n";
        File painFile = painService.generatePainFile(msgId, pmtInfId, sourceIban, sourceBic, amount, currency, receipientIban,
                receipientBankName, recipientBankPostAccount, receipientName, purpose, ourReference, receipientStreet,
                receipientStreetNr, receipientZip, receipientCity, receipientCountry, clearingSystemMemberId, nationalPayment);

        String pain=new EbicsTools().getContent(painFile);
        String command=appConfig.entryPoint+" --xe2 -i "+ painFile.getAbsolutePath();
        
        if(ebicsMode==EbicsMode.enabled) {
        	result+="would issue following command: "+command;
        } else {
        	result+= "would NOT issue this command because EbicsMode is not set to enabled: "+command;
        }
        result+="\nEbics file generated:"+painFile.getAbsolutePath();
        log.debug(result); 
        result+="\nContent of file which will be sent to the bank:\n"+pain.toString();
       
        return result.toString();
	}
    
}
