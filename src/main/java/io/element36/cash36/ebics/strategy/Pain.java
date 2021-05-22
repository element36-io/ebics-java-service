package io.element36.cash36.ebics.strategy;

import java.io.IOException;
import java.math.BigDecimal;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;

import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.Document;

public interface Pain {
    
    public JAXBElement<Document> generatePainFile(String msgId, String pmtInfId, String sourceIban, String sourceBic,
    BigDecimal amount, String currency, String receipientIban,
    String receipientBankName, String recipientBankPostAccount,
    String receipientName, String purpose, String ourReference,
    String receipientStreet, String receipientStreetNr, String receipientZip,
    String receipientCity, String receipientCountry, String clearingSystemMemberId,
    boolean nationalPayment) throws DatatypeConfigurationException, IOException ;


    public String getSchemaLocation();
    
}
