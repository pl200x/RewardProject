# RewardProject · Listening-Time Reward System

A microservice practice project built on **Spring Boot 3.4.3 / Java 17**: users report music listening time → the backend accumulates it and evaluates reward rules → prizes (coins / gems / coupons) are dispatched automatically. It consists of 3 **independent, individually startable** Spring Boot applications that call each other over HTTP (managed by a Maven aggregator), plus a React + Vite frontend.

## Architecture

```
                        ┌──────────────────────┐
   play report POST /music/play                │
        │               │  Music_management     │  :8081  (db: test)
        └──────────────▶│  music/users/likes/   │  Kafka producer+consumer, Redis, email
                        │  rankings             │
                        └──────────┬────────────┘
                                   │ HTTP POST /sync
                                   ▼
                        ┌──────────────────────┐
                        │     Multi_reward      │  :8080  (db: multi_reward)
                        │  orchestration:       │  JEXL rule engine / Kafka retry /
                        │  total time → stage & │  scheduled reconciliation
                        │  amount               │
                        └──────────┬────────────┘
                                   │ HTTP POST /prize/send_reward (cmd.code=coin/gem/coupon)
                                   ▼
                        ┌──────────────────────┐
                        │     PrizeCenter       │  :8082  (db: prize_center)
                        │  dispatch by code:    │  stock deduction + idempotent dedup
                        │  coin / gem / coupon  │  (unique out_biz_no)
                        └──────────────────────┘
```

**Core call chain**: `Music (/music/play)` → `Multi_reward (/sync)` → [JEXL rules evaluate stage / amount from today's accumulated time] → `PrizeCenter (/prize/send_reward)`.

## Tech Stack

- Spring Boot 3.4.3, Spring Web, Spring Scheduling/AOP
- MyBatis (mybatis-spring-boot-starter) + MySQL 8/9
- Redis (spring-data-redis + Redisson distributed lock)
- Kafka (spring-kafka: prize-dispatch retry + dead letter topics / async song-add & notifications)
- MinIO (object storage for audio files and avatars)
- Apache Commons JEXL (reward rule engine; rules live in DB and are hot-editable)
- Lombok, Logback (logstash encoder)
- Frontend: React + TypeScript + Vite

## Project Layout

```
RewardProject/
├── pom.xml                 # aggregator POM (grouping only; each module inherits spring-boot-parent independently)
├── Multi_reward/           # orchestration center            :8080
├── Music_management/       # music/users                     :8081
├── PrizeCenter/            # prize dispatch coin/gem/coupon  :8082
├── frontend/               # React + Vite frontend           :5173
├── sql/                    # schema + seed data for all 3 databases (import after clone)
│   ├── 01_multi_reward.sql
│   ├── 02_prize_center.sql
│   └── 03_test.sql
├── .env.example            # env var template (copy to .env and fill in real values; .env is gitignored)
└── scripts/                # local start/stop scripts
    ├── start-all.sh
    ├── stop-all.sh
    └── status.sh
```

## Prerequisites

| Component | Version | Notes |
|-----------|---------|-------|
| JDK | 17 | |
| Maven | 3.9+ | |
| MySQL | 8.0 / 9.x | needs the `multi_reward`, `prize_center`, `test` databases |
| Redis | 5+ | default `localhost:6379`, **no password** |
| Kafka | 3.x | default `localhost:9092` |
| MinIO | latest | default `localhost:9000` (audio/avatar storage, `scripts/start-minio.sh`) |
| Node.js | 18+ | frontend Vite dev server |

## Quick Start

```bash
# 1) Start the middleware (example; adapt to your machine)
redis-server &                     # Redis
#  Start Kafka however you installed it, listening on 9092
#  Start MySQL and make sure root login works

# 2) Import the databases (create schema + tables + seed data)
mysql -uroot -p < sql/01_multi_reward.sql
mysql -uroot -p < sql/02_prize_center.sql
mysql -uroot -p < sql/03_test.sql

# 3) Configure environment variables (DB password and other secrets; .env is gitignored)
cp .env.example .env && vim .env

# 4) Build and start all services
mvn clean package -DskipTests
./scripts/start-all.sh

# 5) Check status / stop
./scripts/status.sh
./scripts/stop-all.sh
```

> Startup logs go to `logs/`, process PIDs to `run/` (both gitignored).

## Services & Ports

| Service | Port | Database | Responsibilities |
|---------|------|----------|------------------|
| Multi_reward | 8080 | multi_reward | orchestration, rule evaluation, scheduled reconciliation, Kafka retry |
| Music_management | 8081 | test | music/users/likes/rankings, play reporting, Kafka |
| PrizeCenter | 8082 | prize_center | prize dispatch (coin/gem/coupon, selected by prize code) |

## Configuration

Each module configures its port, datasource, Redis and Kafka in its own `src/main/resources/application.properties`. All secrets (DB password, SMTP auth code, MinIO keys) are injected via environment variables — nothing sensitive is hardcoded in the repo:

```bash
cp .env.example .env   # fill in your local values (.env is gitignored and never committed)
```

`scripts/start-all.sh` loads `.env` automatically on startup; when running a single service from the IDE, set the same environment variables in the Run Configuration. Inter-service URLs default to `http://localhost:80xx` (local direct calls) and can be overridden, e.g. via `PRIZECENTER_BASE_URL`.

## Smoke Tests

```bash
# Read-only endpoints (also openable in a browser)
curl http://localhost:8082/prize/getallprize      # prize list
curl http://localhost:8081/music/list             # recommended music
curl http://localhost:8080/config/searchall       # reward rule configs
curl "http://localhost:8081/producer/send?content=hi"   # Kafka produce → consume

# End-to-end automatic reward: send 6 plays in a row
# (each duration ≤ 30; reaching 160 accumulated seconds today triggers a prize)
for i in 0 1 2 3 4 5; do
  curl -s -X POST http://localhost:8081/music/play -H "Content-Type: application/json" \
    -d "{\"userId\":1,\"musicId\":1,\"syncTime\":\"2026-01-01 14:0$i:00\",\"duration\":30,\"scene\":\"Listening_Music\",\"code\":\"coin\"}"; echo
done
# Expected: Multi_reward logs show REWARD_EVALUATED / PRIZE_SENT,
# prize.storage decreases, and a new prize_record row appears
```

Prize dispatch is idempotent: resending the same `out_biz_no` takes effect only once (returns `errorMessage: "same outbizno"`).

## Reliability: Dispatch Retry + Dead Letter Topic (DLT)

Prize dispatch moves value, so messages **must not be lost**. Failure handling has three layers:

```
main-flow sendPrize fails ──▶ REWARD_RETRY_TOPIC (Kafka retry queue)
                                 │ RetryConsumer consumes and dispatches again
                                 │ on failure → DefaultErrorHandler retries in place
                                 │              2 more times (2s apart)
                                 ▼ still failing
                           REWARD_RETRY_TOPIC.DLT (dead letter topic)
                                 │ DeadLetterConsumer logs ERROR
                                 │ (original topic + exception + full payload)
                                 ▼ inspect manually; once downstream recovers,
                                   re-publish the payload to REWARD_RETRY_TOPIC to redeliver
```

- **Non-recoverable errors don't waste retries**: corrupted payloads (deserialization failures) go straight to the DLT;
- **Replay is safe**: dispatch is idempotent on `out_biz_no` — replaying a message twice just hits the unique key and is skipped (log `PRIZE_DUPLICATE_SKIPPED`), never double-pays;
- Music_management's song-add / notification consumers use the same setup (`MUSIC.DLT` / `NOTIFICATION.DLT`).

Local demo (requires Kafka CLI):

```bash
# Poison message → no retry, straight to DLT; Multi_reward logs DLQ_RECEIVED
echo 'not-json' | kafka-console-producer --bootstrap-server localhost:9092 --topic REWARD_RETRY_TOPIC

# Downstream outage → retries exhausted → DLT: stop PrizeCenter, trigger a reward,
# and the logs show, in order:
#   PRIZE_SEND_FAILED → RETRY_ATTEMPT ×3 → DLQ_RECEIVED (payload fully preserved)
# After PrizeCenter recovers, re-publish the payload from .DLT back to
# REWARD_RETRY_TOPIC → PRIZE_SENT (redelivered successfully)
```

## Docker / Cloud Deployment

The whole system (middleware + 3 backends + frontend nginx) is containerized and starts with one command:

```bash
cp .env.example .env   # set DB_PASSWORD, PUBLIC_HOST, etc.
docker compose up -d --build
# open http://<PUBLIC_HOST>:<FRONTEND_PORT>
```

The production nginx mirrors the dev-time Vite proxy routes (`/api/*`, `/minio/*`), so the frontend needs zero changes.
For the full AWS EC2 runbook (instance type / security groups / cost / operations / FAQ) see **[DEPLOY.md](DEPLOY.md)**.

## Known Issues

- `GET /rank/top?N=k` returns 500 when the ranking Redis ZSet is empty (fresh system, no play data yet) — an empty id list produces `ORDER BY FIELD(id,)`. It works normally once play data fills the ranking.
