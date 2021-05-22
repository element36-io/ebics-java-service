package io.element36.cash36.ebics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * camt.053: end-of-day	statement; 
 * Bank to Customer Statement (Bank > Customer); 
 * Same as : MT940 (Kontoauszug); 
 * Check: https://www.sepaforcorporates.com/swift-for-corporates/a-practical-guide-to-the-bank-statement-camt-053-format/
 * @author w
 */
@Data
@Builder
public class StatementDTO {
	
	@ApiModelProperty("IBAN of the account - in case you have more than one account at the bank")
    private String iban;
    
    @ApiModelProperty("Closing booked balance (OPDB): https://subsembly.com/apidoc/sepa/Subsembly.Sepa.SepaBalanceType.html")
    private BigDecimal balanceOP;
    @ApiModelProperty("Closing booked balance currency(OPDB): https://subsembly.com/apidoc/sepa/Subsembly.Sepa.SepaBalanceType.html")
    private String balanceOPCurrency;
    @ApiModelProperty("Opening booked balance (CLDB): https://subsembly.com/apidoc/sepa/Subsembly.Sepa.SepaBalanceType.html") 
    private BigDecimal balanceCL;
    @ApiModelProperty("Closing booked balance currency (CLDB): https://subsembly.com/apidoc/sepa/Subsembly.Sepa.SepaBalanceType.html")
    private String balanceCLCurrency;
    
    @ApiModelProperty("Date of closing balance (OPDB) ")   
    private LocalDate balanceCLDate;
    
    @ApiModelProperty("Booking Date is when transaction appeared in banking system: bookingDate < validationDate ")       
    private LocalDate bookingDate;
    
    @ApiModelProperty("Validation Date is when assets actually become available or are deducted on the account:  bookingDate < validationDate ")       
    private LocalDate validationDate;
    private List<TransactionDTO> incomingTransactions;
    private List<TransactionDTO> outgoingTransactions;
}
