#!/usr/bin/env bash
set -euo pipefail

CHATBOT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROJECT_ROOT="$(cd "$CHATBOT_ROOT/.." && pwd)"

# Project defaults, local overrides, then the legacy chatbot-local file.
for env_file in "$PROJECT_ROOT/.env" "$PROJECT_ROOT/.env.local" "$CHATBOT_ROOT/.env"; do
  if [ -f "$env_file" ]; then
    set -a
    . "$env_file"
    set +a
  fi
done

if [ -z "${OPENAI_API_KEY:-}" ] || [[ "$OPENAI_API_KEY" == replace-with-* ]]; then
  echo "[FAIL] Configure OPENAI_API_KEY in a git-ignored .env.local or ai_chatbot/.env"
  exit 1
fi

exec "$CHATBOT_ROOT/mvnw" -f "$CHATBOT_ROOT/pom.xml" spring-boot:run
