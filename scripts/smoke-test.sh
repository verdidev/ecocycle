#!/usr/bin/env bash
# =============================================================================
# Smoke test da EcoCycle API
#
# Uso:
#   APP_SECURITY_USERNAME=admin APP_SECURITY_PASSWORD=... \
#     ./scripts/smoke-test.sh http://localhost:8080            # completo (cria e apaga dados)
#   ./scripts/smoke-test.sh http://localhost:8080 --readonly   # somente leitura (producao)
#
# Verifica: health UP, seguranca (401 sem login) e, no modo completo, um CRUD
# real em /usuarios, /materiais e /pontos (os registros criados sao removidos).
# =============================================================================
set -euo pipefail

BASE_URL="${1:?informe a URL base, ex.: http://localhost:8080}"
MODE="${2:-full}"
USER_NAME="${APP_SECURITY_USERNAME:?defina APP_SECURITY_USERNAME}"
USER_PASS="${APP_SECURITY_PASSWORD:?defina APP_SECURITY_PASSWORD}"
HEALTH_RETRIES="${HEALTH_RETRIES:-30}"

pass() { echo "  [OK]   $*"; }
fail() { echo "  [FALHA] $*" >&2; exit 1; }

# Faz uma requisicao e devolve "<status>\n<corpo>"
req() { # metodo caminho [json] [auth=yes|no]
  local method="$1" path="$2" body="${3:-}" auth="${4:-yes}"
  local args=(-s -o /tmp/smoke_body -w '%{http_code}' -X "$method" "${BASE_URL}${path}")
  [[ "$auth" == "yes" ]] && args+=(-u "${USER_NAME}:${USER_PASS}")
  [[ -n "$body" ]] && args+=(-H 'Content-Type: application/json' -d "$body")
  curl "${args[@]}"
}

expect() { # status_esperado descricao metodo caminho [json] [auth]
  local expected="$1" desc="$2"; shift 2
  local code; code="$(req "$@")"
  if [[ "$code" == "$expected" ]]; then
    pass "$desc -> HTTP $code"
  else
    fail "$desc -> esperado $expected, recebido $code: $(cat /tmp/smoke_body)"
  fi
}

assert_body() { # texto descricao
  if grep -q "$1" /tmp/smoke_body; then pass "$2"; else fail "$2: '$1' nao encontrado em $(cat /tmp/smoke_body)"; fi
}

extract_id() { grep -o '"id"[[:space:]]*:[[:space:]]*[0-9]*' /tmp/smoke_body | head -1 | grep -o '[0-9]*$'; }

echo "== Smoke test em ${BASE_URL} (modo: ${MODE}) =="

echo "-- 1. Health check"
for i in $(seq 1 "$HEALTH_RETRIES"); do
  code="$(req GET /actuator/health '' no || true)"
  if [[ "$code" == "200" ]] && grep -q '"UP"' /tmp/smoke_body; then
    pass "/actuator/health -> $(cat /tmp/smoke_body)"; break
  fi
  [[ "$i" == "$HEALTH_RETRIES" ]] && fail "aplicacao nao ficou saudavel (ultimo status: $code)"
  echo "  aguardando aplicacao... ($i/$HEALTH_RETRIES)"; sleep 5
done

echo "-- 2. Seguranca"
expect 401 "GET /usuarios sem autenticacao" GET /usuarios '' no
expect 200 "GET /usuarios autenticado"      GET /usuarios
expect 200 "GET /materiais autenticado"     GET /materiais
expect 200 "GET /pontos autenticado"        GET /pontos

if [[ "$MODE" == "--readonly" ]]; then
  echo "== Smoke test (somente leitura) concluido com sucesso =="
  exit 0
fi

echo "-- 3. CRUD /usuarios"
EMAIL="smoke-$(date +%s)-$RANDOM@ecocycle.test"
expect 201 "POST /usuarios" POST /usuarios "{\"nome\":\"Smoke Test\",\"email\":\"${EMAIL}\"}"
UID_CRIADO="$(extract_id)"
expect 200 "GET /usuarios/${UID_CRIADO}" GET "/usuarios/${UID_CRIADO}"
expect 200 "PUT /usuarios/${UID_CRIADO}" PUT "/usuarios/${UID_CRIADO}" "{\"nome\":\"Smoke Test 2\",\"email\":\"${EMAIL}\"}"
assert_body 'Smoke Test 2' "nome atualizado"
expect 204 "DELETE /usuarios/${UID_CRIADO}" DELETE "/usuarios/${UID_CRIADO}"
expect 404 "GET /usuarios/${UID_CRIADO} apos exclusao" GET "/usuarios/${UID_CRIADO}"

echo "-- 4. CRUD /materiais"
expect 201 "POST /materiais" POST /materiais '{"nome":"Garrafa PET","tipo":"Plastico","peso":1.75}'
MID="$(extract_id)"
expect 200 "GET /materiais/${MID}" GET "/materiais/${MID}"
assert_body '1.75' "peso decimal persistido"
expect 204 "DELETE /materiais/${MID}" DELETE "/materiais/${MID}"

echo "-- 5. CRUD /pontos"
expect 201 "POST /pontos" POST /pontos '{"nome":"Ecoponto Smoke","endereco":"Rua Teste, 1","tipoResiduo":"Vidro"}'
PID="$(extract_id)"
expect 200 "GET /pontos/${PID}" GET "/pontos/${PID}"
assert_body 'Vidro' "tipoResiduo persistido"
expect 204 "DELETE /pontos/${PID}" DELETE "/pontos/${PID}"

echo "== Smoke test completo concluido com sucesso =="
