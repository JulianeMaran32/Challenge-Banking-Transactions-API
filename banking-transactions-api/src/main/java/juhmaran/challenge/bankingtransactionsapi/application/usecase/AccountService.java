package juhmaran.challenge.bankingtransactionsapi.application.usecase;

import jakarta.transaction.Transactional;
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

/**
 * Implementação do serviço de contas bancárias.
 * <p>
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
@Service
@RequiredArgsConstructor
public class AccountService { // Não implementa mais AccountServicePort diretamente, mas oferece os métodos públicos

  private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

  private final AccountRepositoryPort accountRepositoryPort;

  // Metodo público que implementa a operação performTransactions do AccountServicePort
  // Pode ser movido para uma nova interface TransactionServicePort se houver mais operações de transação
  @Transactional
  public void performTransactions(List<TransactionRequest> transactions) {
    logger.info("Processing batch of {} transactions.", transactions == null ? 0 : transactions.size());

    if (transactions == null || transactions.isEmpty()) {
      logger.warn("Received empty or null transaction batch.");
      return;
    }

    for (TransactionRequest transaction : transactions) {
      processSingleTransaction(transaction);
    }

    logger.info("Transaction batch completed.");
  }

  // Metodo privado para processar uma única transação, extraído para melhorar SRP e legibilidade
  // Exceções de negócio (AccountNotFound, InsufficientFunds, IllegalArgument) são propagadas
  // para serem tratadas pelo GlobalExceptionHandler. Erros inesperados são capturados e relançados como RuntimeException.
  private void processSingleTransaction(TransactionRequest transaction) {
    Objects.requireNonNull(transaction, "Transaction cannot be null.");
    logger.debug("Processing transaction: {}", transaction);

    // Fetch and lock the account to ensure exclusive access during the transaction
    Account account = accountRepositoryPort.findByAccountNumberWithLock(transaction.getAccountNumber())
      .orElseThrow(() -> {
        logger.warn("Account not found for transaction: {}", transaction.getAccountNumber());
        return new AccountNotFoundException("Conta não encontrada: " + transaction.getAccountNumber());
      });

    // Value validation relies primarily on Bean Validation in the DTO and specific entity methods,
    // but an explicit check before calling debit/credit is also fine.
    // Let's rely on the checks inside account.debit/credit now.
    // If a value <= 0 somehow passes Bean Validation, debit/credit will throw IllegalArgumentException.

    try {
      if (transaction.getType() == null) {
        logger.warn("Transaction type is null for account {}", transaction.getAccountNumber());
        // Handle or throw an exception for invalid transaction type
        throw new IllegalArgumentException("Tipo de transação não especificado.");
      }

      switch (transaction.getType()) {
        case DEBIT:
          logger.debug("Debit of {} from account {}", transaction.getAmount(), account.getAccountNumber());
          account.debit(transaction.getAmount());
          break;
        case CREDIT:
          logger.debug("Credit of {} to account {}", transaction.getAmount(), account.getAccountNumber());
          account.credit(transaction.getAmount());
          break;
        default:
          // This case should theoretically not be reachable if TransactionType is a sealed enum or handled by validation
          logger.warn("Invalid transaction type {} for account {}", transaction.getType(), account.getAccountNumber());
          throw new IllegalArgumentException("Tipo de transação inválido.");
      }

      // Save the account with the new balance.
      accountRepositoryPort.save(account);
      logger.debug("Transaction successfully processed for account {}. New balance: {}", account.getAccountNumber(), account.getBalance());

    } catch (AccountNotFoundException | InsufficientFundsException | IllegalArgumentException e) {
      // Catch known business/validation exceptions and rethrow for GlobalExceptionHandler
      logger.error("Error processing transaction for account {}: {}", transaction.getAccountNumber(), e.getMessage());
      throw e;
    } catch (Exception e) {
      // Catch any other unexpected exceptions
      logger.error("Unexpected error processing transaction for account {}: {}", transaction.getAccountNumber(), e.getMessage(), e);
      // Relançar uma exceção genérica interna, GlobalExceptionHandler a mapeará para 500
      throw new RuntimeException("Erro interno ao processar transação para conta " + transaction.getAccountNumber(), e);
    }
  }


  // Método público que implementa a operação getAccountBalance do AccountServicePort
  // Pode ser movido para uma nova interface AccountQueryServicePort
  public BigDecimal getAccountBalance(String accountNumber) {
    Objects.requireNonNull(accountNumber, "Account number cannot be null.");
    logger.info("Fetching balance for account: {}", accountNumber);

    Account account = accountRepositoryPort.findByAccountNumber(accountNumber)
      .orElseThrow(() -> {
        logger.warn("Account not found when fetching balance: {}", accountNumber);
        return new AccountNotFoundException("Conta não encontrada: " + accountNumber);
      });

    logger.info("Balance found for account {}: {}", accountNumber, account.getBalance());
    return account.getBalance();
  }

  // Método público específico para inicialização de dados, NÃO faz parte do AccountServicePort principal
  // Segue SRP isolando a lógica de criação inicial.
  @Transactional
  public void createAccountIfNotFound(String accountNumber, BigDecimal initialBalance) {
    Objects.requireNonNull(accountNumber, "Account number cannot be null.");
    Objects.requireNonNull(initialBalance, "Initial balance cannot be null.");
    logger.debug("Attempting to create account if not exists: {}", accountNumber);

    // Use existsByAccountNumber for an efficient check without loading the entity
    boolean exists = accountRepositoryPort.existsByAccountNumber(accountNumber);

    if (!exists) {
      try {
        // Create the new entity
        Account newAccount = new Account(null, accountNumber, initialBalance);
        // Save to database
        accountRepositoryPort.save(newAccount);
        logger.info("Account '{}' successfully created with initial balance: {}", accountNumber, initialBalance);
      } catch (Exception e) {
        // Catch exceptions during save (e.g., unique constraint violation in a race condition)
        logger.error("Error saving account '{}' during initialization: {}", accountNumber, e.getMessage(), e);
        // Decide whether to rethrow or just log. For initialization, logging is usually sufficient.
      }
    } else {
      logger.info("Account '{}' already exists. Skipping creation.", accountNumber);
    }
  }

}
