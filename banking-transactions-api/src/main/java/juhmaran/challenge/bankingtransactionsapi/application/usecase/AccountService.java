package juhmaran.challenge.bankingtransactionsapi.application.usecase;

import jakarta.transaction.Transactional;
import juhmaran.challenge.bankingtransactionsapi.application.port.in.AccountServicePort;
import juhmaran.challenge.bankingtransactionsapi.application.port.out.AccountRepositoryPort;
import juhmaran.challenge.bankingtransactionsapi.application.usecase.processor.SingleTransactionProcessor;
import juhmaran.challenge.bankingtransactionsapi.domain.entity.Account;
import juhmaran.challenge.bankingtransactionsapi.domain.exception.AccountNotFoundException;
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
  private final SingleTransactionProcessor singleTransactionProcessor;

  @Override
  @Transactional
  public void performTransactions(List<TransactionRequest> transactions) {
    logger.info("Processando lote de {} transações.", transactions == null ? 0 : transactions.size());

    if (transactions == null || transactions.isEmpty()) {
      logger.warn("Recebido lote de transações vazio ou nulo.");
      return;
    }

    for (TransactionRequest transaction : transactions) {
      singleTransactionProcessor.process(transaction);
    }

    logger.info("Lote de transações concluído.");
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
    return account;
  }

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
