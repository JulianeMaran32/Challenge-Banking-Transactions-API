package juhmaran.challenge.bankingtransactionsapi.infrastructure.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

  private LocalDateTime timestamp;
  private int status;
  private String error; // HTTP status text (e.g., "Not Found", "Bad Request")
  private String message; // Detailed error message
  private String path; // Path of the request that caused the error

  public ErrorResponse(HttpStatus status, String message, String path) {
    this.timestamp = LocalDateTime.now();
    this.status = status.value();
    this.error = status.getReasonPhrase();
    this.message = message;
    this.path = path;
  }

}