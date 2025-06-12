package juhmaran.challenge.bankingtransactionsapi.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "account_number", unique = true, nullable = false)
  private String accountNumber;

  @Column(name = "balance", nullable = false)
  private BigDecimal balance;

  public void credit(BigDecimal amount) {
    Objects.requireNonNull(amount, "Valor de crédito não pode ser nulo.");
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Valor de crédito deve ser positivo.");
    }
    this.balance = this.balance.add(amount);
  }

  public void debit(BigDecimal amount) {
    Objects.requireNonNull(amount, "Valor de débito não pode ser nulo.");
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Valor de débito deve ser positivo.");
    }
    if (this.balance.compareTo(amount) < 0) {
      throw new juhmaran.challenge.bankingtransactionsapi.domain.exception.InsufficientFundsException(
        "Saldo insuficiente na conta " + this.accountNumber + " para debitar " + amount);
    }
    this.balance = this.balance.subtract(amount);
  }

}
