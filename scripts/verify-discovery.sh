#!/usr/bin/env bash
# Isolated, disposable verification. Does not source private .env files or use development volumes.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
mode="${1:-smoke}"
case "$mode" in
  smoke|--full) ;;
  *) echo 'Usage: scripts/verify-discovery.sh [--full]'; exit 2 ;;
esac
project="reward-discovery-test-$$"
read -r DB_PORT REDIS_PORT KAFKA_PORT S3_LOCAL_PORT S3_CONSOLE_PORT SEATA_PORT < <(python3 - <<'PY'
import socket
sockets = [socket.socket() for _ in range(6)]
for sock in sockets: sock.bind(('127.0.0.1', 0))
print(*(sock.getsockname()[1] for sock in sockets))
for sock in sockets: sock.close()
PY
)
export DB_PORT REDIS_PORT KAFKA_PORT S3_LOCAL_PORT S3_CONSOLE_PORT SEATA_PORT
export SEATA_ENABLED=true SEATA_SERVER_ADDR="127.0.0.1:$SEATA_PORT"
export DB_HOST=127.0.0.1 DB_USERNAME=root DB_PASSWORD=reward-local-password
export REDIS_HOST=127.0.0.1 REDIS_PASSWORD= KAFKA_SERVERS="localhost:$KAFKA_PORT"
export AWS_REGION=us-east-1 AWS_ACCESS_KEY_ID=rewardlocal AWS_SECRET_ACCESS_KEY=reward-local-secret
export S3_BUCKET=music S3_ENDPOINT="http://127.0.0.1:$S3_LOCAL_PORT" S3_PATH_STYLE_ACCESS=true
export JWT_SECRET=reward-project-local-jwt-secret-at-least-32-bytes OPENAI_API_KEY=test-key
export DISCOVERY_ENABLED=true RECONCILIATION_ENABLED=false
unset SPRING_PROFILES_ACTIVE SPRING_APPLICATION_JSON SERVER_PORT EUREKA_DEFAULT_ZONE
unset SERVICE_HOSTNAME SERVICE_IP_ADDRESS SERVICE_INSTANCE_ID EUREKA_PREFER_IP_ADDRESS
compose=(docker compose --env-file .env.local.example -p "$project" -f docker-compose.dev.yml)
cleanup() {
  mkdir -p target/seata-it
  "${compose[@]}" logs --no-color seata-server mysql > target/seata-it/infrastructure.log 2>&1 || true
  # Only this invocation's containers and newly created disposable volumes.
  "${compose[@]}" down --volumes --remove-orphans >/dev/null 2>&1 || true
}
trap cleanup EXIT
"${compose[@]}" up -d --wait mysql redis kafka minio seata-server
"${compose[@]}" run --rm --no-deps seata-schema
"${compose[@]}" run --rm --no-deps minio-init
for attempt in $(seq 1 60); do
  if "${compose[@]}" exec -T mysql mysqladmin ping -h127.0.0.1 -uroot -preward-local-password --silent >/dev/null 2>&1 &&
     "${compose[@]}" exec -T kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:29092 --list >/dev/null 2>&1; then
    break
  fi
  if [ "$attempt" -eq 60 ]; then echo 'Test middleware did not become ready'; exit 1; fi
  sleep 1
done
# Build first; --full also runs all Maven/Feign/database/discovery tests in this isolated environment.
if [ "$mode" = --full ]; then
  mvn -B -pl PrizeCenter -DskipTests package
  RUN_SEATA_TESTS=true RUN_DATABASE_TESTS=true DISCOVERY_ENABLED=false mvn -B -Pdiscovery-it clean verify
else
  mvn -B -DskipTests clean package
fi
python3 scripts/verify-discovery.py
