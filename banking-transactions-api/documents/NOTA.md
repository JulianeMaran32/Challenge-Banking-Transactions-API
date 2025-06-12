# Explicações

### 1. Mensagens de Erro e Logs em Português

As mensagens de log e strings de erro lançadas diretamente no código estão em português. Porém, estou utilizando um
arquivo de validação como `messages.properties`, para que as mensagens de erro sejam traduzidas para o idioma do
usuário. Isso é feito através do `ResourceBundle` do Java, que permite carregar mensagens de diferentes idiomas com base
na configuração regional do usuário.

### 2. Utilização do 201 Created no método POST

O endpoint `POST /api/accounts/transactions` processa uma lista de lançamentos, que são, na verdade, *operações* que
modificam recursos existentes (contas), e não a criação de um novo recurso "transação" com uma URI identificável. O
status 201 Created é tipicamente usado para indicar que um *novo recurso* foi criado com sucesso em uma URI específica,
que geralmente é retornada no cabeçalho `Location`.

Para um endpoint que recebe um lote de operações (débito/crédito) que modificam recursos (contas), os status HTTP mais
apropriados seriam `200 OK` (sucesso com possivelmente um corpo de resposta indicando o resultado de cada operação) ou
`204 No Content` (sucesso sem corpo de resposta).

### 3. Aplicação de Arquitetura, Patterns e Clean Code

* **Clean Architecture/Ports & Adapters:** A estrutura `domain`, `application`, `infrastructure` já segue essa ideia.
    * `domain` não conhece `application` ou `infrastructure`.
    * `application` define interfaces (Ports) e a lógica de caso de uso, dependendo de ports de saída.
    * `infrastructure` fornece implementações (Adapters) dos ports e a camada de apresentação (Controller).
* **SOLID:**
    * **SRP:** Tentar manter classes e métodos com uma única responsabilidade. `AccountService` focará na orquestração
      das operações de conta. A lógica de criação inicial será um método separado. O mapeamento será em uma classe
      separada.
    * **OCP:** Aberto para extensão, fechado para modificação. Novas formas de persistência ou apresentação podem ser
      adicionadas implementando interfaces (Ports) sem modificar a lógica de negócio (`AccountService`).
    * **LSP:** Implementações de interfaces (`AccountService` implementando `AccountServicePort`, `AccountJpaAdapter`
      implementando `AccountRepositoryPort`) podem ser substituídas por suas interfaces.
    * **ISP:** Interfaces devem ser pequenas e específicas. `AccountServicePort` conterá apenas as operações de negócio
      principais (lançamentos, saldo). O método de inicialização ficará fora dela.
    * **DIP:** Depender de abstrações (interfaces), não de implementações concretas. O `AccountService` depende de
      `AccountRepositoryPort`, não de `AccountJpaRepository`. O `AccountController` depende de `AccountServicePort`, não
      de `AccountService`. O `DataInitializer` precisará injetar a classe concreta `AccountService` para chamar o método
      `createAccountIfNotFound`, que *não* faz parte da interface principal de operações. Isso é um pequeno desvio do
      DIP estrito para este caso específico de inicialização, mas aceitável para este cenário.
* **Clean Code:** Remover comentários desnecessários, usar nomes significativos, métodos pequenos, tratar erros de forma
  clara.
* **Desacoplamento:** As interfaces (`*Port`) promovem alto desacoplamento entre as camadas `application` e
  `infrastructure`.



