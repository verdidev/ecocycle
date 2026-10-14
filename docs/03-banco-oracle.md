# 3. Banco de dados Oracle

## 3.1 Decisão: Oracle em container

O projeto original usava o Oracle compartilhado da FIAP, com credenciais pessoais no código.
Para que CI, staging e produção sejam reproduzíveis, o Oracle passou a rodar em container,
**sem trocar de SGBD**:

| Item | Escolha | Motivo |
|---|---|---|
| Imagem | `gvenzl/oracle-free:23-slim` (Compose) e `23-slim-faststart` (CI) | Oracle Database 23ai Free, multi-arquitetura (amd64/arm64), configurável por variáveis, `healthcheck.sh` incluso |
| Usuário da aplicação | `APP_USER`/`APP_USER_PASSWORD` | O container cria o schema no PDB `FREEPDB1` na primeira subida |
| URL | `jdbc:oracle:thin:@//oracle-db:1521/FREEPDB1` | Formato *service name* (o original usava SID `ORCL`) |
| Persistência | volume nomeado em `/opt/oracle/oradata` | Os dados sobrevivem à recriação dos containers |

**Limitações conhecidas:**
- O Oracle Free usa até ~2 GB de RAM e 2 threads de CPU.
- A primeira inicialização com volume vazio leva alguns minutos.
- A imagem `gvenzl` é comunitária. A alternativa oficial é `container-registry.oracle.com/database/free`.

## 3.2 Flyway

1. A aplicação sobe e o Flyway (`spring-boot-starter-flyway`) aplica `db/migration/V1__create_tables.sql`
   e registra o resultado na tabela `flyway_schema_history`.
2. O Hibernate roda com `ddl-auto=validate`: só confere se entidades e tabelas batem, nunca altera o schema.
3. `spring.flyway.baseline-on-migrate` fica `false` por padrão. Com `true` em um schema que já tinha
   tabelas, o V1 seria marcado como "baseline" e **não executaria**. Isso explicava por que o projeto
   original "funcionava" no Oracle da FIAP mesmo com o mapeamento divergente.

**Por que adicionar o starter:** no Spring Boot 4 a autoconfiguração foi modularizada, e apenas
`flyway-core` no classpath não dispara as migrations.

## 3.3 Correções de mapeamento entidade × tabela

| Entidade | Antes | Problema | Correção |
|---|---|---|---|
| `Usuario.nome` | `@Column(name="nm_usuario")` | V1 cria `nome` → `validate` falha | `@Column(name="nome")` |
| `Usuario.email` | `@Column(name="ds_email")` | V1 cria `email` → `validate` falha | `@Column(name="email")` |
| `Material.peso` | `Double` sem definição | V1 cria `NUMBER(10,2)`; o Hibernate esperava `float` | `@Column(columnDefinition="NUMBER(10,2)")` |
| `PontoDeColeta.tipoResiduo` | `@Transient` | Valor enviado na API era descartado; coluna `tipo_lixo` nunca usada | `@Column(name="tipo_lixo")` |

A migration V1 **não foi alterada**: o código foi ajustado ao schema versionado.
A coluna `TB_MATERIAL.id_usuario` (FK opcional) continua sem mapeamento na entidade, como no original.

## 3.4 Script PL/SQL de monitoramento energético

O arquivo `Scripts PLSQL.sql` (DISPOSITIVO, SENSOR, CONSUMO_ENERGIA...) pertence a **outro projeto**
e não tem relação com as tabelas da EcoCycle. Por isso **não foi usado**.
