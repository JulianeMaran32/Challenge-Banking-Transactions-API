package juhmaran.challenge.bankingtransactionsapi.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import juhmaran.challenge.bankingtransactionsapi.dtos.request.TransactionRequest;
import juhmaran.challenge.bankingtransactionsapi.dtos.response.AccountBalanceResponse;
import juhmaran.challenge.bankingtransactionsapi.dtos.response.TransactionBatchResponse;
import juhmaran.challenge.bankingtransactionsapi.services.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RestController // Indica que é um controlador REST
@RequestMapping("/accounts") // Base URL para todos os endpoints neste controller
@RequiredArgsConstructor // Lombok: Gera construtor com campos final
@Validated // Habilita a validação de parâmetros @PathVariable e @RequestParam
public class AccountController {

  private final AccountService accountService;

  @PostMapping("/{accountNumber}/transactions")
  public ResponseEntity<TransactionBatchResponse> performTransactions(
    @PathVariable @NotBlank(message = "O número da conta não pode estar em branco.") String accountNumber,
    @RequestBody @Valid List<TransactionRequest> transactions // @Valid valida cada item da lista
  ) {
    // Delega a lógica de negócio para o serviço
    TransactionBatchResponse response = accountService.performTransactions(accountNumber, transactions);
    return ResponseEntity.ok(response); // Retorna 200 OK com a resposta
  }

  @GetMapping("/{accountNumber}/balance")
  public ResponseEntity<AccountBalanceResponse> getBalance(
    @PathVariable @NotBlank(message = "O número da conta não pode estar em branco.") String accountNumber
  ) {
    // Delega a lógica de negócio para o serviço
    AccountBalanceResponse response = accountService.getBalance(accountNumber);
    return ResponseEntity.ok(response); // Retorna 200 OK com a resposta
  }

  @PostMapping
  public ResponseEntity<String> createAccount(
    @RequestParam @NotBlank(message = "O número da conta não pode estar em branco.") String accountNumber,
    @RequestParam(required = false) BigDecimal initialBalance // Permite criar conta com saldo inicial > 0
  ) {
    String createdAccountNumber = accountService.createAccount(accountNumber, initialBalance);
    // Retorna 201 Created com o número da conta criada
    return ResponseEntity.status(HttpStatus.CREATED).body("Conta '" + createdAccountNumber + "' criada com sucesso.");
  }
}
