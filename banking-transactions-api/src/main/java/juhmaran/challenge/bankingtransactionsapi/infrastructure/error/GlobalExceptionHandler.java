package juhmaran.challenge.bankingtransactionsapi.infrastructure.error;

import jakarta.validation.constraints.NotNull;
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
  public ResponseEntity<ErrorResponse> handleAccountNotFoundException(@NotNull AccountNotFoundException ex,
                                                                      WebRequest request) {
    HttpStatus status = HttpStatus.NOT_FOUND; // 404
    String path = ((ServletWebRequest) request).getRequest().getRequestURI();
    logger.warn("Account Not Found: {} - Path: {}", ex.getMessage(), path);
    ErrorResponse errorResponse = ErrorResponse.fromStatusAndMessage(status, ex.getMessage(), path);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler(InsufficientFundsException.class)
  public ResponseEntity<ErrorResponse> handleInsufficientFundsException(@NotNull InsufficientFundsException ex,
                                                                        WebRequest request) {
    HttpStatus status = HttpStatus.CONFLICT; // 409
    String path = ((ServletWebRequest) request).getRequest().getRequestURI();
    logger.warn("Insufficient Funds: {} - Path: {}", ex.getMessage(), path);
    ErrorResponse errorResponse = ErrorResponse.fromStatusAndMessage(status, ex.getMessage(), path);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(@NotNull IllegalArgumentException ex,
                                                                      WebRequest request) {
    // Mapping to 422 Unprocessable Content for semantically invalid arguments (e.g., amount <= 0)
    HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY; // 422
    String path = ((ServletWebRequest) request).getRequest().getRequestURI();
    logger.warn("Illegal Argument / Unprocessable Entity: {} - Path: {}", ex.getMessage(), path);
    ErrorResponse errorResponse = ErrorResponse.fromStatusAndMessage(status, ex.getMessage(), path);
    return new ResponseEntity<>(errorResponse, status);
  }

  // Handler para DataIntegrityViolationException, comum em violações de unique constraints
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex,
                                                                             WebRequest request) {
    HttpStatus status = HttpStatus.CONFLICT; // 409 Conflict for duplicate resources (like account number)
    String path = ((ServletWebRequest) request).getRequest().getRequestURI();
    String message = "Violação de integridade de dados (possível recurso duplicado)."; // Mensagem genérica para o cliente
    logger.error("Data Integrity Violation: {} - Path: {}", ex.getMessage(), path, ex); // Logar detalhe para debug

    // Tentar extrair uma mensagem mais amigável se possível, dependendo do banco e do tipo de erro
    // Exemplo simples: se a mensagem contiver "Constraint", assumir duplicidade.
    if (ex.getCause() != null && ex.getCause().getMessage() != null && ex.getCause().getMessage().contains("Constraint")) {
      message = "Recurso duplicado. Verifique os dados informados (ex: número da conta).";
    }

    ErrorResponse errorResponse = ErrorResponse.fromStatusAndMessage(status, message, path);
    return new ResponseEntity<>(errorResponse, status);
  }


  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(@NotNull MethodArgumentNotValidException ex,
                                                                  WebRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST; // 400
    String path = ((ServletWebRequest) request).getRequest().getRequestURI();
    // Coleta todas as mensagens de erro de validação
    StringBuilder messages = new StringBuilder("Erros de validação: ");
    ex.getBindingResult().getFieldErrors().forEach(error ->
      messages.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ")
    );

    String errorMessage = messages.toString().trim();
    logger.warn("Validation Errors: {} - Path: {}", errorMessage, path);

    ErrorResponse errorResponse = ErrorResponse.fromStatusAndMessage(status, errorMessage, path);
    return new ResponseEntity<>(errorResponse, status);
  }

  // Handler genérico para outras exceções não capturadas
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex, WebRequest request) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
    String path = ((ServletWebRequest) request).getRequest().getRequestURI();
    logger.error("An unexpected error occurred: {} - Path: {}", ex.getMessage(), path, ex);

    ErrorResponse errorResponse = ErrorResponse.fromStatusAndMessage(
      status,
      "Ocorreu um erro interno no servidor. Por favor, tente novamente mais tarde.", // Mensagem mais genérica para o cliente
      path
    );
    return new ResponseEntity<>(errorResponse, status);
  }

}
