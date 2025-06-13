# Banking Transactions API

## Descrição

API RESTful desenvolvida como parte do desafio técnico para a vaga de Analista de Desenvolvimento Java Pleno na Matera.
O objetivo é gerenciar lançamentos de débito e crédito em contas bancárias de forma thread-safe e consistente.

**Links Importantes:**

* Documentação: http://localhost:8080/swagger-ui/index.html#/
* Banco de dados em memória H2: http://localhost:8080/h2-console
* Repositório: https://github.com/JulianeMaran32/Challenge-Banking-Transactions-API

## Tecnologias Utilizadas

* **Linguagem:** Java 21
* **Framework:** Spring Boot 3.3.x
* **Gerenciador de Dependências/Build:** Maven 3.9.x
* **Persistência:** Spring Data JPA / Hibernate
* **Banco de Dados:** H2 Database (Em memória para desenvolvimento/teste)
* **Documentação API:** Springdoc OpenAPI 3 (Swagger UI)
* **Contêineres:** Docker / Docker Compose
* **Testes:** JUnit 5, Mockito
* **Outros:** Lombok, SLF4J (Logging), Bean Validation

## Arquitetura

O projeto segue uma arquitetura em camadas (inspirada em Clean Architecture / Ports and Adapters), dividida nos pacotes
principais:

* `domain`: Contém as entidades e regras de negócio puro.
* `application`: Define os casos de uso e interfaces (Ports) para interação com outras camadas.
* `infrastructure`: Contém as implementações (Adapters) para a camada de aplicação, incluindo controllers REST,
  adaptadores JPA, configurações e tratamento de erros.

Esta estrutura promove a separação de responsabilidades, facilita a manutenibilidade, testabilidade e uma eventual
migração para uma arquitetura de microsserviços, pois as lógicas de negócio (application, domain) são independentes da
tecnologia de persistência ou da interface de comunicação (infrastructure).

## Gerenciamento de Concorrência

A concorrência é gerenciada utilizando **Database Pessimistic Locking** via Spring Data JPA (
`@Lock(LockModeType.PESSIMISTIC_WRITE)`). Ao buscar uma conta para realizar um lançamento, um bloqueio de escrita
exclusivo é adquirido no registro do banco de dados. Isso garante que múltiplas requisições tentando modificar a mesma
conta simultaneamente serão serializadas pelo banco de dados, prevenindo condições de corrida e inconsistência de dados.
A transação JPA (`@Transactional`) garante a atomicidade da operação de busca/bloqueio e atualização.

## Inicialização de Dados

Ao iniciar a aplicação, um componente (`DataInitializer`) que implementa `CommandLineRunner` é executado. Ele verifica a
existência de contas pré-definidas no banco de dados e as cria caso não existam, com um saldo inicial especificado.

## Como Executar Localmente

1. **Pré-requisitos:** Java 21 SDK, Maven 3.9.x.
2. **Clonar o repositório:** `git clone https://github.com/JulianeMaran32/Challenge-Banking-Transactions-API.git`
3. **Navegar para a pasta do projeto:** `cd Challenge-Banking-Transactions-API`
4. **Construir o projeto:** `mvn clean install`
5. **Executar a aplicação:** `java -jar target/banking-transactions-api-0.0.1-SNAPSHOT.jar` (verifique o nome exato do
   JAR gerado)

A aplicação iniciará em `http://localhost:8080`.

## Como Executar com Docker

1. **Pré-requisitos:** Docker, Docker Compose.
2. **Navegar para a pasta do projeto:** `cd Challenge-Banking-Transactions-API`
3. **Construir a imagem Docker:** `docker-compose build`
4. **Executar o contêiner:** `docker-compose up`

A aplicação estará acessível via Docker em `http://localhost:8080`.

## Documentação da API (Swagger UI)

Após executar a aplicação (localmente ou com Docker), acesse a documentação interativa em:
`http://localhost:8080/swagger-ui.html`

## Endpoints

* **`POST /api/accounts/transactions`**
* **Descrição:** Realiza um ou mais lançamentos (débito ou crédito).
* **Corpo da Requisição:** Lista de objetos `TransactionRequest`.

* **Request:**

```cURL
curl --location 'http://localhost:8080/accounts/transactions' \
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

* **Respostas:**
    * `200 OK`: Lançamentos processados com sucesso.
    * `400 Bad Request`: Requisição inválida (erros de validação).
    * `404 Not Found`: Conta não encontrada.
    * `409 Conflict`: Saldo insuficiente para débito.
    * `500 Internal Server Error`: Erro inesperado no servidor.

* **`GET /api/accounts/{accountNumber}/balance`**
* **Descrição:** Obtém o saldo atual de uma conta específica.
* **Parâmetro de Path:** `{accountNumber}` (string) - Número da conta.

* **Request:**

```cURL
curl --location 'http://localhost:8080/accounts/1001-1/balance'
```

* **Respostas:**
    * `200 OK`: Retorna o objeto `AccountBalanceResponse`.
    ```json
    {
    "accountNumber": "1001-1",
    "balance": 1501.00
    }
    ```
    * `404 Not Found`: Conta não encontrada.
    * `500 Internal Server Error`: Erro inesperado no servidor.

## Tratamento de Erros Personalizado

Exceções de negócio e erros de validação são capturados por um `@RestControllerAdvice` (`GlobalExceptionHandler`) e
retornam respostas JSON padronizadas com `timestamp`, `status` HTTP, `error` (texto do status) e uma `message`
detalhada.

## Validação

Utiliza Jakarta Bean Validation com mensagens de erro definidas em `src/main/resources/messages.properties` em
português.

## Testes

Os testes unitários podem ser executados utilizando Maven:
`mvn test`
Os testes focam na lógica de negócio na camada `application`, utilizando Mockito para simular o comportamento do
repositório.

## Postman Collection

Uma coleção Postman para testar os endpoints da API pode ser encontrada na pasta `postman/` na raiz do repositório.
Importe o arquivo `.json` para testar as operações manualmente.
