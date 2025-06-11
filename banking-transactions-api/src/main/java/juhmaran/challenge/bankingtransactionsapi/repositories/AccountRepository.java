package juhmaran.challenge.bankingtransactionsapi.repositories;

import jakarta.persistence.LockModeType;
import juhmaran.challenge.bankingtransactionsapi.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

  /**
   * Encontra uma conta pelo número da conta. <br>
   * Optional é usado para indicar que a conta pode não ser encontrada. <br>
   * `@Lock(LockModeType.PESSIMISTIC_WRITE)`: Aplica um bloqueio pessimista de escrita na linha da tabela
   * 'accounts' correspondente à conta encontrada. <br>
   * Isso impede que outras transações leiam ou escrevam nesta linha até que a transação atual termine
   * (commit ou rollback). Essencial para garantir a consistência do saldo em um ambiente concorrente.
   *
   * @param accountNumber número da conta
   * @return uma conta opcional
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<Account> findAccountWithLockByAccountNumber(String accountNumber);

  // Encontra uma conta pelo número da conta sem bloqueio (para consulta de saldo)
  Optional<Account> findByAccountNumber(String accountNumber);

  // Verifica se uma conta com um dado número existe
  boolean existsByAccountNumber(String accountNumber);

}
