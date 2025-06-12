package juhmaran.challenge.bankingtransactionsapi.infrastructure.error;

import juhmaran.challenge.bankingtransactionsapi.domain.exception.AccountNotFoundException;
import juhmaran.challenge.bankingtransactionsapi.domain.exception.InsufficientFundsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Componente para centralizar o tratamento de exceções em todos os controladores
 * <p>
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(AccountNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException ex, WebRequest request) {
    HttpStatus status = HttpStatus.NOT_FOUND; // 404
    String path = ((ServletWebRequest) request).getRequest().getRequestURI();
    logger.warn("Conta não encontrada: {} - Path: {}", ex.getMessage(), path);
    ErrorResponse errorResponse = ErrorResponse.fromStatusAndMessage(status, ex.getMessage(), path);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler(InsufficientFundsException.class)
  public ResponseEntity<ErrorResponse> handleInsufficientFundsException(InsufficientFundsException ex, WebRequest request) {
    HttpStatus status = HttpStatus.CONFLICT; // 409
    String path = ((ServletWebRequest) request).getRequest().getRequestURI();
    logger.warn("Saldo insuficiente: {} - Path: {}", ex.getMessage(), path);
    ErrorResponse errorResponse = ErrorResponse.fromStatusAndMessage(status, ex.getMessage(), path);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
    // Mapeado para 422 Unprocessable Content para erros semânticos (ex: valor <= 0)
    HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY; // 422
    String path = ((ServletWebRequest) request).getRequest().getRequestURI();
    logger.warn("Argumento inválido / Conteúdo não processável: {} - Path: {}", ex.getMessage(), path);
    ErrorResponse errorResponse = ErrorResponse.fromStatusAndMessage(status, ex.getMessage(), path);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
    HttpStatus status = HttpStatus.CONFLICT; // 409
    String path = ((ServletWebRequest) request).getRequest().getRequestURI();
    String message = "Violação de integridade de dados. Possível recurso duplicado ou conflito.";
    logger.error("Violação de integridade de dados: {} - Path: {}", ex.getMessage(), path, ex);

    String causeMessage = ex.getMostSpecificCause().getMessage();
    if (causeMessage != null && causeMessage.toLowerCase().contains("unique")) {
      message = "Recurso duplicado. Por favor, verifique os dados informados.";
    }

    ErrorResponse errorResponse = ErrorResponse.fromStatusAndMessage(status, message, path);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class})
  public ResponseEntity<ErrorResponse> handleBadRequestExceptions(Exception ex, WebRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST; // 400
    String path = ((ServletWebRequest) request).getRequest().getRequestURI();
    String message;
    StringBuilder validationErrors = new StringBuilder();

    if (ex instanceof MethodArgumentNotValidException) { // Replace this instanceof check and cast with 'instanceof MethodArgumentNotValidException manvEx'
      MethodArgumentNotValidException manvEx = (MethodArgumentNotValidException) ex; // Variable 'manvEx' can be replaced with pattern variable
      manvEx.getBindingResult().getFieldErrors().forEach(error ->
        validationErrors.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ")
      );
      message = "Erros de validação: " + validationErrors.toString().trim();
      logger.warn("Erros de validação: {} - Path: {}", message, path);
    } else if (ex instanceof MethodArgumentTypeMismatchException) { // Replace this instanceof check and cast with 'instanceof MethodArgumentNotValidException manvEx'
      MethodArgumentTypeMismatchException mtmEx = (MethodArgumentTypeMismatchException) ex; // Variable 'manvEx' can be replaced with pattern variable
      message = String.format("Parâmetro '%s' com tipo inválido. Valor recebido: '%s'",
        mtmEx.getName(), mtmEx.getValue());
      logger.warn("Erro de tipo de parâmetro: {} - Path: {}", message, path);
    } else {
      message = "Requisição inválida.";
      logger.warn("Requisição inválida: {} - Path: {}", ex.getMessage(), path);
    }

    ErrorResponse errorResponse = ErrorResponse.fromStatusAndMessage(status, message, path);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex, WebRequest request) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
    String path = ((ServletWebRequest) request).getRequest().getRequestURI();
    logger.error("Ocorreu um erro inesperado: {} - Path: {}", ex.getMessage(), path, ex);

    ErrorResponse errorResponse = ErrorResponse.fromStatusAndMessage(
      status,
      "Ocorreu um erro interno no servidor. Por favor, tente novamente mais tarde.",
      path
    );
    return new ResponseEntity<>(errorResponse, status);
  }

}
