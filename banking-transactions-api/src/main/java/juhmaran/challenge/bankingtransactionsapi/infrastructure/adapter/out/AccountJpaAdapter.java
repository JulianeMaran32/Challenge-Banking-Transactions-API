package juhmaran.challenge.bankingtransactionsapi.infrastructure.adapter.out;

import jakarta.persistence.LockModeType;
import juhmaran.challenge.bankingtransactionsapi.application.port.out.AccountRepositoryPort;
import juhmaran.challenge.bankingtransactionsapi.domain.entity.Account;
import juhmaran.challenge.bankingtransactionsapi.infrastructure.repository.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccountJpaAdapter implements AccountRepositoryPort {

  private final AccountJpaRepository accountJpaRepository;

  @Override
  public Optional<Account> findByAccountNumber(String accountNumber) {
    return accountJpaRepository.findByAccountNumber(accountNumber);
  }

  @Override
  @Transactional
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  public Optional<Account> findByAccountNumberWithLock(String accountNumber) {
    return accountJpaRepository.findByAccountNumber(accountNumber);
  }

  @Override
  public Account save(Account account) {
    return accountJpaRepository.save(account);
  }

  @Override
  public boolean existsByAccountNumber(String accountNumber) {
    return accountJpaRepository.existsByAccountNumber(accountNumber);
  }

}
