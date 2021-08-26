package io.element36.cash36.ebics.dto.libeufin;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BatchedTransactionDetails {
  Name debtor;
  Iban dectorAccount;
  Bic debtorAgent;
  Name creditor;
  Iban creditorAccount;
  // should that be a Bic?
  Name creditorAgent;

  @NoArgsConstructor
  public class Name {
    String name;
  }

  @NoArgsConstructor
  public class Iban {
    String iban;
  }

  @NoArgsConstructor
  public class Bic {
    String bic;
  }
}
