# Banking Transactions API

## Descrição

API RESTful desenvolvida como parte do desafio técnico para a vaga de Analista de Desenvolvimento Java Pleno na Matera.
O objetivo é gerenciar lançamentos de débito e crédito em contas bancárias de forma thread-safe e consistente.

**Links Úteis:**

* Documentação Interativa da API (Swagger
  UI): [http://localhost:8080/swagger-ui.html](http://localhost:8080/api/v1/swagger-ui.html)
* Console do Banco de Dados em Memória H2: [http://localhost:8080/api/v1/h2-console](http://localhost:8080/h2-console)
    * Credenciais (padrão `application.yml`): Usuário: `sa`, Senha: `password`, JDBC URL: `jdbc:h2:mem:testdb`
* Repositório Completo do
  Projeto: [https://github.com/JulianeMaran32/Challenge-Banking-Transactions-API](https://github.com/JulianeMaran32/Challenge-Banking-Transactions-API)

---

## Sumário

1. [Tecnologias Utilizadas](#tecnologias-utilizadas)
2. [Arquitetura](#arquitetura)
3. [Gerenciamento de Concorrência](#gerenciamento-de-concorrência)
4. [Inicialização de Dados](#inicialização-de-dados)
5. [Como Configurar e Executar](#como-configurar-e-executar)
    * [Pré-requisitos](#pré-requisitos)
    * [Execução Local (Maven)](#execução-local-maven)
    * [Execução com Docker Compose](#execução-com-docker-compose)
6. [Endpoints da API](#endpoints-da-api)
    * [POST /api/accounts/transactions](#post-apiaccountstransactions)
    * [GET /api/accounts/{accountNumber}/balance](#get-apiaccountsaccountnumberbalance)
7. [Tratamento de Erros Personalizado](#tratamento-de-erros-personalizado)
8. [Validação](#validação)
9. [Testes](#testes)
    * [Testes Unitários](#testes-unitários)
    * [Testes Manuais (Postman)](#testes-manuais-postman)

---

## Tecnologias Utilizadas

* **Linguagem:** Java 21
* **Framework:** Spring Boot 3.3.x
* **Gerenciador de Dependências/Build:** Maven 3.9.x
* **Persistência:** Spring Data JPA / Hibernate
* **Banco de Dados:** H2 Database (Em memória para desenvolvimento/teste)
* **Documentação API:** Springdoc OpenAPI 3 (Swagger UI)
* **Mapeamento:** MapStruct
* **Contêineres:** Docker / Docker Compose
* **Testes:** JUnit 5, Mockito
* **Outros:** Lombok, SLF4J (Logging), Jakarta Bean Validation

## Arquitetura

O projeto segue uma arquitetura em camadas (inspirada em Clean Architecture / Ports and Adapters), dividida nos pacotes
principais:

* `domain`: Contém as entidades e regras de negócio puro (Entidades, Exceções de Domínio, Serviços de Domínio).
* `application`: Define os casos de uso (Services) e interfaces (Ports) que orquestram a lógica de negócio,
  independentes da tecnologia de infraestrutura.
* `infrastructure`: Contém as implementações (Adapters) dos ports da camada de aplicação, incluindo controllers REST,
  adaptadores JPA para persistência, configurações, mappers DTO <-> Entidade e tratamento de erros.

Esta estrutura promove a separação de responsabilidades, facilita a manutenibilidade, testabilidade e uma eventual
migração para uma arquitetura de microsserviços, pois as lógicas de negócio (application, domain) são independentes da
tecnologia de persistência ou da interface de comunicação (infrastructure).

## Gerenciamento de Concorrência

A concorrência nas operações de débito/crédito é gerenciada utilizando **Database Pessimistic Locking** via Spring Data
JPA (`@Lock(LockModeType.PESSIMISTIC_WRITE)`). Ao buscar uma conta para realizar um lançamento em uma transação (
`@Transactional`), um bloqueio de escrita exclusivo é adquirido no registro correspondente no banco de dados. Isso
garante que múltiplas requisições tentando modificar a mesma conta simultaneamente serão serializadas pelo banco de
dados, prevenindo condições de corrida e garantindo a consistência dos dados.

## Inicialização de Dados

Ao iniciar a aplicação, um componente (`DataInitializer`) que implementa `CommandLineRunner` é executado. Ele verifica a
existência de contas pré-definidas (configuradas no próprio inicializador) no banco de dados e as cria caso não existam,
com um saldo inicial especificado. Isso garante que algumas contas estejam disponíveis para testes imediatos ao iniciar
a aplicação.

## Como Configurar e Executar

Esta seção descreve como colocar a aplicação em funcionamento.

### Pré-requisitos

Certifique-se de ter os seguintes softwares instalados:

* Java Development Kit (JDK) versão 21
* Apache Maven versão 3.9.x
* Docker e Docker Compose (opcional, para execução em contêineres)
* Git

### Execução Local (Maven)

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/JulianeMaran32/Challenge-Banking-Transactions-API.git
   ```
2. **Navegue para a pasta do projeto da API:**
   ```bash
   cd Challenge-Banking-Transactions-API/banking-transactions-api
   ```
3. **Construa o projeto:** Este comando irá compilar o código, executar os testes e empacotar a aplicação em um arquivo
   JAR.
   ```bash
   mvn clean install
   ```
4. **Execute a aplicação:**
   ```bash
   java -jar target/banking-transactions-api-0.0.1-SNAPSHOT.jar
   ```
   *(Nota: Verifique o nome exato do arquivo `.jar` na pasta `target` após a build)*

A aplicação estará acessível em `http://localhost:8080/api/v1`.

### Execução com Docker Compose

1. **Pré-requisitos:** Docker e Docker Compose instalados.
2. **Clone o repositório (se ainda não o fez):**
   ```bash
   git clone https://github.com/JulianeMaran32/Challenge-Banking-Transactions-API.git
   ```
3. **Navegue para a pasta raiz do repositório (onde está o arquivo `docker-compose.yml`):**
   ```bash
   cd Challenge-Banking-Transactions-API
   ```
4. **Construa a imagem Docker e inicie o contêiner:**
   ```bash
   docker compose up --build
   ```
   *(O parâmetro `--build` garante que a imagem será construída a partir do Dockerfile)*

A aplicação estará acessível via Docker em `http://localhost:8080/api/v1`. Para parar os contêineres, pressione `Ctrl+C` no
terminal onde o `docker compose up` está rodando ou use `docker compose down`.

## Endpoints da API

A documentação interativa completa dos endpoints, incluindo exemplos de requisição e resposta, está disponível no
**[Swagger UI](http://localhost:8080/api/v1/swagger-ui.html)** após a execução da aplicação. Abaixo, um resumo dos
endpoints
principais:

### `POST /api/accounts/transactions`

* **Descrição:** Processa um lote de operações de débito ou crédito em contas específicas. Permite múltiplos lançamentos
  em uma única requisição.
* **Corpo da Requisição:** Uma lista (`Array`) de objetos `TransactionRequest`. Veja o modelo `TransactionRequest` na
  documentação Swagger para detalhes dos campos e validações.

* **Exemplo de Request (`cURL`):**

```bash
curl --location 'http://localhost:8080/api/v1/accounts/transactions' \
--header 'Content-Type: application/json' \
--data '[
    {
        "accountNumber": "1001-1",
        "amount": 250.50,
        "type": "CREDIT"
    },
    {
        "accountNumber": "1002-2",
        "amount": 100.00,
        "type": "DEBIT"
    },
    {
        "accountNumber": "1003-3",
        "amount": 50.00,
        "type": "CREDIT"
    }
]'
```

* **Respostas Possíveis:**
    * `200 OK`: Lançamentos processados com sucesso. (Mantido 200 OK conforme solicitação)
    * `400 Bad Request`: Requisição inválida (erros de validação nos DTOs ou argumentos semânticos inválidos).
    * `404 Not Found`: Uma ou mais contas envolvidas nos lançamentos não foram encontradas.
    * `409 Conflict`: Ocorreu um conflito de estado, como saldo insuficiente para uma operação de débito.
    * `422 Unprocessable Content`: Ocorreu um erro semântico nos dados da requisição (ex: valor de transação zero ou
      negativo).
    * `500 Internal Server Error`: Ocorreu um erro inesperado no servidor durante o processamento.

### `GET /api/accounts/{accountNumber}/balance`

* **Descrição:** Obtém o saldo atual de uma conta específica.
* **Parâmetro de Path:** `{accountNumber}` (string) - O número da conta para a qual o saldo será consultado.
* **Responde com:** Um objeto `AccountBalanceResponse` contendo o número da conta e o saldo. Veja o modelo
  `AccountBalanceResponse` na documentação Swagger.

* **Exemplo de Request (`cURL`):**

```bash
curl --location 'http://localhost:8080/api/v1/accounts/1001-1/balance'
```

* **Respostas Possíveis:**
    * `200 OK`: Retorna o objeto `AccountBalanceResponse` com o saldo.
  ```json
  {
    "accountNumber": "1001-1",
    "balance": 1501.00
  }
  ```
    * `404 Not Found`: A conta especificada no path não foi encontrada.
    * `500 Internal Server Error`: Ocorreu um erro inesperado no servidor durante a consulta.

## Tratamento de Erros Personalizado

Exceções de negócio (`AccountNotFoundException`, `InsufficientFundsException`) e erros de validação/sistema são
capturados por um `@RestControllerAdvice` (`GlobalExceptionHandler`). Este handler centraliza o tratamento de erros,
mapeando diferentes tipos de exceções para códigos de status HTTP apropriados (400, 404, 409, 422, 500) e retornando
respostas JSON padronizadas utilizando o Record `ErrorResponse`.

## Validação

Utiliza Jakarta Bean Validation (`spring-boot-starter-validation`) para validar os DTOs de entrada (
`TransactionRequest`). As regras de validação (ex: `@NotBlank`, `@NotNull`, `@DecimalMin`) são declaradas diretamente
nos Records, e as mensagens de erro correspondentes são definidas nos arquivos `messages.properties` para suporte a
i18n.

## Testes

O projeto inclui diferentes abordagens de teste:

### Testes Unitários

Testes focados em verificar a lógica de negócio na camada `application` (Serviços) de forma isolada, utilizando JUnit 5
e Mockito para simular o comportamento das dependências (como o repositório).

Para executar os testes unitários utilizando o Maven:

```bash
mvn test
```

O comando `mvn clean install` (usado para construir o projeto) também executa os testes automaticamente.

### Testes Manuais (Postman)

Uma coleção Postman para testar os endpoints da API manualmente está disponível para facilitar a verificação das
operações e respostas.

Você pode encontrar o arquivo da coleção na pasta `postman/` na raiz do repositório:
`./postman/Banking Transactions API.postman_collection.json`

Importe este arquivo `.json` no Postman para ter acesso rápido aos exemplos de requisição, incluindo cenários de sucesso
e erro, e testar a API em funcionamento.