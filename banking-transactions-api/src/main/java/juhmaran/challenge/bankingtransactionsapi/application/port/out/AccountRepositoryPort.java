package juhmaran.challenge.bankingtransactionsapi.application.port.out;

import juhmaran.challenge.bankingtransactionsapi.domain.entity.Account;

import java.util.Optional;

/**
 * Porta de saída (Outbound Port) da camada de aplicação. <br>
 * Define o contrato para as operações de persistência que a camada de aplicação precisa de um serviço externo
 * (neste caso, a camada de infraestrutura via o JPA Adapter). <br>
 * Abstrai a tecnologia de acesso a dados. Segue o princípio de Ports & Adapters.
 *
 * @author Juliane Maran
 */
public interface AccountRepositoryPort {

  /**
   * Busca uma conta bancária pelo número da conta.
   * Esta busca não aplica nenhum bloqueio no banco de dados.
   *
   * @param accountNumber O número da conta a ser buscada.
   * @return Um {@link Optional} contendo a {@link Account} se encontrada, ou vazio caso contrário.
   */
  Optional<Account> findByAccountNumber(String accountNumber);

  /**
   * Busca uma conta bancária pelo número da conta e adquire um bloqueio pessimista de escrita
   * na linha correspondente no banco de dados. Essencial para garantir a {@code thread-safety}
   * e evitar {@code condições de corrida} em operações de leitura-modificação-escrita no saldo.
   * <p>
   * O tipo de bloqueio pessimista de escrita ({@code LockModeType.PESSIMISTIC_WRITE})
   * impede que outras transações leiam ou escrevam nesta linha até que a transação atual termine.
   * </p>
   *
   * @param accountNumber O número da conta a ser buscada e bloqueada.
   * @return Um {@link Optional} contendo a {@link Account} se encontrada e bloqueada, ou vazio caso contrário.
   * @see jakarta.persistence.LockModeType#PESSIMISTIC_WRITE
   */
  Optional<Account> findByAccountNumberWithLock(String accountNumber);

  /**
   * Salva (insere ou atualiza) uma conta bancária no banco de dados.
   *
   * @param account A entidade {@link Account} a ser salva.
   * @return A entidade {@link Account} salva (com ID gerado, se for uma nova conta).
   */
  Account save(Account account);

  /**
   * Verifica se uma conta bancária com o número especificado já existe.
   * Usado na inicialização de dados para evitar a criação de contas duplicadas.
   * <p>
   * Nota: A combinação de {@code existsByAccountNumber} e {@code save} tem uma pequena janela
   * de {@code condição de corrida} em cenários de alta concorrência na criação.
   * Em sistemas de produção, seria mais robusto tentar salvar sempre e tratar
   * a exceção de violação de unicidade.
   * </p>
   *
   * @param accountNumber O número da conta a ser verificado.
   * @return true se a conta existir, false caso contrário.
   */
  boolean existsByAccountNumber(String accountNumber);

}
