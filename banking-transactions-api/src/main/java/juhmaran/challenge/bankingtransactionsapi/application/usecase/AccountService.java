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

/**
 * Implementação do serviço de contas bancárias.
 * <p>
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
@Service
@RequiredArgsConstructor
public class AccountService implements AccountServicePort {

  private static final Logger logger = LoggerFactory.getLogger(AccountService.class); // Inicializar Logger

  private final AccountRepositoryPort accountRepositoryPort;

  @Override
  @Transactional // Garante que a operação seja atômica.
  public void performTransactions(List<TransactionRequest> transactions) { // Refactor this method to reduce its Cognitive Complexity from 17 to the 15 allowed.
    logger.info("Processando lote de {} transações.", transactions == null ? 0 : transactions.size());

    if (transactions == null || transactions.isEmpty()) {
      logger.warn("Recebido lote de transações vazio ou nulo.");
      return; // Ou lançar uma exceção se uma lista vazia for considerada erro
    }

    for (TransactionRequest transaction : transactions) { // Reduce the total number of break and continue statements in this loop to use at most one
      try {
        logger.debug("Processando transação: {}", transaction);

        // Buscar e bloquear a conta para garantir acesso exclusivo durante a transação
        Account account = accountRepositoryPort.findByAccountNumberWithLock(transaction.getAccountNumber())
          .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada: " + transaction.getAccountNumber()));

        // Validação de valor (Bean Validation já a faz, mas validação de negócio é válida aqui)
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
          logger.warn("Valor de transação inválido para conta {}: {}", transaction.getAccountNumber(), transaction.getAmount());
          // Dependendo da regra de negócio, pode lançar exceção ou pular a transação.
          // Neste caso, o Bean Validation no controller já deveria ter barrado.
          // Vamos logar e pular para seguir com as outras transações do lote.
          continue;
        }

        switch (transaction.getType()) {
          case DEBIT:
            logger.debug("Débito de {} na conta {}", transaction.getAmount(), account.getAccountNumber());
            account.debit(transaction.getAmount());
            break;
          case CREDIT:
            logger.debug("Crédito de {} na conta {}", transaction.getAmount(), account.getAccountNumber());
            account.credit(transaction.getAmount());
            break;
          default:
            // Isto não deveria acontecer com o Bean Validation no DTO, mas é uma salvaguarda
            logger.warn("Tipo de transação inválido para conta {}: {}", transaction.getAccountNumber(), transaction.getType());
            continue; // Pula esta transação inválida
        }

        // Salvar a conta com o novo saldo.
        accountRepositoryPort.save(account);
        logger.debug("Transação processada com sucesso para conta {}. Novo saldo: {}", account.getAccountNumber(), account.getBalance());

      } catch (
        AccountNotFoundException e) { // Either log this exception and handle it, or rethrow it with some contextual information.
        logger.error("Erro ao processar transação: {}", e.getMessage()); // Similar log messages
        throw e; // Relança para ser capturado pelo GlobalExceptionHandler
      } catch (InsufficientFundsException e) { // Combine this catch with the one at line 79, which has the same body.
        // Either log this exception and handle it, or rethrow it with some contextual information.
        logger.error("Erro ao processar transação: {}", e.getMessage()); // Similar log messages
        throw e; // Relança para ser capturado pelo GlobalExceptionHandler (HTTP 409)
      } catch (
        IllegalArgumentException e) { // Either log this exception and handle it, or rethrow it with some contextual information.
        // Captura exceções de argumentos inválidos (ex: amount <= 0 nos métodos debit/credit)
        logger.error("Erro de argumento inválido ao processar transação para conta {}: {}",
          transaction.getAccountNumber(), e.getMessage());
        // Decida se quer relançar como 400 ou tratar de outra forma.
        // Para manter o fluxo do GlobalExceptionHandler, podemos relançar como BadRequest.
        // Poderíamos criar uma exceção customizada para isso. Por enquanto, relançamos com info genérica.
        throw new IllegalArgumentException("Dados da transação inválidos: " + e.getMessage());
      } catch (Exception e) { // Either log this exception and handle it, or rethrow it with some contextual information
        // Captura outros erros inesperados
        logger.error("Erro inesperado ao processar transação para conta {}: {}", transaction.getAccountNumber(), e.getMessage(), e);
        // Decide se quer relançar uma exceção genérica ou específica
        throw new RuntimeException("Erro interno ao processar transação para conta " + transaction.getAccountNumber(), e); // Define and throw a dedicated exception instead of using a generic one.
      }
    }
    logger.info("Lote de transações concluído.");
  }

  @Override
  public BigDecimal getAccountBalance(String accountNumber) {
    logger.info("Buscando saldo para conta: {}", accountNumber);
    Account account = accountRepositoryPort.findByAccountNumber(accountNumber)
      .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada: " + accountNumber));

    logger.info("Saldo encontrado para conta {}: {}", accountNumber, account.getBalance());
    return account.getBalance();
  }

  // --- IMPLEMENTAÇÃO DO METODO DE INICIALIZAÇÃO ---
  @Override
  @Transactional // Garante que a busca e o save (se ocorrer) sejam atômicos
  public void createAccountIfNotFound(String accountNumber, BigDecimal initialBalance) {
    logger.debug("Tentando criar conta se não existir: {}", accountNumber);
    // Usamos existsByAccountNumber para uma verificação rápida e eficiente sem carregar a entidade
    boolean exists = accountRepositoryPort.existsByAccountNumber(accountNumber);

    if (!exists) {
      try {
        // Cria a nova entidade
        Account newAccount = new Account(null, accountNumber, initialBalance);
        // Salva no banco de dados
        accountRepositoryPort.save(newAccount);
        logger.info("Conta '{}' criada com sucesso. Saldo inicial: {}", accountNumber, initialBalance);
      } catch (Exception e) {
        // Captura exceções caso ocorra algum problema no save (ex: violação de unique constraint, embora existsByAccountNumber minimize isso)
        logger.error("Erro ao salvar a conta '{}' durante a inicialização: {}", accountNumber, e.getMessage(), e);
        // Dependendo da necessidade, pode-se relançar ou apenas logar.
        // Para inicialização, logar e continuar é razoável, a menos que o erro seja crítico.
      }
    } else {
      logger.info("Conta '{}' já existe. Pulando criação.", accountNumber);
    }
  }

}
