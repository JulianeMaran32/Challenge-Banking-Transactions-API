package juhmaran.challenge.bankingtransactionsapi.domain.service;

import juhmaran.challenge.bankingtransactionsapi.domain.entity.Account;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class AccountOperationService {

  public void applyCredit(Account account, BigDecimal amount) {
    Objects.requireNonNull(account, "Conta não pode ser nula ao aplicar crédito.");
    Objects.requireNonNull(amount, "Valor de crédito não pode ser nulo.");
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("O valor de crédito deve ser positivo.");
    }
    account.setBalance(account.getBalance().add(amount));
  }

  public void applyDebit(Account account, BigDecimal amount) {
    Objects.requireNonNull(account, "Conta não pode ser nula ao aplicar débito.");
    Objects.requireNonNull(amount, "Valor de débito não pode ser nulo.");
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("O valor de débito deve ser positivo.");
    }
    if (account.getBalance().compareTo(amount) < 0) {
      throw new juhmaran.challenge.bankingtransactionsapi.domain.exception.InsufficientFundsException(
        "Saldo insuficiente para a conta " + account.getAccountNumber() + ". Débito solicitado: " + amount);
    }
    account.setBalance(account.getBalance().subtract(amount));
  }

}
