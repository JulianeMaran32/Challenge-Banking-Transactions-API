package juhmaran.challenge.bankingtransactionsapi.entities;

import jakarta.persistence.*;
import juhmaran.challenge.bankingtransactionsapi.enums.TransactionType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@ToString(exclude = "account")
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "amount", nullable = false)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "transaction_type", nullable = false)
  private TransactionType type;

  @CreationTimestamp
  @Column(name = "timestamp", nullable = false)
  private LocalDateTime timestamp;

  /**
   * Conta associada à transação.
   * <p>
   * * Muitos para um com Account <br>
   * * JoinColumn: Indica a coluna que faz a ligação (chave estrangeira) <br>
   * * nullable = false: Toda transação deve estar associada a uma conta <br>
   * * FetchType.LAZY: Carrega a conta somente quando necessário.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  /**
   * Construtor customizado para criação.
   *
   * @param amount  Quantia da transação
   * @param type    Tipo da transação (DEBITO, CRÉDITO)
   * @param account Conta associada à transação
   */
  public Transaction(BigDecimal amount, TransactionType type, Account account) {
    this.amount = amount;
    this.type = type;
    this.timestamp = LocalDateTime.now(); // Define a data e hora da criação
    this.account = account;
  }

}
