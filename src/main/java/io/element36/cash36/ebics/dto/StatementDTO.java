package io.element36.cash36.ebics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * camt.053: end-of-day statement; Bank to Customer Statement (Bank > Customer); Same as : MT940
 * (Kontoauszug); Check:
 * https://www.sepaforcorporates.com/swift-for-corporates/a-practical-guide-to-the-bank-statement-camt-053-format/
 *
 * Retrieves bank statement and converts Camt.053 format to a simpler JSON-format.
 *  CAMT is an ISO 20022 Payment Message definition that stands for Cash Management 
 * and specifically covers Bank to Customer Cash Management reporting. In prod-mode 
 * with a real bank account you will get any statement ONLY ONCE: A bank statement is
 *  a legal document, not a query result. With this call you retrieve all Camt.053 
 * documents which have not been retrieved yet.This also means, that if you already
 *  have received (downloaded) all statements, then you will get an error, similar 
 * to: 'can not download request file'. E.g. if you issue the command without new 
 * transactions inbetween then you will see the error as well. Anyway you see all 
 * Camt.053 files in the configured out folder (ebics.outputDir). In dev mode you 
 * see the command which is issued to query the daily statement. 
 * @see https://wiki.xmldation.com/General_Information/Payment_Standards/ISO_20022/Bank-to-Customer_Cash_Management
 * @author w
 */
@Data
@Builder
public class StatementDTO {

  @ApiModelProperty("IBAN of the account - in case you have more than one account at the bank")
  private String iban;

  @ApiModelProperty(
      "Closing booked balance (OPDB): https://subsembly.com/apidoc/sepa/Subsembly.Sepa.SepaBalanceType.html")
  private BigDecimal balanceOP;

  @ApiModelProperty(
      "Closing booked balance currency(OPDB): https://subsembly.com/apidoc/sepa/Subsembly.Sepa.SepaBalanceType.html")
  private String balanceOPCurrency;

  @ApiModelProperty(
      "Opening booked balance (CLDB): https://subsembly.com/apidoc/sepa/Subsembly.Sepa.SepaBalanceType.html")
  private BigDecimal balanceCL;

  @ApiModelProperty(
      "Closing booked balance currency (CLDB): https://subsembly.com/apidoc/sepa/Subsembly.Sepa.SepaBalanceType.html")
  private String balanceCLCurrency;

  @ApiModelProperty("Date of closing balance (OPDB) ")
  private LocalDate balanceCLDate;

  @ApiModelProperty(
      "Booking Date is when transaction appeared in banking system: bookingDate < validationDate ")
  private LocalDate bookingDate;

  @ApiModelProperty(
      "Validation Date is when assets actually become available or are deducted on the account:  bookingDate < validationDate ")
  private LocalDate validationDate;

  private List<TransactionDTO> incomingTransactions;
  private List<TransactionDTO> outgoingTransactions;
}
