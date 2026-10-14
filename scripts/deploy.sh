#!/usr/bin/env bash
# =============================================================================
# Deploy da EcoCycle em um servidor com Docker (executado VIA SSH pelo pipeline)
#
# Uso (no servidor, dentro da pasta do ambiente):
#   ./deploy.sh <staging|production> <imagem>
#   ex.: ./deploy.sh staging ghcr.io/usuario/ecocycle:sha-1a2b3c4
#
# Pre-requisitos na pasta: docker-compose.yml e .env (enviados pelo pipeline).
# Cada ambiente usa um projeto Compose proprio (ecocycle-staging / ecocycle-production),
# portanto containers, rede e volume do Oracle ficam isolados.
# =============================================================================
set -euo pipefail

ENV_NAME="${1:?informe o ambiente (staging|production)}"
IMAGE="${2:?informe a imagem completa com tag}"
PROJECT="ecocycle-${ENV_NAME}"

cd "$(dirname "$0")"
[[ -f docker-compose.yml ]] || { echo "docker-compose.yml nao encontrado em $(pwd)"; exit 1; }
[[ -f .env ]] || { echo ".env nao encontrado em $(pwd)"; exit 1; }

# Fixa a imagem desta versao no .env (permite reiniciar o servidor com a mesma versao)
if grep -q '^APP_IMAGE=' .env; then
  sed -i "s|^APP_IMAGE=.*|APP_IMAGE=${IMAGE}|" .env
else
  echo "APP_IMAGE=${IMAGE}" >> .env
fi

echo ">> [${ENV_NAME}] baixando imagem ${IMAGE}"
docker compose -p "${PROJECT}" pull app

echo ">> [${ENV_NAME}] subindo Oracle + aplicacao (aguardando healthchecks)"
docker compose -p "${PROJECT}" up -d --no-build --wait --wait-timeout 900

docker compose -p "${PROJECT}" ps

echo ">> [${ENV_NAME}] evidencias do Flyway"
docker compose -p "${PROJECT}" logs app 2>&1 | grep -iE 'flyway|migrat|schema' | tail -n 10 || true

# Remove imagens antigas sem uso (o volume do Oracle NAO e afetado)
docker image prune -f >/dev/null

echo ">> [${ENV_NAME}] deploy concluido: ${IMAGE}"
