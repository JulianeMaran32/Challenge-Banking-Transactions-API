package juhmaran.challenge.bankingtransactionsapi.infrastructure.config;

import juhmaran.challenge.bankingtransactionsapi.application.port.in.AccountServicePort;
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

  private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class); // Inicializar Logger

  private final AccountServicePort accountServicePort;

  @Override
  public void run(String... args) throws Exception {
    logger.info("Iniciando inicialização de dados...");

    // Lista de contas a serem criadas (accountNumber, initialBalance)
    List<SimpleEntry<String, BigDecimal>> accountsToInitialize = Arrays.asList(
      new SimpleEntry<>("1001-1", new BigDecimal("1000.00")),
      new SimpleEntry<>("1002-2", new BigDecimal("500.00")),
      new SimpleEntry<>("1003-3", new BigDecimal("0.00")),
      new SimpleEntry<>("1004-4", new BigDecimal("2500.75"))
    );

    for (SimpleEntry<String, BigDecimal> accountEntry : accountsToInitialize) {
      String accountNumber = accountEntry.getKey();
      BigDecimal initialBalance = accountEntry.getValue();
      // Chama o metodo no serviço para criar a conta se ela não existir
      // A lógica de try-catch para erros de save já está no serviço.
      // Aqui, podemos simplesmente chamar e deixar as exceções serem propagadas se necessário,
      // ou adicionar um try-catch aqui para logar erros por conta individual.
      try {
        accountServicePort.createAccountIfNotFound(accountNumber, initialBalance);
      } catch (Exception e) {
        // Loga qualquer erro que ocorra durante a criação de uma conta específica
        logger.error("Erro ao inicializar a conta '{}': {}", accountNumber, e.getMessage(), e);
        // Decide se o erro deve parar a aplicação (relançar a exceção) ou apenas logar e continuar.
        // Para inicialização, logar e continuar é mais robusto.
      }
    }

    logger.info("Inicialização de dados concluída.");
  }

}
