# 7. Desafios encontrados e soluções

| # | Desafio | Impacto | Solução |
|---|---|---|---|
| 1 | Texto inválido `cl` após uma `</dependency>` no `pom.xml` | Maven não lia o POM, **nenhum build funcionava** | Texto removido |
| 2 | Spring Boot 4 modularizou a autoconfiguração; só `flyway-core` não executa migrations | Banco novo ficava sem tabelas | `spring-boot-starter-flyway` no lugar de `flyway-core` |
| 3 | Entidade `Usuario` mapeada para `nm_usuario`/`ds_email`, mas a V1 cria `nome`/`email` | Com `ddl-auto=validate`, a aplicação não subia em banco novo | Mapeamento ajustado à migration |
| 4 | `Material.peso` (`Double`) × `NUMBER(10,2)` | Risco de falha na validação de tipo do Hibernate | `columnDefinition = "NUMBER(10,2)"` |
| 5 | `PontoDeColeta.tipoResiduo` era `@Transient` | Dado aceito pela API era perdido | Mapeado para a coluna existente `tipo_lixo` |
| 6 | `baseline-on-migrate=true` mascarava os problemas no Oracle da FIAP (V1 nunca executada) | Divergência invisível até usar banco novo | Padrão `false`, configurável por variável |
| 7 | Credenciais do Oracle e da API no `application.properties`, no README e em `target/` | Vazamento de segredos | Tudo via variáveis de ambiente, `.env.example`, `.gitignore`, GitHub Secrets. **Recomendado trocar a senha antiga** |
| 8 | Dockerfile com `openjdk:17` (imagem descontinuada) e bytecode Java 21 | `UnsupportedClassVersionError`; build dependia de JAR local | Multi-stage com Temurin 21, build a partir do código |
| 9 | O único teste dependia do Oracle da FIAP | CI impossível | Testes unitários e de controller sem banco + integração com Oracle efêmero no CI |
| 10 | Oracle pesado (~2 GB) e lento na primeira subida | Timeouts em Compose e CI | Healthcheck com `start_period`, `depends_on: service_healthy`, `--wait-timeout`, imagem `faststart` no CI |
| 11 | Free tiers comuns têm ~1 GB de RAM e PaaS gratuitos não rodam Oracle | Staging/produção inviáveis nesses serviços | OCI Always Free (ARM) + imagem multi-arquitetura |
| 12 | Collection Insomnia (`/api/Lixeiras`, porta 5126) e o script PL/SQL de energia eram de outros projetos | Risco de testar/documentar a aplicação errada | Descartados; criado `scripts/smoke-test.sh` com os 15 endpoints reais |
| 13 | Ausência de health check | Impossível verificar o deploy | Spring Boot Actuator com `/actuator/health` público (sem detalhes) |
