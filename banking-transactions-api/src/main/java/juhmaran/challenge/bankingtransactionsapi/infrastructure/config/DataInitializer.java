package juhmaran.challenge.bankingtransactionsapi.infrastructure.config;

import juhmaran.challenge.bankingtransactionsapi.application.usecase.AccountService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;

/**
 * Componente responsável por inicializar dados no sistema
 * <p>
 * Created by Juliane Maran
 *
 * @since 12/06/2025
 */
@Component // Marca como um componente Spring
@Profile("!test") // Opcional: Não executa este inicializador durante os testes unitários
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

  // Injeção de dependência da CLASSE AccountService concreta
  private final AccountService accountService;

  @Override
  public void run(String... args) throws Exception {
    logger.info("Starting data initialization...");

    List<SimpleEntry<String, BigDecimal>> accountsToInitialize = Arrays.asList(
      new SimpleEntry<>("1001-1", new BigDecimal("1000.00")),
      new SimpleEntry<>("1002-2", new BigDecimal("500.00")),
      new SimpleEntry<>("1003-3", new BigDecimal("0.00")),
      new SimpleEntry<>("1004-4", new BigDecimal("2500.75"))
    );

    for (SimpleEntry<String, BigDecimal> accountEntry : accountsToInitialize) {
      String accountNumber = accountEntry.getKey();
      BigDecimal initialBalance = accountEntry.getValue();
      // Call the method on the concrete AccountService class
      try {
        accountService.createAccountIfNotFound(accountNumber, initialBalance);
      } catch (Exception e) {
        logger.error("Error initializing account '{}': {}", accountNumber, e.getMessage(), e);
      }
    }

    logger.info("Data initialization completed.");
  }

}
