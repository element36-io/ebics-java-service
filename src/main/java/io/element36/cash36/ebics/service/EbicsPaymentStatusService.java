package io.element36.cash36.ebics.service;

import java.util.List;

import io.element36.cash36.ebics.dto.PaymentStatusReportDTO;

public interface EbicsPaymentStatusService {
    List<PaymentStatusReportDTO> getStatusReport();
}
