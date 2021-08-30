package io.element36.cash36.ebics.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import javax.xml.datatype.DatatypeConfigurationException;

public interface GeneratePainService {

  File generatePainFile(
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
      throws DatatypeConfigurationException, IOException;
}
