package io.element36.cash36.ebics.config;

import io.swagger.annotations.ApiModelProperty;

public enum EbicsMode {
  @ApiModelProperty(
    value ="Ebics commands are sent to the configured bank")
  enabled,
  @ApiModelProperty(
    value ="Ebics commands are NOT sent to the configured bank, but Ebics documents are "+
    "generated in the configured out folder ")
  disabled,
  @ApiModelProperty(
    value ="getBankStatements (/bankstatements) will return a static/fake file for testing purposes")
  proxy;
}
