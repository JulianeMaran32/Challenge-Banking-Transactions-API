# TODO List: Banking Transactions API

Este checklist lista as áreas para melhoria, refatoração e adição de testes com base na análise do código fornecido.

## 1. Testes

* [ ] **Adicionar Testes de Integração:**
    * [ ] Criar testes que simulem requisições concorrentes (múltiplas threads acessando a mesma conta simultaneamente).
    * [ ] Validar se a estratégia de Lock Pessimista (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) está garantindo a
      consistência do saldo sob concorrência.
    * [ ] Testar cenários de `InsufficientFundsException` e `AccountNotFoundException` sob carga.
* [ ] **Aumentar Cobertura de Testes Unitários/de Serviço:**
    * [ ] Adicionar testes para o `SingleTransactionProcessor` (testar lógica de aplicação de débito/crédito, validação
      de tipo, tratamento de erros internos).
    * [ ] Adicionar testes para a camada de Infrastructure (e.g., testar `AccountJpaAdapter`, mocking o JpaRepository).
* [ ] **Adicionar Testes de Controller:**
    * [ ] Criar testes para validar os endpoints `POST /accounts/transactions` e`GET /accounts/{accountNumber}/balance`.
    * [ ] Testar validação de DTOs (`@Valid`).
    * [ ] Testar respostas para diferentes cenários (sucesso, conta não encontrada, saldo insuficiente, bad request,
      etc.).
* [ ] **Adicionar Testes para o GlobalExceptionHandler:**
    * [ ] Garantir que o handler captura e responde corretamente para os diferentes tipos de exceção tratados (NotFound,
      Conflict, BadRequest, UnprocessableEntity, InternalServerError).

## 2. Código & Refatoração

* [ ] **Refatorar Resposta do Endpoint de Transações em Lote:**
    * [ ] Modificar o endpoint `POST /accounts/transactions` para retornar um DTO que detalhe o status de cada transação
      individual no lote (sucesso/falha e motivo), em vez de apenas um `ResponseEntity<Void>`.
    * [ ] Ajustar a lógica de processamento no `AccountService` para coletar os resultados individuais antes de retornar
      a resposta.
* [ ] **(Opcional) Otimizar Processamento de Lote para Mesma Conta:**
    * [ ] Para otimização em cenários com muitas transações para a *mesma* conta em uma única requisição, considerar
      agrupar as transações por número de conta antes de processá-las, adquirindo o lock para cada conta apenas uma vez
      por lote. (Nota: A abordagem atual já é segura, esta é uma otimização potencial).
* [ ] **(Minor) Revisar `SingleTransactionProcessor`:**
    * [ ] Se a lógica de processamento de transação individual se tornar mais complexa, considerar separar ainda mais as
      responsabilidades (e.g., criar classes/métodos específicos para validação de transação, aplicação de operação,
      etc.).

## 3. Infraestrutura & Deploy

* [ ] **Configurar Banco de Dados para Produção:**
    * [ ] Substituir o H2 em memória por um banco de dados persistente (e.g., PostgreSQL, MySQL).
    * [ ] Configurar o DataSource para o novo banco de dados.
* [ ] **Implementar Migrações de Schema:**
    * [ ] Adicionar uma ferramenta de migração de banco de dados (Flyway ou Liquibase) para gerenciar a criação e
      evolução do schema em ambientes que não usam `ddl-auto: create-drop`.
* [ ] **Revisar Configurações do Docker:**
    * [ ] Atualizar o `docker-compose.yml` para incluir um serviço de banco de dados persistente (se aplicável).
    * [ ] Configurar a API para se conectar ao serviço de banco de dados.
    * [ ] Adicionar HEALTHCHECK no Dockerfile.

## 4. Documentação

* [ ] **(Opcional) Refinar Documentação OpenAPI:**
    * [ ] Adicionar exemplos mais detalhados nos `@Schema` ou usar `@Content` para mostrar exemplos de payloads de
      requisição e resposta completos.
    * [ ] Garantir que todas as possíveis respostas de erro (definidas no `GlobalExceptionHandler`) estejam documentadas
      nos `@ApiResponse` relevantes.

## 5. Outros

* [ ] **Expandir Tratamento de Erros:**
    * [ ] Se novas regras de negócio ou tipos de exceção forem introduzidos, adicionar handlers específicos no
      `GlobalExceptionHandler`.
* [ ] **Gerenciamento de Contas:**
    * [ ] O método `createAccountIfNotFound` na `AccountService` é usado apenas pelo `DataInitializer`. Em um sistema
      real, um endpoint para criar contas seria necessário. (Considerar a adição de um endpoint `/accounts` com método
      POST).
* [ ] **Logging:**
    * [ ] Revisar os níveis de log e mensagens para garantir que sejam adequados para diferentes ambientes (dev vs
      prod). Por exemplo, logs de nível DEBUG/TRACE para Hibernate podem ser excessivos em produção.