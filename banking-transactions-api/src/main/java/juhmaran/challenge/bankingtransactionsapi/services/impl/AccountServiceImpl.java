package juhmaran.challenge.bankingtransactionsapi.services.impl;

import juhmaran.challenge.bankingtransactionsapi.dtos.request.TransactionRequest;
import juhmaran.challenge.bankingtransactionsapi.dtos.response.AccountBalanceResponse;
import juhmaran.challenge.bankingtransactionsapi.dtos.response.TransactionBatchResponse;
import juhmaran.challenge.bankingtransactionsapi.dtos.response.TransactionResponse;
import juhmaran.challenge.bankingtransactionsapi.entities.Account;
import juhmaran.challenge.bankingtransactionsapi.entities.Transaction;
import juhmaran.challenge.bankingtransactionsapi.enums.TransactionType;
import juhmaran.challenge.bankingtransactionsapi.exceptions.AccountNotFoundException;
import juhmaran.challenge.bankingtransactionsapi.exceptions.InsufficientFundsException;
import juhmaran.challenge.bankingtransactionsapi.exceptions.TransactionProcessingException;
import juhmaran.challenge.bankingtransactionsapi.repositories.AccountRepository;
import juhmaran.challenge.bankingtransactionsapi.repositories.TransactionRepository;
import juhmaran.challenge.bankingtransactionsapi.services.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do serviço de contas bancárias.
 * <p>
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
@Service
public class AccountServiceImpl implements AccountService {

  private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

  private final AccountRepository accountRepository;
  private final TransactionRepository transactionsRepository;

  @Autowired
  public AccountServiceImpl(AccountRepository accountRepository,
                            TransactionRepository transactionsRepository) {
    this.accountRepository = accountRepository;
    this.transactionsRepository = transactionsRepository;
  }

  // Metodo principal para realizar transações em lote
  // `@Transactional`: Garante que todas as operações de banco dentro deste metodo
  // ocorram em uma única transação atômica. Se qualquer passo falhar, tudo é
  // revertido (rollback). Essencial para a consistência.
  @Override
  @Transactional
  public TransactionBatchResponse performTransactions(String accountNumber, List<TransactionRequest> requests) {
    // 1. Buscar a conta COM BLOQUEIO PESIMISTA
    // O bloqueio (@Lock(LockModeType.PESSIMISTIC_WRITE) no findAccountWithLockByAccountNumber)
    // impede que outras transações concorrentes modifiquem o saldo desta conta
    // até que a transação atual (deste método) termine.
    Account account = accountRepository.findAccountWithLockByAccountNumber(accountNumber)
      .orElseThrow(() -> new AccountNotFoundException("Conta com número '" + accountNumber + "' não encontrada."));

    List<Transaction> createdTransactions = new ArrayList<>();
    BigDecimal currentBalance = account.getBalance(); // Use uma variável local para o saldo durante o processamento

    try {
      // 2. Processar cada requisição de transação no lote
      for (TransactionRequest request : requests) {
        BigDecimal amount = request.amount();
        TransactionType type = request.type();

        // Validação básica do valor (já feita pelo @Valid no controller, mas boa prática defensiva)
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
          // Isso não deve acontecer se a validação funcionar, mas é uma garantia
          throw new TransactionProcessingException("Valor de transação inválido: " + amount);
        }

        // 3. Aplicar a lógica de débito/crédito
        if (type == TransactionType.DEBIT) {
          // Verificar saldo APÓS obter o lock
          if (currentBalance.compareTo(amount) < 0) {
            // Lança exceção se o saldo for insuficiente
            // A anotação @Transactional garantirá que todas as transações anteriores
            // (se houverem no mesmo lote) E a busca inicial com lock serão revertidas.
            throw new InsufficientFundsException(
              "Saldo insuficiente na conta '" + accountNumber + "' para o débito de " + amount + ". Saldo atual: " + currentBalance);
          }
          currentBalance = currentBalance.subtract(amount);
        } else if (type == TransactionType.CREDIT) {
          currentBalance = currentBalance.add(amount);
        } else {
          // Isso não deve acontecer com um enum, mas é uma garantia
          throw new TransactionProcessingException("Tipo de transação inválido: " + type);
        }

        // 4. Criar a entidade Transaction (ainda não salva)
        Transaction transaction = new Transaction(amount, type, account);
        // A entidade Account gerencia a adição da transação à sua lista e a ligação bidirecional
        account.addTransaction(transaction);
        createdTransactions.add(transaction); // Adiciona à lista de transações que serão salvas
      }

      // 5. Atualizar o saldo na entidade Account
      account.setBalance(currentBalance);

      // 6. Salvar a Account (as Transactions são salvas em cascata devido a CascadeType.ALL)
      accountRepository.save(account);

      // O @Transactional commitará todas as mudanças (Account e Transactions) atomicamente aqui
      // se não houver exceções lançadas acima.

      // 7. Construir a resposta
      List<TransactionResponse> responseTransactions = createdTransactions.stream()
        .map(tx -> new TransactionResponse(
          tx.getId(),
          tx.getAmount(),
          tx.getType().name(),
          tx.getTimestamp().toString() // Formato ISO 8601
        ))
        .toList();

      return new TransactionBatchResponse(
        account.getAccountNumber(),
        account.getBalance(), // Retorna o saldo final da entidade salva
        createdTransactions.size(),
        responseTransactions
      );

    } catch (InsufficientFundsException | AccountNotFoundException e) {
      // Relança exceções de negócio conhecidas para serem tratadas pelo ExceptionHandler
      throw e;
    } catch (DataAccessException e) {
      // Captura exceções de acesso a dados (ex: problemas de conexão, deadlock, etc.)
      // Embora o lock pessimista reduza a chance de deadlocks, eles ainda podem ocorrer.
      // Spring lida com deadlocks lançando DataAccessExceptions, que causam rollback.
      // É bom logar e relançar uma exceção mais genérica para a camada superior.
      throw new TransactionProcessingException("Erro ao processar transações para conta '" + accountNumber + "'. Detalhes: " + e.getMessage());
    } catch (Exception e) {
      // Captura quaisquer outras exceções inesperadas
      throw new TransactionProcessingException("Erro inesperado ao processar transações para conta '" + accountNumber + "'. Detalhes: " + e.getMessage());
    }
  }

  /**
   * Obtém o saldo da conta bancária.
   *
   * @param accountNumber O número da conta.
   * @return Uma resposta contendo o número da conta e o saldo.
   */
  @Override
  @Transactional(readOnly = true) // Marca como transação somente leitura (otimização)
  public AccountBalanceResponse getBalance(String accountNumber) {
    // Buscar a conta SEM BLOQUEIO (pois é apenas uma leitura)
    Account account = accountRepository.findByAccountNumber(accountNumber)
      .orElseThrow(() -> new AccountNotFoundException("Conta com número '" + accountNumber + "' não encontrada."));

    return new AccountBalanceResponse(account.getAccountNumber(), account.getBalance());
  }

  /**
   * Metodo auxiliar para criar conta.
   *
   * @param accountNumber O número da nova conta.
   * @return O número da conta criada.
   */
  @Override
  @Transactional
  public String createAccount(String accountNumber) {
    return createAccount(accountNumber, BigDecimal.ZERO);
  }

  /**
   * Metodo auxiliar para criar contas com saldo inicial
   *
   * @param accountNumber  O número da nova conta.
   * @param initialBalance O saldo inicial.
   * @return O número da conta criada.
   */
  @Override
  @Transactional
  public String createAccount(String accountNumber, BigDecimal initialBalance) {
    if (accountRepository.existsByAccountNumber(accountNumber)) {
      // Poderia lançar uma exceção customizada aqui também
      throw new TransactionProcessingException("Conta com número '" + accountNumber + "' já existe.");
    }
    Account account = new Account(accountNumber, initialBalance != null ? initialBalance : BigDecimal.ZERO);
    accountRepository.save(account);
    return account.getAccountNumber();
  }

}
