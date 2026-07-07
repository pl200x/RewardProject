#!/usr/bin/env bash
#
# 停止 RewardProject 的全部前后端(3 个 Spring Boot 服务 + 前端 Vite)。
#   优先用 run/<name>.pid 里的 PID;若无 PID 文件,则按端口兜底查找并结束。
#
set -u

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT/run"

NAMES=(Music_management Multi_reward PrizeCenter)
PORTS=(8081             8080         8082)

stop_pid() {  # $1=pid $2=name
  local pid="$1" name="$2"
  kill "$pid" 2>/dev/null || return 1
  for _ in $(seq 1 10); do kill -0 "$pid" 2>/dev/null || return 0; sleep 1; done
  echo "    [KILL -9] $name (pid $pid) 未优雅退出,强制结束"
  kill -9 "$pid" 2>/dev/null
}

echo "==> 停止服务"
for i in "${!NAMES[@]}"; do
  name="${NAMES[$i]}"; port="${PORTS[$i]}"
  pidf="$RUN_DIR/$name.pid"
  stopped=0

  if [ -f "$pidf" ]; then
    pid="$(cat "$pidf" 2>/dev/null)"
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      stop_pid "$pid" "$name" && echo "    [STOP] $name (pid $pid)"
      stopped=1
    fi
    rm -f "$pidf"
  fi

  if [ "$stopped" -eq 0 ]; then
    # 兜底:按端口找(例如服务不是用本脚本启动的)
    pids="$(lsof -ti tcp:"$port" 2>/dev/null)"
    if [ -n "$pids" ]; then
      for pid in $pids; do stop_pid "$pid" "$name"; done
      echo "    [STOP] $name (端口 $port: $pids)"
      stopped=1
    fi
  fi

  [ "$stopped" -eq 0 ] && echo "    [--]   $name 未在运行"
done

# 前端 (Vite 5173)
front_pidf="$RUN_DIR/frontend.pid"
front_stopped=0
if [ -f "$front_pidf" ]; then
  pid="$(cat "$front_pidf" 2>/dev/null)"
  if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
    stop_pid "$pid" "前端" && echo "    [STOP] 前端 (pid $pid)"
    front_stopped=1
  fi
  rm -f "$front_pidf"
fi
if [ "$front_stopped" -eq 0 ]; then
  pids="$(lsof -ti tcp:5173 2>/dev/null)"
  if [ -n "$pids" ]; then
    for pid in $pids; do stop_pid "$pid" "前端"; done
    echo "    [STOP] 前端 (端口 5173: $pids)"
    front_stopped=1
  fi
fi
[ "$front_stopped" -eq 0 ] && echo "    [--]   前端 未在运行"

echo "完成。"
