package io.element36.cash36.ebics.service.impl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.element36.cash36.ebics.config.AppConfig;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.Document;
import io.element36.cash36.ebics.service.GeneratePainService;
import io.element36.cash36.ebics.strategy.Pain;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GeneratePainServiceImpl implements GeneratePainService {
	
	@Autowired
	AppConfig appConfig; 

    @Autowired
	Pain painStrategy; 
	
	@Override
    public File generatePainFile(String msgId, String pmtInfId, String sourceIban, String sourceBic,
                                        BigDecimal amount, String currency, String receipientIban,
                                        String receipientBankName, String recipientBankPostAccount,
                                        String receipientName, String purpose, String ourReference,
                                        String receipientStreet, String receipientStreetNr, String receipientZip,
                                        String receipientCity, String receipientCountry, String clearingSystemMemberId,
                                        boolean nationalPayment) throws DatatypeConfigurationException, IOException {


         
        JAXBElement<Document> jaxbElement = painStrategy.generatePainFile(msgId, pmtInfId, sourceIban.replaceAll("\\s+",""), sourceBic.replaceAll("\\s+",""), amount, currency.replaceAll("\\s+",""), 
                receipientIban.replaceAll("\\s+",""), receipientBankName, recipientBankPostAccount.replaceAll("\\s+",""), receipientName, 
                purpose, ourReference, receipientStreet, receipientStreetNr, receipientZip, receipientCity, 
                receipientCountry, clearingSystemMemberId.replaceAll("\\s+",""), nationalPayment);

        // Create new file
        File painFile = new File(appConfig.outputDir+"/pain001-" + new Date().toInstant().getEpochSecond() + ".xml");
        painFile.getParentFile().mkdirs();
        painFile.createNewFile();

        JAXBContext context;
        try {
            context = JAXBContext.newInstance(Document.class);
            Marshaller mar = context.createMarshaller();
            mar.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, this.painStrategy.getSchemaLocation());
            mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            mar.marshal(jaxbElement, painFile);
        } catch (JAXBException e) {
        	log.error("genPainFile ",e);
        }
        // TODO: do a XSD check
        return painFile;
    }

}
