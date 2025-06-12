## Coleção Postman

Um arquivo de coleção Postman está disponível na pasta `Postman/Banking Transactions API.postman_collection.json`. Importe-o no Postman para testar os endpoints. Ele inclui exemplos para:

1.  Criar uma conta.
2.  Obter o saldo.
3.  Realizar um débito.
4.  Realizar um crédito.
5.  Realizar um lote de transações (débito e crédito misturados).
6.  Testar saldo insuficiente.
7.  Testar conta não encontrada.
8.  Testar validação de input inválido.

## Considerações de Design e Implementação

*   **BigDecimal:** Utilizado para valores monetários para evitar imprecisões de ponto flutuante.
*   **Thread-Safety:** Garantida primariamente pelo uso de `@Transactional` e bloqueios pessimistas (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) no nível do banco de dados ao acessar/modificar a entidade `Account` no serviço de transações.
*   **Validação:** Utiliza Jakarta Validation (`@Valid`, `@NotNull`, etc.) nos DTOs de requisição e `@Validated` no Controller. Mensagens de validação configuradas em `ValidationMessages.properties` (em português).
*   **Nomenclaturas:** Classes, interfaces, métodos e campos em Inglês, conforme solicitado. Mensagens de validação e erro em português.
*   **Clean Code/POO/SOLID:** Aplicação estruturada em camadas com responsabilidades claras, buscando coesão e baixo acoplamento. Uso de interfaces para abstração.
*   **H2 Database:** Banco de dados em memória adequado para testes e desenvolvimento local rápido. Em um ambiente de produção, seria substituído por um banco persistente (PostgreSQL, MySQL, etc.).