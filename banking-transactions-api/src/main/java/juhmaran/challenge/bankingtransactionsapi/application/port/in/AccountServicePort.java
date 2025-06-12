package juhmaran.challenge.bankingtransactionsapi.application.port.in;

import juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.request.TransactionRequest;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Juliane Maran
 *
 * @since 11/06/2025
 */
public interface AccountServicePort {

  /**
   * Realiza uma lista de transações de débito/crédito em contas específicas.
   * Deve ser thread-safe e lidar com requisições concorrentes.
   *
   * @param transactions A lista de transações a serem realizadas.
   */
  void performTransactions(List<TransactionRequest> transactions);

  /**
   * Obtém o saldo atual de uma conta específica.
   *
   * @param accountNumber O número da conta.
   * @return O saldo atual.
   */
  BigDecimal getAccountBalance(String accountNumber);

  /**
   * Cria uma conta caso ela ainda não exista.
   * Usado para inicialização da aplicação.
   *
   * @param accountNumber  O número da conta a ser criada.
   * @param initialBalance O saldo inicial da conta.
   */
  void createAccountIfNotFound(String accountNumber, BigDecimal initialBalance);

}
