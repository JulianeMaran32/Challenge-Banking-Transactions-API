package juhmaran.challenge.bankingtransactionsapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
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
