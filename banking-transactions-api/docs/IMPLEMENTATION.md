# Implementações

## Implementado conforme a descrição do desafio

1. **API RESTful para lançamentos:** Sim, a API foi criada utilizando Spring Boot, Spring Web, Spring Data JPA e H2 (em
   memória).
2. **Endpoints:**
    * **Realizar lançamentos (débito/crédito) em conta específica:** Implementado no endpoint
      `POST /accounts/transactions`.
    * **Permitir mais de um lançamento na mesma requisição:** Sim, o endpoint `POST /accounts/transactions` aceita uma
      lista (`List<TransactionRequest>`) de transações.
    * **Obter saldo de conta específica:** Implementado no endpoint `GET /accounts/{accountNumber}/balance`.
3. **Thread-safe:** Sim, a API implementa thread-safety e tratamento de concorrência.
   A estratégia utilizada foi o **Lock Pessimista** (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) aplicado na camada de
   persistência (`AccountJpaAdapter` e `AccountJpaRepository`) ao buscar a conta para realizar uma operação que modifica
   seu saldo. Isso garante que apenas uma transação por vez possa modificar o registro de uma conta específica, evitando
   condições de corrida. O uso de `@Transactional` em conjunto com o lock reforça a consistência.
4. **Evitar condições de corrida e garantir consistência:** A implementação do Lock Pessimista e o uso de transações de
   banco de dados garantem que as operações de leitura (com lock), modificação e escrita no saldo da conta sejam
   atômicas e isoladas para requisições concorrentes na mesma conta, protegendo a consistência dos dados. O uso de
   `BigDecimal` também garante a precisão monetária.
5. **Documentar a API:** Sim, a API foi documentada utilizando SpringDoc OpenAPI. A configuração (`OpenApiConfig`) e as
   anotações (`@Operation`, `@ApiResponse`, `@Schema`) estão presentes nos Controllers e DTOs, tornando a documentação
   acessível via Swagger UI (`/swagger-ui.html`).
6. **Escrever testes:** Sim, foi fornecida uma suite de testes unitários (`AccountServiceTest`) para a camada de
   aplicação (Use Case/Service), cobrindo cenários de processamento de transações e busca de saldo.
7. **Uso das tecnologias recomendadas:** Sim, Java, Spring, Hibernate (via Spring Data JPA), H2, Lombok, MapStruct e
   SpringDoc foram utilizados conforme ou além da recomendação.
8. **Preparado para envio (.zip):** A estrutura de projeto Maven (`pom.xml`) e os arquivos de configuração (
   `application.yml`), Dockerfile e docker-compose.yml indicam que o projeto está pronto para ser empacotado e enviado.

## Implementado além do que foi pedido:

1. **Arquitetura em Camadas (Domain, Application, Infrastructure):** O projeto segue uma estrutura clara separando
   preocupações em camadas distintas (Domain, Application - ports & usecases, Infrastructure - adapters & config), o que
   não era um requisito explícito do desafio, mas é uma boa prática de design.
2. **Tratamento Centralizado de Erros:** Foi implementado um `@RestControllerAdvice` (`GlobalExceptionHandler`) para
   capturar e tratar exceções de forma global na API, retornando um formato de erro padronizado (`ErrorResponse`). Isso
   melhora a usabilidade da API em caso de erros, fornecendo respostas consistentes.
3. **Validação de Entrada (Bean Validation):** A validação dos DTOs de requisição (`TransactionRequest`) foi realizada
   utilizando as anotações `@Valid`, `@NotBlank`, `@NotNull`, `@DecimalMin` do Bean Validation. Isso garante que os
   dados de entrada estejam corretos antes mesmo de chegar na lógica de negócio.
4. **Inicialização de Dados:** Um `CommandLineRunner` (`DataInitializer`) foi criado para popular o banco de dados H2
   com algumas contas iniciais ao iniciar a aplicação (exceto no perfil de teste).
5. **Uso de MapStruct:** Utilizado para mapear entidades para DTOs de resposta (`AccountMapper`), o que automatiza o
   processo de mapeamento e reduz código boilerplate.
6. **Configuração de Actuator:** Incluído o Spring Boot Actuator (`/actuator/health`, `/actuator/info`,
   `/actuator/metrics`) para monitoramento básico da aplicação.
7. **Suporte a Docker:** Incluído `Dockerfile` e `docker-compose.yml` para facilitar a containerização e execução da
   aplicação.

## Melhorias/Refatoração que devem ser feitas

1. **Abrangência dos Testes:** Os testes unitários cobrem bem o `AccountService`. No entanto, testes de integração
   seriam valiosos para validar a interação com o banco de dados e, crucialmente, testar a estratégia de Lock Pessimista
   sob carga concorrente real (simulando múltiplas requisições no mesmo endpoint, para a mesma conta). Testes para a
   camada de Controller e para o `GlobalExceptionHandler` também poderiam ser adicionados para aumentar a cobertura.
2. **Tratamento de Múltiplas Transações na Mesma Requisição (Opcional):** A implementação atual processa a lista de
   transações sequencialmente (`for` loop em `AccountService`). Se uma única requisição contiver várias transações para
   a *mesma* conta, cada uma delas adquirirá e liberará o lock pessimista individualmente. Para casos com muitas
   transações na mesma requisição *para a mesma conta*, agrupar as transações por conta primeiro e processar todas as
   operações de uma conta sob um único lock adquirido *uma vez* por conta poderia ser potencialmente mais performático (
   embora a abordagem atual seja segura). No entanto, a complexidade aumenta. A implementação atual é correta e segura,
   essa melhoria é mais focada em otimização para um caso de uso específico.
3. **Resposta da API para Lote de Transações (Opcional):** O endpoint `POST /accounts/transactions` retorna um
   `ResponseEntity<Void>`. Em um cenário real, pode ser útil retornar uma resposta mais detalhada, indicando o status de
   cada transação na lista (quais tiveram sucesso, quais falharam e por quê), em vez de um simples OK ou lançar uma
   exceção que interrompe o processamento de toda a lista na primeira falha. A estratégia atual de "tudo ou nada" é
   válida, mas uma resposta item a item é uma alternativa a considerar dependendo dos requisitos.
4. **Estratégia de Lock (Consideração):** Embora o lock pessimista seja eficaz para garantir a consistência, ele pode
   ser um gargalo sob alta concorrência em contas específicas. Para sistemas de altíssima escala com muitas atualizações
   na mesma conta, outras estratégias (como controle de versão otimista, filas de mensagens para serializar operações
   por conta, ou abordagens baseadas em eventos) podem ser consideradas, mas seriam significativamente mais complexas de
   implementar. Para a maioria dos casos de uso, o lock pessimista é uma solução robusta e mais simples. A configuração
   do `lock.timeout` é importante nesse contexto para evitar que requisições fiquem presas indefinidamente.
5. **Separar Responsabilidades no `SingleTransactionProcessor` (Leve Refatoração):** O método `process` no
   `SingleTransactionProcessor` faz várias coisas: busca+lock, validação de tipo, aplicação da operação, e salvamento.
   Embora não seja estritamente errado, alguns padrões (como Command Pattern) poderiam ser usados para encapsular a
   lógica de débito/crédito de forma mais explícita ou para separar a orquestração da aplicação da lógica. No entanto, a
   estrutura atual está razoável e legível.
6. **Persistência do H2 (Ambientes que não são de Desenvolvimento):** O uso de H2 em memória (`jdbc:h2:mem`) e
   `ddl-auto: create-drop` é ótimo para desenvolvimento e testes, mas para ambientes de staging ou produção, um banco de
   dados persistente (como PostgreSQL, MySQL, etc.) e uma estratégia de migração de schema (como Flyway ou Liquibase)
   seriam necessários.

Em resumo, o código implementa corretamente e de forma robusta os requisitos principais do desafio, especialmente no que
diz respeito à thread-safety e consistência usando locks pessimistas. As implementações extras (tratamento de erro,
documentação, validação) demonstram boas práticas. As melhorias sugeridas são mais focadas em testes de integração,
otimizações potenciais para casos de uso específicos de alta concorrência/lotes grandes, e considerações para ambientes
de produção.












