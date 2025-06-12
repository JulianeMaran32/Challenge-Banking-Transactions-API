package juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Record para representar a resposta do saldo da conta
 * <p>
 * Created by Juliane Maran
 *
 * @since 10/06/2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceResponse {

  private String accountNumber;
  private BigDecimal balance;

}