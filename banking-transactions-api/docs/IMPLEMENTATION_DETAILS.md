# Banking Transactions API - Detalhes de Implementação

Este documento complementa o `README.md` principal, oferecendo uma visão mais aprofundada sobre as decisões técnicas,
arquitetura e implementação da solução para o Desafio Matera - API de Lançamentos Bancários.

## 🧩 Tecnologias Utilizadas

As principais tecnologias e frameworks utilizados no projeto são:

* **Linguagem:** Java 21
* **Framework:** Spring Boot 3.3.x
* **Gerenciador de Dependências/Build:** Apache Maven 3.9.x
* **Persistência:** Spring Data JPA / Hibernate
* **Banco de Dados:** H2 Database (Em memória para desenvolvimento/teste. Escolhido pela simplicidade e facilidade de
  configuração para o desafio).
* **Documentação API:** Springdoc OpenAPI 3 (Swagger UI. Para gerar documentação interativa automaticamente a partir do
  código).
* **Mapeamento:** MapStruct (Gerador de código para mapeamento entre DTOs e Entidades, reduzindo código boilerplate).
* **Contêineres:** Docker / Docker Compose (Para empacotamento e orquestração, facilitando o deploy e teste em
  diferentes ambientes).
* **Testes:** JUnit 5, Mockito (Frameworks padrão para testes unitários e de integração).
* **Outros:** Lombok (Reduz código boilerplate de getters/setters, construtores, etc.), SLF4J (Abstração para logging),
  Jakarta Bean Validation (Para validação declarativa de entrada).

## 🏛️ Arquitetura

O projeto foi estruturado seguindo uma arquitetura em camadas, com inspirações em princípios da Clean Architecture e
Ports and Adapters. A organização principal é feita através dos pacotes:

* `domain`: Camada mais interna. Contém a lógica de negócio pura e entidades (e.g., `Account`, `Transaction`), exceções
  de domínio específicas (`AccountNotFoundException`, `InsufficientFundsException`), e serviços de domínio que
  encapsulam regras complexas. Esta camada é independente de qualquer tecnologia de infraestrutura ou framework externo.
* `application`: Orquestra a lógica de negócio definida na camada `domain`. Contém os Use Cases (representados por
  classes de Serviço como `AccountService`) que definem as operações que a aplicação pode realizar (e.g.,
  `processTransactions`, `getBalance`). Interfaces ("Ports") que definem as operações de infraestrutura necessárias (
  e.g., `AccountRepositoryPort`) residem aqui, mas suas implementações ficam na camada externa.
* `infrastructure`: Camada mais externa ("Adapters"). Implementa os "Ports" definidos na camada `application`,
  conectando a lógica de negócio a tecnologias específicas. Inclui:
    * Controllers REST (`AccountController`) que expõem os Use Cases da camada `application` via HTTP.
    * Implementações JPA dos repositórios (`AccountJpaRepository`).
    * Mappers (`AccountMapper`, `TransactionMapper`) entre entidades de domínio e DTOs de transporte (utilizando
      MapStruct).
    * Configurações específicas do Spring (`Spring Beans`, `DataSource`).
    * Tratamento de Erros Global (`GlobalExceptionHandler`).

Esta separação promove:

* **Separação de Responsabilidades:** Cada camada tem um papel bem definido.
* **Testabilidade:** As camadas `domain` e `application` podem ser testadas de forma isolada sem a necessidade de subir
  a infraestrutura real.
* **Manutenibilidade:** Alterações em tecnologias de infraestrutura (ex: trocar H2 por PostgreSQL) afetam primariamente
  a camada `infrastructure`.
* **Flexibilidade:** A lógica de negócio é desacoplada da forma como é exposta (REST) ou persistida (JPA).

## 🔒 Gerenciamento de Concorrência

Um dos requisitos cruciais do desafio era garantir a thread-safety e evitar condições de corrida nas operações de
lançamento. A estratégia adotada foi utilizar **Database Pessimistic Locking** fornecido pelo Spring Data JPA.

Ao realizar operações que modificam o saldo de uma conta (`DEBIT` ou `CREDIT`), a conta é buscada do banco de dados
dentro de uma transação (`@Transactional`) utilizando a anotação `@Lock(LockModeType.PESSIMISTIC_WRITE)` no método do
repositório.

```java
// Exemplo ilustrativo no repositório (adaptado)
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<Account> findByAccountNumber(String accountNumber);
```

* `PESSIMISTIC_WRITE`: Adquire um bloqueio de escrita exclusivo na linha da tabela `account` correspondente à conta
  sendo acessada.

**Como funciona:**
Quando várias threads tentam acessar a *mesma* conta simultaneamente para um lançamento:

1. A primeira thread adquire o bloqueio de escrita para aquela conta.
2. As threads subsequentes que tentam acessar a *mesma* conta com o mesmo tipo de bloqueio (ou compatível, dependendo do
   banco) serão **bloqueadas** (pausadas) pelo banco de dados até que a primeira thread complete sua transação (commit
   ou rollback) e libere o bloqueio.
3. Após a liberação do bloqueio, a próxima thread na fila adquire o bloqueio e prossegue.

Esta abordagem delega a responsabilidade da serialização das operações ao banco de dados, que é altamente otimizado para
gerenciar bloqueios, garantindo que cada operação de atualização de saldo para uma conta específica ocorra de forma
atômica e isolada, prevenindo condições de corrida e garantindo a consistência dos saldos.

## 🌱 Inicialização de Dados

Para facilitar os testes e a execução inicial, a aplicação inclui um inicializador de dados. Um componente que
implementa `CommandLineRunner` (`DataInitializer`) é executado automaticamente pelo Spring Boot na inicialização.

Este inicializador verifica se contas padrão já existem no banco de dados em memória H2. Se não existirem, ele cria
algumas contas pré-definidas (configuráveis no próprio inicializador) com saldos iniciais. Isso garante que sempre
haverá contas disponíveis para realizar lançamentos e consultas logo após a aplicação subir.

## 🚨 Tratamento de Erros Personalizado

Um tratamento de erros centralizado foi implementado usando `@RestControllerAdvice` (`GlobalExceptionHandler`). Este
componente intercepta exceções lançadas pelas camadas inferiores (especialmente a camada `application`) e as mapeia para
respostas HTTP apropriadas e um formato de erro JSON padrão.

* **Exceções de Negócio:** Exceções como `AccountNotFoundException` ou `InsufficientFundsException` (definidas na camada
  `domain`) são capturadas e mapeadas para códigos de status HTTP semanticamente corretos (e.g., `404 Not Found`,
  `409 Conflict`), fornecendo mensagens de erro claras no corpo da resposta.
* **Erros de Validação:** Erros de validação do Bean Validation são capturados (`MethodArgumentNotValidException`) e
  retornam um `400 Bad Request` com detalhes sobre quais campos falharam na validação.
* **Erros Genéricos/Sistema:** Exceções não tratadas explicitamente são capturadas e retornam um
  `500 Internal Server Error`, evitando expor detalhes internos sensíveis ao cliente.

Todas as respostas de erro seguem um formato JSON consistente, geralmente utilizando um objeto ou Record `ErrorResponse`
contendo o timestamp, status HTTP, tipo de erro e uma mensagem descritiva.

## ✅ Validação

A validação da entrada da API é realizada utilizando **Jakarta Bean Validation**, integrado ao Spring Boot via
`spring-boot-starter-validation`.

* As regras de validação (ex: `@NotBlank`, `@NotNull`, `@DecimalMin(value = "0.01")`) são declaradas diretamente nos
  DTOs (Records) utilizados no corpo da requisição (`TransactionRequest`).
* O Spring MVC aciona automaticamente o processo de validação quando o controller é anotado com `@Validated` ou
  `@Valid`.
* Mensagens de erro personalizadas e suporte a internacionalização (i18n) são configurados utilizando arquivos
  `messages.properties`.

Isso garante que a entrada da API seja verificada o mais cedo possível no pipeline de processamento da requisição, antes
mesmo de a lógica de negócio ser executada.

---

Este documento buscou detalhar os aspectos técnicos mais relevantes da solução. Para explorar o código-fonte, o
repositório completo está disponível
em: [https://github.com/JulianeMaran32/Challenge-Banking-Transactions-API](https://github.com/JulianeMaran32/Challenge-Banking-Transactions-API)






