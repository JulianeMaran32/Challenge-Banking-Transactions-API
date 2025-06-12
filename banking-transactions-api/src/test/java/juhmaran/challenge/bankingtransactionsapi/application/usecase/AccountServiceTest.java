package juhmaran.challenge.bankingtransactionsapi.application.usecase;

import juhmaran.challenge.bankingtransactionsapi.application.port.out.AccountRepositoryPort;
import juhmaran.challenge.bankingtransactionsapi.domain.entity.Account;
import juhmaran.challenge.bankingtransactionsapi.domain.exception.AccountNotFoundException;
import juhmaran.challenge.bankingtransactionsapi.domain.exception.InsufficientFundsException;
import juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.enums.TransactionType;
import juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.request.TransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita o Mockito com JUnit 5
class AccountServiceTest {

  @Mock // Cria um mock para a dependência AccountRepositoryPort
  private AccountRepositoryPort accountRepositoryPort;

  @InjectMocks // Injeta os mocks na instância de AccountService
  private AccountService accountService;

  private Account account;
  private final String accountNumber = "12345";

  @BeforeEach
  void setUp() {
    // Configura uma conta inicial para os testes
    account = new Account(1L, accountNumber, new BigDecimal("1000.00"));
  }

  @Test
  @DisplayName("Deve realizar lançamento de crédito com sucesso")
  void shouldPerformCreditTransactionSuccessfully() {
    BigDecimal creditAmount = new BigDecimal("500.00");
    TransactionRequest creditRequest = new TransactionRequest();
    creditRequest.setAccountNumber(accountNumber);
    creditRequest.setAmount(creditAmount);
    creditRequest.setType(TransactionType.CREDIT);

    // Simula o comportamento do repositório ao buscar e bloquear a conta
    when(accountRepositoryPort.findByAccountNumberWithLock(accountNumber))
      .thenReturn(Optional.of(account));

    // Simula o comportamento do repositório ao salvar (não precisa retornar a conta modificada para este teste)
    when(accountRepositoryPort.save(any(Account.class)))
      .thenReturn(account); // Ou simply doNothing() if save doesn't return in real implementation

    accountService.performTransactions(Collections.singletonList(creditRequest));

    // Verifica se o saldo foi atualizado corretamente
    assertEquals(new BigDecimal("1500.00"), account.getBalance());

    // Verifica se os métodos do repositório foram chamados
    verify(accountRepositoryPort, times(1)).findByAccountNumberWithLock(accountNumber);
    verify(accountRepositoryPort, times(1)).save(account);
  }

  @Test
  @DisplayName("Deve realizar lançamento de débito com sucesso")
  void shouldPerformDebitTransactionSuccessfully() {
    BigDecimal debitAmount = new BigDecimal("200.00");
    TransactionRequest debitRequest = new TransactionRequest();
    debitRequest.setAccountNumber(accountNumber);
    debitRequest.setAmount(debitAmount);
    debitRequest.setType(TransactionType.DEBIT);

    when(accountRepositoryPort.findByAccountNumberWithLock(accountNumber))
      .thenReturn(Optional.of(account));
    when(accountRepositoryPort.save(any(Account.class))).thenReturn(account);


    accountService.performTransactions(Collections.singletonList(debitRequest));

    assertEquals(new BigDecimal("800.00"), account.getBalance());
    verify(accountRepositoryPort, times(1)).findByAccountNumberWithLock(accountNumber);
    verify(accountRepositoryPort, times(1)).save(account);
  }

  @Test
  @DisplayName("Deve lançar exceção quando o saldo for insuficiente para débito")
  void shouldThrowInsufficientFundsExceptionWhenDebitAmountIsTooHigh() {
    BigDecimal debitAmount = new BigDecimal("1500.00"); // Maior que o saldo inicial
    TransactionRequest debitRequest = new TransactionRequest();
    debitRequest.setAccountNumber(accountNumber);
    debitRequest.setAmount(debitAmount);
    debitRequest.setType(TransactionType.DEBIT);

    when(accountRepositoryPort.findByAccountNumberWithLock(accountNumber))
      .thenReturn(Optional.of(account));

    // Verifica se a exceção InsufficientFundsException é lançada
    assertThrows(InsufficientFundsException.class, () -> {
      accountService.performTransactions(Collections.singletonList(debitRequest));
    });

    // Verifica que a conta não foi salva (a transação falhou e o save não deve ser chamado)
    verify(accountRepositoryPort, times(1)).findByAccountNumberWithLock(accountNumber);
    verify(accountRepositoryPort, never()).save(any(Account.class)); // Save NÃO deve ser chamado
  }

  @Test
  @DisplayName("Deve lançar exceção quando a conta não for encontrada para lançamento")
  void shouldThrowAccountNotFoundExceptionWhenAccountDoesNotExistForTransaction() {
    String nonExistentAccountNumber = "99999";
    TransactionRequest transactionRequest = new TransactionRequest();
    transactionRequest.setAccountNumber(nonExistentAccountNumber);
    transactionRequest.setAmount(new BigDecimal("100.00"));
    transactionRequest.setType(TransactionType.CREDIT);

    // Simula o comportamento do repositório retornando Optional.empty()
    when(accountRepositoryPort.findByAccountNumberWithLock(nonExistentAccountNumber))
      .thenReturn(Optional.empty());

    assertThrows(AccountNotFoundException.class, () -> {
      accountService.performTransactions(Collections.singletonList(transactionRequest));
    });

    verify(accountRepositoryPort, times(1)).findByAccountNumberWithLock(nonExistentAccountNumber);
    verify(accountRepositoryPort, never()).save(any(Account.class));
  }

  @Test
  @DisplayName("Deve processar múltiplos lançamentos na mesma requisição")
  void shouldProcessMultipleTransactionsInBatch() {
    // Conta 1
    Account account1 = new Account(1L, "ACC001", new BigDecimal("500.00"));
    TransactionRequest req1 = new TransactionRequest();
    req1.setAccountNumber("ACC001");
    req1.setAmount(new BigDecimal("100.00"));
    req1.setType(TransactionType.CREDIT);

    // Conta 2
    Account account2 = new Account(2L, "ACC002", new BigDecimal("200.00"));
    TransactionRequest req2 = new TransactionRequest();
    req2.setAccountNumber("ACC002");
    req2.setAmount(new BigDecimal("50.00"));
    req2.setType(TransactionType.DEBIT);

    // Conta 1 novamente (outro lançamento)
    TransactionRequest req3 = new TransactionRequest();
    req3.setAccountNumber("ACC001");
    req3.setAmount(new BigDecimal("20.00"));
    req3.setType(TransactionType.DEBIT);

    List<TransactionRequest> transactions = Arrays.asList(req1, req2, req3);

    // Configurar mocks para buscar/bloquear cada conta
    when(accountRepositoryPort.findByAccountNumberWithLock("ACC001"))
      .thenReturn(Optional.of(account1)); // Mockando para ser encontrada 2 vezes
    when(accountRepositoryPort.findByAccountNumberWithLock("ACC002"))
      .thenReturn(Optional.of(account2));

    // Configurar mock para save (pode ser chamado múltiplas vezes)
    when(accountRepositoryPort.save(any(Account.class)))
      .thenAnswer(invocation -> invocation.getArgument(0)); // Retorna o mesmo objeto que foi passado

    accountService.performTransactions(transactions);

    // Verificar saldos finais
    assertEquals(new BigDecimal("580.00"), account1.getBalance()); // 500 + 100 - 20
    assertEquals(new BigDecimal("150.00"), account2.getBalance()); // 200 - 50

    // Verificar chamadas do repositório
    verify(accountRepositoryPort, times(2)).findByAccountNumberWithLock("ACC001"); // Buscada 2 vezes
    verify(accountRepositoryPort, times(1)).findByAccountNumberWithLock("ACC002"); // Buscada 1 vez
    verify(accountRepositoryPort, times(3)).save(any(Account.class)); // Salva 3 vezes (1 por lançamento processado)
  }

  @Test
  @DisplayName("Deve retornar o saldo da conta com sucesso")
  void shouldGetAccountBalanceSuccessfully() {
    when(accountRepositoryPort.findByAccountNumber(accountNumber))
      .thenReturn(Optional.of(account));

    BigDecimal balance = accountService.getAccountBalance(accountNumber);

    assertEquals(new BigDecimal("1000.00"), balance);
    verify(accountRepositoryPort, times(1)).findByAccountNumber(accountNumber);
  }

  @Test
  @DisplayName("Deve lançar exceção ao buscar saldo de conta inexistente")
  void shouldThrowAccountNotFoundExceptionWhenGettingBalanceForNonExistentAccount() {
    String nonExistentAccountNumber = "99999";
    when(accountRepositoryPort.findByAccountNumber(nonExistentAccountNumber))
      .thenReturn(Optional.empty());

    assertThrows(AccountNotFoundException.class, () -> {
      accountService.getAccountBalance(nonExistentAccountNumber);
    });

    verify(accountRepositoryPort, times(1)).findByAccountNumber(nonExistentAccountNumber);
  }

  // Testes adicionais para validação (embora Bean Validation seja testado em integração)
  // Testes para valores nulos, negativos etc. - o GlobalExceptionHandler lida com MethodArgumentNotValidException
  // mas é bom testar a lógica do serviço caso a validação do DTO falhe ou seja removida.

}