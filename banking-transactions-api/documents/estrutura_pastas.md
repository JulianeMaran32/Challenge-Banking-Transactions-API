# Estrutura de Pastas do Projeto

```text
src/main/java/juhmaran/challenge/bankingtransactionsapi
├── domain
│   ├── entity
│   │   └── Account.java
│   └── exception
│       ├── AccountNotFoundException.java
│       └── InsufficientFundsException.java
├── application
│   ├── port
│   │   ├── in
│   │   │   └── AccountServicePort.java
│   │   └── out
│   │       └── AccountRepositoryPort.java
│   └── usecase
│       └── AccountService.java
├── infrastructure
│   ├── adapter
│   │   ├── in
│   │   │   └── AccountController.java
│   │   └── out
│   │       └── AccountJpaAdapter.java
│   ├── config
│   │   └── DataInitializer.java 
│   ├── error
│   │   ├── GlobalExceptionHandler.java
│   │   └── ErrorResponse.java
│   ├── dto
│   │   ├── TransactionRequest.java
│   │   └── AccountBalanceResponse.java
│   ├── dto.enums                
│   │   └── TransactionType.java
│   └── repository
│       └── AccountJpaRepository.java
└── BankingTransactionsApiApplication.java
```