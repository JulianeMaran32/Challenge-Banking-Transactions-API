package juhmaran.challenge.bankingtransactionsapi.dtos.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Record para representar a resposta de um lote de transações
 * <p>
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
public record TransactionBatchResponse(
  String accountNumber,
  BigDecimal newBalance, // Saldo após as transações
  int successfulTransactionsCount, // Quantidade de transações bem sucedidas no lote
  List<TransactionResponse> transactions // Detalhes das transações criadas
) {
}
