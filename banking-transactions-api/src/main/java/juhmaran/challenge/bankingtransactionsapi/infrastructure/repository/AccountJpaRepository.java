package juhmaran.challenge.bankingtransactionsapi.infrastructure.repository;

import juhmaran.challenge.bankingtransactionsapi.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountJpaRepository extends JpaRepository<Account, Long> {

  Optional<Account> findByAccountNumber(String accountNumber);

  boolean existsByAccountNumber(String accountNumber);

}
