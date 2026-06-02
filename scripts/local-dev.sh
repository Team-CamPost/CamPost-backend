#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

# ── .env 파일 확인 ──────────────────────────────────────────────
if [[ ! -f .env ]]; then
  if [[ ! -f .env.example ]]; then
    echo "[ERROR] .env not found and .env.example is missing."
    echo "[ERROR] Create .env manually before running this script."
    exit 1
  fi
  cp .env.example .env
  echo "[INFO] Created .env from .env.example"
fi

if grep -q "<REQUIRED>" .env; then
  echo "[ERROR] .env contains <REQUIRED> placeholders."
  echo "        Fill required values before running backend locally."
  exit 1
fi

# ── .env 로드 ──────────────────────────────────────────────────
set -a
# shellcheck disable=SC1091
source .env
set +a

ensure_java_home() {
  local candidates=(
    "/c/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot"
    "/mnt/c/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot"
  )
  local candidate

  if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" && -x "$JAVA_HOME/bin/javac" ]]; then
    return
  fi

  if command -v cygpath >/dev/null 2>&1 && [[ -n "${JAVA_HOME:-}" ]]; then
    candidate="$(cygpath --unix "$JAVA_HOME" 2>/dev/null || true)"
    if [[ -n "$candidate" && -x "$candidate/bin/java" && -x "$candidate/bin/javac" ]]; then
      export JAVA_HOME="$candidate"
      return
    fi
  fi

  for candidate in "${candidates[@]}"; do
    if [[ -x "$candidate/bin/java" && -x "$candidate/bin/javac" ]]; then
      export JAVA_HOME="$candidate"
      export PATH="$JAVA_HOME/bin:$PATH"
      echo "[INFO] JAVA_HOME set to detected JDK: $JAVA_HOME"
      return
    fi
  done
}

ensure_java_home

# ── DB 기동 (Neon 사용 시 스킵) ────────────────────────────────
if echo "${SPRING_DATASOURCE_URL:-}" | grep -q "neon.tech"; then
  echo "[INFO] Neon DB detected — skipping local Docker DB startup."
else
  echo "[INFO] Starting local PostgreSQL (db) and Mailpit..."
  docker compose up -d db mailpit

  echo "[INFO] Waiting for db health check..."
  for _ in $(seq 1 60); do
    if docker compose ps db | grep -qw "healthy"; then
      break
    fi
    sleep 1
  done

  if ! docker compose ps db | grep -qw "healthy"; then
    echo "[ERROR] db is not healthy yet. Check logs with: docker compose logs db"
    exit 1
  fi
fi

echo "[INFO] Starting backend locally..."
exec ./mvnw spring-boot:run
