# 9. Checklist final

Legenda: ✅ Concluído · ⚠️ Pendente de configuração externa ou de execução real · ❌ Não concluído

> Atualize este checklist depois da primeira execução real do pipeline e da configuração dos servidores.

| # | Requisito | Status | Onde |
|---|---|---|---|
| 1 | Pipeline CI/CD | ✅ Implementado · ⚠️ falta a 1ª execução no GitHub | `.github/workflows/ci-cd.yml` |
| 2 | Build automático | ✅ Implementado · ⚠️ falta a 1ª execução | job `build-test` |
| 3 | Testes automatizados | ✅ Implementado · ⚠️ falta a 1ª execução | `src/test`, job `build-test` |
| 4 | Deploy automático em staging | ⚠️ Implementado, depende do servidor e dos secrets | `deploy.yml`, Environment `staging` |
| 5 | Deploy automático em produção (com aprovação) | ⚠️ Implementado, depende do servidor, dos secrets e dos reviewers | `deploy.yml`, Environment `production` |
| 6 | Dockerfile funcional | ✅ Implementado · ⚠️ build real ocorre no CI | `Dockerfile` |
| 7 | Docker Compose | ✅ Implementado (`docker compose config` validado) | `docker-compose.yml` |
| 8 | Aplicação + banco | ✅ app + Oracle Free | `docker-compose.yml` |
| 9 | Volumes | ✅ `oracle-data` | `docker-compose.yml` |
| 10 | Variáveis de ambiente | ✅ `.env.example`, `application.properties` | — |
| 11 | Redes | ✅ `ecocycle-net` | `docker-compose.yml` |
| 12 | README.md | ✅ | `README.md` |
| 13 | Documentação técnica (base do PDF/PPT) | ✅ textos · ⚠️ falta gerar o PDF/PPT final | `docs/` |
| 14 | Evidências / prints | ⚠️ coletar após as execuções reais | `docs/08-evidencias.md` |
| 15 | Checklist | ✅ | este arquivo |
| 16 | Health check | ✅ `/actuator/health` | `SecurityConfig`, `application.properties` |
| 17 | Segredos fora do Git | ✅ · ⚠️ trocar a senha antiga da conta Oracle FIAP | `.gitignore`, GitHub Secrets |
