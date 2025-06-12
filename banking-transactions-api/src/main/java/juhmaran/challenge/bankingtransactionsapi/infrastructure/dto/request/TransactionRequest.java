package juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.enums.TransactionType;

import java.math.BigDecimal;

public record TransactionRequest(
  @NotBlank(message = "{transaction.accountNumber.notBlank}")
  String accountNumber,

  @NotNull(message = "{transaction.amount.notNull}")
  @DecimalMin(value = "0.01", inclusive = false, message = "{transaction.amount.decimalMin}") // inclusive=false para > 0
  BigDecimal amount,

  @NotNull(message = "{transaction.type.notNull}")
  TransactionType type
) {
}