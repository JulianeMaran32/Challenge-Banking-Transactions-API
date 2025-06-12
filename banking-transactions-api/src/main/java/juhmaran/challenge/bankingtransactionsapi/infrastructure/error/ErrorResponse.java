package juhmaran.challenge.bankingtransactionsapi.infrastructure.error;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
public record ErrorResponse(
  LocalDateTime timestamp,
  int status,
  String error, // HTTP status text (e.g., "Not Found", "Bad Request")
  String message, // Detailed error message
  String path // Path of the request that caused the error
) {

  // Construtor customizado (compact constructor ou metodo estático)
  // Usando metodo estático para manter a lógica de preenchimento de timestamp, status e error
  public static ErrorResponse fromStatusAndMessage(
    @NotNull HttpStatus status,
    String message, String path) {
    return new ErrorResponse(
      LocalDateTime.now(),
      status.value(),
      status.getReasonPhrase(),
      message,
      path
    );
  }

}