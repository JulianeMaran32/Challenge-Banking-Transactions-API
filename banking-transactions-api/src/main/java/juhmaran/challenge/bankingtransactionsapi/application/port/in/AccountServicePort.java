package juhmaran.challenge.bankingtransactionsapi.application.port.in;

import juhmaran.challenge.bankingtransactionsapi.domain.entity.Account;
import juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.request.TransactionRequest;

import java.util.List;

public interface AccountServicePort {

  void performTransactions(List<TransactionRequest> transactions);

  Account getAccountBalance(String accountNumber);

}
