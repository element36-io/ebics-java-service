package io.element36.cash36.ebics.dto.libeufin;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
/**
 * Get result from sPOST - /bank-accounts/CH2108307000289537320/fetch-transactions
 * or command
 * libeufin-cli accounts fetch-transactions CH2108307000289537320
 */
public class FetchTransactionsResponse {

  int newTransactions;
  int downloadedTransactions;

}
