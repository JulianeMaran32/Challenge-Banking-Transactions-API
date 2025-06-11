package juhmaran.challenge.bankingtransactionsapi.exceptions;

import java.time.LocalDateTime;

/**
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
public record ErrorResponse(
  String message,
  String code, // Código de erro customizado (opcional, mas bom para documentação)
  int status,   // Status HTTP numérico
  String error, // Reason phrase do status HTTP (ex: "Not Found")
  LocalDateTime timestamp,
  String path   // Path da requisição que gerou o erro
) {
}
