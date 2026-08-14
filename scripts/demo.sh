#!/usr/bin/env bash
set -euo pipefail

BASE="${BASE:-http://localhost:8080}"
OPS_TOKEN="${OPS_TOKEN:-dev-ops-token}"
USER_NAME="${USER_NAME:-alice}"
PASSWORD="${PASSWORD:-secret1}"

echo "== health =="
curl -sS "${BASE}/actuator/health" | python -m json.tool || curl -sS "${BASE}/actuator/health"
echo

echo "== register (ignore if already exists) =="
curl -sS -X POST "${BASE}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${USER_NAME}\",\"password\":\"${PASSWORD}\",\"nickname\":\"Alice\"}" || true
echo

echo "== login =="
LOGIN=$(curl -sS -X POST "${BASE}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${USER_NAME}\",\"password\":\"${PASSWORD}\"}")
echo "${LOGIN}"
echo

echo "== online =="
curl -sS "${BASE}/api/ops/online" -H "X-Ops-Token: ${OPS_TOKEN}"
echo
echo
echo "Swagger: ${BASE}/swagger-ui.html"
echo "Debug:   ${BASE}/debug.html"
echo "Health:  ${BASE}/actuator/health"
