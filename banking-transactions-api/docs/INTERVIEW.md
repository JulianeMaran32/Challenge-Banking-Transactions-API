# Technical Interview Guide

### Resumo Inicial do Projeto

"Bom dia/tarde. O projeto que desenvolvi é a **Banking Transactions API**, que foi o desafio proposto para a vaga.

Meu objetivo foi criar uma API RESTful em **Java** com **Spring Boot** para gerenciar operações financeiras básicas:
**realizar lançamentos (débito/crédito)** em contas, suportando múltiplos lançamentos em uma única requisição, e
**consultar o saldo** de uma conta específica.

O ponto técnico central e mais desafiador foi garantir que a API fosse **thread-safe** e mantivesse a **consistência dos
dados** sob requisições concorrentes. Para isso, implementei uma estratégia de **Lock Pessimista** no nível do banco de
dados utilizando Spring Data JPA, garantindo que as operações que modificam o saldo de uma conta sejam serializadas.

A arquitetura segue um modelo em **camadas**, inspirado em Ports & Adapters, separando as preocupações entre Domínio,
Aplicação (Use Cases) e Infraestrutura (Adapters).

Utilizei também outras ferramentas e práticas como **Bean Validation** para validar a entrada, **SpringDoc OpenAPI**
para documentação automática, **Global Exception Handling** para respostas de erro consistentes, e incluí **testes
unitários**, além de **Docker** para facilitar o deploy.

Em resumo, é uma API funcional que atende aos requisitos do desafio, com foco na robustez e consistência em um ambiente
multi-threaded. Estou à disposição para explicar qualquer detalhe do código ou das escolhas de design."

## Breve Apresentação do Projeto

"Bom dia/tarde. Agradeço a oportunidade. Desenvolvi a API de Lançamentos Bancários como parte do desafio técnico
proposto.

O objetivo principal foi criar uma API RESTful que permitisse realizar operações de débito e crédito em contas bancárias
e consultar o saldo dessas contas.

Os requisitos chave incluíam:

1. Expor endpoints REST para as operações.
2. Permitir múltiplos lançamentos em uma única requisição para o endpoint de transações.
3. Consultar o saldo de uma conta.
4. Garantir que a API fosse **thread-safe** ('_THred sāf_') e consistente em ambientes de requisições concorrentes.
5. Documentar a API.
6. Incluir testes.

Utilizei **Java** com o framework **Spring Boot**, que facilita a construção de aplicações robustas e escaláveis. Para a
persistência, optei pelo **Spring Data JPA** e **Hibernate** com um banco de dados **H2 em memória** para agilidade no
desenvolvimento e teste.

A arquitetura segue o padrão de **camadas** (Controller, Service/Use Case, Repository/Adapter), com uma influência da
arquitetura **Ports & Adapters** (Hexagonal) ao definir interfaces (ports) para a camada de aplicação se comunicar com a
camada de infraestrutura (adapters), promovendo baixo acoplamento.

O maior desafio técnico foi garantir a **thread-safety** para operações que modificam o saldo da conta sob concorrência.
Adotei a estratégia de **Lock Pessimista** no nível do banco de dados, utilizando a anotação
`@Lock(LockModeType.PESSIMISTIC_WRITE)` do Spring Data JPA. Isso garante que, ao processar uma transação para uma conta
específica, o registro dessa conta seja bloqueado, prevenindo que outras threads o modifiquem simultaneamente e evitando
condições de corrida no cálculo do saldo.

Outras funcionalidades implementadas que agregam valor incluem:

* **Tratamento Centralizado de Exceções:** Utilizando `@RestControllerAdvice` para fornecer respostas de erro
  consistentes e informativas.
* **Validação de Requisição:** Uso de Bean Validation (`@Valid`) nos DTOs para garantir a integridade dos dados de
  entrada.
* **Documentação Automática:** Integração com SpringDoc OpenAPI para gerar a documentação Swagger UI da API.
* **Inicialização de Dados:** Um `CommandLineRunner` para popular o H2 com algumas contas iniciais.
* **Containerização:** Adição de `Dockerfile` e `docker-compose.yml` para facilitar o build e execução da aplicação em
  ambiente de container.

Escrevi testes unitários para a camada de serviço (`AccountServiceTest`) para validar a lógica de negócio e o fluxo de
processamento das transações.

Em resumo, o projeto atende a todos os requisitos do desafio, com foco especial na segurança e consistência de dados em
um cenário multi-threaded, utilizando tecnologias modernas do ecossistema Java/Spring."

---

### Perguntas e Respostas para a Entrevista Técnica

**1. Pergunta:** Explique a estrutura do seu projeto e as camadas que você utilizou. Por que escolheu essa estrutura?

**Resposta:**
"Utilizei uma estrutura em camadas que se aproxima da arquitetura Ports & Adapters (ou Hexagonal). Tenho três camadas
principais:

* **Domain:** Contém as entidades (`Account`), exceções de negócio (`InsufficientFundsException`,
  `AccountNotFoundException`, etc.) e a lógica central de negócio pura (`AccountOperationService`). Esta camada é
  agnóstica a frameworks externos e detalhes de infraestrutura.
* **Application:** Contém a lógica da aplicação e os casos de uso (`AccountService`). Define as interfaces (Ports)
  `AccountServicePort` (port de entrada, API que o Controller usa) e `AccountRepositoryPort` (port de saída, API que o
  serviço usa para acessar dados). O `SingleTransactionProcessor` reside aqui, orquestrando a lógica para uma única
  transação.
* **Infrastructure:** Contém as implementações (Adapters) dos ports, como `AccountJpaAdapter` (implementação do
  `AccountRepositoryPort` usando JPA), `AccountController` (implementação do port de entrada REST), configurações (
  `OpenApiConfig`, `DataInitializer`) e DTOs/tratamento de erros.

Escolhi essa estrutura porque ela promove a **separação de preocupações**, o que torna o código mais organizado,
testável e de fácil manutenção. A camada de Application não depende da Infrastructure, o que facilita a troca de
tecnologias de persistência, por exemplo (basta criar um novo adapter que implemente `AccountRepositoryPort`). O Domain
fica protegido no centro, contendo a essência do negócio."

> Incluir a informação de que eu utilizava a Arquitetura Hexagonal nos microsserviços no dia a dia da empresa.

**2. Pergunta:** Como você garantiu a thread-safety e a consistência dos dados, especialmente no cenário de requisições
concorrentes que modificam o saldo da mesma conta?

**Resposta:**
"Este foi um ponto crítico do desafio. Para garantir a thread-safety e evitar condições de corrida no saldo das contas
sob alta concorrência, implementei um **Lock Pessimista** no nível do banco de dados.

Na camada de Infrastructure, o `AccountJpaAdapter` implementa o port `AccountRepositoryPort`. O método
`findByAccountNumberWithLock`, usado pelo `SingleTransactionProcessor` ao iniciar o processamento de uma transação que
modifica o saldo, está anotado com `@Lock(LockModeType.PESSIMISTIC_WRITE)`.

Dentro de uma transação de banco de dados (gerenciada pelo `@Transactional` no `AccountService` e também no método
`findByAccountNumberWithLock`), quando uma thread busca uma conta usando este método, o banco de dados adquire um lock
de escrita exclusivo para a linha correspondente àquela conta. Isso impede que outras threads que tentem adquirir o
mesmo tipo de lock (escrita) para a *mesma conta* prossigam até que a primeira transação seja concluída (commit ou
rollback), liberando o lock.

Dessa forma, garantimos que a operação de 'ler o saldo -> calcular novo saldo -> atualizar saldo' para uma conta
específica seja **atômica** para as threads concorrentes, prevenindo leituras obsoletas (dirty reads) ou atualizações
perdidas (lost updates)."

**3. Pergunta:** Por que Lock Pessimista? Quais seriam outras abordagens para lidar com concorrência nesse caso, e quais
as vantagens/desvantagens de cada uma?

**Resposta:**
"Escolhi o Lock Pessimista por ser uma solução **efetiva e relativamente simples** para garantir a consistência forte
dos dados em um cenário onde atualizações frequentes em registros específicos (contas) são esperadas. É uma abordagem
baseada em banco de dados, confiando no seu mecanismo de locking transacional.

Outras abordagens incluem:

* **Lock Otimista (Optimistic Locking):** Esta abordagem assume que conflitos são raros. Geralmente envolve adicionar
  uma coluna de **versão** (um timestamp ou contador) na entidade `Account`. Ao ler a conta, você lê a versão. Ao tentar
  atualizar, você verifica se a versão no banco ainda é a mesma que você leu. Se for diferente, significa que outra
  thread modificou a conta no meio tempo, e a sua operação falha (normalmente lança uma
  `ObjectOptimisticLockingFailureException` no JPA), exigindo que a operação seja retentada.
* **Vantagens:** Melhor desempenho em cenários de baixa ou moderada concorrência, pois não há bloqueio direto no banco
  até o momento da escrita. Escalabilidade potencialmente maior, pois menos threads ficam esperando por locks.
* **Desvantagens:** Requer lógica de retentativa (retry logic) na camada de aplicação. Conflitos frequentes sob alta
  concorrência podem levar a muitas falhas e retentativas, degradando a performance.
* *Aplicabilidade aqui:* Seria uma alternativa viável, especialmente se a concorrência na *mesma* conta não fosse
  esperada ser extremamente alta.

* **Filas de Mensagens / Processamento Assíncrono:** As requisições de transação poderiam ser enviadas para uma fila de
  mensagens (como Kafka, RabbitMQ). Um processador single-threaded (ou com workers que processam contas diferentes em
  paralelo, mas serializam operações na *mesma* conta) consumiria essas mensagens e aplicaria as transações
  sequencialmente para cada conta.
* **Vantagens:** Escala bem, desacopla o recebimento da requisição do processamento, ideal para picos de carga. Elimina
  a necessidade de locks de banco de dados para a concorrência entre threads na *mesma instância* da aplicação.
* **Desvantagens:** Aumenta a complexidade da arquitetura (introduz um broker de mensagens). Requer idempotência nas
  mensagens (para retentativas em caso de falha). A resposta ao usuário final seria assíncrona (202 Accepted), o que
  pode não ser ideal para todos os casos de uso.
* *Aplicabilidade aqui:* Seria uma solução mais robusta para um sistema bancário real de alta escala, mas seria
  excessivo para o escopo do desafio.

* **Sincronização em Memória (Java `synchronized`, `ReentrantLock`):** Poderia-se manter as contas em memória (talvez um
  cache) e usar mecanismos de sincronização Java para controlar o acesso.
* **Vantagens:** Muito rápido para operações em memória.
* **Desvantagens:** **Não funciona** em um ambiente distribuído com múltiplas instâncias da aplicação. O estado da conta
  estaria apenas na memória de uma instância, levando a inconsistências entre as instâncias. A persistência no banco
  ainda precisaria de um mecanismo para garantir a atomicidade ao salvar.
* *Aplicabilidade aqui:* Totalmente inadequada para uma API distribuída.

Escolhi o Lock Pessimista porque, dentro do contexto de um desafio que foca em thread-safety em uma API stateless (que
pode rodar em múltiplas instâncias futuramente) e usando um banco de dados relacional, é a solução mais direta e eficaz
para garantir a consistência forte exigida, sem introduzir a complexidade de arquiteturas assíncronas ou a necessidade
de lógica de retentativa do lock otimista (embora lock otimista seja muito comum e válido em outros cenários)."

**4. Pergunta:** Como funciona o Lock Pessimista com JPA/Hibernate? Onde e por quanto tempo o lock é mantido?

**Resposta:**
"No Spring Data JPA/Hibernate, o Lock Pessimista (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) se traduz em um comando SQL
de **SELECT ... FOR UPDATE** (ou similar, dependendo do dialeto do banco de dados).

Quando o método `accountRepositoryPort.findByAccountNumberWithLock(accountNumber)` é chamado dentro de uma transação (
`@Transactional`), o Hibernate executa um `SELECT ... FOR UPDATE` para buscar a linha da conta no banco de dados. O
banco de dados então aplica um lock exclusivo nessa linha.

Este lock é mantido **até o final da transação** que o adquiriu. Se a transação for comitada com sucesso, o lock é
liberado. Se a transação falhar e for feito rollback, o lock também é liberado. Enquanto o lock estiver ativo, outras
transações que tentem adquirir um lock de escrita na *mesma linha* (seja pessimista ou otimista) serão bloqueadas e
esperarão (ou falharão por timeout, se configurado).

No meu `application.yml`, configurei `jakarta.persistence.lock.timeout: 5000` (5 segundos) e
`org.hibernate.jpa.lock.timeout: 5000`. Isso significa que se uma thread tentar adquirir um lock em uma conta que já
está bloqueada por outra thread e tiver que esperar por mais de 5 segundos, ela receberá uma exceção (
`LockTimeoutException` ou similar, dependendo do driver/banco), evitando que a aplicação trave indefinidamente."

**5. Pergunta:** Como o seu código lida com a validação dos dados de entrada (DTOs)?

**Resposta:**
"Utilizei o padrão de **Bean Validation** (implementado pelo Hibernate Validator, incluído nas dependências do Spring
Boot Validation).

Nos DTOs de requisição, como `TransactionRequest`, adicionei anotações como `@NotBlank`, `@NotNull`, `@DecimalMin`. Por
exemplo, `@DecimalMin(value = "0.01", inclusive = false, message = "O valor da transação deve ser positivo.")` garante
que o valor da transação seja sempre maior que zero.

No `AccountController`, adicionei a anotação `@Valid` no parâmetro `@RequestBody List<TransactionRequest> transactions`.
Isso instrui o Spring a aplicar as validações definidas nos DTOs antes de chamar o método do controller.

Se a validação falhar, o Spring automaticamente lança uma `MethodArgumentNotValidException` ou
`MethodArgumentTypeMismatchException`. Meu `GlobalExceptionHandler` captura essas exceções e retorna uma resposta HTTP *
*400 Bad Request** com detalhes dos erros de validação, fornecendo feedback claro ao cliente da API."

**6. Pergunta:** Explique o tratamento centralizado de erros que você implementou. Por que isso é uma boa prática?

**Resposta:**
"Implementei o tratamento centralizado de erros utilizando um `@RestControllerAdvice` (`GlobalExceptionHandler`). Esta
classe intercepta exceções lançadas por qualquer controller na aplicação.

Para cada tipo de exceção de negócio ou de framework que quero tratar especificamente (`AccountNotFoundException`,
`InsufficientFundsException`, `MethodArgumentNotValidException`, `DataIntegrityViolationException`,
`IllegalArgumentException`), criei um método anotado com `@ExceptionHandler`.

Dentro de cada handler, mapeio a exceção para um status HTTP apropriado (`404 Not Found`, `409 Conflict`,
`400 Bad Request`, `422 Unprocessable Entity`, `500 Internal Server Error`) e crio um objeto `ErrorResponse` padronizado
que inclui timestamp, status, mensagem e o path da requisição.

Isso é uma boa prática porque:

* **Consistência:** Garante que as respostas de erro da API tenham sempre o mesmo formato, facilitando para os
  consumidores da API.
* **Separation of Concerns:** Mantém a lógica de tratamento de erros fora dos controllers, que devem se focar apenas em
  receber requisições e delegar para a camada de serviço.
* **Manutenibilidade:** É mais fácil gerenciar e modificar o tratamento de erros em um único local."

**7. Pergunta:** Como a API lida com a requisição que contém múltiplos lançamentos? Qual a garantia transacional para
essa lista?

**Resposta:**
"O endpoint `POST /accounts/transactions` recebe uma `List<TransactionRequest>`. O `AccountService.performTransactions`
itera sobre essa lista e delega o processamento de cada transação individual para o `SingleTransactionProcessor`.

O método `performTransactions` é anotado com `@Transactional`. No Spring, por padrão, isso significa que **toda a lista
de transações** é processada dentro de uma **única transação de banco de dados**.

Se qualquer transação na lista falhar (por exemplo, por saldo insuficiente na conta, ou conta não encontrada, ou erro de
lock), uma exceção é lançada. Como estamos dentro de uma transação transacional, essa exceção fará com que um **rollback
** ocorra. Nenhum dos lançamentos processados até o ponto da falha será persistido no banco de dados.

Isso garante uma semântica de **'tudo ou nada'** para o lote. Ou todos os lançamentos na lista são aplicados com
sucesso, ou nenhum deles é. Isso é crucial para a consistência em sistemas financeiros."

**8. Pergunta:** Você usou H2 como banco de dados. Qual a vantagem disso para o desafio e o que mudaria em um ambiente
de produção?

**Resposta:**
"Usei o H2 em memória (`jdbc:h2:mem`) por ser **extremamente rápido** para setup e execução durante o desenvolvimento e
testes. Ele não requer instalação separada, inicia junto com a aplicação Spring Boot e é ideal para demonstrações e
testes automatizados (`create-drop`).

Em um ambiente de produção, o H2 em memória **não seria adequado** porque os dados seriam perdidos toda vez que a
aplicação fosse reiniciada.

Para produção, eu usaria um banco de dados persistente relacional robusto, como PostgreSQL, MySQL, Oracle, etc. As
mudanças envolveriam:

* Adicionar a dependência do driver JDBC do banco de dados escolhido no `pom.xml`.
* Configurar a URL de conexão, usuário e senha corretos no `application.yml` (possivelmente usando variáveis de
  ambiente).
* Remover `spring.jpa.hibernate.ddl-auto: create-drop` (ou mudar para `validate` ou `none`).
* Adicionar uma ferramenta de **migração de schema** como Flyway ou Liquibase para gerenciar a criação e evolução das
  tabelas do banco de dados de forma versionada e confiável."

**9. Pergunta:** Você utilizou Lombok e MapStruct. Qual o propósito dessas bibliografias e por que as incluiu?

**Resposta:**
"**Lombok:** É uma biblioteca que reduz código boilerplate em classes Java, gerando automaticamente getters, setters,
construtores (`@RequiredArgsConstructor`, `@NoArgsConstructor`, `@AllArgsConstructor`), métodos `equals`, `hashCode`,
`toString` (`@Data`, `@Getter`, `@Setter`). Usei para as entidades (`Account`) e outras classes que se beneficiam,
tornando o código mais conciso e legível.

**MapStruct:** É um gerador de código para mappers de Java beans. Ele automatiza o mapeamento entre diferentes objetos (
por exemplo, entidade `Account` para DTO `AccountBalanceResponse`). Eu o configurei como um componente Spring (
`@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)`), e ele gera a implementação da interface
`AccountMapper` em tempo de compilação.
Usei o MapStruct para evitar escrever manualmente o código de conversão entre a entidade de domínio (`Account`) e o DTO
de resposta (`AccountBalanceResponse`), o que é repetitivo e propenso a erros. Ele melhora a produtividade e a segurança
do código."

**10. Pergunta:** Quais testes você escreveu e o que eles cobrem? O que você testaria adicionalmente se tivesse mais
tempo?

**Resposta:**
"Escrevi testes unitários para a camada de serviço (`AccountServiceTest`) utilizando JUnit 5 e Mockito. Estes testes
focam na **lógica de negócio** e no **fluxo** dentro do `AccountService` e sua interação com o
`SingleTransactionProcessor` e `AccountRepositoryPort` (mocks). Cobri cenários como:

* Processamento de lote de transações (verificando se delega corretamente para o processador individual).
* Tratamento de listas nulas ou vazias.
* Propagação de exceções do processador individual.
* Busca de saldo para contas existentes e inexistentes.
* Validação de parâmetros nulos.
* Lógica de inicialização de contas (`createAccountIfNotFound`).

Se tivesse mais tempo, adicionaria:

* **Testes de Integração:** Crucial para testar a interação real com o banco de dados e, especialmente, para validar a
  estratégia de **concorrência/Lock Pessimista** sob carga simulada (usando, por exemplo, `ExecutorService` para
  disparar múltiplas threads acessando a mesma conta).
* **Testes de Controller:** Para validar se os endpoints estão configurados corretamente, se a validação de Bean
  Validation funciona como esperado e se o `GlobalExceptionHandler` mapeia exceções para respostas HTTP corretas.
* **Testes para `SingleTransactionProcessor`:** Para testar a lógica específica de débito/crédito e as validações de
  valores (positivo) e saldo.
* **Testes para `GlobalExceptionHandler`:** Garantir que todos os handlers de exceção estejam funcionando corretamente.

Embora os testes unitários do serviço cubram a lógica, os testes de integração são essenciais para validar o
comportamento da API em um ambiente mais próximo ao real, especialmente em relação à persistência e concorrência."

**11. Pergunta:** O endpoint de transações retorna `ResponseEntity<Void>`. Qual o impacto disso e o que você faria de
diferente em um sistema real?

**Resposta:**
"Retornar `ResponseEntity<Void>` significa que a API responde com status 200 OK (ou um código de erro em caso de falha
do lote inteiro), mas sem um corpo de resposta. O impacto é que o cliente que consome a API não recebe feedback
detalhado sobre o resultado de **cada transação individual** dentro do lote. Se houver uma falha, o lote inteiro é
revertido, e o cliente sabe apenas que algo falhou, mas não *qual* transação e *por quê*.

Em um sistema real, especialmente para processamento em lote, seria mais útil retornar um DTO de resposta que contivesse
o status de cada transação enviada na requisição. Por exemplo:

```json
[
  {
    "accountNumber": "1001-1",
    "type": "CREDIT",
    "amount": 100.00,
    "status": "SUCCESS"
  },
  {
    "accountNumber": "1002-2",
    "type": "DEBIT",
    "amount": 5000.00,
    "status": "FAILED",
    "errorMessage": "Saldo insuficiente para a conta 1002-2..."
  }
]
```

Isso permitiria ao cliente saber exatamente o que aconteceu com cada item do lote. Implementar isso exigiria modificar a
lógica do `AccountService.performTransactions` para não simplesmente lançar uma exceção e parar, mas sim capturar
exceções por transação, registrar o resultado (sucesso ou falha com mensagem) e continuar processando as transações
subsequentes na lista. O retorno final seria uma lista desses resultados. A semântica transacional 'tudo ou nada' com
rollback automático do Spring `@Transactional` precisaria ser reconsiderada ou gerenciada de forma diferente nesse
caso (talvez processando cada transação em sua própria transação menor, ou compensando transações bem-sucedidas se uma
falha ocorrer). Para o desafio, a abordagem 'tudo ou nada' com rollback foi escolhida pela simplicidade e garantia de
consistência."

**12. Pergunta:** Como você gerenciaria logs e monitoramento em produção? Você incluiu algo para isso?

**Resposta:**
"O projeto utiliza SLF4J com Logback (que é o default do Spring Boot) para logging. Configurei diferentes níveis de
log (`INFO`, `DEBUG`, `ERROR`) no `application.yml` para ter visibilidade do fluxo da aplicação e identificar problemas.
Para produção, configuraria o Logback para enviar logs para um sistema centralizado (como ELK stack, Splunk, CloudWatch
Logs) para análise e monitoramento. Ajustaria os níveis de log para serem menos verbosos (e.g., INFO para o pacote da
aplicação, WARN/ERROR para frameworks) para evitar sobrecarga.

Para monitoramento, incluí o **Spring Boot Actuator**. Ele expõe endpoints como `/health` (para verificar se a aplicação
está rodando e saudável), `/info` (informações da aplicação) e `/metrics` (métricas de desempenho, uso de memória,
etc.). Em produção, esses endpoints seriam monitorados por ferramentas externas (como Prometheus com Grafana, Datadog,
etc.) para acompanhar a saúde e o desempenho da API em tempo real, configurar alertas e identificar gargalos. O
`management.endpoints.web.exposure.include` no `application.yml` habilita quais endpoints do Actuator ficam
disponíveis."

**13. Pergunta:** Como você garantiria a segurança dessa API em um ambiente real?

**Resposta:**
"Para um ambiente real, a segurança seria uma preocupação primordial. Adicionaria as seguintes medidas:

* **Autenticação e Autorização:** Implementaria um mecanismo de segurança, como OAuth2/JWT, para garantir que apenas
  clientes autorizados possam acessar os endpoints. Cada requisição exigiria um token válido, e a autorização definiria
  quais operações cada cliente pode realizar (e.g., quais contas um cliente pode acessar).
* **HTTPS:** A comunicação entre o cliente e a API deve ser sempre via HTTPS para criptografar os dados em trânsito e
  proteger contra ataques man-in-the-middle.
* **Validação de Entrada Robusta:** Além do Bean Validation, implementaria validações mais complexas de negócio (ex:
  formatos específicos de número de conta, limites de transação).
* **Rate Limiting/Throttling:** Para proteger contra ataques de negação de serviço (DoS) ou uso excessivo, implementaria
  limites na quantidade de requisições que um cliente pode fazer em um determinado período.
* **Auditoria:** Registraria detalhes importantes sobre cada transação realizada (quem fez, quando, valor, conta
  afetada) para fins de auditoria e conformidade.
* **Secrets Management:** Credenciais de banco de dados e outras informações sensíveis não devem ficar diretamente no
  `application.yml`, mas sim gerenciadas de forma segura (e.g., HashiCorp Vault, AWS Secrets Manager, Kubernetes
  Secrets).
* **Configuração Segura do Banco de Dados:** O usuário do banco de dados da aplicação deve ter apenas as permissões
  estritamente necessárias (minimizar privilégios).

No contexto do desafio, a segurança não foi um requisito explícito, mas em produção, todas essas camadas seriam
essenciais."

**14. Pergunta:** Dada a vaga Java PL/SQL, como você vê a interação entre código Java e procedimentos armazenados (
PL/SQL) em um sistema bancário? Como o JPA/Hibernate poderia se encaixar nisso?

**Resposta:**
"Em sistemas bancários tradicionais, é comum ter uma lógica de negócio crítica implementada em procedimentos armazenados
no banco de dados (PL/SQL, T-SQL, etc.). Isso geralmente ocorre por razões históricas, performance (execução próxima aos
dados) ou para impor regras de negócio no nível do banco.

A interação de uma aplicação Java com essa lógica poderia ocorrer de algumas formas:

* **Via JDBC Direto:** A aplicação Java chamaria os procedimentos armazenados usando a API JDBC. Isso oferece controle
  total sobre a chamada, parâmetros e tratamento de resultados, mas pode ser mais verboso.
* **Via Spring JDBC:** O Spring JDBC Template simplifica o uso direto do JDBC, incluindo a chamada de procedimentos
  armazenados.
* **Via JPA/Hibernate:** JPA/Hibernate também suporta chamadas a procedimentos armazenados (usando
  `@NamedStoredProcedureQuery` ou a API `StoredProcedureQuery`). No entanto, a integração pode ser mais complexa se o
  procedimento for muito procedural ou retornar tipos complexos. JPA/Hibernate é mais otimizado para mapeamento
  Objeto-Relacional e CRUD.

Em um cenário misto, a API Java (desenvolvida com Spring Boot/JPA) poderia ser responsável por:

* A camada REST e validação de entrada.
* Orquestração de fluxos que envolvem múltiplas operações.
* Lógica de negócio que não seja estritamente ligada a manipulação direta de dados no banco.
* Consultas mais complexas ou relatórios.

E delegar operações críticas de modificação de saldo ou regras complexas para procedimentos armazenados (PL/SQL):

* Em vez de `account.setBalance(account.getBalance().add(amount))` no Java, a aplicação chamaria um SP
  `atualiza_saldo(account_number, amount, type)`.
* O procedimento armazenado no banco seria responsável por buscar o saldo atual, aplicar a lógica de débito/crédito,
  verificar saldo insuficiente, e atualizar a linha na tabela `accounts`.
* A **concorrência** seria tratada **dentro do procedimento armazenado** ou pelo próprio mecanismo de locking do banco
  de dados ativado pelo SP (por exemplo, se o SP incluir um `SELECT ... FOR UPDATE`).

A escolha entre implementar a lógica no Java ou no PL/SQL dependeria dos requisitos específicos, performance, reuso da
lógica (se outros sistemas também usam o SP), e a estratégia de consistência e concorrência definida para o sistema
bancário como um todo. JPA ainda seria útil para mapear outras entidades e realizar consultas, mas a operação de
transação principal poderia ser uma chamada direta ao SP via JPA ou Spring JDBC."

**15. Pergunta:** Quais seriam os próximos passos ou melhorias que você implementaria neste projeto? (Baseado no seu
TODO)

**Resposta:**
"Com base na análise e no checklist que preparei, os próximos passos seriam:

1. **Fortalecer os Testes:** Adicionar testes de integração para validar a camada de persistência e, fundamentalmente,
   testar a estratégia de Lock Pessimista sob cenários de concorrência real. Adicionar testes para o controller e o
   handler de erros para garantir a cobertura completa.
2. **Refinar a Resposta do Lote:** Alterar o endpoint `/transactions` para retornar um resultado detalhado para cada
   transação na lista, oferecendo melhor feedback ao cliente da API. Isso exigiria ajustar a lógica de processamento
   para não interromper o lote na primeira falha.
3. **Preparação para Produção:** Configurar o projeto para usar um banco de dados persistente (não H2 em memória) e
   implementar migrações de schema com Flyway ou Liquibase.
4. **Gerenciamento de Contas:** Adicionar um endpoint para criação de novas contas, já que a inicialização atual é
   apenas para setup de desenvolvimento.
5. **Documentação:** Aprimorar a documentação OpenAPI com exemplos de requisições/respostas e detalhar os possíveis
   códigos de erro.
6. **(Opcional) Otimização para Alta Concorrência:** Se a necessidade por alta vazão em contas específicas surgir,
   explorar a otimização do processamento em lote agrupando transações por conta ou até mesmo considerar uma arquitetura
   assíncrona com filas para processamento serializado por conta."

---

**Dicas Finais para a Entrevista:**

* **Seja Confiante:** Você fez o trabalho, conhece o código.
* **Explique o "Porquê":** Sempre justifique suas escolhas (por que Spring Boot, por que Lock Pessimista, por que H2,
  por que essa estrutura).
* **Mostre Alternativas:** Discutir outras opções demonstra que você considerou diferentes abordagens e entende os
  trade-offs (como na pergunta sobre Lock Pessimista).
* **Seja Honesto:** Se não souber algo, diga que não sabe, mas mostre interesse em aprender e talvez como você abordaria
  a busca pela resposta. Se há limitações ou coisas que poderiam ser melhores no seu código, reconheça-as (como na
  pergunta sobre melhorias ou resposta do lote).
* **Prepare Perguntas:** Tenha algumas perguntas sobre a Matera, a equipe, os desafios técnicos que eles enfrentam, etc.
  Isso mostra interesse.
* **Revise seu Código:** Esteja pronto para navegar pelo seu código e explicar qualquer classe, método ou linha que eles
  apontarem.
* **Pratique a Apresentação:** Fale a breve apresentação em voz alta algumas vezes para se sentir confortável com ela.

---

## Outras perguntas

### 1. O que é uma thread-safe?

**Resposta:**

"Uma classe, um método ou um código é considerado **thread-safe** (seguro para threads) se ele se comporta corretamente
quando acessado **concorrentemente** por múltiplos threads, sem a necessidade de sincronização ou coordenação externa
por parte do código que o utiliza.

Isso significa que, independentemente de quantos threads estejam chamando os métodos ou acessando os dados
compartilhados desse código simultaneamente, o estado interno do objeto ou recurso compartilhado permanece **consistente
**, e o resultado das operações é o esperado, sem corrupção de dados ou resultados imprevisíveis.

Em essência, código thread-safe gerencia seus próprios recursos compartilhados (como variáveis de instância mutáveis) de
forma a evitar problemas de concorrência, como condições de corrida, deadlock, starvation, ou inconsistência de dados."

---

### 2. Como utilizar em uma API Java para lidar com requisições de concorrência?

**Resposta:**

"Em uma API Java construída com frameworks como Spring Boot, cada requisição HTTP recebida é tipicamente processada em
uma **thread separada** por um pool de threads (como o do servidor web embarcado, e.g., Tomcat ou Netty). Isso significa
que várias requisições podem estar sendo atendidas ao mesmo tempo, e essas threads podem tentar acessar e modificar os
mesmos recursos compartilhados.

Para garantir que sua API seja thread-safe ao lidar com requisições concorrentes, você precisa aplicar mecanismos de
sincronização e controle de acesso aos recursos compartilhados. Os principais recursos compartilhados em uma API de
backend geralmente são:

* **Dados no Banco de Dados:** O estado principal (saldo das contas, por exemplo) está persistido no banco. A
  concorrência aqui é gerenciada por transações e locks de banco de dados.
* **Objetos em Memória com Estado Compartilhado:** Menos comum em APIs REST *stateless* (que são a maioria), mas pode
  ocorrer com caches, singletons que mantém estado mutável, ou variáveis estáticas.
* **Recursos Externos:** Integrações com outros sistemas, filas, etc.

As formas de garantir thread-safety em uma API Java incluem:

1. **Utilizar Estruturas de Dados Concorrentes:** A API `java.util.concurrent` oferece coleções (`ConcurrentHashMap`,
   `CopyOnWriteArrayList`, etc.) e outras ferramentas que são projetadas para serem thread-safe e geralmente mais
   performáticas que coleções sincronizadas manualmente.
2. **Aplicar Sincronização no Código Java:** Usar a palavra-chave `synchronized` (em métodos ou blocos) ou locks mais
   flexíveis da API `java.util.concurrent.locks` (`ReentrantLock`) para proteger **seções críticas** do código onde
   recursos compartilhados são acessados ou modificados. Apenas uma thread pode executar a seção crítica sincronizada
   por vez.
3. **Imutabilidade:** Tornar os objetos cujo estado é compartilhado **imutáveis**. Se um objeto não pode ser modificado
   após sua criação, ele é inerentemente thread-safe. Embora não se aplique diretamente a entidades cujo estado
   *precisa* mudar (como o saldo de uma conta), o uso de DTOs imutáveis (como `record` no Java) ou objetos de valor
   imutáveis é uma boa prática geral que indiretamente ajuda na thread-safety ao reduzir a superfície de estado mutável.
4. **Gerenciar Transações de Banco de Dados e Locks:** Como fiz no desafio, usar os mecanismos transacionais do banco de
   dados e JPA/Hibernate (`@Transactional`, `@Lock`) para garantir que operações no banco sejam atômicas e isoladas.
   Esta é frequentemente a abordagem mais importante para garantir a consistência de dados persistidos.
5. **Projetar Serviços como Stateless:** Na arquitetura Spring, os serviços (anotados com `@Service`) são singletons por
   padrão. Eles *não* devem manter estado mutável específico de uma requisição em variáveis de instância. O estado da
   requisição deve ser passado como parâmetro. Isso evita problemas de concorrência dentro da própria instância do
   serviço.

No contexto do desafio, o foco principal da thread-safety recai sobre a garantia de acesso seguro e consistente ao
registro da conta no banco de dados durante as operações de débito/crédito, que é onde a concorrência pode levar a
resultados incorretos no saldo."

---

### 3. Como evitar condições de corrida e garantir a consistência dos dados compartilhados entre as threads?

**Resposta:**

"Condições de corrida ocorrem quando a ordem de execução de operações concorrentes em um recurso compartilhado afeta o
resultado final, geralmente de forma inesperada e incorreta. No cenário bancário, um exemplo clássico é o 'lost update':
duas threads leem o saldo da mesma conta simultaneamente, calculam um novo saldo com base no valor lido, e ambas tentam
salvar o resultado. Uma das atualizações sobrescreverá a outra, levando a um saldo incorreto.

Garantir a consistência dos dados significa assegurar que o estado dos dados sempre reflita as regras de negócio (e.g.,
saldo >= 0, total de ativos = total de passivos + capital social, etc.) e que as operações atômicas preservem essa
integridade.

Para evitar condições de corrida e garantir a consistência, utilizamos as seguintes técnicas, muitas das quais já
mencionei na resposta anterior, focando agora em *como* elas resolvem esses problemas:

1. **Serialização do Acesso (Sincronização/Locks):** A maneira mais direta é garantir que apenas uma thread por vez
   possa executar a parte do código (a 'seção crítica') que lê ou modifica o recurso compartilhado.
    * **Em Java:** Usando `synchronized`, `ReentrantLock`. Isso impede que múltiplas threads entrem na seção crítica ao
      mesmo tempo, forçando-as a esperar na fila.
    * **No Banco de Dados (Locks Pessimistas):** Como implementado no desafio, o `SELECT ... FOR UPDATE` bloqueia a
      linha da tabela no banco, impedindo que outras transações leiam a linha com um lock de escrita até que a transação
      atual termine. Isso serializa o acesso à linha específica da conta.

2. **Controle de Versão (Locks Otimistas):** Em vez de bloquear preventivamente, você verifica se o recurso
   compartilhado foi modificado por outra thread antes de salvar suas alterações.
    * **No Banco de Dados (Locks Otimistas):** Adicionando uma coluna de versão (número ou timestamp) na entidade. Ao
      ler, você pega a versão. Ao atualizar, a operação de `UPDATE` inclui uma cláusula `WHERE version = <versão lida>`.
      Se a linha não for atualizada (porque a versão no banco já é diferente), significa que outra thread modificou a
      conta, e você lida com o conflito (geralmente retentando a operação). Isso evita o 'lost update'.

3. **Operações Atômicas:** Para operações simples que precisam ser indivisíveis (como incrementar um contador), usar
   classes atômicas (`AtomicInteger`, `AtomicReference`) garante que a operação completa aconteça sem interrupção por
   outras threads.
4. **Transações de Banco de Dados:** Transações (`@Transactional` no Spring) garantem que um conjunto de operações no
   banco de dados seja tratada como uma única unidade lógica. Elas fornecem propriedades ACID (Atomicidade,
   Consistência, Isolamento, Durabilidade). O **Isolamento** é crucial para concorrência: diferentes níveis de
   isolamento (READ COMMITTED, REPEATABLE READ, SERIALIZABLE) definem o quão protegidas as transações estão umas das
   outras contra problemas como dirty reads, non-repeatable reads e phantom reads. Em conjunto com locks (pessimistas ou
   otimistas), as transações garantem que as modificações de dados sejam consistentes.
5. **Arquiteturas Baseadas em Eventos/Filas:** Serializar as operações *por entidade* (e.g., todas as operações para a
   conta '1001-1' vão para a mesma partição em uma fila e são processadas em ordem) pode eliminar a concorrência no
   processamento, delegando a complexidade para a infraestrutura de mensageria.

No meu projeto, evitei condições de corrida no saldo das contas e garanti a consistência utilizando a combinação de *
*Transações de Banco de Dados (`@Transactional`)** e **Locks Pessimistas (`@Lock(LockModeType.PESSIMISTIC_WRITE)`)** no
nível da linha da tabela. Ao buscar a conta para aplicar um débito ou crédito, adquiro um lock de escrita que impede
outras operações de escrita na mesma conta até que a transação atual seja finalizada. Isso força as operações
concorrentes na *mesma conta* a serem executadas *sequencialmente* no banco de dados, garantindo que cada operação veja
o saldo atual correto antes de modificá-lo."
