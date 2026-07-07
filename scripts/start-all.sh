#!/usr/bin/env bash
#
# 启动 RewardProject 的全部前后端(本地):3 个 Spring Boot 服务 + 前端 Vite。
#   - 检查依赖中间件(MySQL/Redis/Kafka)是否在跑
#   - 若缺少可执行 jar,自动 mvn package
#   - 按依赖顺序启动:PrizeCenter -> Multi_reward -> Music_management -> 前端(5173)
#   - 日志写到 logs/,PID 写到 run/,等待各服务就绪后汇总
#   - 只起后端(不起前端): ./scripts/start-all.sh --no-frontend
#
set -u

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

# 加载本地环境变量(数据库密码/邮箱授权码等敏感配置,.env 不入库,见 .env.example)
if [ -f "$ROOT/.env" ]; then
  set -a
  . "$ROOT/.env"
  set +a
fi

# 是否同时起前端(默认起;传 --no-frontend 只起后端)
WITH_FRONTEND=1
[ "${1:-}" = "--no-frontend" ] && WITH_FRONTEND=0

# 服务名 / 端口(下标对齐)。启动顺序:被调用方在前。
NAMES=(PrizeCenter Multi_reward Music_management)
PORTS=(8082        8080         8081)

LOG_DIR="$ROOT/logs"
RUN_DIR="$ROOT/run"
mkdir -p "$LOG_DIR" "$RUN_DIR"

port_open() { nc -z 127.0.0.1 "$1" >/dev/null 2>&1; }

echo "==> 1) 检查依赖中间件"
miss=0
for dep in "MySQL:3306" "Redis:6379" "Kafka:9092" "MinIO:9000"; do
  name="${dep%%:*}"; port="${dep##*:}"
  if port_open "$port"; then
    echo "    [OK]   $name ($port)"
  else
    echo "    [WARN] $name ($port) 未运行 —— 依赖它的功能会失败"
    miss=1
  fi
done
[ "$miss" -eq 1 ] && echo "    (提示:Redis 用 'redis-server';Kafka 自行启动;MySQL 需导入 sql/;MinIO 用 scripts/start-minio.sh)"

echo "==> 2) 检查可执行 jar"
need_build=0
for i in "${!NAMES[@]}"; do
  [ -f "$ROOT/${NAMES[$i]}/target/${NAMES[$i]}-0.0.1-SNAPSHOT.jar" ] || need_build=1
done
if [ "$need_build" -eq 1 ]; then
  echo "    缺少 jar,执行 mvn package -DskipTests ..."
  mvn -B -q -DskipTests package || { echo "    构建失败,退出"; exit 1; }
else
  echo "    已存在,跳过构建(如需重建可先运行: mvn clean package -DskipTests)"
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
