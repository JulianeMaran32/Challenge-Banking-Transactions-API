package juhmaran.challenge.bankingtransactionsapi.domain.exception;

public class TransactionProcessingException extends RuntimeException {

  public TransactionProcessingException(String message, Throwable cause) {
    super(message, cause);
  }

}