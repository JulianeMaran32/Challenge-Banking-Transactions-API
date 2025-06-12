package juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Record para representar a requisição de uma única transação.
 * <p>
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
@Data
public class TransactionRequest {

  @NotBlank(message = "{transaction.accountNumber.notBlank}")
  private String accountNumber;

  @NotNull(message = "{transaction.amount.notNull}")
  @DecimalMin(value = "0.01", inclusive = false, message = "{transaction.amount.decimalMin}")
  // inclusive=false para > 0
  private BigDecimal amount;

  @NotNull(message = "{transaction.type.notNull}")
  private TransactionType type;

}
