package juhmaran.challenge.bankingtransactionsapi.application.port.in;

import juhmaran.challenge.bankingtransactionsapi.domain.entity.Account;
import juhmaran.challenge.bankingtransactionsapi.domain.exception.AccountNotFoundException;
import juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.request.TransactionRequest;

import java.util.List;

/**
 * Porta de entrada (Inbound Port) da camada de aplicação.
 * Define o contrato para as operações que a camada de aplicação oferece
 * a outras camadas (neste caso, a camada de infraestrutura via o Controller).
 * Segue o princípio de Ports & Adapters.
 *
 * @author Juliane Maran
 */
public interface AccountServicePort {

  /**
   * Executa um lote de transações (débito ou crédito) em contas bancárias.
   * Cada transação no lote é processada individualmente, garantindo a consistência
   * através de mecanismos de controle de concorrência.
   *
   * @param transactions Uma lista de {@link TransactionRequest} representando as transações a serem processadas.
   *                     Pode ser nula ou vazia, caso em que nenhuma operação é realizada.
   *                     Não deve conter itens nulos.
   */
  void performTransactions(List<TransactionRequest> transactions);

  /**
   * Obtém o saldo atual de uma conta bancária específica.
   *
   * @param accountNumber O número da conta para a qual o saldo deve ser buscado. Deve ser não nulo.
   * @return A entidade {@link Account} contendo o saldo atual.
   * @throws NullPointerException     Se o número da conta for nulo.
   * @throws AccountNotFoundException Se a conta com o número especificado não for encontrada.
   */
  Account getAccountBalance(String accountNumber);

}
