# Banking Transaction API

## Como Rodar

1. Clonar o repositório
2. Instalar as dependências com `mvn install package`
3. Iniciar o servidor com `mvn spring-boot:run`
4. Acessar a API em `http://localhost:8080/api/v1`
5. Para testar a API, você pode usar o Postman ou qualquer outro cliente HTTP.
6. Para executar os testes, use o comando `mvn test`.

---

1. **Estrutura de Projeto:** Pacotes bem definidos para cada camada (controller, service, repository, entity, dto,
   exception, config).
2. **Entidades (`Entity`):** `Account` e `Transaction`.
3. **Records (`Record`):** DTOs para requisições e respostas.
4. **Enums (`Enum`):** `TransactionType` (DEBIT, CREDIT).
5. **Interfaces (`Interface`):** `AccountRepository`, `TransactionRepository`, `AccountService`.
6. **Implementações (`Implementation`):** `AccountServiceImpl`.
7. **Controllers (`Controller`):** `AccountController`.
8. **Exceções Personalizadas (`Exception`):** Para casos como conta não encontrada, saldo insuficiente, etc.
9. **Tratamento de Erro Global (`@RestControllerAdvice`):** Para formatar respostas de erro personalizadas.
10. **Validação (`Jakarta Validation`):** Adições de anotações e mensagens em português.
11. **Configuração (`application.yml`):** H2 Database, JPA, logging.
12. **Swagger/OpenAPI 3:** Documentação da API.
13. **Testes Unitários (`JUnit 5`, `Mockito`):** Para Service e Controller.
14. **Testes de Integração:** Com foco em concorrência.
15. **Dockerfile:** Para construir a imagem Docker.
16. **docker-compose.yml:** Para subir a aplicação em container.
17. **README.md:** Instruções de uso e descrição.
18. **Postman Collection:** Arquivo de exemplo.

---

**Estrutura de Pacotes:**

```
src/main/java/juhmaran/challenge/bankingtransactionsapi/
├── config/
│   └── OpenApiConfig.java
├── controller/
│   └── AccountController.java
├── dto/
│   ├── request/
│   │   └── TransactionRequest.java
│   └── response/
│       ├── AccountBalanceResponse.java
│       └── TransactionBatchResponse.java
├── entity/
│   ├── Account.java
│   └── Transaction.java
├── enums/
│   └── TransactionType.java
├── exception/
│   ├── AccountNotFoundException.java
│   ├── ErrorResponse.java
│   ├── ExceptionHandlerAdvice.java
│   ├── InsufficientFundsException.java
│   └── TransactionProcessingException.java
├── repository/
│   ├── AccountRepository.java
│   └── TransactionRepository.java
├── service/
│   ├── AccountService.java
│   └── AccountServiceImpl.java
└── BankingTransactionsApiApplication.java

src/main/resources/
├── application.yml
└── ValidationMessages.properties

src/test/java/juhmaran/challenge/bankingtransactionsapi/
├── controller/
│   └── AccountControllerTest.java
├── repository/
│   └── AccountRepositoryTest.java
└── service/
    └── AccountServiceImplTest.java

src/main/docker/
├── Dockerfile
└── docker-compose.yml

Postman/
└── Banking Transactions API.postman_collection.json
```

---

### Uso de Records

* Records são imutáveis e concisos, ideais para DTOs.
* O construtor canônico é gerado automaticamente.
* Getters para amount e type são gerados automaticamente.

## Observações:

> **Nota:** Para `TransactionResponse.timestamp`, usar `java.time.ZonedDateTime` ou formatar para `String` no
> serviço/controlador é geralmente melhor do que `LocalDateTime` para APIs REST, pois inclui informações de fuso
> horário.
> Mantive `LocalDateTime` para simplicidade, mas tenha isso em mente para aplicações reais.

---

**Explicação da Concorrência:**

O ponto crucial da thread-safety aqui é a combinação de `@Transactional` e `@Lock(LockModeType.PESSIMISTIC_WRITE)`:

1. **`@Transactional`:** Garante que todo o *lote* de operações (buscar a conta, atualizar o saldo, criar várias
   transações, salvar tudo) seja uma única unidade de trabalho no banco de dados. Se algo falhar no meio (ex: saldo
   insuficiente para o 3º débito), a transação inteira é desfeita, garantindo que o saldo e as transações fiquem como se
   nada tivesse acontecido.
2. **`@Lock(LockModeType.PESSIMISTIC_WRITE)`:** Quando `findAccountWithLockByAccountNumber` é chamado dentro da
   transação, o banco de dados aplica um bloqueio exclusivo (`FOR UPDATE` em SQL, por exemplo) na linha da tabela
   `accounts` que está sendo lida. Qualquer outra transação que tente ler *ou* escrever nessa mesma linha terá que
   esperar este bloqueio ser liberado (quando a transação atual terminar - commit ou rollback). Isso impede que duas
   threads leiam o mesmo saldo, calculem um novo saldo e tentem salvar *ao mesmo tempo*, evitando a condição de corrida.
3. **Saldo Local:** Usar uma variável `currentBalance` dentro do loop de processamento é bom para a lógica *sequencial*
   dentro da transação. A garantia de thread-safety vem do *bloqueio no banco* ao ler o saldo inicial e ao salvar o
   saldo final, não do gerenciamento da variável local em si.

Este é um dos métodos robustos para lidar com atualizações concorrentes de saldo em bancos de dados relacionais.