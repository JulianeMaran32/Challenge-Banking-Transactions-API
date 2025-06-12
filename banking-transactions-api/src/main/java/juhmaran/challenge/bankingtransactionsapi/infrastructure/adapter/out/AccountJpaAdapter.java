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

/**
 * Created by Juliane Maran
 *
 * @since 11/06/2025
 */
@Component
@RequiredArgsConstructor
public class AccountJpaAdapter implements AccountRepositoryPort {

  private final AccountJpaRepository accountJpaRepository;

  @Override
  public Optional<Account> findByAccountNumber(String accountNumber) {
    return accountJpaRepository.findByAccountNumber(accountNumber);
  }

  @Override
  @Transactional // Necessário para o bloqueio funcionar corretamente
  @Lock(LockModeType.PESSIMISTIC_WRITE) // Bloqueio pessimista de escrita no banco
  public Optional<Account> findByAccountNumberWithLock(String accountNumber) {
    // No Spring Data JPA, o metodo findBy... já lida com a execução da query
    // e a aplicação do lock quando a anotação @Lock está presente no metodo do Repository
    // ou, como fizemos aqui, no metodo do Adapter que chama o Repository.
    // É importante que o metodo findByAccountNumberWithLock seja transacional.
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
