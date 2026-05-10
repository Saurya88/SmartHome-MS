#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

log() {
  printf '\n[%s] %s\n' "$(date +%H:%M:%S)" "$1"
}

extract_field() {
  local body="$1"
  local field="$2"
  printf '%s' "$body" | sed -n "s/.*\"${field}\":\"\\([^\"]*\\)\".*/\\1/p"
}

request() {
  local method="$1"
  local path="$2"
  local expected_status="$3"
  local data="${4:-}"

  local tmp
  tmp="$(mktemp)"

  local status
  if [[ -n "$data" ]]; then
    status="$(curl -sS -o "$tmp" -w "%{http_code}" \
      -X "$method" \
      -H "Content-Type: application/json" \
      -d "$data" \
      "${BASE_URL}${path}")"
  else
    status="$(curl -sS -o "$tmp" -w "%{http_code}" \
      -X "$method" \
      "${BASE_URL}${path}")"
  fi

  local body
  body="$(cat "$tmp")"
  rm -f "$tmp"

  if [[ "$status" != "$expected_status" ]]; then
    printf 'Request failed: %s %s\nExpected: %s\nActual:   %s\nBody: %s\n' \
      "$method" "$path" "$expected_status" "$status" "$body" >&2
    exit 1
  fi

  printf '%s' "$body"
}

log "Creating a device through the API Gateway"
device_body="$(request POST /api/v1/devices 201 '{
  "deviceName":"Front Door Lock",
  "deviceType":"LOCK",
  "status":"ONLINE",
  "firmwareVersion":"1.2.3",
  "capabilities":["LOCK"],
  "connectivityType":"ZIGBEE"
}')"
device_id="$(extract_field "$device_body" "deviceId")"
[[ -n "$device_id" ]] || { echo "Could not extract deviceId" >&2; exit 1; }

log "Getting devices through the API Gateway"
request GET /api/v1/devices 200 >/dev/null

log "Creating a user with device permission through the API Gateway"
user_body="$(request POST /api/v1/users 201 "{
  \"username\":\"scene-owner\",
  \"email\":\"owner@smarthome.com\",
  \"role\":\"OWNER\",
  \"permissions\":[
    {
      \"deviceId\":\"${device_id}\",
      \"accessLevel\":\"FULL_ACCESS\"
    }
  ]
}")"
user_id="$(extract_field "$user_body" "userId")"
[[ -n "$user_id" ]] || { echo "Could not extract userId" >&2; exit 1; }

log "Getting users through the API Gateway"
request GET /api/v1/users 200 >/dev/null

log "Creating a low-level automation scene through the API Gateway"
scene_body="$(request POST /api/v1/scenes 201 '{
  "name":"Gateway Smoke Scene",
  "status":"INACTIVE"
}')"
scene_id="$(extract_field "$scene_body" "sceneId")"
[[ -n "$scene_id" ]] || { echo "Could not extract sceneId" >&2; exit 1; }

log "Getting scenes through the API Gateway"
request GET /api/v1/scenes 200 >/dev/null

log "Creating a scene orchestration aggregate through the API Gateway"
orchestration_body="$(request POST /api/v1/scene-orchestrations 201 "{
  \"ownerUserId\":\"${user_id}\",
  \"name\":\"Night Routine\",
  \"status\":\"ACTIVE\",
  \"deviceActions\":[
    {
      \"deviceId\":\"${device_id}\",
      \"actionType\":\"LOCK\",
      \"parameters\":\"{}\"
    }
  ],
  \"triggerRules\":[
    {
      \"triggerId\":\"night-trigger\",
      \"triggerType\":\"TIME_BASED\",
      \"conditionExpression\":\"time == 22:00\",
      \"enabled\":true,
      \"triggerValue\":\"22:00\"
    }
  ]
}")"
orchestration_id="$(extract_field "$orchestration_body" "orchestrationId")"
[[ -n "$orchestration_id" ]] || { echo "Could not extract orchestrationId" >&2; exit 1; }

log "Getting all scene orchestrations"
request GET /api/v1/scene-orchestrations 200 >/dev/null

log "Getting the created scene orchestration by id"
request GET "/api/v1/scene-orchestrations/${orchestration_id}" 200 >/dev/null

log "Updating the scene orchestration"
request PUT "/api/v1/scene-orchestrations/${orchestration_id}" 200 "{
  \"ownerUserId\":\"${user_id}\",
  \"name\":\"Night Routine - Scheduled\",
  \"status\":\"SCHEDULED\",
  \"deviceActions\":[
    {
      \"deviceId\":\"${device_id}\",
      \"actionType\":\"LOCK\",
      \"parameters\":\"{\\\"delay\\\":0}\"
    }
  ],
  \"triggerRules\":[
    {
      \"triggerId\":\"night-trigger\",
      \"triggerType\":\"TIME_BASED\",
      \"conditionExpression\":\"time == 23:00\",
      \"enabled\":true,
      \"triggerValue\":\"23:00\"
    }
  ]
}" >/dev/null

log "Deleting the scene orchestration"
request DELETE "/api/v1/scene-orchestrations/${orchestration_id}" 204 >/dev/null

log "System integration checks completed successfully"
