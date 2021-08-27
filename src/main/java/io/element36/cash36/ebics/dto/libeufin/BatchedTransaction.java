package io.element36.cash36.ebics.dto.libeufin;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BatchedTransaction {
    
     String amount;
     String creditDebitIndicator;
     String endToEndId;
     String unstructuredRemittanceInformation;
    
     BatchedTransactionDetails details;

}
