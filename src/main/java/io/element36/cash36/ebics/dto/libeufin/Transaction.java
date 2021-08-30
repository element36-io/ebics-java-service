package io.element36.cash36.ebics.dto.libeufin;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Transaction {
    

	  // money moved by the transaction
    String amount;

	  // CRDT or DBIT
    String creditDebitIndicator;

	  // Two of the most used values are BOOK, or PENDING
    String status;

    String bankTransactionCode;

    String valueDate;

	  // When this payment got booked.  In the form YYYY-MM-DD
    String bookingDate;

    String accountServicerRef;

    Batches[] batches;
    
}
