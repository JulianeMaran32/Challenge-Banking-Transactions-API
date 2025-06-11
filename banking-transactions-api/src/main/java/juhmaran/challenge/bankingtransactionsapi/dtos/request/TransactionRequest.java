package juhmaran.challenge.bankingtransactionsapi.dtos.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import juhmaran.challenge.bankingtransactionsapi.enums.TransactionType;

import java.math.BigDecimal;

/**
 * Record para representar a requisição de uma única transação.
 * <p>
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
public record TransactionRequest(
  @NotNull(message = "{transactionRequest.amount.notnull}") // Validação: não pode ser nulo
  @DecimalMin(value = "0.01", message = "{transactionRequest.amount.min}") // Validação: valor mínimo (positivo)
  BigDecimal amount,

  @NotNull(message = "{transactionRequest.type.notnull}") // Validação: não pode ser nulo
  TransactionType type
) {
}
