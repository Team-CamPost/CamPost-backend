#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

echo "[INFO] Running integration smoke (db + backend + pipeline)..."
docker compose up -d --build db backend pipeline
docker compose ps

echo "[INFO] Recent backend logs"
docker compose logs --no-color --tail=80 backend || true

echo "[INFO] Recent pipeline logs"
docker compose logs --no-color --tail=80 pipeline || true
