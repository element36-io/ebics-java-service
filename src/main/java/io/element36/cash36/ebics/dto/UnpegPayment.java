package io.element36.cash36.ebics.dto;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
public class UnpegPayment {

	@ApiModelProperty(value = "Amount - 1,1", example = "")
	@NotNull(message = "amount cannot be null")
	private BigDecimal amount;
	
	@ApiModelProperty(value = "Currency in CHF or EUR", example = "CHF")
	@NotBlank(message = "currency cannot be null")
	private String currency;
	
	@ApiModelProperty(value = "Purpose - e.g. add code or wallet address here", example = "0x9A0cab4250613cb8437F06ecdEc64F4644Df4D87")
	@NotBlank(message = "purpose cannot be null")
	private String purpose;
	
	@ApiModelProperty(value = "Private reference, eg transaction-id", example = "txId")
	@NotBlank(message = "ourReference cannot be null")
	private String ourReference;

	@ApiModelProperty(value = "IBAN account number of destination account", example = "CH12 3011 6000 2895 3731 2")
	@NotBlank(message = "receipientIban cannot be null")
	private String receipientIban;
	
	@ApiModelProperty(value = "Optional bank name", example = "Hypi Lenzburg AG")
	// @NotBlank(message = "receipientBankName cannot be null") - only for nationalPayment
	private String receipientBankName;
	
	@ApiModelProperty(value = "Optional post account as used in Switzerland", example = " ")
	// @NotBlank(message = "recipientBankPostAccount cannot be null") - only for nationalPayment
	private String recipientBankPostAccount;
	
	@ApiModelProperty(value = "Receipient name", example = "element36 AG")	
	@NotBlank(message = "receipientName cannot be null")
	private String receipientName;
	
	@ApiModelProperty(value = "Streetname", example = "Bahnmatt")	
	@NotBlank(message = "receipientStreet cannot be null")
	private String receipientStreet;

	@ApiModelProperty(value = "Streetnumber", example = "25")		
	@NotBlank(message = "receipientStreetNr cannot be null")
	private String receipientStreetNr;

	@ApiModelProperty(value = "Zip code - 4 digits in Switzerland, more in other countries", example = "6340")		
	@NotBlank(message = "receipientZip cannot be null")
	private String receipientZip;

	@ApiModelProperty(value = "City", example = "Baar")		
	@NotBlank(message = "receipientCity cannot be null")
	private String receipientCity;
	
	@ApiModelProperty(value = "2-digit ISO 3166 country codes (CH, DE, FR,...)", example = "CH")		
	@NotBlank(message = "receipientCountry cannot be null")
	private String receipientCountry;
	
	@ApiModelProperty(value = "Optional clearing no", example = " ")		
	// @NotBlank(message = "clearingSystemMemberId cannot be null") - only for
	// nationalPayment
	private String clearingSystemMemberId;

	@ApiModelProperty(value = "Optional flag - try to use always the national payment even cross-border, it affects fees positively", example = "")		
	private boolean nationalPayment=false;
}
