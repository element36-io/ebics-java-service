package io.element36.cash36.ebics.dto;

import java.math.BigDecimal;
import java.util.ArrayList;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionDTO {

  @ApiModelProperty("IBAN of the foreign account")
  private String iban;

  @ApiModelProperty("Account name or beneficiary")
  private String name;

  @ApiModelProperty("Several address lines")
  private ArrayList<String> addrLine;

  @ApiModelProperty("Currency like EUR or CHF")
  private String currency;

  @ApiModelProperty("Amount of the transaction")
  private BigDecimal amount;

  @ApiModelProperty("Reference field - you add your code or a wallet address here ")
  private String reference;

  @ApiModelProperty("ID field which BOTH parties sharing")
  private String endToEndId;

  @ApiModelProperty("ID field between you and your bank")
  private String instrId;

  @ApiModelProperty("ID field of the instruction file - how it was initially triggered")
  private String msgId;

  @ApiModelProperty("ID field of the transaction - how it was initially triggered")
  private String pmtInfId;
}
