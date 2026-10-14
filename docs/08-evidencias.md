# 8. Evidências

> Preencha só com execuções reais. Salve as imagens em `docs/evidencias/` com os nomes sugeridos
> e cole aqui o link da execução no GitHub Actions.

## 8.1 Execução local

| # | Evidência | Comando / tela | Arquivo |
|---|---|---|---|
| L1 | Build da imagem | `docker build -t ecocycle-api:local .` (final do log) | `evidencias/L1-docker-build.png` |
| L2 | Compose válido | `docker compose config` | `evidencias/L2-compose-config.png` |
| L3 | Containers saudáveis | `docker compose ps` (app e oracle-db *healthy*) | `evidencias/L3-compose-ps.png` |
| L4 | Flyway executado | `docker compose logs app` (linhas *Flyway* / *Successfully applied 1 migration*) | `evidencias/L4-flyway.png` |
| L5 | Health | `curl http://localhost:8080/actuator/health` | `evidencias/L5-health.png` |
| L6 | CRUD | `./scripts/smoke-test.sh http://localhost:8080` | `evidencias/L6-smoke-local.png` |
| L7 | Rede e volume | `docker network ls` e `docker volume ls` | `evidencias/L7-network-volume.png` |
| L8 | Persistência | Criar um registro → `docker compose down` → `up` → registro ainda existe | `evidencias/L8-persistencia.png` |

## 8.2 GitHub Actions

| # | Evidência | Onde | Arquivo / link |
|---|---|---|---|
| G1 | Pipeline completo (grafo dos jobs) | Actions → execução da `main` | `evidencias/G1-pipeline.png` — link: _preencher_ |
| G2 | Testes no Job Summary | Job `Build e testes` → Summary | `evidencias/G2-testes.png` |
| G3 | Smoke test no Compose do CI | Job `Docker Compose (app + Oracle) e smoke test` | `evidencias/G3-e2e.png` |
| G4 | Imagem no GHCR com tags `sha-` e `latest` | Perfil/repo → Packages → ecocycle | `evidencias/G4-ghcr.png` |
| G5 | Secrets configurados (só os **nomes**, nunca os valores) | Settings → Environments | `evidencias/G5-environments.png` |
| G6 | Deploy staging + smoke | Job `Deploy staging` | `evidencias/G6-deploy-staging.png` |
| G7 | Aprovação manual de produção | Tela *Review deployments* | `evidencias/G7-aprovacao.png` |
| G8 | Deploy produção + smoke | Job `Deploy producao` | `evidencias/G8-deploy-producao.png` |
| G9 | Health dos ambientes pela URL pública | Navegador: `http://<IP>:8081/actuator/health` e `:8080` | `evidencias/G9-health-ambientes.png` |
| G10 | Containers no servidor | `docker compose -p ecocycle-production ps` via SSH | `evidencias/G10-servidor.png` |

## 8.3 URLs dos ambientes

| Ambiente | URL | Status |
|---|---|---|
| Staging | _não provisionado_ | — |
| Produção | _não provisionado_ | — |
