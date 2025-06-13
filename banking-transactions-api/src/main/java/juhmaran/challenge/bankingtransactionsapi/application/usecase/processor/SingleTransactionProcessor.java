package juhmaran.challenge.bankingtransactionsapi.application.usecase.processor;

import juhmaran.challenge.bankingtransactionsapi.application.port.out.AccountRepositoryPort;
import juhmaran.challenge.bankingtransactionsapi.domain.entity.Account;
import juhmaran.challenge.bankingtransactionsapi.domain.exception.AccountNotFoundException;
import juhmaran.challenge.bankingtransactionsapi.domain.exception.InsufficientFundsException;
import juhmaran.challenge.bankingtransactionsapi.domain.exception.InvalidTransactionTypeException;
import juhmaran.challenge.bankingtransactionsapi.domain.exception.TransactionProcessingException;
import juhmaran.challenge.bankingtransactionsapi.domain.service.AccountOperationService;
import juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.enums.TransactionType;
import juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.request.TransactionRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SingleTransactionProcessor {

  private static final Logger logger = LoggerFactory.getLogger(SingleTransactionProcessor.class);

  private final AccountRepositoryPort accountRepositoryPort;
  private final AccountOperationService accountOperationService;

  public void process(TransactionRequest transaction) {
    Objects.requireNonNull(transaction, "Transação não pode ser nula.");
    logger.debug("Iniciando processamento de transação para conta {}", transaction.accountNumber());

    try {
      Account account = findAndLockAccount(transaction.accountNumber());
      validateTransactionType(transaction.type(), transaction.accountNumber());
      applyOperation(account, transaction.amount(), transaction.type());
      saveAccount(account);
      logger.debug("Processamento de transação concluído com sucesso para conta {}", account.getAccountNumber());
    } catch (AccountNotFoundException | InsufficientFundsException | InvalidTransactionTypeException e) {
      logger.error("Erro de domínio ao processar transação para conta {}: {}", transaction.accountNumber(), e.getMessage());
      throw new TransactionProcessingException(
        "Erro ao processar transação para conta " + transaction.accountNumber() + ": " + e.getMessage(), e
      );
    } catch (Exception e) {
      logger.error("Erro inesperado ao processar transação para conta {}: {}",
        transaction.accountNumber(), e.getMessage(), e);
      throw new TransactionProcessingException(
        "Ocorreu um erro interno ao processar a transação para a conta " + transaction.accountNumber(), e
      );
    }
  }

  private Account findAndLockAccount(String accountNumber) {
    Objects.requireNonNull(accountNumber, "Número da conta não pode ser nulo ao buscar.");
    logger.debug("Buscando e bloqueando conta: {}", accountNumber);
    return accountRepositoryPort.findByAccountNumberWithLock(accountNumber)
      .orElseThrow(() -> {
        logger.warn("Conta não encontrada para processamento: {}", accountNumber);
        return new AccountNotFoundException("Conta não encontrada: " + accountNumber);
      });
  }

  private void validateTransactionType(TransactionType type, String accountNumber) {
    if (type == null) {
      logger.warn("Tipo de transação é nulo para conta {}", accountNumber);
      throw new InvalidTransactionTypeException("Tipo de transação não especificado para conta: " + accountNumber);
    }
  }

  private void applyOperation(Account account, BigDecimal amount, TransactionType type) {
    Objects.requireNonNull(account, "Conta não pode ser nula ao aplicar operação.");
    Objects.requireNonNull(amount, "Valor não pode ser nulo ao aplicar operação.");
    Objects.requireNonNull(type, "Tipo não pode ser nulo ao aplicar operação.");

    logger.debug("Delegando operação {} de {} para AccountOperationService na conta {}",
      type, amount, account.getAccountNumber());

    switch (type) {
      case DEBIT:
        accountOperationService.applyDebit(account, amount);
        break;
      case CREDIT:
        accountOperationService.applyCredit(account, amount);
        break;
      default:
        logger.warn("Tipo de transação inválido {} encontrado ao aplicar operação para conta {}",
          type, account.getAccountNumber());
        throw new InvalidTransactionTypeException("Tipo de transação inválido para conta: " +
          account.getAccountNumber());
    }
  }

  private void saveAccount(Account account) {
    Objects.requireNonNull(account, "Conta não pode ser nula ao salvar.");
    logger.debug("Salvando conta {}", account.getAccountNumber());
    accountRepositoryPort.save(account);
  }

}
