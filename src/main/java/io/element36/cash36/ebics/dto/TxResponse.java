package io.element36.cash36.ebics.dto;

import io.element36.cash36.ebics.config.EbicsMode;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TxResponse {

  @ApiModelProperty("Status of the request")
  private TxStatusEnum status;
    
  @ApiModelProperty("message id (ebics document) of the request/transaction")
  private String msgId;

  @ApiModelProperty("message - either error message or informative OK message")
  private String message;

  @ApiModelProperty("path of the generated ebics document if applicable")
  private String ebicsDocumentPath;

  @ApiModelProperty("Command issued to Ebics interface")
  private String command;

  @ApiModelProperty("ebics.mode setting")
  private EbicsMode ebicsMode;

}
