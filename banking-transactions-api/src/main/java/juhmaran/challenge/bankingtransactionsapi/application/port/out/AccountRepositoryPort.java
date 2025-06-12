package juhmaran.challenge.bankingtransactionsapi.application.port.out;

import juhmaran.challenge.bankingtransactionsapi.domain.entity.Account;

import java.util.Optional;

/**
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
public interface AccountRepositoryPort {

  Optional<Account> findByAccountNumber(String accountNumber);

  Optional<Account> findByAccountNumberWithLock(String accountNumber);

  Account save(Account account);

  boolean existsByAccountNumber(String accountNumber);

}
