package juhmaran.challenge.bankingtransactionsapi.application.port.in;

import juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.request.TransactionRequest;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Juliane Maran
 *
 * @since 11/06/2025
 */
public interface AccountServicePort {

  void performTransactions(List<TransactionRequest> transactions);

  BigDecimal getAccountBalance(String accountNumber);

  // remover
  void createAccountIfNotFound(String accountNumber, BigDecimal initialBalance);

}
