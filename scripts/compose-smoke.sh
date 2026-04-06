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
		echo "[ERROR] Create .env manually before running integration smoke."
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
		echo "[ERROR] .env is missing required values for integration smoke: ${missing[*]}"
		echo "[ERROR] Fill .env first (copied from .env.example if needed), then rerun."
		exit 1
	fi
}

ensure_compose_env_file
validate_env_keys \
	POSTGRES_DB \
	POSTGRES_USER \
	POSTGRES_PASSWORD \
	SPRING_DATASOURCE_URL \
	SPRING_DATASOURCE_USERNAME \
	SPRING_DATASOURCE_PASSWORD \
	APP_CORS_ALLOWED_ORIGINS \
	JWT_SECRET \
	JWT_EXPIRY_MS \
	CRAWL_INTERVAL_MINUTES \
	HEADLESS \
	RAW_STORE_DIR \
	AI_RESULT_DIR

echo "[INFO] Running integration smoke (db + backend + pipeline)..."
docker compose up -d --build db backend pipeline
docker compose ps

echo "[INFO] Recent backend logs"
docker compose logs --no-color --tail=80 backend || true

echo "[INFO] Recent pipeline logs"
docker compose logs --no-color --tail=80 pipeline || true
