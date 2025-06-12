package juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.response;

import java.math.BigDecimal;

public record AccountBalanceResponse(
  String accountNumber,
  BigDecimal balance
) {
}