package juhmaran.challenge.bankingtransactionsapi.repositories;

import juhmaran.challenge.bankingtransactionsapi.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {


}
