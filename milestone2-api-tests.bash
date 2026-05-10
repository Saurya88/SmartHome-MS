#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

SEED_OWNER_ID="aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
SEED_DEVICE_ID="11111111-1111-1111-1111-111111111111"

curl_json() {
  local method="$1"
  local path="$2"
  local body="${3:-}"

  if [[ -n "${body}" ]]; then
    curl -sS -X "${method}" \
      -H "Content-Type: application/json" \
      -d "${body}" \
      "${BASE_URL}${path}"
  else
    curl -sS -X "${method}" "${BASE_URL}${path}"
  fi
}

curl_status() {
  local method="$1"
  local path="$2"
  local body="${3:-}"

  if [[ -n "${body}" ]]; then
    curl -sS -o /dev/null -w "%{http_code}" -X "${method}" \
      -H "Content-Type: application/json" \
      -d "${body}" \
      "${BASE_URL}${path}"
  else
    curl -sS -o /dev/null -w "%{http_code}" -X "${method}" "${BASE_URL}${path}"
  fi
}

assert_status() {
  local expected="$1"
  local method="$2"
  local path="$3"
  local body="${4:-}"
  local actual

  actual="$(curl_status "${method}" "${path}" "${body}")"
  if [[ "${actual}" != "${expected}" ]]; then
    echo "Expected ${method} ${path} to return ${expected}, got ${actual}"
    if [[ -n "${body}" ]]; then
      curl_json "${method}" "${path}" "${body}" || true
    else
      curl_json "${method}" "${path}" || true
    fi
    echo
    exit 1
  fi
}

wait_for_gateway() {
  local smoke_id="00000000-0000-0000-0000-000000000000"
  local status

  for _ in {1..30}; do
    status="$(curl_status "DELETE" "/api/v1/scene-orchestrations/${smoke_id}" 2>/dev/null || true)"
    if [[ "${status}" == "404" || "${status}" == "204" ]]; then
      return 0
    fi
    sleep 2
  done

  echo "API Gateway did not become ready at ${BASE_URL}"
  exit 1
}

json_field() {
  local json="$1"
  local field="$2"
  local value

  value="$(printf '%s' "${json}" | tr -d '\r\n' | sed -nE "s/.*\"${field}\"[[:space:]]*:[[:space:]]*\"([^\"]+)\".*/\1/p")"
  if [[ -z "${value}" ]]; then
    echo "Missing JSON field '${field}' in response:"
    printf '%s\n' "${json}"
    exit 1
  fi
  printf '%s' "${value}"
}

new_id() {
  local prefix="$1"
  printf '%s-%s' "${prefix}" "$(date +%s%N)"
}

echo "== Smoke check: Docker/API Gateway is reachable =="
wait_for_gateway

echo "== Device Service through API Gateway: POST + GET by id =="
DEVICE_NAME="M2 Audit Lock $(new_id lock)"
DEVICE_BODY="{\"deviceName\":\"${DEVICE_NAME}\",\"deviceType\":\"LOCK\",\"firmwareVersion\":\"1.2.3\",\"capabilities\":[\"LOCK\"],\"connectivityType\":\"ZIGBEE\"}"
DEVICE_RESP="$(curl_json "POST" "/api/v1/devices" "${DEVICE_BODY}")"
DEVICE_ID="$(json_field "${DEVICE_RESP}" "deviceId")"
assert_status "200" "GET" "/api/v1/devices/${DEVICE_ID}"

echo "== User Permissions Service through API Gateway: POST + GET by id =="
USERNAME="owner_m2_$(date +%s%N)"
USER_BODY="{\"username\":\"${USERNAME}\",\"email\":\"${USERNAME}@smarthome.com\",\"role\":\"OWNER\",\"permissions\":[{\"deviceId\":\"${DEVICE_ID}\",\"accessLevel\":\"CONTROL\"}]}"
USER_RESP="$(curl_json "POST" "/api/v1/users" "${USER_BODY}")"
USER_ID="$(json_field "${USER_RESP}" "userId")"
assert_status "200" "GET" "/api/v1/users/${USER_ID}"

echo "== Automation Service through API Gateway: Scene POST + GET by id =="
SCENE_NAME="M2 Audit Scene $(new_id scene)"
SCENE_BODY="{\"name\":\"${SCENE_NAME}\",\"status\":\"INACTIVE\"}"
SCENE_RESP="$(curl_json "POST" "/api/v1/scenes" "${SCENE_BODY}")"
SCENE_ID="$(json_field "${SCENE_RESP}" "sceneId")"
assert_status "200" "GET" "/api/v1/scenes/${SCENE_ID}"

echo "== Automation Service through API Gateway: Trigger Rule POST + GET by id =="
TRIGGER_ID="$(new_id m2-trigger)"
TRIGGER_BODY="{\"triggerId\":\"${TRIGGER_ID}\",\"triggerType\":\"TIME_BASED\",\"conditionExpression\":\"time == 22:00\",\"enabled\":true}"
TRIGGER_RESP="$(curl_json "POST" "/api/v1/trigger-rules" "${TRIGGER_BODY}")"
CREATED_TRIGGER_ID="$(json_field "${TRIGGER_RESP}" "triggerId")"
assert_status "200" "GET" "/api/v1/trigger-rules/${CREATED_TRIGGER_ID}"

echo "== Aggregator through API Gateway: GET ALL, POST, GET, PUT, DELETE =="
assert_status "200" "GET" "/api/v1/scene-orchestrations"

ORCH_TRIGGER_ID="$(new_id m2-orch-trigger)"
ORCH_BODY="{\"ownerUserId\":\"${SEED_OWNER_ID}\",\"name\":\"M2 Orchestrated Night Routine\",\"status\":\"ACTIVE\",\"deviceActions\":[{\"deviceId\":\"${SEED_DEVICE_ID}\",\"actionType\":\"TURN_ON\",\"parameters\":\"{}\"}],\"triggerRules\":[{\"triggerId\":\"${ORCH_TRIGGER_ID}\",\"triggerType\":\"TIME_BASED\",\"conditionExpression\":\"time == 22:00\",\"enabled\":true,\"triggerValue\":\"22:00\"}]}"
ORCH_RESP="$(curl_json "POST" "/api/v1/scene-orchestrations" "${ORCH_BODY}")"
ORCH_ID="$(json_field "${ORCH_RESP}" "orchestrationId")"
assert_status "200" "GET" "/api/v1/scene-orchestrations/${ORCH_ID}"

ORCH_UPDATED_TRIGGER_ID="$(new_id m2-orch-trigger)"
ORCH_PUT_BODY="{\"ownerUserId\":\"${SEED_OWNER_ID}\",\"name\":\"M2 Orchestrated Night Routine Updated\",\"status\":\"INACTIVE\",\"deviceActions\":[{\"deviceId\":\"${SEED_DEVICE_ID}\",\"actionType\":\"TURN_OFF\",\"parameters\":\"{}\"}],\"triggerRules\":[{\"triggerId\":\"${ORCH_UPDATED_TRIGGER_ID}\",\"triggerType\":\"TIME_BASED\",\"conditionExpression\":\"time == 23:00\",\"enabled\":true,\"triggerValue\":\"23:00\"}]}"
assert_status "200" "PUT" "/api/v1/scene-orchestrations/${ORCH_ID}" "${ORCH_PUT_BODY}"
assert_status "204" "DELETE" "/api/v1/scene-orchestrations/${ORCH_ID}"

echo "All Milestone 2 API-Gateway tests passed."
