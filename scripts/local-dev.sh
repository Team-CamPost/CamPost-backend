#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

ensure_compose_env_file() {
  if [[ -f .env ]]; then
    return
  fi

  if [[ ! -f .env.example ]]; then
    echo "[ERROR] .env not found and .env.example is missing."
    echo "[ERROR] Create .env manually before running this script."
    exit 1
  fi

  cp .env.example .env
  echo "[INFO] Created .env from .env.example"
}

get_env_value() {
  local key="$1"
  grep -E "^${key}=" .env | head -n1 | cut -d '=' -f2- || true
}

validate_env_keys() {
  local missing=()
  local key value

  for key in "$@"; do
    value="$(get_env_value "$key")"
    if [[ -z "$value" || "$value" == "<REQUIRED>" ]]; then
      missing+=("$key")
    fi
  done

  if (( ${#missing[@]} > 0 )); then
    echo "[ERROR] .env is missing required values for db startup: ${missing[*]}"
    echo "[ERROR] Fill .env first (copied from .env.example if needed), then rerun."
    exit 1
  fi
}

if [[ ! -f .env.local ]]; then
  if [[ ! -f .env.local.example ]]; then
    echo "[ERROR] .env.local not found and .env.local.example is missing."
    exit 1
  fi

  cp .env.local.example .env.local
  echo "[INFO] Created .env.local from .env.local.example"
fi

if grep -q "<REQUIRED>" .env.local; then
  echo "[ERROR] .env.local contains <REQUIRED> placeholders."
  echo "        Fill required values before running backend locally."
  exit 1
fi

ensure_compose_env_file
validate_env_keys POSTGRES_DB POSTGRES_USER POSTGRES_PASSWORD

echo "[INFO] Starting PostgreSQL container (db)..."
docker compose up -d db

echo "[INFO] Waiting for db health check..."
for _ in $(seq 1 60); do
  if docker compose ps db | grep -q "healthy"; then
    break
  fi
  sleep 1
done

if ! docker compose ps db | grep -q "healthy"; then
  echo "[ERROR] db is not healthy yet. Check logs with: docker compose logs db"
  exit 1
fi

echo "[INFO] Loading .env.local and starting backend locally..."
set -a
# shellcheck disable=SC1091
source .env.local
set +a

exec ./mvnw spring-boot:run
