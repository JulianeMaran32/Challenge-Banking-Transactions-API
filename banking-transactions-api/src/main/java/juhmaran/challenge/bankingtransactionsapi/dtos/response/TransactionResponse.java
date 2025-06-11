package juhmaran.challenge.bankingtransactionsapi.dtos.response;

import java.math.BigDecimal;

/**
 * Record para representar uma única transação na resposta
 * <p>
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
public record TransactionResponse(
  Long id,
  BigDecimal amount,
  String type, // Usar String para serialização mais simples
  String timestamp // Usar String ou ZonedDateTime para formato ISO 8601
) {
}
