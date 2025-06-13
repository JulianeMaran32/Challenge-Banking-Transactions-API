package juhmaran.challenge.bankingtransactionsapi.application.usecase;

import juhmaran.challenge.bankingtransactionsapi.application.port.out.AccountRepositoryPort;
import juhmaran.challenge.bankingtransactionsapi.application.usecase.processor.SingleTransactionProcessor;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

  @Mock
  private AccountRepositoryPort accountRepositoryPort;

  @Mock
  private SingleTransactionProcessor singleTransactionProcessor;

  @InjectMocks
  private AccountService accountService;

  private Account account;
  private final String accountNumber = "12345";
  private final BigDecimal initialBalance = new BigDecimal("1000.00");

  @BeforeEach
  void setUp() {
    account = new Account(1L, accountNumber, initialBalance);
  }

  @Test
  @DisplayName("Deve processar um lote de transações delegando ao processador individual")
  void shouldPerformTransactionsByDelegatingToSingleProcessor() {
    var tx1 = new TransactionRequest("1001", new BigDecimal("100"), TransactionType.CREDIT);
    var tx2 = new TransactionRequest("1002", new BigDecimal("50"), TransactionType.DEBIT);
    List<TransactionRequest> transactions = Arrays.asList(tx1, tx2);
    doNothing().when(singleTransactionProcessor).process(any(TransactionRequest.class));
    accountService.performTransactions(transactions);
    verify(singleTransactionProcessor, times(1)).process(tx1);
    verify(singleTransactionProcessor, times(1)).process(tx2);
    verifyNoInteractions(accountRepositoryPort);
  }

  @Test
  @DisplayName("Não deve processar transações se a lista for nula")
  void shouldNotProcessTransactionsIfListIsNull() {
    accountService.performTransactions(null);
    verifyNoInteractions(singleTransactionProcessor);
    verifyNoInteractions(accountRepositoryPort);
  }

  @Test
  @DisplayName("Não deve processar transações se a lista for vazia")
  void shouldNotProcessTransactionsIfListIsEmpty() {
    accountService.performTransactions(Collections.emptyList());
    verifyNoInteractions(singleTransactionProcessor);
    verifyNoInteractions(accountRepositoryPort);
  }

  @Test
  @DisplayName("Deve propagar exceção se o processador individual falhar")
  void shouldPropagateExceptionIfSingleProcessorFails() {
    var tx1 = new TransactionRequest("1001", new BigDecimal("100"), TransactionType.CREDIT);
    var tx2 = new TransactionRequest("1002", new BigDecimal("50"), TransactionType.DEBIT);
    List<TransactionRequest> transactions = Arrays.asList(tx1, tx2);
    doThrow(new InsufficientFundsException("Simulated Insufficient Funds"))
      .when(singleTransactionProcessor).process(tx1);
    assertThrows(InsufficientFundsException.class, () -> accountService.performTransactions(transactions));
    verify(singleTransactionProcessor, times(1)).process(tx1);
    verify(singleTransactionProcessor, never()).process(tx2);
    verifyNoInteractions(accountRepositoryPort);
  }

  @Test
  @DisplayName("Deve retornar a conta (para obter saldo) se a conta existir")
  void shouldReturnAccountWhenGettingBalanceIfAccountExists() {
    when(accountRepositoryPort.findByAccountNumber(accountNumber))
      .thenReturn(Optional.of(account));
    Account retrievedAccount = accountService.getAccountBalance(accountNumber);
    assertNotNull(retrievedAccount);
    assertEquals(accountNumber, retrievedAccount.getAccountNumber());
    assertEquals(initialBalance, retrievedAccount.getBalance());
    verify(accountRepositoryPort, times(1)).findByAccountNumber(accountNumber);
    verifyNoMoreInteractions(accountRepositoryPort);
    verifyNoInteractions(singleTransactionProcessor);
  }

  @Test
  @DisplayName("Deve lançar AccountNotFoundException ao buscar saldo de conta inexistente")
  void shouldThrowAccountNotFoundExceptionWhenGettingBalanceForNonExistentAccount() {
    String nonExistentAccountNumber = "99999";
    when(accountRepositoryPort.findByAccountNumber(nonExistentAccountNumber))
      .thenReturn(Optional.empty());
    assertThrows(AccountNotFoundException.class, () -> accountService.getAccountBalance(nonExistentAccountNumber));
    verify(accountRepositoryPort, times(1)).findByAccountNumber(nonExistentAccountNumber);
    verifyNoMoreInteractions(accountRepositoryPort);
    verifyNoInteractions(singleTransactionProcessor);
  }

  @Test
  @DisplayName("Deve lançar NullPointerException ao buscar saldo com número de conta nulo")
  void shouldThrowNullPointerExceptionWhenGettingBalanceWithNullAccountNumber() {
    assertThrows(NullPointerException.class, () -> accountService.getAccountBalance(null));
    verifyNoInteractions(accountRepositoryPort);
    verifyNoInteractions(singleTransactionProcessor);
  }

  @Test
  @DisplayName("Deve criar conta se não existir durante a inicialização")
  void shouldCreateAccountIfNotFound() {
    String newAccountNumber = "99999";
    BigDecimal newInitialBalance = new BigDecimal("500.00");
    when(accountRepositoryPort.existsByAccountNumber(newAccountNumber)).thenReturn(false);
    when(accountRepositoryPort.save(any(Account.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));
    accountService.createAccountIfNotFound(newAccountNumber, newInitialBalance);
    verify(accountRepositoryPort, times(1)).existsByAccountNumber(newAccountNumber);
    verify(accountRepositoryPort, times(1)).save(any(Account.class));
    verifyNoMoreInteractions(accountRepositoryPort);
    verifyNoInteractions(singleTransactionProcessor);
  }

  @Test
  @DisplayName("Não deve criar conta se já existir durante a inicialização")
  void shouldNotCreateAccountIfAlreadyExists() {
    String existingAccountNumber = "1001-1";
    when(accountRepositoryPort.existsByAccountNumber(existingAccountNumber)).thenReturn(true);
    accountService.createAccountIfNotFound(existingAccountNumber, new BigDecimal("5000.00"));
    verify(accountRepositoryPort, times(1)).existsByAccountNumber(existingAccountNumber);
    verify(accountRepositoryPort, never()).save(any(Account.class));
    verifyNoMoreInteractions(accountRepositoryPort);
    verifyNoInteractions(singleTransactionProcessor);
  }

  @Test
  @DisplayName("Deve lançar NullPointerException ao criar conta com número nulo")
  void shouldThrowNullPointerExceptionWhenCreateAccountWithNullAccountNumber() {
    assertThrows(NullPointerException.class, () -> accountService
      .createAccountIfNotFound(null, new BigDecimal("100.00")));
    verifyNoInteractions(accountRepositoryPort);
    verifyNoInteractions(singleTransactionProcessor);
  }

  @Test
  @DisplayName("Deve lançar NullPointerException ao criar conta com saldo inicial nulo")
  void shouldThrowNullPointerExceptionWhenCreateAccountWithNullInitialBalance() {
    assertThrows(NullPointerException.class, () -> accountService
      .createAccountIfNotFound("test-acc", null));
    verifyNoInteractions(accountRepositoryPort);
    verifyNoInteractions(singleTransactionProcessor);
  }

}