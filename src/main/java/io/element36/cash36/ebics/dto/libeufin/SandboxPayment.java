package io.element36.cash36.ebics.dto.libeufin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SandboxPayment {

  // curl -d
  // '{"creditorIban":"DE18500105172929531881","creditorBic":"INGDDEFFXXX","creditorName":"element36
  // account1","debtorIban":"DE18500105172929531882","debtorBic":"INGDDEFFXXX", "debtorName":"test",
  // "amount":"200", "currency":"EUR", "subject":"test", "direction":"CRDT","uid":"abc"}' -H
  // "Content-Type: application/json" -X POST $LIBEUFIN_SANDBOX_URL"admin/payments"

  // IBAN that will receive the payment.
  String creditorIban;
  String creditorBic;
  String creditorName;
  // IBAN that will send the payment.
  String debtorIban;
  String debtorBic;
  String debtorName;
  String amount;
  String currency;
  // subject of the payment.
  String subject;
  // Whether the payment is credit or debit *for* the
  // account being managed *by* the running sandbox.
  // Can take the values: "CRDT" or "DBIT".
  String direction;
  String uid; 
}
