# Challenge - Banking Transactions API

Este repositório contém a solução proposta para o **Desafio 6728457- API de Lançamentos** da Matera.

A solução consiste em uma API RESTful desenvolvida em Java com Spring Boot para gerenciar lançamentos de débito e
crédito em contas bancárias, com foco em thread-safety e consistência de dados.

## Desafio 6728457- API de Lançamentos

Você tem a missão de desenvolver uma API RESTful para um sistema bancário que precisa lidar com lançamentos de débito e
crédito nas contas dos clientes.

### Instruções Originais:

1. Crie uma API RESTful para realizar lançamentos bancários de débito e crédito nas contas dos clientes.
2. Seus endpoints devem permitir as seguintes operações:
    * Realizar um lançamento de débito e crédito em uma conta específica.
    * Deve permitir mais de um lançamento na mesma requisição.
    * Obter o saldo atual de uma conta específica.
3. Certifique-se de que a API seja thread-safe para lidar com requisições concorrentes.
4. Evite condições de corrida e garanta a consistência dos dados compartilhados entre as threads.
5. Documente a API, especificando os endpoints, métodos HTTP suportados, parâmetros esperados e formatos de resposta.
6. Escreva um conjunto de testes que você julgar necessário.
7. Recomenda-se o uso das seguintes tecnologias/frameworks: Java, Spring, Hibernate e outros que você julgar necessário

---

Para detalhes sobre a implementação, arquitetura, como executar o projeto localmente ou com Docker, e documentação
completa da API, por favor, consulte o README específico da aplicação:

👉 **[README da Aplicação: Banking Transactions API](./banking-transactions-api/README.md)**

---

## Contato

[Entre em contato via E-mail](mailto:julianemaran@gmail.com)

## Licença

Este projeto está licenciado sob a [Licença MIT](./LICENSE).
