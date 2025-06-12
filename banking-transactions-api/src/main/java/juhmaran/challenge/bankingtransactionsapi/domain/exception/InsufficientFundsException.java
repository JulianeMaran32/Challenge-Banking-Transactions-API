package juhmaran.challenge.bankingtransactionsapi.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando a requisição de transação não pode ser processada devido a saldo insuficiente.
 * <p>
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InsufficientFundsException extends RuntimeException {

  public InsufficientFundsException(String message) {
    super(message);
  }

}