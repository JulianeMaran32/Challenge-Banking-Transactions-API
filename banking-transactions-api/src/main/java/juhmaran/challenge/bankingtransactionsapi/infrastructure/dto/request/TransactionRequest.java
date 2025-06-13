package juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.enums.TransactionType;

import java.math.BigDecimal;

@Schema(description = "Detalhes de uma transação a ser realizada (débito ou crédito).")
public record TransactionRequest(
  @Schema(description = "Número da conta bancária.", example = "1001-1", type = "string")
  @NotBlank(message = "{transaction.accountNumber.notBlank}")
  String accountNumber,

  @Schema(description = "Valor da transação.", example = "150.75",
    type = "number", format = "double", minimum = "0.01")
  @NotNull(message = "{transaction.amount.notNull}")
  @DecimalMin(value = "0.01", inclusive = false, message = "{transaction.amount.decimalMin}")
  BigDecimal amount,

  @Schema(description = "Tipo da transação (DEBIT ou CREDIT).", example = "CREDIT",
    type = "string", allowableValues = {"DEBIT", "CREDIT"})
  @NotNull(message = "{transaction.type.notNull}")
  TransactionType type
) {
}