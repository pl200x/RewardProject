#!/usr/bin/env bash
#
# 启动本机 MinIO(对象存储,存音乐音频)。
#   二进制: ~/minio-tools/minio (官方 dl.min.io 下载;brew 版在本机 macOS 上启动即 SIGSEGV,勿用)
#   数据:   ~/minio/data (含 bucket: music=音频, test=历史练习)
#   端口:   9000 (S3 API) / 9001 (Web 控制台)   凭据: minioadmin / minioadmin
#
set -u
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
mkdir -p "$ROOT/logs" "$ROOT/run"

BIN="$HOME/minio-tools/minio"
DATA="$HOME/minio/data"
[ -x "$BIN" ] || { echo "[FAIL] 未找到 $BIN (从 https://dl.min.io/server/minio/release/darwin-arm64/minio 下载并 chmod +x)"; exit 1; }

if lsof -ti tcp:9000 >/dev/null 2>&1; then
  echo "[SKIP] 端口 9000 已被占用(MinIO 可能已在运行)"
  exit 0
fi

MINIO_ROOT_USER=minioadmin MINIO_ROOT_PASSWORD=minioadmin \
  nohup "$BIN" server "$DATA" --address ":9000" --console-address ":9001" > "$ROOT/logs/minio.log" 2>&1 &
echo $! > "$ROOT/run/minio.pid"

for _ in $(seq 1 15); do
  curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:9000/minio/health/live" 2>/dev/null | grep -q 200 && {
    echo "[OK] MinIO 已就绪  API:9000  控制台: http://localhost:9001  (pid $(cat "$ROOT/run/minio.pid"))"
    exit 0
  }
  sleep 1
done
echo "[FAIL] MinIO 未就绪,见 logs/minio.log"
exit 1
