package juhmaran.challenge.bankingtransactionsapi.dtos.response;

import java.math.BigDecimal;

/**
 * Record para representar a resposta do saldo da conta
 * <p>
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
public record AccountBalanceResponse(
  String accountNumber,
  BigDecimal balance
) {
}
