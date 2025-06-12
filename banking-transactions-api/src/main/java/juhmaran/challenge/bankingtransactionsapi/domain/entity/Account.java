package juhmaran.challenge.bankingtransactionsapi.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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

  // Metodo de negócio para adicionar crédito
  public void credit(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      // Validação de valor deve ser feita antes (no DTO/Serviço)
      // Se chegar aqui com valor inválido, pode lançar exceção ou logar e retornar
      throw new IllegalArgumentException("Valor de crédito inválido.");
    }
    this.balance = this.balance.add(amount);
  }

  // Metodo de negócio para subtrair débito
  public void debit(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Valor de débito inválido.");
    }
    if (this.balance.compareTo(amount) < 0) {
      throw new juhmaran.challenge.bankingtransactionsapi.domain.exception.InsufficientFundsException(
        "Saldo insuficiente na conta " + this.accountNumber + " para debitar " + amount);
    }
    this.balance = this.balance.subtract(amount);
  }

}
