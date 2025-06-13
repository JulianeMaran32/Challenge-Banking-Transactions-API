package juhmaran.challenge.bankingtransactionsapi.infrastructure.error;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Schema(description = "Resposta padronizada para erros da API.")
public record ErrorResponse(
  @Schema(description = "Timestamp do erro.",
    example = "2025-06-12T23:18:02.041Z", type = "string", format = "date-time")
  LocalDateTime timestamp,

  @Schema(description = "Código de status HTTP.",
    example = "404", type = "integer", format = "int32")
  int status,

  @Schema(description = "Texto do status HTTP.",
    example = "Not Found", type = "string")
  String error,

  @Schema(description = "Mensagem detalhada do erro.",
    example = "Conta não encontrada: 1001-0", type = "string")
  String message,

  @Schema(description = "Caminho da requisição que gerou o erro.",
    example = "/api/accounts/1001-0/balance", type = "string")
  String path
) {

  public static ErrorResponse fromStatusAndMessage(@NotNull HttpStatus status,
                                                   String message, String path) {
    return new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path);
  }

}