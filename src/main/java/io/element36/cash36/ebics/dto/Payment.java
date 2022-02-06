package io.element36.cash36.ebics.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
public class Payment extends PeggingPayment {

  @ApiModelProperty(
      value =
          "Maximum 35 characters, Reference SEPA file creator for the purpose of clearer Identification of the SEPA/payment file", example="emtpy")
  @Size(max = 35, message = "max 35 chars")
  @NotBlank(message = "msgId cannot be null")
  private String msgId;

  @ApiModelProperty(
      value =
          "Maximum of 35 characters reference for the clear identification of a collector in the account statement (= DTA field A10)", example="empty")
  @Size(max = 35, message = "max 35 chars")
  @NotBlank(message = "pmtInfId cannot be null")
  private String pmtInfId;

  @ApiModelProperty(
      value = "IBAN account number of source (your) account",
      example = "CH2108307000289537320")
  @NotBlank(message = "sourceIban cannot be null")
  private String sourceIban;

  @ApiModelProperty(value = "Bic/Swift number of source (your) bank", example = "HYPLCH22")
  @NotBlank(message = "sourceBic cannot be null")
  private String sourceBic;
}
