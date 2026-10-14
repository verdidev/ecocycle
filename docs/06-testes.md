# 6. Estratégia de testes

## 6.1 Situação original
Havia só `EcocycleApplicationTests.contextLoads()` (`@SpringBootTest`). Ele dependia do Oracle da
FIAP e das credenciais fixas, então **não rodava no CI**. Também não cobria nenhuma regra.

## 6.2 Pirâmide adotada

| Nível | Classes | Ferramentas | Banco | O que valida |
|---|---|---|---|---|
| Unitário (Service) | `UsuarioServiceTest`, `MaterialServiceTest`, `PontoDeColetaServiceTest` | JUnit 5, Mockito, AssertJ | Não | `atualizar` altera os campos; lança exceção quando o id não existe e não chama `save`; `excluir` delega ao repositório |
| Web (Controller) | `UsuarioControllerTest`, `MaterialControllerTest`, `PontoDeColetaControllerTest` | MockMvc *standalone* | Não | Rotas, JSON e status 200/201/204/404 |
| Integração | `EcocycleApplicationTests` | `@SpringBootTest` | **Oracle real** | Contexto completo: Flyway aplica a V1 e o Hibernate valida o schema |
| Integração ponta a ponta | `EcocycleIntegrationTests` | `@SpringBootTest(RANDOM_PORT)` + `java.net.http.HttpClient` | **Oracle real** | Health público; 401 sem credenciais; CRUD de usuário; `peso` decimal; `tipoResiduo` persistido |
| Smoke (pós-deploy) | `scripts/smoke-test.sh` | curl | Ambiente real | Health, segurança e CRUD (staging) / leitura (produção) |

## 6.3 Como os testes de integração encontram o banco
Os testes de integração usam `@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")`:
- **Local sem banco:** `mvn test` roda só os unitários e os de controller, e ignora os de integração.
- **CI:** o job `build-test` sobe `gvenzl/oracle-free` como *service container* e define `DB_URL`,
  então **todos** os testes rodam contra Oracle real.
- **Local com banco:** suba o Oracle com `docker-compose.dev.yml`, exporte `DB_URL`, `DB_USERNAME`,
  `DB_PASSWORD`, `APP_SECURITY_USERNAME` e `APP_SECURITY_PASSWORD`, e rode `./mvnw test`.

Optamos por *service container* em vez de Testcontainers porque é mais simples de explicar, não
adiciona dependências ao `pom.xml` e o mesmo Oracle serve aos dois testes de integração.

## 6.4 Evidência
O job `build-test` publica uma tabela de resultados no *Job Summary* e o artefato `surefire-reports`.
