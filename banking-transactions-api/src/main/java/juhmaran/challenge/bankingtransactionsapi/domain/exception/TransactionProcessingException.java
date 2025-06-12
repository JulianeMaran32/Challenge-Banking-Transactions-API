package juhmaran.challenge.bankingtransactionsapi.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando ocorre um erro no processamento de uma transação.
 * <p>
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class TransactionProcessingException extends RuntimeException {

  public TransactionProcessingException(String message) {
    super(message);
  }

  public TransactionProcessingException(String message, Throwable cause) {
    super(message, cause);
  }

}
