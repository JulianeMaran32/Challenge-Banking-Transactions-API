package juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Resposta contendo o saldo atual de uma conta.")
public record AccountBalanceResponse(
  @Schema(description = "Número da conta bancária.", example = "1001-1")
  String accountNumber,
  @Schema(description = "Saldo atual da conta.", example = "1250.75")
  BigDecimal balance
) {
}