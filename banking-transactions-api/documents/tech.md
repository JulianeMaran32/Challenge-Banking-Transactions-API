# Justificativa das Escolhas Tecnológicas - API de Lançamentos Bancários

Este documento descreve as motivações por trás da seleção das principais tecnologias e dependências utilizadas no
projeto da API de Lançamentos Bancários.

## Visão Geral

O projeto foi desenvolvido utilizando um stack tecnológico moderno e amplamente adotado no mercado Java, visando atender
aos requisitos do desafio, incluindo a criação de uma API RESTful thread-safe, com persistência, validação, documentação
e testes. A arquitetura em camadas (domain, application, infrastructure) foi adotada para promover organização,
manutenibilidade e facilitar futuras evoluções, como a migração para microsserviços.

## Justificativa das Principais Tecnologias

### Java 21

* **LTS (Long-Term Support):** O Java 21 é uma versão de suporte de longo prazo, garantindo atualizações e suporte por
  um período estendido.
* **Recursos Modernos:** Oferece melhorias de performance, novas funcionalidades na linguagem (como *Pattern Matching* e
  *Record Patterns* definitivos, *Virtual Threads* em preview) e na JVM que podem ser exploradas em projetos futuros ou
  em otimizações.
* **Desempenho:** Versões mais recentes do Java geralmente trazem otimizações significativas na JVM e garbage
  collectors.
* **Alinhamento com Frameworks:** É totalmente compatível com versões recentes do Spring Boot 3.x.

### Maven 3.9.x

* **Padrão de Mercado:** É o gerenciador de dependências e ferramenta de build mais utilizado no ecossistema Java.
* **Gerenciamento de Dependências:** Facilita a declaração, resolução e gerenciamento de dependências transitivas.
* **Ciclo de Vida Padronizado:** Oferece um ciclo de vida de build consistente (compile, test, package, install,
  deploy).
* **Plugins:** Rico ecossistema de plugins para diversas tarefas (compilação, empacotamento, testes, análise de código,
  etc.).
* **Compatibilidade:** A versão 3.9.x é uma versão recente e estável, garantindo compatibilidade com o Java 21 e Spring
  Boot 3.x.

### Spring Boot 3.5.0

* **Framework Acelerador:** Baseado no Spring Framework, o Spring Boot simplifica drasticamente a criação de aplicações
  stand-alone e "production-ready".
* **Autoconfiguração:** Reduz a necessidade de configurações boilerplate, configurando automaticamente diversos
  componentes com base nas dependências presentes (convenção sobre configuração).
* **Starters:** Agrupa conjuntos de dependências comuns (`spring-boot-starter-*`), facilitando a inclusão de
  funcionalidades como Web, JPA, Testes, etc.
* **Servidor Embarcado:** Permite empacotar a aplicação como um JAR executável com um servidor web embarcado (Tomcat por
  padrão), simplificando o deployment.
* **Versão 3.x:** Alinhada com as especificações mais recentes do Jakarta EE (como Jakarta Persistence e Jakarta
  Validation), sendo a escolha natural para projetos modernos com Java 17+. A versão 3.5.0 representa o target para a
  próxima release, indicando o uso da versão mais vanguardista e planejada do framework.

## Justificativa das Principais Dependências (Starters e Outras)

Listadas conforme aparecem no `pom.xml`:

1. **`spring-boot-starter-actuator`**:

    * **Propósito:** Adiciona endpoints de monitoramento e gerenciamento para a aplicação (saúde, métricas, informações,
      etc.).
    * **Justificativa:** Essencial para visibilidade operacional da aplicação em ambientes de produção ou staging,
      permitindo verificar o estado e coletar dados importantes.

2. **`spring-boot-starter-data-jpa`**:

    * **Propósito:** Integração com JPA (Java Persistence API) e Hibernate, facilitando a persistência de dados em
      bancos relacionais.
    * **Justificativa:** Permite mapear entidades Java para tabelas no banco de dados e realizar operações CRUD de forma
      abstrata, além de ser fundamental para implementar a camada de persistência conforme a arquitetura proposta. É
      crucial para gerenciar o estado das contas e seus saldos.

3. **`spring-boot-starter-validation`**:

    * **Propósito:** Integração com a API de Validação do Jakarta (anteriormente Bean Validation).
    * **Justificativa:** Permite definir regras de validação declarativamente em DTOs e entidades (`@NotNull`,
      `@DecimalMin`, etc.), garantindo a integridade dos dados recebidos pela API antes de processá-los, com mensagens
      personalizadas em português.

4. **`spring-boot-starter-web`**:

    * **Propósito:** Construção de aplicações web, incluindo APIs RESTful. Contém o servidor web embarcado (Tomcat por
      padrão) e as dependências necessárias para criar controllers e lidar com requisições HTTP.
    * **Justificativa:** Base para implementar a interface REST da API, expondo os endpoints solicitados no desafio (
      `/transactions`, `/balance`).

5. **`h2database` (scope runtime)**:

    * **Propósito:** Banco de dados relacional em memória (ou baseado em arquivo).
    * **Justificativa:** Ideal para desenvolvimento e testes unitários/de integração devido à sua facilidade de
      configuração (não requer instalação externa) e inicialização rápida. Permite que cada execução ou teste comece com
      um estado de banco de dados limpo e conhecido. (Nota: Para produção, seria substituído por um banco de dados
      robusto como PostgreSQL, MySQL, etc.).

6. **`spring-boot-configuration-processor` (optional)**:

    * **Propósito:** Gera metadados para propriedades de configuração customizadas (`@ConfigurationProperties`),
      melhorando o suporte em IDEs (autocompletar, documentação).
    * **Justificativa:** Ajuda na produtividade ao trabalhar com arquivos de configuração (`application.yml`), embora
      seja opcional para o funcionamento básico.

7. **`lombok` (optional)**:

    * **Propósito:** Ferramenta que gera código boilerplate (getters, setters, construtores, etc.) via anotações em
      tempo de compilação.
    * **Justificativa:** Reduz a verbosidade do código, tornando classes como DTOs e Entidades mais concisas e legíveis.
      Utilizado com `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`.

8. **`springdoc-openapi-starter-webmvc-api`** e **`springdoc-openapi-starter-webmvc-ui`**:

    * **Propósito:** Geração automática de documentação da API no formato OpenAPI 3 e interface gráfica Swagger UI.
    * **Justificativa:** Essencial para documentar a API RESTful conforme solicitado, tornando os endpoints, parâmetros
      e respostas facilmente acessíveis e testáveis através da UI do Swagger. Promove a colaboração e o entendimento da
      API.

9. **`spring-boot-starter-test` (scope test)**:

    * **Propósito:** Fornece dependências essenciais para testes de aplicações Spring Boot, incluindo JUnit 5, Mockito,
      AssertJ, Spring Test, etc.
    * **Justificativa:** Fundamental para a implementação dos testes unitários e de integração solicitados no desafio,
      garantindo a qualidade e o correto funcionamento da lógica de negócio e dos componentes da aplicação.

