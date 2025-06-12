package juhmaran.challenge.bankingtransactionsapi.infrastructure.repository;

import juhmaran.challenge.bankingtransactionsapi.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by Juliane Maran
 *
 * @since 11/06/2025
 */
@Repository
public interface AccountJpaRepository extends JpaRepository<Account, Long> {

  Optional<Account> findByAccountNumber(String accountNumber);

  // Spring Data JPA implementa automaticamente este metodo
  boolean existsByAccountNumber(String accountNumber);

}
