package juhmaran.challenge.bankingtransactionsapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Poderia ser BAD_REQUEST, mas UnprocessableEntity indica erro semântico
 * <p>
 * UNPROCESSABLE_ENTITY: 422 indica que a requisição está sintaticamente correta, mas semanticamente incorreta (saldo insuficiente)
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