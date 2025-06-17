# Entrevista

O projeto parece bem estruturado e demonstra um bom entendimento dos requisitos, especialmente a parte de concorrência.

Vamos organizar o material para sua apresentação e para as perguntas e respostas.

---

## 1. Breve Apresentação do Projeto

Okay, vamos simular como você apresentaria o projeto ao entrevistador. O ideal é ser conciso, destacar os requisitos
principais e como você os abordou.

"Olá [Nome do Entrevistador/Equipe], obrigado pela oportunidade. O desafio proposto foi o desenvolvimento de uma API
RESTful para gerenciar lançamentos de débito e crédito em contas bancárias, com a crucial exigência de ser thread-safe
para lidar com requisições concorrentes e evitar condições de corrida, garantindo a consistência dos dados.

Minha solução implementa dois endpoints principais:

1. `POST /api/v1/accounts/transactions`: Para realizar lançamentos. Ele aceita uma lista de transações (débito ou
   crédito) para diferentes contas ou a mesma conta.
2. `GET /api/v1/accounts/{accountNumber}/balance`: Para obter o saldo atual de uma conta específica.

Para atender aos requisitos de **thread-safety**, **consistência dos dados** e evitar **condições de corrida** em
cenários de **requisições concorrentes** que acessam o mesmo recurso compartilhado (o saldo da conta), utilizei *
*Pessimistic Locking** (Bloqueio Pessimista) no nível do banco de dados, gerenciado pelo Spring Data JPA e Hibernate.

A arquitetura segue princípios de **Camadas** e ports & adapters, dividindo o código em `domain`, `application`, e
`infrastructure`. O core da lógica de negócio reside na camada `domain` e `application`, enquanto a `infrastructure`
lida com detalhes técnicos como acesso a banco, API REST e tratamento de erros.

Utilizei Spring Boot para agilizar o desenvolvimento, Spring Data JPA/Hibernate para persistência e ORM, MapStruct para
mapeamento DTO-Entity, Lombok para boilerplate code, e SpringDoc para documentação da API (Swagger UI). Para a base de
dados neste desafio, optei pelo H2 em memória pela facilidade de configuração e startup rápido, com
`ddl-auto: create-drop` e um `CommandLineRunner` para inicializar algumas contas.

Implementei testes unitários para a camada de `application` (Use Case), focando na lógica de negócio e interações com as
portas.

Acredito que essa abordagem atende aos requisitos do desafio, garantindo a confiabilidade das transações mesmo sob carga
concorrente."

*(Nota: Este é um script. Adapte conforme a conversa fluir e se sinta confortável em explicar cada ponto.)*

---

## 2. Explicação Detalhada das Escolhas (Conceitos + Código)

Aqui, você aprofunda os pontos da apresentação, conectando os conceitos de concorrência diretamente com o código que
você escreveu.

### Arquitetura em Camadas (`domain`, `application`, `infrastructure`)

* **Motivo:** Promover a separação de interesses (Separation of Concerns). A lógica de negócio (`domain` e
  `application`) é independente da tecnologia de persistência ou do protocolo de comunicação (`infrastructure`). Isso
  torna o código mais modular, testável e fácil de manter.
* `domain`: Contém as entidades (`Account`), exceptions específicas do domínio e a lógica de negócio básica (
  `AccountOperationService`). É o coração da aplicação, sem dependências externas.
* `application`: Contém os casos de uso (`AccountService`) e as definições das portas (`AccountServicePort`,
  `AccountRepositoryPort`). Implementa a orquestração das operações de negócio. O `SingleTransactionProcessor` encapsula
  a lógica de processamento de uma *única* transação para *uma* conta, utilizando o `AccountRepositoryPort` e o
  `AccountOperationService`.
* `infrastructure`: Implementa os adaptadores para as portas (`AccountJpaAdapter`, `AccountController`), lida com
  detalhes como acesso ao banco de dados (JPA Repository), comunicação HTTP (Spring MVC Controller), DTOs, mappers e
  tratamento de erros globais.
* **Conexão com o Desafio:** Essa estrutura facilita a aplicação de lógica de concorrência no lugar certo (`application`
  orquestrando a busca com lock via `infrastructure`, e `domain` contendo a operação atômica no saldo).

### Tecnologias/Frameworks (Spring Boot, JPA, Hibernate, H2, MapStruct, Lombok, SpringDoc)

* **Spring Boot:** Escolhido para rapididade no setup e desenvolvimento. Oferece auto-configuração, gerencia
  dependências, possui servidor web embarcado e facilita a criação de APIs REST.
* **Spring Data JPA / Hibernate:** Padrão de mercado para persistência em Java. Simplifica o acesso a dados, o
  mapeamento ORM e gerencia transações declarativas (`@Transactional`). Essencial para a implementação do locking.
* **H2 Database:** Banco de dados em memória. Ideal para desafios e desenvolvimento local pela facilidade e startup
  rápido. Para um ambiente de produção, seria substituído por um DB robusto como PostgreSQL, MySQL, Oracle, etc. O
  locking pessimista funcionaria de forma similar (com nuances na implementação de lock de cada DB).
* **MapStruct:** Reduz o boilerplate code de mapeamento entre Entities e DTOs, tornando o código mais limpo e menos
  propenso a erros manuais.
* **Lombok:** Anotações para reduzir código repetitivo como getters, setters, construtores, etc. Melhora a legibilidade.
* **SpringDoc (Swagger UI):** Essencial para documentação da API. Facilita para outros desenvolvedores (ou testadores)
  entenderem como usar a API e quais são os contratos (endpoints, requests, responses).

### Implementação do Código (com foco nos conceitos de concorrência)

* **Requisições Concorrentes:** O servidor web embarcado (como Tomcat, padrão do Spring Boot) lida com múltiplas
  requisições simultâneas através de um pool de threads. Cada requisição é processada por uma thread distinta.
* **Compartilhamento entre Threads:** O principal dado compartilhado por essas threads que pode levar a problemas é o
  estado persistido no banco de dados, especificamente o saldo (`balance`) da entidade `Account`. Múltiplas threads
  podem tentar ler, modificar e salvar o *mesmo* registro de conta ao mesmo tempo.
* **Condições de Corrida:** Se duas threads (lidando com requisições concorrentes) lerem o saldo de uma conta (
  `saldo = 100`), ambas decidirem realizar uma operação (`thread A: crédito de 50; thread B: débito de 30`), e depois
  tentarem salvar o resultado *sem coordenação*, pode ocorrer uma condição de corrida. Ex: Thread A lê 100, calcula 150.
  Thread B lê 100, calcula 70. Thread A salva 150. Thread B salva 70 (o 150 é perdido). O saldo final deveria ser 100 +
  50 - 30 = 120, mas ficaria 70.
* **Thread-Safe:** O código é considerado thread-safe *em relação ao acesso ao saldo da conta* porque a lógica de
  modificação (read-modify-write) é protegida por um mecanismo que garante que apenas uma thread por vez possa
  executá-la para uma *determinada conta*.
* **Consistência dos Dados:** A garantia de consistência (o saldo final estar correto após múltiplas operações
  concorrentes) é atingida combinando **transações de banco de dados** e **bloqueio pessimista**.
* **Como Funciona no Código (`@Transactional`, `@Lock(LockModeType.PESSIMISTIC_WRITE)`):**
    * O método `performTransactions` no `AccountService` é anotado com `@Transactional`. Isso significa que o
      Spring/Hibernate inicia uma transação de banco de dados no início do método e a commita ou rola de volta no final.
    * Dentro deste método, cada `TransactionRequest` é processada individualmente pelo
      `singleTransactionProcessor.process(transaction)`. Embora o `performTransactions` seja uma única transação
      *lógica* em um contexto mais amplo (poderia ser usado para rollback se algo falhasse *depois* de alguns
      processamentos individuais), a parte crucial do bloqueio acontece *dentro* da chamada
      `accountRepositoryPort.findByAccountNumberWithLock(accountNumber)` feita pelo `SingleTransactionProcessor`.
    * O método `findByAccountNumberWithLock` no `AccountJpaAdapter` é anotado com `@Transactional` e
      `@Lock(LockModeType.PESSIMISTIC_WRITE)`. Isso instrui o Hibernate a adquirir um **bloqueio de escrita pessimista**
      na linha da tabela `accounts` correspondente ao `accountNumber` solicitado *no momento em que a conta é lida*.
    * Um bloqueio de escrita pessimista impede que *outras* transações leiam ou escrevam naquela mesma linha até que a
      transação atual (a que adquiriu o lock) seja concluída (commit ou rollback).
    * Portanto, se múltiplas requisições concorrentes tentarem processar transações para a *mesma* conta, elas serão
      serializadas no ponto onde tentam adquirir o lock. A primeira a chegar obtém o lock, processa a transação, salva a
      conta e commita (ou rola de volta) sua transação interna/o lock é liberado. A próxima thread na fila espera pelo
      lock e só continua depois que ele é liberado. Isso garante que a operação read-modify-write no saldo daquela conta
      específica seja atômica em relação a outras operações concorrentes na mesma conta.
    * A escolha do `LockModeType.PESSIMISTIC_WRITE` foi intencional para garantir que a conta não possa ser lida por
      outras transações com intenção de escrita (WRITE lock) enquanto estiver sendo processada, prevenindo precisamente
      as condições de corrida no saldo.

### Testes

* **Motivo:** Garantir que a lógica implementada funciona conforme o esperado, cobrir casos de sucesso, falha (conta não
  encontrada, saldo insuficiente) e validar as interações entre os componentes (ports).
* **Cobertura:** Os testes unitários na camada `application.usecase` (`AccountServiceTest`) validam o fluxo principal de
  processamento de lote e busca de saldo, mockando as dependências (`AccountRepositoryPort`,
  `SingleTransactionProcessor`). Isso garante que o `AccountService` se comporta corretamente em relação às suas
  dependências. O teste do `SingleTransactionProcessor` (embora não incluído, seria vital) validaria a lógica
  individual, incluindo a aplicação das operações de débito/crédito e a interação com o lock.
* **Faltando/Melhorias:** Seriam importantes testes de integração (testando a camada `infrastructure` e a interação real
  com o banco de dados) e, crucialmente para este desafio, **testes de concorrência** que simulem múltiplas threads
  acessando as mesmas contas simultaneamente para verificar se o locking funciona corretamente e a consistência é
  mantida.

### Tratamento de Erros (`GlobalExceptionHandler`)

* **Motivo:** Fornecer respostas de erro padronizadas e informativas para o cliente da API, mapeando exceções internas
  para status HTTP apropriados (ex: 404 para conta não encontrada, 409 para saldo insuficiente ou conflito de dados, 400
  para validação, 500 para erro interno). Melhora a usabilidade da API.

---

## 3. Perguntas e Respostas para a Entrevista Técnica

Aqui está uma lista abrangente de perguntas, com respostas detalhadas que incorporam os conceitos de concorrência e
explicam suas escolhas de código.

### Perguntas Gerais e sobre o Projeto

1. **P: Poderia descrever o projeto que você desenvolveu para o desafio da Matera? Qual era o objetivo principal?**
    * O projeto é uma API RESTful simples para simular lançamentos bancários de débito e crédito e consulta de saldo.
    * O objetivo principal, além da implementação básica, era garantir que as transações fossem processadas de forma
      segura e consistente, especialmente em ambientes com **requisições concorrentes**.
    * A garantia de **thread-safety** e a prevenção de **condições de corrida** no saldo das contas eram requisitos
      centrais.

2. **P: Quais endpoints você implementou e para que servem?**
    * Implementei o `POST /api/v1/accounts/transactions` que recebe uma lista de objetos representando transações (
      conta, valor, tipo) e processa cada uma.
    * Permite realizar múltiplos lançamentos em uma única chamada.
    * O outro endpoint é `GET /api/v1/accounts/{accountNumber}/balance`, que retorna o saldo atual de uma conta
      específica.

3. **P: Por que você escolheu usar Spring Boot, Spring Data JPA e Hibernate? Quais os benefícios no contexto deste
   projeto?**
    * Spring Boot foi escolhido pela sua capacidade de inicialização rápida e convenções que aceleram o desenvolvimento
      de aplicações Spring.
    * Spring Data JPA e Hibernate fornecem uma camada ORM robusta que abstrai os detalhes do acesso ao banco de dados.
    * No contexto deste projeto, eles foram essenciais para gerenciar as **transações de banco de dados** e,
      crucialmente, para implementar o **bloqueio pessimista** de forma declarativa usando anotações como
      `@Transactional` e `@Lock`.

4. **P: Você seguiu alguma arquitetura específica? Poderia explicar o rationale por trás dela?**
    * Sim, segui uma arquitetura em camadas (Domain, Application, Infrastructure) com inspiração em Ports & Adapters.
    * A ideia é separar a lógica de negócio (`domain`, `application`) das preocupações técnicas (`infrastructure`).
    * Isso torna o código mais limpo, testável e manutenível.
    * O `application` define as "ports" (interfaces `AccountServicePort`, `AccountRepositoryPort`), e a `infrastructure`
      fornece os "adapters" (Controller, JpaAdapter) que implementam essas portas, conectando o domínio ao mundo
      exterior (HTTP, Banco de Dados).

5. **P: Explique o uso de DTOs e Mappers no seu projeto.**
    * Usei DTOs (Data Transfer Objects), como `TransactionRequest` e `AccountBalanceResponse`, para definir o contrato
      de comunicação da API.
    * Eles representam os dados que entram e saem do sistema através da camada de `infrastructure` (Controller).
    * Entidades de domínio (`Account`) devem ser protegidas e não expostas diretamente na camada de apresentação ou API.
    * O MapStruct é usado para gerar automaticamente o código que mapeia entre Entidades e DTOs, reduzindo o boilerplate
      e garantindo que apenas os dados necessários sejam transferidos.

### Perguntas sobre Concorrência e Consistência

1. **P: O desafio pedia que a API fosse 'thread-safe'. O que significa thread-safe e como seu código garante isso para
   as operações de lançamento?**
    * Um código thread-safe é aquele que pode ser executado corretamente por múltiplas threads simultaneamente sem
      causar resultados inesperados, como corrupção de dados ou condições de corrida.
    * No meu código, a thread-safety para as operações de lançamento é garantida principalmente pelo uso de **bloqueio
      pessimista** no banco de dados, aplicado à entidade `Account`.
    * Quando uma thread (processando uma requisição) tenta modificar o saldo de uma conta, ela adquire um lock
      exclusivo (via `LockModeType.PESSIMISTIC_WRITE`) naquela linha específica do banco de dados antes de realizar a
      operação read-modify-write.
    * Isso impede que outras threads acessem ou modifiquem a mesma linha até que a transação atual finalize, garantindo
      que a operação no saldo seja atômica em relação a outras threads.

2. **P: O que são 'requisições concorrentes' no contexto da sua API e quais problemas elas podem causar?**
    * Requisições concorrentes ocorrem quando múltiplos clientes (ou o mesmo cliente com várias solicitações em
      paralelo) enviam requisições HTTP para a API ao mesmo tempo, e essas requisições são processadas simultaneamente
      por diferentes threads no servidor.
    * O principal problema que podem causar neste projeto é a **condição de corrida** ao acessar e modificar o mesmo
      recurso compartilhado, que é o saldo (`balance`) de uma conta bancária no banco de dados.

3. **P: Explique o conceito de 'condição de corrida' e dê um exemplo de como ela poderia acontecer no seu projeto se não
   houvesse um mecanismo de controle de concorrência.**
    * Uma condição de corrida é uma situação em que o resultado de uma operação depende da ordem ou do *timing*
      específico em que múltiplas threads acessam e modificam um recurso compartilhado. Como mencionei antes, no meu
      projeto, sem bloqueio, se duas requisições (processadas por threads diferentes) tentassem creditar/debitar na
      *mesma* conta ao mesmo tempo:
        * Ambas as threads leriam o saldo inicial da conta (ex: 100).
        * Ambas calculariam o novo saldo independentemente (Thread A: +50 -> 150; Thread B: -30 -> 70).
    * A thread que salvar por último sobrescreveria o trabalho da outra. Se a Thread B salvar por último, o saldo final
      seria 70, quando o correto seria 100 + 50 - 30 = 120. Isso é uma **condição de corrida** resultando em uma **perda
      de atualização** e **inconsistência dos dados**.

4. **P: Como você garantiu a 'consistência dos dados' do saldo da conta sob carga concorrente? Detalhe o mecanismo
   utilizado no código.**
    * A consistência dos dados é garantida através da combinação de **transações de banco de dados** e **bloqueio
      pessimista**.
        * O `@Transactional` garante a atomicidade da operação no banco: ou todas as mudanças dentro do método
          transacional são salvas (commit), ou nenhuma é (rollback).
        * O `@Lock(LockModeType.PESSIMISTIC_WRITE)` na busca da conta (`findByAccountNumberWithLock`) é o mecanismo que
          impede a condição de corrida. Ele adquire um lock exclusivo na linha da conta no banco de dados. Qualquer
          outra transação que tentar ler ou escrever nesta mesma linha terá que esperar o lock ser liberado. Isso
          serializa o acesso ao saldo para uma conta específica, garantindo que cada operação de read-modify-write (ler
          saldo, calcular novo saldo, salvar saldo) seja executada de forma isolada para aquela conta, mesmo que
          múltiplas requisições a estejam acessando concorrentemente. Isso previne perdas de atualização e garante que o
          saldo final sempre reflita corretamente a soma de todas as operações aplicadas.

5. **P: O que você quer dizer com 'compartilhamento entre threads' neste contexto?**
    * No contexto desta API, o 'compartilhamento entre threads' refere-se principalmente ao acesso e modificação do
      estado persistido no banco de dados, especificamente as linhas da tabela `accounts`.
    * Várias threads que estão processando diferentes requisições HTTP podem, ao mesmo tempo, precisar ler ou atualizar
      o saldo da *mesma* conta. O desafio reside em gerenciar esse acesso compartilhado para evitar inconsistências.
    * A solução implementada gerencia esse compartilhamento através do bloqueio pessimista no nível do banco de dados,
      tratando cada linha de conta como um recurso compartilhado que necessita de acesso coordenado.

6. **P: Você considerou outras estratégias para lidar com a concorrência, como bloqueio otimista ou sincronização em
   memória? Por que escolheu o bloqueio pessimista no banco de dados?**
    * Sim, outras estratégias existem.
        * **Bloqueio Otimista:** Envolve verificar uma versão do registro (ou um checksum) antes de salvar, e falhar se
          a versão lida for diferente da versão atual no banco. É útil quando a probabilidade de conflito é baixa, pois
          não trava o acesso. Poderia ser implementado com `@Version` no JPA. No entanto, para operações financeiras
          onde a **consistência forte** é crítica e a reversão (rollback) de uma transação falha por conflito otimista
          pode ser complexa ou indesejada, o bloqueio pessimista que *impede* o conflito desde o início costuma ser mais
          adequado.
        * **Sincronização em Memória (ex: `synchronized` em um serviço singleton):** Poderia ser usada para proteger o
          acesso a estruturas de dados em memória, mas não é eficaz para proteger um estado que está sendo persistido no
          banco de dados e acessado por múltiplas instâncias da aplicação (se rodasse em cluster) ou até mesmo por
          outros processos acessando o mesmo banco. O estado *verdadeiro* está no DB.
    * **Escolha do Bloqueio Pessimista:** Escolhi o bloqueio pessimista no banco de dados (`PESSIMISTIC_WRITE`) porque
      ele oferece a garantia mais forte de **consistência imediata** e evita ativamente as **condições de corrida** no
      ponto de acesso ao dado persistido. Para transações financeiras, onde cada centavo importa e não se pode perder
      atualizações, essa garantia é fundamental. Ele serializa o acesso à linha da conta no DB, garantindo que a lógica
      de negócio (aplicar débito/crédito) opere sobre o estado mais atual da conta, sem o risco de ser afetada por
      operações concorrentes na mesma conta durante o processamento.

7. **P: O método `performTransactions` recebe uma lista de transações. Cada transação nesta lista será processada dentro
   da mesma transação de banco de dados ou em transações separadas? Qual a implicação para a consistência?**
    * **R:** No código atual, o método `performTransactions` está anotado com `@Transactional`, o que sugere uma única
      transação de banco de dados para o *lote inteiro*. No entanto, a implementação do `SingleTransactionProcessor`
      busca a conta *com lock* (`findByAccountNumberWithLock`) que está *também* anotado com `@Transactional`. Por
      padrão no Spring, uma chamada de um método `@Transactional` para *outro* método `@Transactional` *dentro da mesma
      classe* não inicia uma nova transação (o contexto transacional é propagado, a menos que se use
      `Propagation.REQUIRES_NEW`). No entanto, a chamada é feita de `AccountService` para `SingleTransactionProcessor`.
      **Revisando o código:** A busca com lock está no `AccountJpaAdapter`, que é um `@Component` separado. Chamar um
      `@Transactional` (`findByAccountNumberWithLock` no `AccountJpaAdapter`) de dentro de outro `@Transactional` (
      `performTransactions` no `AccountService`) *inicia sim* uma nova transação com a propagação padrão (`REQUIRED`). *
      *Portanto, cada chamada ao `singleTransactionProcessor.process` (que
      chama `accountRepositoryPort.findByAccountNumberWithLock`) está acontecendo DENTRO da transação externa iniciada
      pelo `performTransactions`, mas a *busca com lock* pode ter nuances dependendo do provedor JPA e se o lock
      realmente é aplicado e liberado por cada chamada individual ou mantido até o fim da transação externa.** A
      intenção clara do código é que o lock seja adquirido *para cada transação individual* dentro do lote. Uma forma
      mais explícita de garantir isso seria:
        * Remover o `@Transactional` de `performTransactions`.
        * Adicionar `@Transactional` ao `SingleTransactionProcessor.process`.
        * Manter o `@Transactional` e `@Lock` no `AccountJpaAdapter.findByAccountNumberWithLock`.
        * *Explicação da implicação:* Com o `@Transactional` no `performTransactions` e no
          `findByAccountNumberWithLock`, cada transação individual no lote tenta obter o lock. Se uma falhar (saldo
          insuficiente), a exceção é lançada. Se o `@Transactional` estivesse apenas no
          `SingleTransactionProcessor.process`, cada transação do lote seria sua própria transação de banco de dados
          atômica e com seu próprio lock lifecycle. A abordagem atual com o `@Transactional` externo significa que se o
          processamento de uma transação no meio do lote falhar *com uma exceção não capturada* dentro do loop (como
          `InsufficientFundsException` que não é capturada e relançada como `TransactionProcessingException`), a
          transação **externa** falhará, potencialmente realizando rollback das transações *bem-sucedidas* que a
          precederam no lote, dependendo da configuração de rollback e das exceções. Se a intenção é que cada transação
          no lote seja independente e commitada/rollback individualmente, o `@Transactional` deveria estar no
          `SingleTransactionProcessor.process`. *Baseado no código fornecido, parece que a intenção era que cada item da
          lista fosse processado em sua própria sub-transação gerenciada pelo `SingleTransactionProcessor.process`,
          potencialmente utilizando o lock dentro dessa sub-transação.*

8. **P: O que acontece se duas requisições tentarem realizar transações (débito ou crédito) na mesma conta exatamente ao
   mesmo tempo?**
    * **R:** Elas tentarão processar a transação, o que envolverá buscar a conta com bloqueio (
      `accountRepositoryPort.findByAccountNumberWithLock`). A primeira requisição a alcançar essa linha no banco de
      dados obterá o **bloqueio de escrita pessimista**. A segunda requisição (e quaisquer outras) que tentarem obter o
      lock para a mesma conta ficarão **bloqueadas** e esperarão que o primeiro lock seja liberado. O
      `hibernate.jakarta.persistence.lock.timeout` configurado (5000ms) define o tempo máximo de espera pelo lock antes
      que uma exceção (`LockTimeoutException`) seja lançada. Uma vez que a primeira transação conclua (commit ou
      rollback) e libere o lock, a próxima requisição na fila obterá o lock e continuará seu processamento. Isso garante
      que, para uma dada conta, as operações de read-modify-write no saldo sejam serializadas.

9. **P: E se uma das transações em um lote falhar (por exemplo, saldo insuficiente)? O que acontece com as outras
   transações no mesmo lote?**
    * **R:** No código atual, a exceção (como `InsufficientFundsException`) é capturada no `SingleTransactionProcessor`
      e re-lançada como `TransactionProcessingException`. Esta exceção, por ser uma `RuntimeException`, se não for
      tratada de forma específica no `AccountService`, fará com que o `@Transactional` do método `performTransactions`
      seja marcado para rollback. Portanto, se uma transação falhar, a **transação externa completa** será desfeita.
      Isso significa que quaisquer transações *bem-sucedidas* que já haviam sido processadas e salvas *anteriormente no
      mesmo lote*, mas dentro da *mesma transação externa*, serão revertidas. Se a intenção era que cada transação do
      lote fosse independente, o `@Transactional` deveria estar no `SingleTransactionProcessor.process` e não no
      `AccountService.performTransactions`. *(Seja honesto sobre isso e diga que, ao revisar para a entrevista, notou
      essa nuance e qual seria a alternativa para transações independentes no lote).*

10. **P: Como o H2 lida com `PESSIMISTIC_WRITE`? É um comportamento confiável para ambientes de produção?**
    * **R:** O H2, por ser um banco de dados em memória simples, simula o comportamento de bloqueio, mas sua
      implementação pode não ser tão robusta ou eficiente quanto a de bancos de dados de produção (PostgreSQL, MySQL,
      Oracle) sob alta carga concorrente real. Para um desafio ou desenvolvimento local, ele é adequado. Em produção, um
      banco de dados mais robusto com um gerenciador de locks otimizado seria essencial para garantir a escalabilidade e
      o comportamento correto do bloqueio pessimista em larga escala. A configuração `DB_CLOSE_DELAY=-1` é importante
      para que o banco não feche imediatamente após a última conexão, útil para testes interativos ou inicialização de
      dados.

### Perguntas sobre Código e Boas Práticas

1. **P: Por que você usou `BigDecimal` para o saldo e os valores das transações?**
    * **R:** `BigDecimal` é a classe recomendada em Java para lidar com valores monetários e cálculos financeiros.
      Diferente de `double` ou `float`, `BigDecimal` representa números decimais de forma exata, evitando problemas de
      precisão de ponto flutuante que podem levar a erros de arredondamento em cálculos financeiros.

2. **P: Como você lidou com a validação dos dados de entrada na API?**
    * **R:** Usei a API de validação do Bean Validation (`jakarta.validation`) junto com o Spring (
      `spring-boot-starter-validation`). Anotações como `@NotBlank`, `@NotNull`, `@DecimalMin` foram usadas nos DTOs (
      `TransactionRequest`). A anotação `@Valid` no parâmetro do controller (`performTransactions`) dispara essa
      validação automaticamente. O `GlobalExceptionHandler` captura o `MethodArgumentNotValidException` e retorna uma
      resposta de erro 400 formatada, informando quais campos falharam na validação.

3. **P: Explique o uso de `@RestControllerAdvice` e `@ExceptionHandler`.**
    * **R:** `@RestControllerAdvice` é uma anotação Spring que permite centralizar o tratamento de exceções para
      múltiplos controllers em um único lugar. `@ExceptionHandler` é usado dentro de uma classe anotada com
      `@RestControllerAdvice` (ou `@ControllerAdvice`) para definir métodos que tratam tipos específicos de exceções (
      ex: `AccountNotFoundException`, `InsufficientFundsException`). Isso mantém o código dos controllers limpo,
      separando a lógica de negócio do tratamento de erros, e garante uma resposta de erro consistente em toda a API.

4. **P: Por que você usou `@Component` e `@Service`? Qual a diferença?**
    * **R:** `@Component` é a anotação genérica para qualquer classe gerenciada pelo Spring IoC container. `@Service` é
      uma especialização de `@Component` que semanticamente indica que a classe contém lógica de negócio. Em termos
      técnicos, para o Spring, não há muita diferença na injeção de dependência básica, mas usar `@Service` melhora a
      clareza do código e pode ser usado por ferramentas ou configurações específicas do Spring (como para AOP ou
      transações, embora `@Transactional` funcione em `@Component` também). `AccountService` e `AccountOperationService`
      são `@Service` ou `@Component` porque contêm a lógica de negócio. `AccountJpaAdapter` é um `@Component` por ser um
      adapter, mas também poderia ser `@Repository` (outra especialização de `@Component` para classes que acessam
      dados).

5. **P: Como você inicializou os dados das contas no banco H2?**
    * **R:** Usei uma classe `DataInitializer` anotada com `@Component` e implementando `CommandLineRunner`. Esta classe
      é executada uma vez que a aplicação Spring Boot inicia. Nela, eu chamo um método (`createAccountIfNotFound`) no
      `AccountService` para criar algumas contas predefinidas, apenas se elas ainda não existirem no banco de dados.
      Isso facilita testar a API com dados iniciais conhecidos. A anotação `@Profile("!test")` garante que essa
      inicialização não ocorra durante a execução dos testes unitários.

6. **P: Quais tipos de testes você escreveu? Você acha que a cobertura é suficiente para este desafio? Que outros testes
   adicionaria?**
    * **R:** Escrevi testes unitários para a camada de `application` (`AccountServiceTest`) usando JUnit 5 e Mockito.
      Esses testes verificam a lógica do serviço, como o processamento de lotes e a busca de saldo, mockando as
      dependências (o repositório e o processador individual). Eles cobrem casos de sucesso e alguns casos de falha (
      conta não encontrada, lista vazia).
    * A cobertura atual valida a orquestração na camada de serviço, mas não a lógica de processamento individual de
      transação ou a interação real com o banco e o locking.
    * Para uma solução completa, eu definitivamente adicionaria:
        * **Testes unitários para `SingleTransactionProcessor`:** Para testar a lógica de aplicar débito/crédito,
          validação de saldo insuficiente, e como ele interage com o repositório (incluindo a chamada ao método com
          lock).
        * **Testes de integração:** Para testar o fluxo completo da API, do controller ao banco de dados, garantindo que
          Spring, JPA, Hibernate e o H2 (ou outro DB) funcionam juntos corretamente.
        * **Testes de concorrência (Cruciais!):** Testes que simulam várias threads chamando o endpoint `/transactions`
          simultaneamente, direcionando operações para as *mesmas* contas. Esses testes seriam a validação final de que
          o mecanismo de locking e a consistência de dados funcionam sob carga real. Poderiam ser implementados usando
          `ExecutorService` ou ferramentas de teste de carga.

7. **P: O código usa `LockModeType.PESSIMISTIC_WRITE`. O que isso significa e qual sua diferença
   para `PESSIMISTIC_READ`?**
    * **R:** `PESSIMISTIC_WRITE` adquire um lock que impede que *outras* transações leiam ou escrevam na linha bloqueada
      até que o lock seja liberado. É o tipo mais restritivo, garantindo exclusividade total para a transação que detém
      o lock sobre aquela linha.
    * `PESSIMISTIC_READ` adquire um lock que impede que *outras* transações *escrevam* na linha bloqueada, mas permite
      que outras transações *leiam* a linha (possivelmente também com um lock de leitura). É usado para garantir que os
      dados lidos não mudem durante a transação, mas permite leituras simultâneas.
    * Para a operação de débito/crédito (read-modify-write), `PESSIMISTIC_WRITE` é necessário porque precisamos garantir
      que o saldo lido não seja alterado por ninguém *antes* de calcularmos o novo saldo e o salvarmos.
      `PESSIMISTIC_READ` não seria suficiente, pois permitiria que outra thread lesse o mesmo saldo inicial antes de
      você completar sua operação de escrita.

### Perguntas sobre o Futuro ou Melhorias

1. **P: Como você lidaria com um grande volume de transações ou requisições concorrentes em um sistema real, talvez
   distribuído (em cluster)? O Pessimistic Locking no DB seria suficiente?**
    * **R:** O bloqueio pessimista no banco de dados é eficaz para garantir a consistência **em um único banco de dados
      **, mesmo com várias instâncias da aplicação acessando-o. No entanto, sob *carga muito alta* ou com *contas que
      são alvo frequente de concorrência*, o bloqueio pode se tornar um gargalo de performance, pois as requisições
      ficam esperando pelos locks.
    * Em um sistema distribuído de alta escala:
        * A arquitetura de microserviços ou baseada em eventos pode ser considerada, talvez com um serviço de Conta
          dedicado.
        * Alternativas ao bloqueio de DB podem ser exploradas para certos casos, como bloqueio otimista se a chance de
          colisão for baixa, ou abordagens assíncronas/orientadas a eventos onde as transações são enfileiradas e
          processadas sequencialmente por conta.
        * Tecnologias como filas de mensagens (Kafka, RabbitMQ) poderiam ser usadas para desacoplar a recepção da
          requisição do processamento da transação, permitindo um processamento mais controlado e serializado por conta.
        * Soluções de caching distribuído com suporte a locks (como Redis com Redlock) poderiam ser usadas para locks de
          curta duração.
        * Uma estratégia comum é usar o bloqueio pessimista para a consistência imediata, mas monitorar performance e
          considerar estratégias de scaling vertical (banco de dados mais poderoso) ou horizontal (sharding de contas se
          possível) para o banco de dados.
        * A escolha depende dos requisitos de latência, volume e nível de consistência necessário. Para consistência
          forte em operações financeiras críticas, bloqueio de DB (pessimista ou otimista robusto com retry) ainda é
          comum.

2. **P: Se o requisito fosse que as transações dentro de um lote (`POST /transactions`) devessem ser processadas de
   forma independente (ou seja, se uma falhar, as outras continuam), como você mudaria a implementação?**
    * **R:** Como mencionei anteriormente, eu removeria a anotação `@Transactional` do método
      `AccountService.performTransactions`. Em vez disso, eu adicionaria `@Transactional` ao método
      `SingleTransactionProcessor.process`. Cada chamada a `process` se tornaria sua própria transação de banco de dados
      atômica. Se uma chamada a `process` lançasse uma exceção, apenas aquela transação individual faria rollback, e o
      loop no `performTransactions` poderia (idealmente com um bloco try-catch ao redor da chamada a `process`)
      continuar a processar as transações restantes no lote. Eu também adicionaria alguma forma de reportar quais
      transações falharam.

3. **P: Você incluiu um Dockerfile e docker-compose.yml. Qual a vantagem de containerizar a aplicação neste caso?**
    * **R:** Containerizar a aplicação usando Docker e Docker Compose oferece portabilidade e consistência. Garante que
      a aplicação rodará da mesma forma em qualquer ambiente (desenvolvimento, teste, produção) que tenha Docker
      instalado, resolvendo o problema "funciona na minha máquina". Facilita o deploy, o scaling e a orquestração da
      aplicação, especialmente em ambientes de nuvem ou com múltiplas dependências. Para este desafio, permite que
      qualquer pessoa execute a API e o banco de dados H2 embarcado facilmente com um simples `docker compose up`.

---

### Dicas Finais para a Entrevista

* **Pratique:** Leia as perguntas e respostas várias vezes. Tente explicá-las em voz alta sem ler.
* **Seja Confiante:** Você implementou a solução, você a conhece melhor do que ninguém.
* **Seja Honesto:** Se não souber algo, diga que não tem certeza, mas demonstre vontade de aprender ou raciocine sobre
  como abordaria a questão. No caso da nuance do `@Transactional` no lote, ser transparente sobre a observação e a
  alternativa demonstra atenção aos detalhes e capacidade de aprendizado.
* **Use o Código:** Quando explicar a concorrência, aponte para as linhas de código relevantes (`@Lock`,
  `@Transactional`, `findByAccountNumberWithLock`). Isso mostra que você entende a implementação prática.
* **Faça Perguntas:** No final, faça perguntas sobre a equipe, a cultura, os desafios da vaga. Demonstra interesse.
* **Revise os Conceitos Básicos:** Certifique-se de que está confortável com os pilares de Java (OOP, Collections,
  Threads básicas), Spring (DI, IoC container, MVC), JPA (Entidades, Repositórios, Transações).

Boa sorte na sua entrevista! Você tem uma base sólida com essa implementação.

---

Excelente iniciativa! Fazer perguntas ao entrevistador demonstra interesse, proatividade e que você está avaliando a
vaga e a empresa tanto quanto eles estão te avaliando. Preparei uma lista dividida por temas, com sugestões de perguntas
que você pode fazer ao final da entrevista técnica (ou quando a oportunidade surgir). Escolha aquelas que *genuinamente*
te interessam.

**Dica:** Tente relacionar suas perguntas com o desafio ou com a discussão que tiveram. Isso mostra que você estava
atento e está pensando sobre o contexto real.

---

**Perguntas Sobre a Vaga e o Dia a Dia:**

1. **Como seria um dia típico para um Analista de Desenvolvimento Java Pleno nesta equipe?**
    * *Por que perguntar:* Ajuda a entender as responsabilidades diárias e o tipo de trabalho que você faria.
2. **Quais são os principais desafios técnicos que a equipe ou esta vaga enfrentam atualmente?**
    * *Por que perguntar:* Mostra que você se interessa pelos problemas a serem resolvidos e pode dar uma ideia da
      complexidade e do ritmo do trabalho.
3. **Em quais tipos de projetos ou sistemas eu estaria trabalhando primariamente?**
    * *Por que perguntar:* Ajuda a alinhar suas expectativas com a realidade do trabalho.
4. **Qual é a proporção entre desenvolvimento de novas features e manutenção/refatoração em projetos existentes?**
    * *Por que perguntar:* Esclarece se a vaga é mais focada em construir do zero ou em evoluir sistemas legados.

**Perguntas Sobre a Equipe:**

1. **Como a equipe é estruturada? Quantas pessoas, quais papéis (desenvolvedores, QAs, POs)?**
    * *Por que perguntar:* Ajuda a entender a dinâmica de trabalho e com quem você interagiria.
2. **Como a equipe colabora no dia a dia? (Ex: Pair programming, revisões de código, daily meetings?)**
    * *Por que perguntar:* Entender a cultura de colaboração é importante para ver se o fit é bom.
3. **Qual o nível de senioridade e experiência na equipe com as tecnologias que utilizamos (Java, Spring, SQL)?**
    * *Por que perguntar:* Dá uma ideia do potencial de aprendizado e mentoria dentro da equipe.
4. **Como a equipe lida com o compartilhamento de conhecimento e aprendizado contínuo?**
    * *Por que perguntar:* Mostra interesse em crescimento profissional e nas práticas da equipe.

**Perguntas Sobre Tecnologia e Arquitetura (Relacionando ao Desafio):**

1. **Em sistemas de produção na Matera que lidam com alto volume de transações concorrentes, que estratégias são
   geralmente utilizadas para garantir a consistência dos dados? (Além do Locking no DB, consideram Filas, Event
   Sourcing, etc.?)**
    * *Por que perguntar:* Esta pergunta é excelente e diretamente relacionada ao coração do desafio (concorrência,
      consistência). Mostra que você pensou na escalabilidade e nas soluções do mundo real.
2. **Que ferramentas ou práticas de monitoramento de performance e de concorrência são utilizadas para identificar e
   diagnosticar gargalos ou condições de corrida em produção?**
    * *Por que perguntar:* Complementa a pergunta anterior e mostra preocupação com a operação dos sistemas.
3. **Qual a stack tecnológica principal que a equipe utiliza em produção (Banco de Dados, Cloud, Ferramentas de CI/CD,
   etc.)?**
    * *Por que perguntar:* Ajuda a entender o ambiente técnico real e se alinha com seus interesses e experiências.
4. **Como vocês abordam a evolução da arquitetura e a gestão de débito técnico nos projetos?**
    * *Por que perguntar:* Mostra preocupação com a saúde de longo prazo dos sistemas.

**Perguntas Sobre a Cultura e o Crescimento:**

1. **Quais as oportunidades de crescimento e desenvolvimento profissional dentro da Matera para um desenvolvedor pleno?
   **
    * *Por que perguntar:* Demonstra ambição e interesse em construir uma carreira na empresa.
2. **Como a Matera incentiva a inovação e a experimentação com novas tecnologias ou abordagens?**
    * *Por que perguntar:* Ótima pergunta se você é curioso e gosta de explorar novas ideias.
3. **Qual a cultura da equipe em relação a feedbacks e melhoria contínua?**
    * *Por que perguntar:* Entender como a equipe lida com feedbacks pode indicar um ambiente de trabalho saudável e
      focado em crescimento.

**Perguntas Sobre o Processo e Próximos Passos:**

1. **Qual o próximo passo no processo seletivo após esta etapa?**
    * *Por que perguntar:* Essencial para saber o que esperar.
2. **Qual a expectativa de cronograma para as próximas etapas?**
    * *Por que perguntar:* Ajuda a gerenciar suas próprias expectativas e prazos.
3. **Há algo mais que eu possa fornecer ou alguma dúvida que eu possa esclarecer sobre minha experiência ou sobre a
   solução do desafio?**
    * *Por que perguntar:* Oferece uma última oportunidade para reforçar seu interesse e sanar qualquer ponto pendente.

**Opcional (Se houver abertura e parecer natural):**

* **Sobre a minha solução para o desafio, houve algum ponto específico que chamou mais a atenção positivamente, ou
  alguma área que vocês consideram que poderia ser abordada de forma diferente em um ambiente de produção e que seria um
  ponto de aprendizado para mim?** (Use com cuidado, apenas se a conversa fluir para isso e você sentir que é apropriado
  pedir feedback direto de forma construtiva).

**Como usar:**

* Escolha 3 a 5 perguntas que você *realmente* quer saber a resposta.
* Anote-as (mentalmente ou em um papel, se for o caso).
* Ouça atentamente durante a entrevista, pois algumas de suas perguntas podem ser respondidas durante a conversa.
* Ao final, quando perguntarem "Você tem alguma pergunta?", diga que sim e faça suas perguntas escolhidas.

Boa sorte! Tenho certeza que você se sairá bem.

---

* Lock Mode Type = Tipo de Modo de Bloqueio
* Transactional = Transacional
* Find By Account Number With Lock = Encontrar por número de conta com bloqueio
* Accounts = Contas
* Transactions = Transações
* Account Number = Número da Conta
* Balance = Saldo
* Thread-Safety = Segurança de Thread
* Command Line Runner = Executor de linha de comando
* Single Transaction Processor = Processador de Transações Únicas
* Pessimistic Write = Escreva pessimista

---