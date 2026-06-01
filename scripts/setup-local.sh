#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

if [[ ! -f .env ]]; then
  if [[ ! -f .env.example ]]; then
    echo "[ERROR] .env.example not found."
    exit 1
  fi

  cp .env.example .env
  echo "[INFO] Created .env from .env.example"
  echo "[INFO] Fill required values in .env before running backend."
else
  echo "[INFO] .env already exists."
fi

if grep -q "<REQUIRED>" .env; then
  echo "[WARN] .env still has <REQUIRED> placeholders."
  echo "[WARN] Please edit .env before local backend run."
fi

echo "[INFO] Local setup check complete."
