package io.element36.cash36.ebics.strategy;

import javax.servlet.http.HttpServletRequest;

import io.element36.cash36.ebics.dto.UnpegPayment;

public interface GeneratePaymentIds {

  String getMsgId(UnpegPayment r, HttpServletRequest req);

  String getPmtInfId(UnpegPayment r, HttpServletRequest req);
}
