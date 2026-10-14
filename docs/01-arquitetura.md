# 1. Arquitetura

## 1.1 Aplicação

A EcoCycle é uma API REST monolítica em camadas:

| Camada | Responsabilidade | Classes |
|---|---|---|
| Segurança | Basic Auth com usuário único definido por variável de ambiente; `/actuator/health` público | `SecurityConfig` |
| Controller | Rotas HTTP e códigos de status (200/201/204/404) | `UsuarioController`, `MaterialController`, `PontoDeColetaController` |
| Service | Regras de CRUD (buscar, salvar, atualizar, excluir) | `UsuarioService`, `MaterialService`, `PontoDeColetaService` |
| Repository | Acesso a dados com Spring Data JPA | `*Repository extends JpaRepository` |
| Persistência | Oracle; schema versionado pelo Flyway e validado pelo Hibernate | `V1__create_tables.sql` |
| Observabilidade | Spring Boot Actuator (`health`, `info`) | — |

```mermaid
flowchart TD
    C[Cliente HTTP] -->|Basic Auth| SEC[Spring Security]
    SEC --> CT[Controllers]
    CT --> SV[Services]
    SV --> R[Repositories JPA]
    R --> DB[(Oracle 23ai Free)]
    FW[Flyway - na inicialização] --> DB
    HC[/actuator/health/] --> DB
```

Modelo de dados (criado pela migration V1):

```mermaid
erDiagram
    TB_USUARIO ||--o{ TB_MATERIAL : "id_usuario (FK, opcional)"
    TB_USUARIO {
        NUMBER id PK
        VARCHAR2 nome
        VARCHAR2 email UK
    }
    TB_MATERIAL {
        NUMBER id PK
        VARCHAR2 nome
        VARCHAR2 tipo
        NUMBER peso
        NUMBER id_usuario FK
    }
    TB_PONTO_COLETA {
        NUMBER id PK
        VARCHAR2 nome
        VARCHAR2 endereco
        VARCHAR2 tipo_lixo
    }
```

## 1.2 Infraestrutura e entrega

```mermaid
flowchart LR
    Dev -->|push/PR| GH[GitHub]
    GH --> A[Actions: build + testes com Oracle]
    A --> E[Actions: compose e2e + smoke]
    E --> I[Actions: imagem multi-arch]
    I --> R[(GHCR)]
    I --> S[Deploy staging via SSH]
    S -->|smoke OK + aprovação| P[Deploy produção via SSH]
    R -. pull .-> SV1[VM staging: app + Oracle]
    R -. pull .-> SV2[VM produção: app + Oracle]
    S --> SV1
    P --> SV2
```

**Princípios adotados:**
- **Build once, deploy many:** a mesma imagem (`sha-<commit>`) passa por CI, staging e produção.
- **Configuração externa:** o código não muda entre ambientes, só o `.env` e os Secrets.
- **Isolamento:** cada ambiente tem servidor, banco, volume e rede próprios.
- **Portões de qualidade:** testes → e2e → staging + smoke → aprovação humana → produção.
