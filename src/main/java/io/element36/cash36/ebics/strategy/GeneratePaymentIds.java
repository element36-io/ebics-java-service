package io.element36.cash36.ebics.strategy;

import javax.servlet.http.HttpServletRequest;

import io.element36.cash36.ebics.dto.PeggingPayment;

public interface GeneratePaymentIds {

  String getMsgId(PeggingPayment r, HttpServletRequest req);

  String getPmtInfId(PeggingPayment r, HttpServletRequest req);
}
