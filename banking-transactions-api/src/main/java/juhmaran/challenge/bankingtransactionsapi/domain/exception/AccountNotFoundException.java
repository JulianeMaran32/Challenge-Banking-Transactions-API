package juhmaran.challenge.bankingtransactionsapi.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando uma conta bancária não é encontrada.
 * <p>
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AccountNotFoundException extends RuntimeException {

  public AccountNotFoundException(String message) {
    super(message);
  }

}
