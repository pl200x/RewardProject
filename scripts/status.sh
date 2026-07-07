#!/usr/bin/env bash
#
# 查看 RewardProject 各服务与依赖中间件的运行状态。
#
set -u

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT/run"

NAMES=(Multi_reward Music_management PrizeCenter)
PORTS=(8080         8081             8082)

port_open() { nc -z 127.0.0.1 "$1" >/dev/null 2>&1; }

echo "== 中间件 =="
for dep in "MySQL:3306" "Redis:6379" "Kafka:9092" "MinIO:9000"; do
  name="${dep%%:*}"; port="${dep##*:}"
  if port_open "$port"; then echo "  [UP]   $name ($port)"; else echo "  [DOWN] $name ($port)"; fi
done

echo "== 服务 =="
for i in "${!NAMES[@]}"; do
  name="${NAMES[$i]}"; port="${PORTS[$i]}"
  pidf="$RUN_DIR/$name.pid"
  pid="-"
  [ -f "$pidf" ] && pid="$(cat "$pidf" 2>/dev/null)"
  if port_open "$port"; then
    printf "  [UP]   %-18s 端口 %s  pid %s\n" "$name" "$port" "$pid"
  else
    printf "  [DOWN] %-18s 端口 %s\n" "$name" "$port"
  fi
done

echo "== 前端 =="
front_pidf="$RUN_DIR/frontend.pid"
front_pid="-"
[ -f "$front_pidf" ] && front_pid="$(cat "$front_pidf" 2>/dev/null)"
# Vite 绑 localhost(macOS 常为 IPv6),用 lsof 检测,避免 nc 只查 IPv4 的误报
if lsof -ti tcp:5173 >/dev/null 2>&1; then
  printf "  [UP]   %-18s 端口 %s  pid %s  ->  http://localhost:5173\n" "frontend(Vite)" "5173" "$front_pid"
else
  printf "  [DOWN] %-18s 端口 %s\n" "frontend(Vite)" "5173"
fi
