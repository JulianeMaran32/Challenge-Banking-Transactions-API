package juhmaran.challenge.bankingtransactionsapi.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Componente para centralizar o tratamento de exceções em todos os controladores
 * <p>
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  // Trata exceções de validação (@Valid)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST; // 400 Bad Request para erros de validação

    // Coleta todos os erros de campo
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage(); // Mensagem da anotação de validação (@NotNull, @DecimalMin, etc.)
      errors.put(fieldName, errorMessage);
    });

    // Formata as mensagens de erro de validação em uma única string ou de outra forma desejada
    String errorMessage = errors.entrySet().stream()
      .map(entry -> entry.getKey() + ": " + entry.getValue())
      .collect(Collectors.joining("; "));

    ErrorResponse errorResponse = new ErrorResponse(
      "Erro de validação: " + errorMessage, // Mensagem mais descritiva
      "VALIDATION_ERROR", // Código customizado
      status.value(),
      status.getReasonPhrase(),
      LocalDateTime.now(),
      getPathFromRequest(request)
    );

    return new ResponseEntity<>(errorResponse, status);
  }

  // Trata AccountNotFoundException
  @ExceptionHandler(AccountNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException ex, WebRequest request) {
    HttpStatus status = HttpStatus.NOT_FOUND; // 404 Not Found

    ErrorResponse errorResponse = new ErrorResponse(
      ex.getMessage(), // Mensagem da exceção
      "ACCOUNT_NOT_FOUND", // Código customizado
      status.value(),
      status.getReasonPhrase(),
      LocalDateTime.now(),
      getPathFromRequest(request)
    );

    return new ResponseEntity<>(errorResponse, status);
  }

  // Trata InsufficientFundsException
  @ExceptionHandler(InsufficientFundsException.class)
  public ResponseEntity<ErrorResponse> handleInsufficientFundsException(InsufficientFundsException ex, WebRequest request) {
    HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY; // 422 Unprocessable Entity

    ErrorResponse errorResponse = new ErrorResponse(
      ex.getMessage(), // Mensagem da exceção
      "INSUFFICIENT_FUNDS", // Código customizado
      status.value(),
      status.getReasonPhrase(),
      LocalDateTime.now(),
      getPathFromRequest(request)
    );

    return new ResponseEntity<>(errorResponse, status);
  }

  // Trata TransactionProcessingException e outras exceções inesperadas
  @ExceptionHandler({TransactionProcessingException.class, Exception.class})
  public ResponseEntity<ErrorResponse> handleTransactionProcessingException(Exception ex, WebRequest request) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 500 Internal Server Error

    // Para exceções genéricas ou inesperadas, evite expor detalhes internos no message.
    // Use uma mensagem genérica e logue o erro original no servidor.
    String message = (ex instanceof RuntimeException)
      ? ex.getMessage() // Exibe mensagem para exceções conhecidas que podem ter detalhes úteis
      : "Ocorreu um erro interno ao processar sua requisição."; // Mensagem genérica para outros casos

    String code = (ex instanceof TransactionProcessingException) ? "TRANSACTION_PROCESSING_ERROR" : "INTERNAL_SERVER_ERROR";

    // Logar a exceção original para depuração
    // Logger logger = LoggerFactory.getLogger(this.getClass());
    // logger.error("Erro interno na API", ex);

    ErrorResponse errorResponse = new ErrorResponse(
      message,
      code,
      status.value(),
      status.getReasonPhrase(),
      LocalDateTime.now(),
      getPathFromRequest(request)
    );

    return new ResponseEntity<>(errorResponse, status);
  }

  // Metodo auxiliar para obter o path da requisição
  private String getPathFromRequest(WebRequest request) {
    if (request instanceof ServletWebRequest) {
      HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
      return servletRequest.getRequestURI();
    }
    return request.getContextPath(); // Retorna algo genérico se não for HttpServletRequest
  }

}
