package io.element36.cash36.ebics.service;

import java.math.BigDecimal;

public interface EbicsPaymentService {
    String makePayment(String msgId, String pmtInfId, String sourceIban, String sourceBic, BigDecimal amount,
                       String currency, String receipientIban, String receipientBankName, String recipientBankPostAccount,
                       String receipientName, String purpose, String ourReference, String receipientStreet, String receipientStreetNr,
                       String receipientZip, String receipientCity, String receipientCountry,
                       String clearingSystemMemberId, boolean nationalPayment) throws Exception;

    String simulatePayment(String msgId, String pmtInfId, String sourceIban, String sourceBic, BigDecimal amount,
            String currency, String receipientIban, String receipientBankName, String recipientBankPostAccount,
            String receipientName, String purpose, String ourReference, String receipientStreet, String receipientStreetNr,
            String receipientZip, String receipientCity, String receipientCountry,
            String clearingSystemMemberId, boolean nationalPayment) throws Exception;

}
