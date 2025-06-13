package juhmaran.challenge.bankingtransactionsapi.domain.exception;

public class InvalidTransactionTypeException extends RuntimeException {

  public InvalidTransactionTypeException(String message) {
    super(message);
  }

}
