#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

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
