package juhmaran.challenge.bankingtransactionsapi.domain.exception;

public class AccountNotFoundException extends RuntimeException {

  public AccountNotFoundException(String message) {
    super(message);
  }

}
