package juhmaran.challenge.bankingtransactionsapi.infrastructure.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import juhmaran.challenge.bankingtransactionsapi.application.port.in.AccountServicePort;
import juhmaran.challenge.bankingtransactionsapi.domain.entity.Account;
import juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.request.TransactionRequest;
import juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.response.AccountBalanceResponse;
import juhmaran.challenge.bankingtransactionsapi.infrastructure.error.ErrorResponse;
import juhmaran.challenge.bankingtransactionsapi.infrastructure.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
  private final AccountMapper accountMapper; // Injetar a interface do MapStruct

  @Operation(summary = "Realizar lançamentos (débito/crédito)",
    description = "Executa uma lista de operações de débito ou crédito em contas específicas.")
  @ApiResponse(responseCode = "200", description = "Lançamentos processados com sucesso")
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
    logger.info("Recebida requisição para realizar lançamentos. Quantidade: {}", transactions != null ? transactions.size() : 0);
    accountServicePort.performTransactions(transactions);
    logger.info("Lançamentos processados com sucesso.");
    return ResponseEntity.status(HttpStatus.OK).build();
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
    logger.info("Recebida requisição para obter saldo da conta: {}", accountNumber);
    // O serviço retorna a entidade Account
    Account account = accountServicePort.getAccountBalance(accountNumber);
    // Usa o mapper MapStruct para converter a entidade para o DTO Record
    AccountBalanceResponse response = accountMapper.toResponse(account);
    // Acessa os campos do Record diretamente
    logger.info("Saldo da conta {} retornado: {}", accountNumber, response.accountNumber() + "/" + response.balance());
    return ResponseEntity.ok(response);
  }

}
