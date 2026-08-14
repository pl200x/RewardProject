#!/usr/bin/env bash
#
# Export the legacy MinIO "music" bucket, then upload it to Amazon S3.
# The local export is deliberately kept as a rollback copy.
#
# Usage:
#   ./scripts/migrate-minio-to-s3.sh <s3-bucket> [export-directory]
#
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if [ -f "$ROOT/.env" ]; then
  set -a
  . "$ROOT/.env"
  set +a
fi

S3_BUCKET_ARG="${1:-}"
EXPORT_DIR="${2:-$ROOT/migration-export/music}"
AWS_REGION_VALUE="${AWS_REGION:-us-east-1}"
S3_PREFIX_VALUE="${S3_PREFIX:-audio}"
MINIO_ENDPOINT_VALUE="${MINIO_ENDPOINT:-http://127.0.0.1:9000}"
MINIO_BUCKET_VALUE="${MINIO_BUCKET:-music}"
MC_CONFIG_DIR_VALUE="$ROOT/migration-export/.mc-config"

if [ -z "$S3_BUCKET_ARG" ]; then
  echo "Usage: $0 <s3-bucket> [export-directory]"
  exit 2
fi
if [ -z "${MINIO_ACCESS_KEY:-}" ] || [ -z "${MINIO_SECRET_KEY:-}" ]; then
  echo "[FAIL] MINIO_ACCESS_KEY and MINIO_SECRET_KEY are required for the source export."
  exit 2
fi
command -v mc >/dev/null 2>&1 || { echo "[FAIL] MinIO Client (mc) is not installed."; exit 1; }
command -v aws >/dev/null 2>&1 || { echo "[FAIL] AWS CLI is not installed."; exit 1; }

mkdir -p "$EXPORT_DIR" "$MC_CONFIG_DIR_VALUE"

echo "==> 1) Verify AWS identity and destination bucket"
aws sts get-caller-identity --output table
aws s3api head-bucket --bucket "$S3_BUCKET_ARG" --region "$AWS_REGION_VALUE"

echo "==> 2) Export MinIO bucket to $EXPORT_DIR"
mc --config-dir "$MC_CONFIG_DIR_VALUE" alias set reward-minio \
  "$MINIO_ENDPOINT_VALUE" "$MINIO_ACCESS_KEY" "$MINIO_SECRET_KEY"
mc --config-dir "$MC_CONFIG_DIR_VALUE" mirror --overwrite \
  "reward-minio/$MINIO_BUCKET_VALUE" "$EXPORT_DIR"

DESTINATION="s3://$S3_BUCKET_ARG"
NORMALIZED_PREFIX="${S3_PREFIX_VALUE#/}"
NORMALIZED_PREFIX="${NORMALIZED_PREFIX%/}"
if [ -n "$NORMALIZED_PREFIX" ]; then
  DESTINATION="$DESTINATION/$NORMALIZED_PREFIX"
fi

echo "==> 3) Upload the export to $DESTINATION"
aws s3 sync "$EXPORT_DIR" "$DESTINATION" \
  --region "$AWS_REGION_VALUE" \
  --exclude "*" --include "*.mp3" \
  --content-type "audio/mpeg" \
  --only-show-errors

echo "==> 4) List migrated objects"
aws s3 ls "$DESTINATION/" --recursive --region "$AWS_REGION_VALUE"
echo "[OK] Migration finished. Local rollback copy kept at: $EXPORT_DIR"
