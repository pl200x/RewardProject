#!/usr/bin/env bash
#
# 启动 RewardProject 的全部前后端(本地):注册中心 + 4 个 Spring Boot 服务 + 前端 Vite。
#   - 检查依赖中间件(MySQL/Redis/Kafka)是否在跑
#   - 每次启动前执行 mvn clean package,确保可执行 jar 与当前源码一致
#   - 每次都先停掉已在运行的旧进程再重启,否则重新构建出的 jar 不会被加载
#   - 按依赖顺序启动:service-registry -> PrizeCenter -> Multi_reward -> Music_management -> ai_chatbot -> 前端(5173)
#   - 日志写到 logs/,PID 写到 run/,等待各服务就绪后汇总
#   - 只起后端(不起前端): ./scripts/start-all.sh --no-frontend
#   - 跳过重新构建(仅快速重启,复用已有 jar): ./scripts/start-all.sh --skip-build
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

# 4 个模块都是 <java.version>17</java.version>,但 Homebrew 的 mvn 默认跑在它自带的 openjdk
# (当前 25)上,Lombok 不支持,编译会报:
#   ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN
# 所以这里把构建和运行都显式钉到 JDK 17。需要换版本时:JAVA_MAJOR=21 ./scripts/start-all.sh
JAVA_MAJOR="${JAVA_MAJOR:-17}"
if [ -x /usr/libexec/java_home ]; then
  if jdk_home="$(/usr/libexec/java_home -v "$JAVA_MAJOR" 2>/dev/null)"; then
    export JAVA_HOME="$jdk_home"
    export PATH="$JAVA_HOME/bin:$PATH"
  else
    echo "[WARN] 未找到 JDK $JAVA_MAJOR,回退到默认 JDK,构建可能失败"
  fi
fi

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

export EUREKA_DEFAULT_ZONE="${EUREKA_DEFAULT_ZONE:-http://localhost:${REGISTRY_PORT:-8761}/eureka/}"

# 服务名 / 端口(下标对齐)。启动顺序:被调用方在前。
NAMES=(service-registry PrizeCenter Multi_reward Music_management ai_chatbot)
PORTS=("${REGISTRY_PORT:-8761}" "${PRIZE_PORT:-8082}" "${REWARD_PORT:-8080}" "${MUSIC_PORT:-8081}" "${CHATBOT_PORT:-8083}")

LOG_DIR="$ROOT/logs"
RUN_DIR="$ROOT/run"
mkdir -p "$LOG_DIR" "$RUN_DIR"

port_open() { nc -z 127.0.0.1 "$1" >/dev/null 2>&1; }

# 只列出「监听」该端口的进程。不能省略 -sTCP:LISTEN:否则连到该端口的客户端
# (浏览器、curl)也会被列出来,重启时会误杀。
listener_pids() { lsof -ti tcp:"$1" -sTCP:LISTEN 2>/dev/null; }

kill_pid() {  # $1=pid $2=name
  local pid="$1" name="$2"
  kill "$pid" 2>/dev/null || return 1
  for _ in $(seq 1 10); do kill -0 "$pid" 2>/dev/null || return 0; sleep 1; done
  echo "    [KILL -9] $name (pid $pid) 未优雅退出,强制结束"
  kill -9 "$pid" 2>/dev/null
}

# 重启前清场:先按 run/<name>.pid 停,再按端口兜底,确保旧 jar 的进程不会残留。
stop_service() {  # $1=name $2=port
  local name="$1" port="$2" pidf="$RUN_DIR/$1.pid" pid pids
  if [ -f "$pidf" ]; then
    pid="$(cat "$pidf" 2>/dev/null)"
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      kill_pid "$pid" "$name"
      echo "    [STOP]  $name 旧进程 (pid $pid)"
    fi
    rm -f "$pidf"
  fi
  pids="$(listener_pids "$port")"
  if [ -n "$pids" ]; then
    for pid in $pids; do kill_pid "$pid" "$name"; done
    echo "    [STOP]  $name 端口 $port 上的残留进程 ($pids)"
  fi
}

echo "==> 1) 检查本地依赖中间件"
miss=0
for dep in "MySQL:${DB_PORT:-3306}" "Redis:${REDIS_PORT:-6379}" "Kafka:${KAFKA_PORT:-9092}" "Seata:${SEATA_PORT:-18091}"; do
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
echo "    JDK: ${JAVA_HOME:-<系统默认>}"
if [ "$SKIP_BUILD" -eq 0 ]; then
  echo "    执行 mvn clean package -DskipTests,同步当前源码 ..."
  mvn -B -q -DskipTests clean package || { echo "    构建失败,退出"; exit 1; }
  echo "    [OK]   5 个后端 jar 已重新构建"
else
  echo "    [SKIP] 已通过 --skip-build 跳过重新构建"
  for i in "${!NAMES[@]}"; do
    jar="$ROOT/${NAMES[$i]}/target/${NAMES[$i]}-0.0.1-SNAPSHOT.jar"
    [ -f "$jar" ] || { echo "    缺少 $jar,无法跳过构建"; exit 1; }
  done
fi

echo "==> 3) 重启服务(先停旧进程,再用刚构建的 jar 启动)"
for i in "${!NAMES[@]}"; do
  name="${NAMES[$i]}"; port="${PORTS[$i]}"
  jar="$ROOT/$name/target/$name-0.0.1-SNAPSHOT.jar"
  pidf="$RUN_DIR/$name.pid"

  stop_service "$name" "$port"
  if port_open "$port"; then
    echo "    [FAIL] 端口 $port 仍被占用(不是本脚本启动的?),未启动 $name"
    continue
  fi
  # 控制台日志用 .console.log:Multi_reward 的 logback 写的是 logs/multi_reward.log,
  # macOS 文件名大小写不敏感,直接写 logs/Multi_reward.log 会把它截断。
  nohup java -Dlog.path="$LOG_DIR/$name-$port" -DLOG_HOME="$LOG_DIR/$name-$port" -jar "$jar" --server.port="$port" > "$LOG_DIR/$name.console.log" 2>&1 &
  echo $! > "$pidf"
  echo "    [START] $name (端口 $port, pid $!)"
done

echo "==> 4) 等待就绪"
fail=0
for i in "${!NAMES[@]}"; do
  name="${NAMES[$i]}"; port="${PORTS[$i]}"; log="$LOG_DIR/$name.console.log"
  pid="$(cat "$RUN_DIR/$name.pid" 2>/dev/null)"
  ok=0
  # 日志每次重启都被截断,所以 "Started ...Application in" 一定来自本次启动,不会误判成旧进程。
  for _ in $(seq 1 60); do
    if grep -qs "Started .*Application in" "$log"; then ok=1; break; fi
    if grep -qsiE "APPLICATION FAILED TO START|Error starting ApplicationContext" "$log"; then break; fi
    # 兜底:本次启动的进程还活着且端口已监听(清场过,不可能是别人的进程)
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null && port_open "$port"; then ok=1; break; fi
    sleep 1
  done
  if [ "$ok" -eq 1 ]; then
    echo "    [OK]   $name ($port)"
  else
    echo "    [FAIL] $name ($port) —— 见 logs/$name.console.log"
    fail=1
  fi
done

if [ "$WITH_FRONTEND" -eq 1 ]; then
  echo "==> 5) 重启前端 (Vite)"
  FRONT_DIR="$ROOT/frontend"
  FRONT_PIDF="$RUN_DIR/frontend.pid"
  # Vite 绑 localhost(macOS 常为 IPv6 ::1),用 lsof 检测端口,避免 nc 只查 IPv4 的误报
  front_up() { [ -n "$(listener_pids 5173)" ]; }
  # 一并重启:5173 上可能是别的目录/别的分支起的 Vite,那样会一直服务旧代码。
  stop_service frontend 5173
  if front_up; then
    echo "    [FAIL] 端口 5173 仍被占用,未启动前端"; fail=1
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
