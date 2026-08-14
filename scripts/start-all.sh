#!/usr/bin/env bash
#
# 启动 RewardProject 的全部前后端(本地):4 个 Spring Boot 服务 + 前端 Vite。
#   - 检查依赖中间件(MySQL/Redis/Kafka)是否在跑
#   - 每次启动前执行 mvn clean package,确保可执行 jar 与当前源码一致
#   - 按依赖顺序启动:PrizeCenter -> Multi_reward -> Music_management -> ai_chatbot -> 前端(5173)
#   - 日志写到 logs/,PID 写到 run/,等待各服务就绪后汇总
#   - 只起后端(不起前端): ./scripts/start-all.sh --no-frontend
#   - 跳过重新构建(仅快速启动): ./scripts/start-all.sh --skip-build
#
set -u

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

# 先加载通用/部署配置,再加载本地覆盖。.env.local 不入库。
for env_file in "$ROOT/.env" "$ROOT/.env.local" "$ROOT/ai_chatbot/.env"; do
  if [ -f "$env_file" ]; then
    set -a
    . "$env_file"
    set +a
  fi
done

# 启动选项。参数可以组合使用,例如:--no-frontend --skip-build
WITH_FRONTEND=1
SKIP_BUILD=0
for arg in "$@"; do
  case "$arg" in
    --no-frontend) WITH_FRONTEND=0 ;;
    --skip-build) SKIP_BUILD=1 ;;
    *) echo "未知参数:$arg"; exit 2 ;;
  esac
done

# 服务名 / 端口(下标对齐)。启动顺序:被调用方在前。
NAMES=(PrizeCenter Multi_reward Music_management ai_chatbot)
PORTS=(8082        8080         8081             8083)

LOG_DIR="$ROOT/logs"
RUN_DIR="$ROOT/run"
mkdir -p "$LOG_DIR" "$RUN_DIR"

port_open() { nc -z 127.0.0.1 "$1" >/dev/null 2>&1; }

echo "==> 1) 检查本地依赖中间件"
miss=0
for dep in "MySQL:${DB_PORT:-3306}" "Redis:${REDIS_PORT:-6379}" "Kafka:${KAFKA_PORT:-9092}"; do
  name="${dep%%:*}"; port="${dep##*:}"
  if port_open "$port"; then
    echo "    [OK]   $name ($port)"
  else
    echo "    [WARN] $name ($port) 未运行 —— 依赖它的功能会失败"
    miss=1
  fi
done
[ "$miss" -eq 1 ] && echo "    (提示:运行 scripts/local-infra.sh start 可一次启动全部本地中间件)"
[ -z "${S3_BUCKET:-}" ] && echo "    [WARN] S3_BUCKET 未配置 —— 音频上传和播放会失败"
if [ -n "${S3_ENDPOINT:-}" ]; then
  if curl -sS --connect-timeout 2 -o /dev/null "$S3_ENDPOINT"; then
    echo "    [OK]   S3-compatible storage ($S3_ENDPOINT)"
  else
    echo "    [WARN] S3-compatible storage 不可达 ($S3_ENDPOINT)"
    miss=1
  fi
fi

echo "==> 2) 构建可执行 jar"
if [ "$SKIP_BUILD" -eq 0 ]; then
  echo "    执行 mvn clean package -DskipTests,同步当前源码 ..."
  mvn -B -q -DskipTests clean package || { echo "    构建失败,退出"; exit 1; }
  echo "    [OK]   4 个后端 jar 已重新构建"
else
  echo "    [SKIP] 已通过 --skip-build 跳过重新构建"
  for i in "${!NAMES[@]}"; do
    jar="$ROOT/${NAMES[$i]}/target/${NAMES[$i]}-0.0.1-SNAPSHOT.jar"
    [ -f "$jar" ] || { echo "    缺少 $jar,无法跳过构建"; exit 1; }
  done
fi

echo "==> 3) 启动服务"
for i in "${!NAMES[@]}"; do
  name="${NAMES[$i]}"; port="${PORTS[$i]}"
  jar="$ROOT/$name/target/$name-0.0.1-SNAPSHOT.jar"
  pidf="$RUN_DIR/$name.pid"

  if [ -f "$pidf" ] && kill -0 "$(cat "$pidf" 2>/dev/null)" 2>/dev/null; then
    echo "    [SKIP] $name 已在运行 (pid $(cat "$pidf"))"
    continue
  fi
  if port_open "$port"; then
    echo "    [SKIP] 端口 $port 已被占用,未启动 $name"
    continue
  fi
  nohup java -jar "$jar" > "$LOG_DIR/$name.log" 2>&1 &
  echo $! > "$pidf"
  echo "    [START] $name (端口 $port, pid $!)"
done

echo "==> 4) 等待就绪"
fail=0
for i in "${!NAMES[@]}"; do
  name="${NAMES[$i]}"; port="${PORTS[$i]}"; log="$LOG_DIR/$name.log"
  ok=0
  for _ in $(seq 1 60); do
    if grep -qs "Started .*Application in" "$log"; then ok=1; break; fi
    if grep -qsiE "APPLICATION FAILED TO START|Error starting ApplicationContext" "$log"; then break; fi
    if port_open "$port" && [ ! -f "$RUN_DIR/$name.pid" ]; then ok=1; break; fi
    sleep 1
  done
  if [ "$ok" -eq 1 ] || port_open "$port"; then
    echo "    [OK]   $name ($port)"
  else
    echo "    [FAIL] $name ($port) —— 见 logs/$name.log"
    fail=1
  fi
done

if [ "$WITH_FRONTEND" -eq 1 ]; then
  echo "==> 5) 启动前端 (Vite)"
  FRONT_DIR="$ROOT/frontend"
  FRONT_PIDF="$RUN_DIR/frontend.pid"
  # Vite 绑 localhost(macOS 常为 IPv6 ::1),用 lsof 检测端口,避免 nc 只查 IPv4 的误报
  front_up() { lsof -ti tcp:5173 >/dev/null 2>&1; }
  if [ -f "$FRONT_PIDF" ] && kill -0 "$(cat "$FRONT_PIDF" 2>/dev/null)" 2>/dev/null; then
    echo "    [SKIP] 前端已在运行 (pid $(cat "$FRONT_PIDF"))"
  elif front_up; then
    echo "    [SKIP] 端口 5173 已被占用,未启动前端"
  elif [ ! -x "$FRONT_DIR/node_modules/.bin/vite" ]; then
    echo "    [WARN] 未安装前端依赖,请先执行: cd frontend && npm install"
  else
    (
      cd "$FRONT_DIR" || exit
      nohup "$FRONT_DIR/node_modules/.bin/vite" > "$LOG_DIR/frontend.log" 2>&1 &
      echo $! > "$FRONT_PIDF"
    )
    okf=0
    for _ in $(seq 1 30); do front_up && { okf=1; break; }; sleep 1; done
    if [ "$okf" -eq 1 ]; then
      echo "    [OK]   前端 (5173)  ->  http://localhost:5173"
    else
      echo "    [FAIL] 前端 (5173) —— 见 logs/frontend.log"; fail=1
    fi
  fi
fi

echo ""
echo "完成。前端: http://localhost:5173  |  日志: logs/  |  停止: scripts/stop-all.sh  |  状态: scripts/status.sh"
exit $fail
