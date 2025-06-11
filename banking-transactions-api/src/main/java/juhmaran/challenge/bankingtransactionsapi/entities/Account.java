package juhmaran.challenge.bankingtransactionsapi.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "account_number", unique = true, nullable = false)
  private String accountNumber;

  /**
   * Usamos BigDecimal para precisão monetária.
   * <p>
   * Sincronização ou bloqueio no Service/Repository para garantir thread-safety ao modificar o saldo
   */
  @Column(name = "balance", nullable = false)
  private BigDecimal balance;

  /**
   * Mapeamento um para muitos com Transactions <br>
   * CascadeType.ALL: Operações em Account cascateiam para Transactions <br>
   * orphanRemoval = true: Remove Transactions órfãs <br>
   * mappedBy: Indica que o relacionamento é mapeado pela propriedade 'account' em Transaction <br>
   * FetchType.LAZY: Carrega as transações somente quando necessário (performance) <br>
   */
  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Transaction> transactions = new ArrayList<>();

  /**
   * Construtor customizado para criação inicial.
   *
   * @param accountNumber  Número da conta
   * @param initialBalance Saldo inicial da conta
   */
  public Account(String accountNumber, BigDecimal initialBalance) {
    this.accountNumber = accountNumber;
    this.balance = initialBalance;
  }

  /**
   * Metodo para adicionar transação e atualizar saldo.
   *
   * @param transaction Transação a ser adicionada
   */
  public void addTransaction(Transaction transaction) {
    transactions.add(transaction);
    transaction.setAccount(this);
  }

  /**
   * Metodo para manipulação do saldo (thread-safe garantida externamente pelo serviço com lock).
   *
   * @param amount Quantia a ser debitada ou creditada
   */
  public void deposit(BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("O valor do depósito deve ser positivo."); // Validação interna básica
    }
    this.balance = this.balance.add(amount);
  }

  /**
   * Metodo para manipulação do saldo (thread-safe garantida externamente pelo serviço com lock).
   *
   * @param amount Quantia a ser debitada
   */
  public void withdraw(BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("O valor do saque deve ser positivo."); // Validação interna básica
    }
    // A verificação de saldo suficiente deve ser feita no serviço, APÓS obter o lock
    this.balance = this.balance.subtract(amount);
  }

}
