# 🏆 Rinha de Backend 2025

Este projeto é uma implementação para o desafio [Rinha de Backend 2025](https://github.com/zanfranceschi/rinha-de-backend-2025/blob/main/INSTRUCOES.md).

## 🚀 Tecnologias Utilizadas

- **Linguagem:** Java 21
- **Framework:** Spring Boot 3
- **Banco de Dados:** PostgreSQL
- **Build:** Maven
- **Virtualização:** Docker
- **Load Balancer:** Nginx
- **Compilação Nativa:** GraalVM

## 🏗️ Arquitetura

A arquitetura da aplicação foi projetada para ser resiliente e escalável, utilizando os seguintes componentes:

```mermaid
graph TD
    subgraph "Infraestrutura"
        LB(Nginx Load Balancer)
        B1(Backend 1)
        B2(Backend 2)
        DB(PostgreSQL)
    end

    subgraph "Fluxo da Requisição"
        C(Cliente) --> LB
        LB --> B1
        LB --> B2
        B1 --> DB
        B2 --> DB
    end

    subgraph "Processamento Assíncrono"
        B1 --> P(Processador de Pagamentos)
        B2 --> P
    end
```


- **Nginx:** Atua como um load balancer, distribuindo as requisições entre as instâncias da aplicação backend.
- **Backend (Spring Boot):** A aplicação principal, responsável por processar os pagamentos. O `docker-compose.yml` está configurado para executar duas instâncias da aplicação para alta disponibilidade.
- **PostgreSQL:** O banco de dados utilizado para persistir os dados da aplicação.
- **GraalVM:** O projeto está configurado para compilar uma imagem nativa, o que resulta em um tempo de inicialização mais rápido e menor consumo de memória.

### ✨ Padrões e Funcionalidades

- **Padrão Outbox:** A aplicação utiliza o padrão outbox para garantir a consistência entre o banco de dados e os sistemas externos. Os eventos de pagamento são salvos em uma tabela `outbox` e processados de forma assíncrona. O processamento é thread-safe entre as múltiplas instâncias da aplicação através de um lock no banco de dados, garantindo que cada evento seja processado apenas uma vez.
- **Health Check:** Um sistema de health check monitora a saúde dos processadores de pagamento, permitindo que a aplicação troque para um processador de fallback caso o principal fique indisponível.
- **Agendadores (Schedulers):** Tarefas agendadas são utilizadas para processar os eventos da outbox, limpar a tabela de eventos e atualizar o status dos health checks.

## 🌊 Fluxos Detalhados da Aplicação

### Fluxo de API (Cliente)
```mermaid
graph TD
    A[Cliente]
    API{API Backend}
    DB[(PostgreSQL)]

    subgraph "Criação de Pagamento"
      A -- "POST /payments" --> API
      API -- "Salva pagamento (pending) e cria evento na Outbox" --> DB
    end

    subgraph "Consulta de Sumário"
      A -- "GET /payments-summary" --> API
      API -- "Lê e agrega dados" --> DB
    end
```

### Fluxo de Health Check
```mermaid
graph TD
    DB[(PostgreSQL)]
    E[Health Check Service]
    F[Default Processor]
    G[Fallback Processor]

    E -- "GET /service-health" --> F
    E -- "GET /service-health" --> G
    E -- "Atualiza status no banco" --> DB
```

### Fluxo de Cache de Health Check
```mermaid
graph TD
    DB[(PostgreSQL)]
    E[Health Check Cache]

    E -- "Lê status do Health Check" --> DB
```

### Fluxo de Seleção de Processador
```mermaid
graph TD
    E[Best Payment Processor Calculator]
    F[Health-Check Cache]
    G[Best Payment Processor Holder]

    E -- "Lê dados do cache" --> F
    E -- "Define o melhor processador" --> G
```

### Fluxo de Processamento de Pagamentos (Worker)
```mermaid
graph TD
    DB[(PostgreSQL)]
    A[Worker]
    B[Best Processor Holder]
    C[Payment Gateway]
    D[Default Processor]
    E[Fallback Processor]

    A -- "Carrega pagamentos não processados (com lock)" --> DB
    A -- "Envia para o gateway" --> C
    C -- "Obtém o melhor processador" --> B
    C -- "POST /payments" --> D
    C -- "POST /payments" --> E
    A -- "Atualiza status do pagamento" --> DB
```

## ⚙️ Como Executar

Para executar o projeto, você precisa ter o Docker e o Docker Compose instalados.

**Importante:** Esta aplicação depende dos processadores de pagamento externos. Certifique-se de que eles estejam em execução e acessíveis pela rede `payment-processor`. Para mais detalhes, consulte o [repositório oficial da Rinha de Backend](https://github.com/zanfranceschi/rinha-de-backend-2025).

1. **Construa a imagem da aplicação:**
   ```bash
   ./build.sh
   ```

2. **Inicie os containers:**
   ```bash
   docker-compose up -d
   ```

A aplicação estará disponível na porta `9999`.
