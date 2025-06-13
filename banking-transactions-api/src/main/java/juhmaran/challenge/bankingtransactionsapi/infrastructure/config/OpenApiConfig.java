package juhmaran.challenge.bankingtransactionsapi.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
      .openapi("3.0.0")
      .info(new Info()
        .title("Banking Transactions API")
        .version("1.0.0")
        .description("API RESTful desenvolvida como parte do desafio técnico para a vaga de Analista de " +
          "Desenvolvimento Java Pleno na Matera. O objetivo é gerenciar lançamentos de débito e crédito em " +
          "contas bancárias de forma thread-safe e consistente.")
        .contact(
          new Contact().name("Juliane Maran").email("julianemaran@gmail.com").url("https://github.com/JulianeMaran32"))
        .license(
          new License().name("MIT License").url("https://opensource.org/licenses/MIT"))
      )
      .servers(List.of(
        new Server().url("http://localhost:8080/api/v1").description("Ambiente de Desenvolvimento"))
      )
      .tags(List.of(
        new Tag().name("Contas").description("Gerenciamento de contas bancárias e lançamentos")
      ));
  }

}
