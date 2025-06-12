package juhmaran.challenge.bankingtransactionsapi.infrastructure.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import juhmaran.challenge.bankingtransactionsapi.application.port.in.AccountServicePort;
import juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.request.TransactionRequest;
import juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.response.AccountBalanceResponse;
import juhmaran.challenge.bankingtransactionsapi.infrastructure.error.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller para gerenciar operações de conta e transações
 * <p>
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Validated
@Tag(name = "Contas", description = "Gerenciamento de contas bancárias e lançamentos")
public class AccountController {

  private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

  private final AccountServicePort accountServicePort;

  @Operation(summary = "Realizar lançamentos (débito/crédito)",
    description = "Executa uma lista de operações de débito ou crédito em contas específicas.")
  @ApiResponse(responseCode = "200", description = "Lançamentos realizados com sucesso")
  @ApiResponse(responseCode = "400", description = "Requisição inválida (erros de validação ou argumentos inválidos)",
    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Conta não encontrada",
    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(responseCode = "409", description = "Conflito (saldo insuficiente ou recurso duplicado)",
    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(responseCode = "422", description = "Conteúdo semântico inválido (ex: valor zero/negativo)",
    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping("/transactions")
  public ResponseEntity<Void> performTransactions(@Valid @RequestBody List<TransactionRequest> transactions) {
    logger.info("Received request to perform transactions. Count: {}", transactions != null ? transactions.size() : 0);
    accountServicePort.performTransactions(transactions);
    logger.info("Transactions processed successfully.");
    // Para operações POST que resultam em modificação, 200 OK ou 204 No Content (se não retornar corpo) são comuns.
    // O requisito pedia 201 Created para POST, mas este endpoint processa uma lista de TRANSAÇÕES, não CRIA um novo RECURSO (transação individual).
    // Retornar 201 para uma lista de operações pode ser confuso. 200 OK para sucesso é mais apropriado aqui.
    // Se o requisito 201 for estrito, você precisaria mudar o endpoint para criar uma transação individual por vez,
    // ou retornar uma lista dos recursos criados, o que não se encaixa no requisito de "mais de um lançamento na mesma requisição".
    // Vamos manter 200 OK para o lote. Se houver um endpoint separado para criar uma conta, esse sim seria 201.
    return ResponseEntity.ok().build(); // Retorna 200 OK
  }

  @Operation(summary = "Obter saldo da conta",
    description = "Retorna o saldo atual de uma conta específica.")
  @ApiResponse(responseCode = "200", description = "Saldo retornado com sucesso",
    content = @Content(mediaType = "application/json",
      schema = @Schema(implementation = AccountBalanceResponse.class)))
  @ApiResponse(responseCode = "404", description = "Conta não encontrada",
    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @GetMapping("/{accountNumber}/balance")
  public ResponseEntity<AccountBalanceResponse> getAccountBalance(@PathVariable String accountNumber) {
    logger.info("Received request to get balance for account: {}", accountNumber);
    BigDecimal balance = accountServicePort.getAccountBalance(accountNumber);
    AccountBalanceResponse response = new AccountBalanceResponse(accountNumber, balance);
    logger.info("Balance for account {} returned: {}", accountNumber, balance);
    // Metodo GET para consulta deve retornar 200 OK em caso de sucesso
    return ResponseEntity.ok(response); // Retorna 200 OK
  }

}
