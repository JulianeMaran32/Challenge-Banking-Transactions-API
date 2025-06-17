# Lista de Melhorias

Lista de melhorias que podem ser consideradas para a aplicação, tanto em termos de robustez, escalabilidade quanto de
funcionalidades e boas práticas, com base no código fornecido e nos requisitos do desafio:

1. **Gerenciamento de Transação do Lote (`performTransactions`):**
    * **Problema:** Atualmente, o método `AccountService.performTransactions` está anotado com `@Transactional`. Isso
      faz com que todas as transações dentro do lote sejam processadas em uma **única transação de banco de dados
      externa**. Se uma transação no meio do lote falhar (ex: saldo insuficiente), a exceção propagará e a transação
      externa será **rollback**, desfazendo todas as transações *bem-sucedidas* que a precederam neste lote.
    * **Melhoria:** Mudar a estratégia transacional para que **cada transação individual no
      lote (`SingleTransactionProcessor.process`) seja sua própria transação de banco de dados atômica**.
    * **Como fazer:** Remover `@Transactional` de `AccountService.performTransactions`. Adicionar `@Transactional` ao
      método `SingleTransactionProcessor.process`. Adicionar um bloco `try-catch` ao redor da chamada
      `singleTransactionProcessor.process(transaction)` dentro do loop em `performTransactions` para que, se uma
      transação individual falhar, a exceção seja tratada (talvez logada ou registrada), mas o loop continue tentando
      processar as transações restantes do lote.
    * **Benefício:** Garante que a falha de uma transação não afete outras transações no mesmo lote, permitindo
      processamento parcial do lote.

2. **Resposta da API para Lote de Transações:**
    * **Problema:** O endpoint `POST /transactions` retorna `ResponseEntity<Void>`, ou seja, apenas indica sucesso (HTTP
      200) ou falha geral (via `GlobalExceptionHandler`). Ele não informa quais transações específicas dentro do lote
      foram bem-sucedidas e quais falharam, ou o motivo da falha individual.
    * **Melhoria:** Retornar uma resposta mais detalhada que liste o resultado de cada transação submetida no lote.
    * **Como fazer:** Criar um novo DTO de resposta (ex: `BatchTransactionResultResponse`) que contenha uma lista de
      resultados individuais (ex: `TransactionResult`). Cada `TransactionResult` poderia incluir o identificador da
      transação (talvez um ID temporário enviado na requisição ou o número da conta+tipo+valor), um status (SUCESSO,
      FALHA) e uma mensagem de erro, se aplicável. O `AccountService.performTransactions` precisaria retornar essa lista
      de resultados, e o Controller mapeá-la para o `ResponseEntity`.
    * **Benefício:** O cliente da API tem feedback claro sobre o processamento de cada item no lote.

3. **Testes Abrangentes:**
    * **Problema:** A cobertura de testes unitários existe apenas para `AccountService`, mockando dependências chave.
      Faltam testes para o processador individual e testes de integração.
    * **Melhoria:** Adicionar diferentes níveis de teste para aumentar a confiança na aplicação.
    * **Como fazer:**
        * **Testes Unitários para `SingleTransactionProcessor`:** Testar a lógica específica de aplicar débito/crédito,
          validação de saldo, e as interações esperadas com o `AccountRepositoryPort` (verificando se
          `findByAccountNumberWithLock` e `save` são chamados corretamente).
        * **Testes de Integração:** Usando `@SpringBootTest`, testar o fluxo completo da API, do Controller ao banco de
          dados H2, para garantir que todos os componentes se integram corretamente.
        * **Testes de Concorrência (Crucial para este desafio!):** Implementar testes que simulem múltiplas threads
          chamando o endpoint `/transactions` *simultaneamente* para a *mesma* conta. Verificar se o bloqueio pessimista
          funciona conforme esperado e se o saldo final está correto após todas as operações. Ferramentas como
          `ExecutorService` e `CountDownLatch` em JUnit podem ser usadas para simular concorrência.
        * **Testes para `GlobalExceptionHandler`:** Testar se as exceções esperadas são mapeadas para os status HTTP e
          formatos de erro corretos.

4. **Persistência em Banco de Dados de Produção:**
    * **Problema:** O H2 em memória é ótimo para desenvolvimento e testes rápidos, mas não é adequado para produção (
      dados voláteis, performance, funcionalidades de locking).
    * **Melhoria:** Configurar a aplicação para usar um banco de dados relacional robusto e persistente (ex: PostgreSQL,
      MySQL).
    * **Como fazer:** Adicionar a dependência JDBC do banco escolhido no `pom.xml`. Configurar a `datasource` no
      `application.yml` com as credenciais e URL do banco. Garantir que o locking pessimista funcione corretamente no
      banco escolhido (geralmente `@Lock` do JPA funciona, mas pode ter nuances de configuração).
    * **Benefício:** Dados persistentes, maior robustez, performance e escalabilidade.

5. **Tratamento de Erro de Bloqueio (`LockTimeoutException`):**
    * **Problema:** Se uma requisição esperar pelo lock por mais tempo que o configurado (
      `hibernate.jakarta.persistence.lock.timeout`), uma `LockTimeoutException` (ou similar) será lançada. Atualmente,
      ela cairia no handler genérico de `Exception.class`, retornando um 500 INTERNAL SERVER ERROR.
    * **Melhoria:** Adicionar um handler específico para exceções de timeout de lock.
    * **Como fazer:** Criar um novo método `@ExceptionHandler` no `GlobalExceptionHandler` para
      `jakarta.persistence.LockTimeoutException` (ou a exceção específica do Hibernate/Spring Data JPA se for
      diferente). Retornar um status HTTP mais apropriado, como **429 Too Many Requests** ou **503 Service Unavailable
      ** (com uma mensagem indicando que a requisição não pôde ser processada devido a alta concorrência ou timeout).
    * **Benefício:** Fornece feedback mais preciso ao cliente da API em cenários de alta contenção.

6. **Adicionar Campo de Histórico/Extrato de Transações:**
    * **Problema:** O modelo de dados atual (`Account`) guarda apenas o saldo final. Não há registro individual das
      transações que ocorreram.
    * **Melhoria:** Criar uma nova entidade `Transaction` (ou `StatementEntry`) para registrar cada débito/crédito.
    * **Como fazer:** Adicionar uma nova entidade `Transaction` com campos como ID, número da conta, valor, tipo,
      timestamp, maybe description. Criar um `TransactionRepository`. No `SingleTransactionProcessor`, após aplicar a
      operação e salvar a conta, criar e salvar um registro dessa transação. Criar um novo endpoint (ex:
      `GET /accounts/{accountNumber}/transactions`) para buscar o histórico de transações de uma conta.
    * **Benefício:** Permite visualizar o extrato da conta, importante para sistemas financeiros reais, e serve como log
      auditável.

7. **Mover Lógica de Operação para Entidade de Domínio:**
    * **Problema:** A lógica de `applyCredit` e `applyDebit` está em `AccountOperationService`, separada da entidade
      `Account`. Embora seja uma divisão válida (Anemic vs. Rich Domain Model), alguns padrões de DDD sugerem que o
      comportamento (métodos) deve estar junto dos dados (atributos) na entidade.
    * **Melhoria:** Mover os métodos `applyCredit` e `applyDebit` para dentro da classe `Account`.
    * **Como fazer:** Adicionar os métodos `public void applyCredit(BigDecimal amount)` e
      `public void applyDebit(BigDecimal amount)` na classe `Account`, contendo a lógica de validação e atualização do
      saldo. O `SingleTransactionProcessor` chamaria `account.applyCredit(amount)` ou `account.applyDebit(amount)`
      diretamente após obter a conta.
    * **Benefício:** Encapsula o comportamento junto dos dados que ele opera, potencialmente tornando o domínio mais
      expressivo. `AccountOperationService` pode ser removido ou refatorado se houver lógica que orquestre *várias*
      contas ou operações.

8. **Tratar a Criação Inicial de Contas de Forma Mais Robusta:**
    * **Problema:** O método `createAccountIfNotFound` usa `existsByAccountNumber` seguido de `save`. Existe uma pequena
      **condição de corrida** aqui: se duas instâncias da aplicação iniciarem ao mesmo tempo e verificarem se a conta
      existe (ambas veem que NÃO existe), ambas tentarão salvá-la, o que levará a uma `DataIntegrityViolationException`
      para uma delas. O código já trata a exceção na inicialização, o que é um mitigador, mas o padrão não é 100% seguro
      se a concorrência na criação for esperada.
    * **Melhoria:** Eliminar a condição de corrida "check then act" (verificar se existe e depois salvar) em
      `createAccountIfNotFound`.
    * **Como fazer:** Tentar salvar a nova conta **sempre** e lidar com a `DataIntegrityViolationException` como o caso
      de "já existe".
    * **Exemplo Simplificado:**
      ```java
      @Transactional
      public void createAccount(String accountNumber, BigDecimal initialBalance) {
          try {
              Account newAccount = new Account(null, accountNumber, initialBalance);
              accountRepositoryPort.save(newAccount);
              logger.info("Conta '{}' criada com sucesso com saldo inicial: {}", accountNumber, initialBalance);
          } catch (DataIntegrityViolationException e) {
              logger.info("Conta '{}' já existe. Pulando criação.", accountNumber);
              // Opcional: verificar a causa específica da exceção se houver outras constraints
          } catch (Exception e) {
               logger.error("Erro inesperado ao salvar a conta '{}': {}", accountNumber, e.getMessage(), e);
               throw e; // ou tratar de outra forma
          }
      }
      ```

      O `DataInitializer` chamaria `accountService.createAccount(...)`. Isso garante que, mesmo com concorrência na
      inicialização, uma única conta será criada e a outra tentativa resultará em uma exceção tratada de forma elegante.

9. **Adicionar Métricas e Monitoramento:**
    * **Problema:** A aplicação base não tem métricas de negócio (ex: total de transações processadas, tempo médio de
      processamento de transação por tipo, contagem de erros por tipo de erro). Embora o Actuator esteja presente, as
      métricas de negócio precisam ser adicionadas.
    * **Melhoria:** Instrumentar o código para coletar métricas relevantes usando Micrometer (integrado ao Spring Boot
      Actuator).
    * **Como fazer:** Injetar um `MeterRegistry` e usar objetos como `Counter` (para contagem de transações por tipo) e
      `Timer` (para medir o tempo de processamento no `SingleTransactionProcessor`).
    * **Benefício:** Essencial para monitorar a saúde e performance da aplicação em produção, identificar gargalos e
      entender o volume de operações.

10. **Implementar Idempotência para Requisições de Lote/Transações:**
    * **Problema:** Se o cliente enviar a mesma lista de transações duas vezes devido a um erro de rede, as transações
      serão processadas duas vezes, levando a saldos incorretos.
    * **Melhoria:** Adicionar um mecanismo de idempotência.
    * **Como fazer:** O cliente enviaria um `idempotencyKey` único por requisição de lote (ou por transação individual
      dentro do lote). O servidor armazenaria esse ID e, se uma requisição com um ID já visto for recebida, ele não
      processaria a transação novamente ou retornaria o resultado da primeira vez. Isso geralmente envolve armazenar os
      IDs usados por um tempo em um cache ou banco de dados.
    * **Benefício:** Garante que múltiplas chamadas idênticas não alterem o estado do sistema mais de uma vez.

Essas são algumas das melhorias mais relevantes que poderiam ser discutidas, variando de correções na implementação
transacional a recursos adicionais e considerações de produção/escalabilidade. Ao discutir essas melhorias na
entrevista, mostre que você as identificou através da análise crítica do seu próprio código e pensando nos requisitos de
um sistema real.