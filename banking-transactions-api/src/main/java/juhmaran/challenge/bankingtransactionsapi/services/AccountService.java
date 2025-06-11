package juhmaran.challenge.bankingtransactionsapi.services;

import juhmaran.challenge.bankingtransactionsapi.dtos.request.TransactionRequest;
import juhmaran.challenge.bankingtransactionsapi.dtos.response.AccountBalanceResponse;
import juhmaran.challenge.bankingtransactionsapi.dtos.response.TransactionBatchResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interface definindo o contrato para o serviço de contas
 * <p>
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
public interface AccountService {

  /**
   * Realiza um lote de transações (débito/crédito) em uma conta específica.
   * Garante atomicidade e thread-safety.
   *
   * @param accountNumber O número da conta.
   * @param transactions  A lista de requisições de transação.
   * @return Um DTO contendo o novo saldo e os detalhes das transações realizadas.
   */
  TransactionBatchResponse performTransactions(String accountNumber, List<TransactionRequest> transactions);

  /**
   * Obtém o saldo atual de uma conta específica.
   *
   * @param accountNumber O número da conta.
   * @return Um DTO contendo o número da conta e o saldo.
   */
  AccountBalanceResponse getBalance(String accountNumber);

  /**
   * Cria uma nova conta com saldo inicial zero.
   * Útil para inicializar contas para testes/uso da API.
   *
   * @param accountNumber O número da nova conta.
   * @return O número da conta criada.
   */
  String createAccount(String accountNumber); // Metodo auxiliar para criação

  /**
   * Cria uma nova conta com saldo inicial especificado.
   * Útil para inicializar contas para testes/uso da API.
   *
   * @param accountNumber  O número da nova conta.
   * @param initialBalance O saldo inicial.
   * @return O número da conta criada.
   */
  String createAccount(String accountNumber, BigDecimal initialBalance); // Metodo auxiliar com saldo inicial

}
