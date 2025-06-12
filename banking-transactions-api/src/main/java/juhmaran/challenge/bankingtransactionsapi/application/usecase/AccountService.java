package juhmaran.challenge.bankingtransactionsapi.application.usecase;

import jakarta.transaction.Transactional;
import juhmaran.challenge.bankingtransactionsapi.application.port.in.AccountServicePort;
import juhmaran.challenge.bankingtransactionsapi.application.port.out.AccountRepositoryPort;
import juhmaran.challenge.bankingtransactionsapi.domain.entity.Account;
import juhmaran.challenge.bankingtransactionsapi.domain.exception.AccountNotFoundException;
import juhmaran.challenge.bankingtransactionsapi.domain.exception.InsufficientFundsException;
import juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.request.TransactionRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AccountService implements AccountServicePort {

  private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

  private final AccountRepositoryPort accountRepositoryPort;

  @Override
  @Transactional
  public void performTransactions(List<TransactionRequest> transactions) {
    logger.info("Processando lote de {} transações.", transactions == null ? 0 : transactions.size());

    if (transactions == null || transactions.isEmpty()) {
      logger.warn("Recebido lote de transações vazio ou nulo.");
      return;
    }

    for (TransactionRequest transaction : transactions) {
      processSingleTransaction(transaction);
    }

    logger.info("Lote de transações concluído.");
  }

  private void processSingleTransaction(TransactionRequest transaction) {
    Objects.requireNonNull(transaction, "Transação não pode ser nula.");
    logger.debug("Processando transação: {}", transaction);

    Account account = accountRepositoryPort.findByAccountNumberWithLock(transaction.accountNumber())
      .orElseThrow(() -> {
        logger.warn("Conta não encontrada para transação com número: {}", transaction.accountNumber());
        return new AccountNotFoundException("Conta não encontrada: " + transaction.accountNumber());
      });

    try {
      if (transaction.type() == null) {
        logger.warn("Tipo de transação é nulo para conta {}", transaction.accountNumber());
        throw new IllegalArgumentException("Tipo de transação não especificado.");
      }

      switch (transaction.type()) {
        case DEBIT:
          logger.debug("Débito de {} na conta {}", transaction.amount(), account.getAccountNumber());
          account.debit(transaction.amount());
          break;
        case CREDIT:
          logger.debug("Crédito de {} na conta {}", transaction.amount(), account.getAccountNumber());
          account.credit(transaction.amount());
          break;
        default:
          logger.warn("Tipo de transação inválido {} para conta {}", transaction.type(), account.getAccountNumber());
          throw new IllegalArgumentException("Tipo de transação inválido.");
      }

      accountRepositoryPort.save(account);
      logger.debug("Transação processada com sucesso para conta {}. Novo saldo: {}", account.getAccountNumber(), account.getBalance());

    } catch (AccountNotFoundException | InsufficientFundsException | IllegalArgumentException e) {
      logger.error("Erro ao processar transação para conta {}: {}", transaction.accountNumber(), e.getMessage());
      throw e;
    } catch (Exception e) {
      logger.error("Erro inesperado ao processar transação para conta {}: {}", transaction.accountNumber(), e.getMessage(), e);
      throw new RuntimeException("Ocorreu um erro interno ao processar a transação para a conta " + transaction.accountNumber(), e);
    }
  }

  @Override
  public Account getAccountBalance(String accountNumber) {
    Objects.requireNonNull(accountNumber, "Número da conta não pode ser nulo.");
    logger.info("Buscando saldo para conta: {}", accountNumber);

    Account account = accountRepositoryPort.findByAccountNumber(accountNumber)
      .orElseThrow(() -> {
        logger.warn("Conta não encontrada ao buscar saldo: {}", accountNumber);
        return new AccountNotFoundException("Conta não encontrada: " + accountNumber);
      });

    logger.info("Conta encontrada ao buscar saldo para {}. Saldo: {}", accountNumber, account.getBalance());
    return account; // Retorna a entidade Account
  }

  // Método de inicialização
  @Transactional
  public void createAccountIfNotFound(String accountNumber, BigDecimal initialBalance) {
    Objects.requireNonNull(accountNumber, "Número da conta não pode ser nulo.");
    Objects.requireNonNull(initialBalance, "Saldo inicial não pode ser nulo.");
    logger.debug("Tentando criar conta se não existir: {}", accountNumber);

    boolean exists = accountRepositoryPort.existsByAccountNumber(accountNumber);

    if (!exists) {
      try {
        Account newAccount = new Account(null, accountNumber, initialBalance);
        accountRepositoryPort.save(newAccount);
        logger.info("Conta '{}' criada com sucesso com saldo inicial: {}", accountNumber, initialBalance);
      } catch (Exception e) {
        logger.error("Erro ao salvar a conta '{}' durante a inicialização: {}", accountNumber, e.getMessage(), e);
      }
    } else {
      logger.info("Conta '{}' já existe. Pulando criação.", accountNumber);
    }
  }

}
