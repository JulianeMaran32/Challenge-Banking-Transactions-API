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
  @NotBlank(message = "O número da conta não pode estar em branco.")
  String accountNumber,

  @Schema(description = "Valor da transação.", example = "150.75",
    type = "number", format = "double", minimum = "0.01")
  @NotNull(message = "O valor da transação não pode ser nulo.")
  @DecimalMin(value = "0.01", inclusive = false, message = "O valor da transação deve ser positivo.")
  BigDecimal amount,

  @Schema(description = "Tipo da transação (DEBIT ou CREDIT).", example = "CREDIT",
    type = "string", allowableValues = {"DEBIT", "CREDIT"})
  @NotNull(message = "O tipo da transação não pode ser nulo.")
  TransactionType type
) {
}