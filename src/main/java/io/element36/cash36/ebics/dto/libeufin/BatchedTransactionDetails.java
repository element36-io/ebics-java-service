package io.element36.cash36.ebics.dto.libeufin;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BatchedTransactionDetails {
  Name debtor;
  Iban debtorAccount;
  Bic debtorAgent;
  Name creditor;
  Iban creditorAccount;
  // should that be a Bic?
  Name creditorAgent;

  @NoArgsConstructor
  @Data
  public class Name {
    String name;
  }

  @NoArgsConstructor
  @Data
  public class Iban {
    String iban;
  }

  @NoArgsConstructor
  @Data
  public class Bic {
    String bic;
  }
  
  String endToEndId; 
  String paymentInformationId; 
  String unstructuredRemittanceInformation; 
}
