#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

if [[ ! -f .env.local ]]; then
  if [[ ! -f .env.local.example ]]; then
    echo "[ERROR] .env.local.example not found."
    exit 1
  fi

  cp .env.local.example .env.local
  echo "[INFO] Created .env.local from .env.local.example"
  echo "[INFO] Fill required values in .env.local before running backend."
else
  echo "[INFO] .env.local already exists."
fi

if grep -q "<REQUIRED>" .env.local; then
  echo "[WARN] .env.local still has <REQUIRED> placeholders."
  echo "[WARN] Please edit .env.local before local backend run."
fi

echo "[INFO] Local setup check complete."
