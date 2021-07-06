package io.element36.cash36.ebics.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentStatusReportDTO {
  private String msgId;
  private boolean paymentAccepted; // ACCP
  private boolean paymentValid; // ACTC
  private List<String> errorCodesALevel;
  private List<String> errorCodesBLevel;
  private List<String> errorCodesCLevel;
}
