# 5. Staging e Produção

## 5.1 Recomendação de infraestrutura

O requisito decisivo é **rodar o Oracle**. O Oracle Database Free precisa de cerca de 2 GB de RAM por
instância, com persistência em disco. Isso elimina a maioria dos PaaS gratuitos.

| Opção | Custo | Docker | Oracle | Integração com Actions | Avaliação |
|---|---|---|---|---|---|
| **Oracle Cloud (OCI) Always Free: VM Ampere A1 (ARM)** | Gratuito (pede cartão para verificar a conta) | Sim | Sim (`gvenzl/oracle-free` tem imagem arm64) | SSH | ✅ **Recomendada** |
| AWS / Azure / GCP: VM do free tier | Gratuito por 12 meses ou permanente, mas com ~1 GB de RAM | Sim | Não cabe com folga em 1 GB | SSH | ❌ RAM insuficiente |
| Render / Railway / Fly.io / Heroku | Free limitado ou pago | Parcial | Sem Oracle; volumes restritos ou pagos | Nativa | ❌ Não roda Oracle adequadamente |
| OCI Autonomous Database (Always Free) + app em PaaS | Gratuito | App sim | Oracle gerenciado, exige *wallet*/mTLS | Nativa | ⚠️ Possível, mas muda a arquitetura (sem Oracle no Compose) |
| PC próprio + *self-hosted runner* | Gratuito | Sim | Sim | Runner local | ⚠️ Só funciona com o PC ligado; não é um "ambiente real" |

**Atenção ao free tier da OCI:** em 2026 a Oracle reduziu a cota Always Free de Ampere A1 de
4 OCPU/24 GB para **2 OCPU/12 GB** no total. Isso continua suficiente para este projeto.

**Arquitetura recomendada (dentro da cota de 2 OCPU/12 GB):**

| Ambiente | Servidor | Shape | Stack |
|---|---|---|---|
| Staging | VM `ecocycle-staging` | VM.Standard.A1.Flex, 1 OCPU / 6 GB | app + Oracle (projeto Compose `ecocycle-staging`) |
| Produção | VM `ecocycle-prod` | VM.Standard.A1.Flex, 1 OCPU / 6 GB | app + Oracle (projeto Compose `ecocycle-production`) |

São dois servidores separados, cada um com banco, volume, rede e segredos próprios: isolamento
real entre staging e produção. A imagem é publicada como multi-arquitetura (amd64 + arm64) justamente
para rodar nessas VMs ARM.

**Alternativa econômica:** uma única VM (2 OCPU/12 GB) com as duas stacks. Nesse caso o `SSH_HOST`
é o mesmo nos dois Environments, as portas são diferentes (`APP_PORT` 8081 e 8080) e o isolamento
continua por projeto Compose (containers, rede e volume separados). O workflow suporta as duas
opções sem mudanças.

**Limitações a declarar no trabalho:**
- Tráfego HTTP sem TLS, com Basic Auth. Uma melhoria futura seria um proxy reverso com HTTPS (ex.: Caddy) e um domínio.
- A disponibilidade de A1 varia por região (erro *Out of host capacity*); pode ser preciso tentar outro *Availability Domain* ou horário.

## 5.2 Passo a passo (ações manuais necessárias)

> Nada abaixo foi executado automaticamente. São ações que exigem a sua conta.

### A. Repositório GitHub
1. Crie o repositório (de preferência **público**: minutos de Actions ilimitados e imagem GHCR pública).
2. Envie o código (`git init`, `git add .`, `git commit`, `git push -u origin main`).
3. Confirme que o primeiro pipeline (`build-test`, `compose-e2e`, `publish-image`) ficou verde.
4. Em **Packages → ecocycle → Package settings**, mude a visibilidade para **Public**.
   Se preferir manter privado, crie um PAT *classic* com `read:packages` e salve como secret de repositório `GHCR_PULL_TOKEN`.

### B. Servidores (Oracle Cloud)
1. Crie a conta OCI Always Free e escolha a *home region*.
2. Gere um par de chaves **exclusivo para deploy** no seu computador:
   `ssh-keygen -t ed25519 -f ecocycle_deploy -N ""`
3. Crie as VMs: **Ubuntu 24.04 (aarch64)**, shape **VM.Standard.A1.Flex** (1 OCPU / 6 GB cada),
   com IP público e a chave pública `ecocycle_deploy.pub`.
4. Em cada VM, instale o Docker e o plugin Compose:
   ```bash
   curl -fsSL https://get.docker.com | sudo sh
   sudo usermod -aG docker ubuntu   # depois saia e entre de novo na sessão SSH
   docker compose version
   ```
5. (Opcional, para acessar pela internet) Libere a porta da aplicação:
   - na OCI: VCN → Security List → Ingress TCP `8080` (produção) / `8081` (staging);
   - no Ubuntu da OCI, que tem regras de iptables próprias:
     ```bash
     sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8080 -j ACCEPT
     sudo netfilter-persistent save
     ```
6. Obtenha o *known_hosts* de cada servidor: `ssh-keyscan -H <IP_DA_VM>`.

### C. GitHub Environments e Secrets
Em **Settings → Environments**, crie:

**`staging`** (sem aprovação):

| Tipo | Nome | Valor |
|---|---|---|
| Secret | `SSH_HOST` | IP da VM de staging |
| Secret | `SSH_USER` | `ubuntu` |
| Secret | `SSH_PRIVATE_KEY` | conteúdo de `ecocycle_deploy` (chave privada) |
| Secret | `SSH_KNOWN_HOSTS` | saída do `ssh-keyscan -H <IP>` |
| Secret | `ORACLE_PASSWORD` | senha forte (letras e números) |
| Secret | `DB_PASSWORD` | senha forte (letras e números) |
| Secret | `APP_SECURITY_PASSWORD` | senha da API em staging |
| Variable | `APP_SECURITY_USERNAME` | ex.: `admin` |
| Variable | `APP_PORT` | `8081` |
| Variable | `APP_URL` | (opcional) `http://<IP>:8081` |

**`production`**: os mesmos nomes, com **valores diferentes** (outra VM, outras senhas), `APP_PORT=8080`, e mais:
- **Required reviewers**: adicione você (ou o professor/grupo). O deploy fica aguardando aprovação.
- **Deployment branches**: só `main`.

Por fim, em **Settings → Secrets and variables → Actions → Variables**, crie a variável de repositório
`DEPLOY_ENABLED` = `true` e reexecute o workflow.

## 5.3 Como o deploy funciona

1. O runner conecta via SSH com a chave de deploy, validando o host pelo `known_hosts`.
2. Envia `docker-compose.yml`, `deploy.sh`, `smoke-test.sh` e o `.env` montado a partir dos Secrets (com permissão `600` no servidor).
3. `deploy.sh` fixa `APP_IMAGE=ghcr.io/<owner>/ecocycle:sha-<commit>` e executa `docker compose pull` e `docker compose up -d --no-build --wait`.
4. O smoke test roda no servidor. Em staging é completo (CRUD); em produção é somente leitura.
5. Se `APP_URL` estiver definida, o runner também verifica o `/actuator/health` pela internet.
