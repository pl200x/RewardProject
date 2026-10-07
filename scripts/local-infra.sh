#!/usr/bin/env bash
# Manage the Docker middleware used by host-run Spring services.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="$ROOT/docker-compose.dev.yml"
LOCAL_ENV_FILE="${LOCAL_ENV_FILE:-$ROOT/.env.local}"

if [ ! -f "$LOCAL_ENV_FILE" ]; then
  echo "[FAIL] Missing $LOCAL_ENV_FILE"
  echo "       Run: cp $ROOT/.env.local.example $ROOT/.env.local"
  exit 1
fi

compose() {
  docker compose --env-file "$LOCAL_ENV_FILE" -f "$COMPOSE_FILE" "$@"
}

case "${1:-start}" in
  start)
    compose up -d --wait mysql redis kafka minio seata-server
    compose run --rm --no-deps seata-schema
    compose run --rm --no-deps minio-init
    compose ps
    ;;
  stop)
    # Named volumes are intentionally retained.
    compose down
    ;;
  status)
    compose ps
    ;;
  logs)
    shift
    compose logs -f "$@"
    ;;
  *)
    echo "Usage: $0 {start|stop|status|logs [service]}"
    exit 2
    ;;
esac
