# Desafio Matera - API de Lançamentos Bancários

![Java](https://img.shields.io/badge/Java-21-blue?logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-green?logo=spring&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9.9-red?logo=apache-maven&logoColor=white)
![H2 Database](https://img.shields.io/badge/Database-H2-orange)
![Docker](https://img.shields.io/badge/Docker-blue?logo=docker&logoColor=white)
![Swagger](https://img.shields.io/badge/API%20Doc-Swagger-85EA2D?logo=swagger&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

Este arquivo `.zip` contém a solução proposta para o **Desafio 6728457 - API de Lançamentos** da Matera.

A solução consiste em uma API RESTful robusta, desenvolvida em **Java com Spring Boot**, projetada para gerenciar
lançamentos de débito e crédito em contas bancárias. Um foco primordial foi dado à **thread-safety e consistência de
dados** em ambientes concorrentes, requisitos essenciais do desafio.

## 🎯 O Desafio Original

O objetivo era criar uma API para operações bancárias com as seguintes características principais:

1. Crie uma API RESTful para realizar lançamentos bancários de débito e crédito nas contas dos clientes.
2. Seus endpoints devem permitir as seguintes operações:
    * Realizar um lançamento de débito e crédito em uma conta específica.
    * Deve permitir mais de um lançamento na mesma requisição.
    * Obter o saldo atual de uma conta específica.
3. Certifique-se de que a API seja thread-safe para lidar com requisições concorrentes.
4. Evite condições de corrida e garanta a consistência dos dados compartilhados entre as threads.
5. Documente a API, especificando os endpoints, métodos HTTP suportados, parâmetros esperados e formatos de resposta.
6. Escreva um conjunto de testes que você julgar necessário.
7. Recomenda-se o uso das seguintes tecnologias/frameworks: Java, Spring, Hibernate e outros que você julgar necessário.

## ✨ Destaques da Solução

A solução implementada atende a todos os requisitos do desafio e incorpora boas práticas de desenvolvimento. Alguns
pontos de destaque incluem:

* **API RESTful:** Endpoints claros para lançamentos em lote e consulta de saldo.
* **Thread-Safety e Consistência:** Implementação robusta utilizando técnicas de bloqueio de banco de dados para
  garantir a integridade dos dados em operações concorrentes.
* **Documentação Automática:** Uso de Swagger/OpenAPI para documentar a API de forma interativa.
* **Testes:** Cobertura com testes unitários e facilidades para testes manuais (Postman).
* **Execução Facilitada:** Suporte para execução local via Maven e em contêineres Docker.

## 🚀 Como Configurar e Executar

Esta seção fornece as instruções necessárias para rodar a aplicação rapidamente a partir dos arquivos entregues.

### Pré-requisitos

Certifique-se de ter os seguintes softwares instalados em sua máquina:

* **Java Development Kit (JDK):** Versão 21 ou superior.
* **Apache Maven:** Versão 3.x ou superior.
* **Docker e Docker Compose:** (Opcional) Para execução em contêineres.

### 📥 Conteúdo da Entrega e Como Iniciar

O arquivo `.zip` entregue contém o código-fonte completo do projeto.

1. **Extraia** o conteúdo do arquivo `.zip` para uma pasta de sua preferência.
2. Abra o terminal e **navegue para a pasta raiz do projeto** onde o arquivo `README.md` está localizado.

Você encontrará a estrutura do projeto, incluindo a pasta `banking-transactions-api` que contém o código principal da
aplicação.

### 🛠️ Execução Local (com Maven)

1. No terminal, navegue para o diretório do módulo principal da API:
   ```bash
   cd banking-transactions-api
   ```
2. Construa o projeto: Este comando irá compilar o código, executar os testes automatizados e empacotar a aplicação em
   um arquivo JAR.
   ```bash
   mvn clean install
   ```
3. Execute o arquivo JAR gerado:
   ```bash
   java -jar target/banking-transactions-api-0.0.1-SNAPSHOT.jar
   ```
   *(Nota: Verifique o nome exato do arquivo `.jar` na pasta `target` após a build)*

A aplicação será iniciada e estará acessível em `http://localhost:8080/api/v1`.

### 🐳 Execução com Docker Compose

1. Certifique-se de estar na pasta raiz do projeto (onde o arquivo `docker-compose.yml` está localizado).
   ```bash
   # Verifique se você está na pasta correta
   ls docker-compose.yml 
   ```
2. Construa a imagem Docker e inicie o contêiner:
   ```bash
   docker compose up --build
   ```
   *(O parâmetro `--build` garante que a imagem será construída a partir do Dockerfile)*

A aplicação estará acessível via Docker em `http://localhost:8080/api/v1`. Para parar os contêineres, pressione `Ctrl+C`
no terminal onde o `docker compose up` está rodando ou use `docker compose down`.

## Testes

O projeto inclui testes unitários para a lógica da camada de serviço (`AccountServiceTest`).

### Executando Testes Unitários

Para executar todos os testes unitários usando Maven:

```bash
mvn test
```

### 🌐 Acesso à API e Ferramentas Úteis

Após iniciar a aplicação (localmente ou via Docker), você pode acessá-la e ferramentas de desenvolvimento nos seguintes
endereços:

* **Base URL da API:** `http://localhost:8080/api/v1`

* Acesse: `http://localhost:8080/api/v1/swagger-ui.html`
* **Documentação Interativa (Swagger UI):**
    * **URL:** [http://localhost:8080/api/v1/swagger-ui.html](http://localhost:8080/api/v1/swagger-ui.html)
    * *Este é o local recomendado para explorar e testar os endpoints.*
* **Console do Banco de Dados H2 (Em Memória):**
    * **URL:** [http://localhost:8080/api/v1/h2-console](http://localhost:8080/api/v1/h2-console)
    * **Credenciais (conforme configurado em `application.yml`):**
        * Usuário: `sa`
            * Senha: (campo senha é vazio)
            * JDBC URL: `jdbc:h2:mem:bankdb`
        * *Útil para inspecionar o estado do banco de dados em memória.*

## 📖 Endpoints da API

Os principais endpoints implementados são:

### `POST /api/v1/accounts/transactions`

* **Descrição:** Processa um ou mais lançamentos de débito/crédito em contas específicas.
* **Método HTTP:** `POST`
* **Corpo da Requisição:** Um array de objetos representando as transações a serem realizadas. Consulte o Swagger UI
  para a estrutura detalhada do objeto de requisição (`TransactionRequest`) e suas validações.
* **Exemplo cURL:**
  ```bash
  curl --location 'http://localhost:8080/api/v1/accounts/transactions' \
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
      }
  ]'
  ```
* **Possíveis Respostas (Status HTTP):** `200 OK`, `400 Bad Request`, `404 Not Found`, `409 Conflict`,
  `422 Unprocessable Content`, `500 Internal Server Error`. Detalhes sobre o tratamento de erros são explicados no
  documento de detalhes técnicos.

### `GET /api/v1/accounts/{accountNumber}/balance`

* **Descrição:** Obtém o saldo atual de uma conta específica.
* **Método HTTP:** `GET`
* **Parâmetros de Path:** `{accountNumber}` (string) - O número da conta.
* **Resposta de Sucesso (`200 OK`):** Um objeto contendo o número da conta e o saldo (`AccountBalanceResponse`).
  Consulte o Swagger UI.
* **Exemplo cURL:**
  ```bash
  curl --location 'http://localhost:8080/api/v1/accounts/1001-1/balance'
  ```
* **Possíveis Respostas (Status HTTP):** `200 OK`, `404 Not Found`, `500 Internal Server Error`.

## ✅ Testes

O projeto inclui testes para garantir a correção e robustez da aplicação.

### Testes Automatizados (Unitários)

Testes unitários foram implementados para verificar a lógica de negócio isoladamente.

Para executar os testes automatizados:

```bash
cd banking-transactions-api # Navegue para o diretório do módulo da API
mvn test
```

*O comando `mvn clean install` também executa os testes automaticamente.*

### Testes Manuais (Postman)

Uma coleção Postman foi criada para facilitar a execução manual dos endpoints.

O arquivo da coleção está disponível
em [API Lançamento - Postman Collection](./src/main/resources/collections/API%20Lançamento.postman_collection.json).

Importe este arquivo no Postman para testar os endpoints com exemplos pré-configurados, incluindo cenários de sucesso e
erro.

## 📚 Detalhes Técnicos da Implementação

Para uma compreensão aprofundada sobre as decisões de arquitetura, a estratégia de gerenciamento de concorrência (
thread-safety), tratamento de erros, validações, inicialização de dados e outras escolhas de implementação, por favor,
consulte o documento dedicado:

➡️ **[Detalhes de Implementação](./docs/IMPLEMENTATION_DETAILS.md)**

## 📞 Contato

Para quaisquer perguntas ou feedback, sinta-se à vontade para entrar em contato:

[Entre em contato via E-mail](mailto:juhvaliatimaran@gmail.com)

## 📄 Licença

Este projeto está licenciado sob a [Licença MIT](https://opensource.org/licenses/MIT).

