# AWS 部署：EC2 + Docker Compose + S3

这套方案面向演示环境和低流量试运行：一台 EC2 运行前端、四个业务后端、Eureka 注册中心以及
MySQL/Redis/Kafka，音乐文件存入 Amazon S3。它的重点是成本和可理解性，不是高可用。

```text
Browser ── HTTPS ──> EC2 / Caddy ──> frontend nginx ──> 4 Spring services
                            │
                            ├── MySQL / Redis / Kafka (Docker volumes)
                            └── private Amazon S3 (presigned audio URLs)
```

S3 bucket 始终保持私有。浏览器播放音乐时，后端生成一个一小时有效的预签名 URL；
浏览器不需要 AWS 凭据，也不需要公开 bucket。

## 1. 创建私有 S3 bucket

在与 EC2 相同的 Region 创建一个名称全局唯一的 bucket，例如
`reward-project-audio-<account-id>`。

建议设置：

- Block Public Access：四项全部开启；
- Default encryption：SSE-S3 即可；
- Bucket Versioning：演示环境可选，担心误删时建议开启；
- Object Ownership：Bucket owner enforced。

应用把音频写到 `audio/<歌曲标题>.mp3`。默认头像已经随前端发布，不再存入 S3。

如果使用域名，把 [`infra/s3/cors.json`](infra/s3/cors.json) 中的域名替换后应用：

```bash
aws s3api put-bucket-cors \
  --bucket <你的-bucket> \
  --cors-configuration file://infra/s3/cors.json
```

## 2. 给 EC2 配置 IAM Role

不要把长期 Access Key 写进 EC2 的 `.env`。

1. 打开 IAM，创建一个信任实体为 **EC2** 的 Role；
2. 复制 [`infra/s3/ec2-role-policy.json`](infra/s3/ec2-role-policy.json)；
3. 把其中的 `REPLACE_WITH_YOUR_BUCKET` 换成真实 bucket 名；
4. 将该策略附加到 Role，再把 Role 关联到 EC2 实例。

这份策略只允许应用列出、读取、上传和删除 `audio/` 下的对象；前缀以外仍不可见。
生成预签名 URL 不需要额外的公开权限。

AWS SDK 使用默认凭据链：本地可读取 AWS profile 或临时环境变量，EC2 上会自动读取
实例 IAM Role 的临时凭据。参考
[AWS SDK 默认凭据链](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials-chain.html)。

## 3. 创建 EC2

推荐起点：

| 配置 | 建议 |
| --- | --- |
| Region | `us-east-1`，或离主要访问者最近的 Region |
| AMI | Ubuntu Server 24.04 LTS |
| Instance type | `t3.medium`，4 GiB RAM |
| Disk | 30 GiB gp3 |
| IAM instance profile | 上一步创建的 Role |

安全组入站规则：

| 端口 | 来源 |
| --- | --- |
| 22 | 仅你的 IP |
| 80 | `0.0.0.0/0` |
| 443 | `0.0.0.0/0` |

不再开放 8761、9000、3306、6379、8080、8081、8082、8083 或 9092。

在 Advanced details 中将实例元数据设置为：

- Metadata version：IMDSv2 only；
- Metadata response hop limit：`2`。

Docker 容器访问 EC2 IAM Role 时需要额外一跳。AWS 也建议容器环境把 hop limit 设为
2，见 [EC2 IMDS 配置说明](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/configuring-IMDS-new-instances.html)。

## 4. 安装并启动

连接实例后安装 Docker：

```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu
exit
```

重新连接，然后部署：

```bash
git clone https://github.com/pl200x/RewardProject.git
cd RewardProject
cp .env.example .env
```

至少修改以下变量：

```dotenv
DB_PASSWORD=<新的强密码>
JWT_SECRET=<openssl rand -base64 48 的结果>
OPENAI_API_KEY=<OpenAI project API key>
OPENAI_MODEL=gpt-5.4-mini
AWS_REGION=us-east-1
S3_BUCKET=<你的-bucket>
S3_PREFIX=audio
FRONTEND_BASE_URL=https://music.example.com
```

EC2 上不要设置下面三项，保持 `.env.example` 中的注释状态：

```dotenv
# AWS_ACCESS_KEY_ID=
# AWS_SECRET_ACCESS_KEY=
# AWS_SESSION_TOKEN=
```

如果暂时只用 IP 和 HTTP：

```dotenv
FRONTEND_BIND=0.0.0.0
FRONTEND_PORT=80
```

如果准备配置域名和 HTTPS，让应用只监听本机 8080，由 Caddy 对外：

```dotenv
FRONTEND_BIND=127.0.0.1
FRONTEND_PORT=8080
```

启动：

```bash
docker compose up -d --build
docker compose ps
docker compose logs --tail=100 music
docker compose logs --tail=100 chatbot
```

首次构建通常需要数分钟。`mysql` 的 health check 通过后，四个后端才会依次启动。
Compose 已为所有容器设置日志轮转，避免长期运行占满磁盘。

如果数据库卷由旧版本创建，Docker 不会再次自动执行新增的初始化脚本。请在首次启动
chatbot 前运行一次幂等迁移：

```bash
docker compose exec -T mysql sh -c 'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD"' < sql/07_ai_chatbot.sql
```

## 5. 把旧 MinIO 音频迁到 S3

代码迁移和数据迁移是两件事：

- 新版本应用只从 S3 的 `audio/` 前缀读写；
- 旧 MinIO 文件不会自动消失，也不会被部署过程删除；
- 迁移脚本先保留一份本地导出，再上传到 S3，方便回滚。

在仍能访问旧 MinIO 的电脑上安装 AWS CLI 和 MinIO Client (`mc`)，让 AWS CLI 登录到
有目标 bucket 写权限的身份，然后设置旧 MinIO 凭据：

```bash
export MINIO_ENDPOINT=http://127.0.0.1:9000
export MINIO_ACCESS_KEY=<旧 MinIO access key>
export MINIO_SECRET_KEY=<旧 MinIO secret key>
export AWS_REGION=us-east-1
export S3_PREFIX=audio

./scripts/migrate-minio-to-s3.sh <你的-bucket>
```

脚本会：

1. 验证当前 AWS 身份和目标 bucket；
2. 把旧 `music` bucket 导出到 `migration-export/music`；
3. 只同步 `.mp3` 文件到 `s3://<bucket>/audio/`；
4. 列出迁移后的对象，但保留本地导出和原 MinIO 数据。

确认网站中的历史音频都能播放后，再单独决定何时关闭 MinIO。不要在同一天删除源数据
和本地导出。

## 6. 域名和 HTTPS

低成本单机方案可以在宿主机使用 Caddy。先把域名的 A 记录指向 EC2 的 Elastic IP，
然后按照 [Caddy 官方安装说明](https://caddyserver.com/docs/install#debian-ubuntu-raspbian)
安装 Caddy。

`/etc/caddy/Caddyfile`：

```caddyfile
music.example.com {
    encode zstd gzip
    reverse_proxy 127.0.0.1:8080
}
```

检查并加载：

```bash
sudo caddy validate --config /etc/caddy/Caddyfile
sudo systemctl reload caddy
```

Caddy 会申请和续期 TLS 证书。确认 `.env` 中
`FRONTEND_BIND=127.0.0.1`、`FRONTEND_PORT=8080`，避免绕过 HTTPS 直接访问应用端口。

## 7. 验收

```bash
curl https://music.example.com/api/music/music/list
curl https://music.example.com/api/reward/config/searchall
curl https://music.example.com/api/prize/prize/getallprize
```

浏览器验收：

1. 注册并登录；
2. 上传一首小于 20 MB 的 MP3；
3. 打开详情页播放，确认拿到 S3 预签名 URL；
4. 上报播放时长，确认奖励记录和奖品库存变化；
5. 查看 `reward` 日志，确认 Kafka retry/DLT 没有异常堆积。
6. 登录后发送一条 AI 消息；退出登录后直接请求聊天接口应返回 401。

S3 权限问题优先查看：

```bash
docker compose logs --tail=200 music
```

常见原因是 bucket 名或 Region 不一致、IAM policy 的 bucket ARN 没替换，或 EC2 的
IMDS hop limit 仍为 1。

## 8. 更新、停止和备份

```bash
git pull
docker compose up -d --build

docker compose logs -f music
docker compose restart music
docker compose down
```

`docker compose down` 会保留命名数据卷；不要随意使用 `down -v`，它会删除数据库和
Kafka 数据。部署真实数据后，至少定期做 MySQL dump 和 EBS snapshot。

当前架构没有多可用区容灾。需要正式生产化时，再把 MySQL、Redis、Kafka 迁往
RDS、ElastiCache 和 MSK，并把容器迁往 ECS。

## 9. Spring Cloud 迁移与回滚

本次迁移不需要数据库 schema 变更。先保留上一版镜像与配置，按“注册中心 → 被调用方
Prize → Reward → 调用方 Music/Chatbot”的顺序发布。先单实例跑通，再在 staging 增加实例。
Docker Compose 的 `depends_on` 只保证启动顺序，不能证明 Eureka 已完成注册和缓存同步；
放量前检查注册列表及业务调用。当前 Compose 是单节点模板，不会自动完成跨节点部署。

| 模块 | Eureka 逻辑名称 | 默认端口 | 内部调用 |
| --- | --- | --- | --- |
| Music_management | music-service | 8081 | POST reward-service/sync |
| Multi_reward | reward-service | 8080 | POST prize-service/prize/send_reward；GET /prize_reward/count、/record-by-minutes |
| PrizeCenter | prize-service | 8082 | 无 |
| ai_chatbot | chatbot-service | 8083 | Reward 的奖励记录、今日播放记录、奖励配置 GET |
| service-registry | 注册中心 | 8761 | 仅服务注册/发现 |

`spring.application.name` 保留原值；Eureka 的 `appname`、VIP 使用上述逻辑身份。
所有实例需要唯一 `SERVICE_INSTANCE_ID`，默认由服务名、主机名和端口组成。
同一服务的不同实例必须访问同一套已有业务存储，不能各自初始化独立生产数据库。

### 本地运行与观察

`./scripts/start-all.sh` 现在同时构建并启动注册中心。可通过 `REGISTRY_PORT`、`PRIZE_PORT`、
`REWARD_PORT`、`MUSIC_PORT`、`CHATBOT_PORT` 调整本地端口。

```bash
curl -H 'Accept: application/json' http://localhost:8761/eureka/apps
curl http://localhost:8082/actuator/health
curl -H 'X-Request-ID: migration-check' http://localhost:8080/config/searchall
```

在已加载本地 `.env.local` 的 shell 中，可单独启动第二个 Prize 实例：

```bash
java -Dlog.path=logs/prize-9082 -DLOG_HOME=logs/prize-9082 \
  -jar PrizeCenter/target/PrizeCenter-0.0.1-SNAPSHOT.jar \
  --server.port=9082 --eureka.instance.instance-id=prize-local-9082
```

Reward 调用地址始终是 `http://prize-service`。查看两个实例日志中的
`event=HTTP_REQUEST`，以及调用方的 `event=DOWNSTREAM_RESPONSE`；日志包含 service、instance、
requestId、目标、路径、状态和耗时。`X-Request-ID` 会跨内部 HTTP 调用传播。
测试时可加 `--observability.instance-header-enabled=true` 返回 `X-Service-Instance`，
默认不向外部暴露该标识。没有增加 Token 转发规则，原 JWT/用户身份检查保持原样。

Feign 负载均衡由三个调用方 `FeignClientConfig` 上的 `@LoadBalancerClient` 显式配置，
策略为 `RoundRobinLoadBalancer`。启动第二个 Prize 后，等待 Eureka 中两个实例均为 UP
以及调用方发现列表更新；Reward 的定时对账读取会经由负载均衡访问两个实例。
在调用方日志中查看实际选中的地址：

```bash
rg 'event=LOAD_BALANCER_SELECTED' logs/Multi_reward.console.log
```

日志的 `targetInstance` 可区分两个 Prize 实例，`requestId` 可关联下游请求日志。
轮询覆盖同一目标服务的所有 API，count 和 record-by-minutes 可能各命中一台实例；
不应只观察一个 API 就判断负载均衡失效。只有一台 UP 实例时，请求都落在该实例。
`direct` profile 则按固定 URL 调用，不参与负载均衡。

也可运行 `./scripts/verify-discovery.sh --full` 在独立测试环境自动完成双实例验证。
脚本使用独立数据库和随机端口，检查 Music 的 12 次请求按 6/6 分配、Chatbot 的 4 次
请求按 2/2 分配，以及三个调用方日志中是否均出现两个下游实例。

若要手动启动第二个 Reward，使用不同端口和实例 ID，并加
`--reward.reconciliation.enabled=false`。只有一个实例启用对账任务；这属于部署约束，
当前没有自动选主或任务故障转移。用户日汇总的并发写入仍是全面扩容前的单独发布门槛。
Chatbot 保持单实例，直到共享会话记忆或会话路由经过验证。

### 跨主机部署

每台机器使用可访问的注册中心地址；业务实例还必须发布所有调用方均可访问的私网地址：

```dotenv
EUREKA_DEFAULT_ZONE=http://registry.internal:8761/eureka/
EUREKA_PREFER_IP_ADDRESS=true
SERVICE_IP_ADDRESS=10.0.2.18
SERVICE_INSTANCE_ID=prize-node-b-9082
SERVER_PORT=9082
```

也可以 `EUREKA_PREFER_IP_ADDRESS=false`、`SERVICE_HOSTNAME=prize-b.internal`。
端口应为实例真实对外可达的服务端口。各服务监听端口可统一使用命令行 `--server.port` 覆盖。
在 NAT/端口映射环境中需要正确发布映射后的端口，不能直接把不可路由的容器 IP 注册给其他主机。
单机 Compose 自动使用容器 IP；跨节点需要可路由容器网络，或使用宿主机可达地址及正确端口。
Eureka 不提供网络隧道，也不代理业务流量。

仅在私网开放 registry 和服务端口。当前 registry 没有新增认证层，禁止直接暴露公网。
nginx 仍采用原来的入口 upstream；本次解决内部寻址，新增入口副本需要单独调整 nginx/LB。
生产注册中心当前也是单实例，HA 应在后续运维阶段增加并验证。

### 超时和故障行为

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| INTERNAL_CONNECT_TIMEOUT_MS | 1000 | 内部 HTTP 连接超时 |
| INTERNAL_READ_TIMEOUT_MS | Reward/Chatbot 3000；Music 5000 | Music 需要覆盖 Reward → Prize 的嵌套调用预算 |
| DISCOVERY_ENABLED | true | 注册/发现开关；关闭本项本身不等于切换直接寻址 |
| RECONCILIATION_ENABLED | true | Reward 对账任务，仅一个实例启用 |

Feign 使用 `Retryer.NEVER_RETRY`，HTTP LoadBalancer 重试也关闭，未加入 Spring Retry。
`@FeignClient` 方法声明原有 GET/POST、请求体和查询参数；HTTP transport 仍为 REST。
超时不代表下游未提交；原有 Reward Kafka
retry/DLT 和 `out_biz_no` 幂等机制保持不变。业务失败响应仍使用既有 envelope 和异常映射，
没有把所有异常统一改成新的 HTTP 状态码。

注册中心不可用时，已获取列表的客户端仍可访问缓存中的可达实例；冷启动或空列表会失败。
实例 DOWN、优雅注销、异常退出的生效都依赖注册与拉取收敛，不能承诺立即摘除。
生产保留 Eureka 默认心跳/租约周期及 self-preservation；自动测试中的 1 秒间隔、关闭
self-preservation 和响应缓存仅用于缩短验证时间，不应复制到生产。
Actuator 健康状态会上报 Eureka，数据库/Redis 故障可令实例 DOWN；为避免注册中心故障
连带摘除业务实例，发现客户端自身的 health indicator 不参与业务健康聚合。
Music 的可选 SMTP 健康检查也不参与该聚合。

Prize 扣库存现在使用带库存条件的原子 SQL，并与幂等记录处于同一事务。
这修复了并发丢失扣减和库存耗尽后的重放失败。Reward → Prize 发奖链路现已接入
Seata AT 分布式事务；库存查询从 MySQL 读取，避免 Redis 保存全局回滚前的库存。

### 显式回滚

保留了三组 `application-direct.properties` 和 Prize 的 direct profile。
旧 `MULTIREWARD_BASE_URL` / `PRIZECENTER_BASE_URL` 仅在 direct profile 中生效，为 Feign 注解的
`url` 占位符提供显式地址；正常模式 `url` 为空，使用注解 `name` 通过 Eureka 发现实例。
没有在发现失败后自动退回旧地址，以避免静默调用错误实例。

本地回滚示例（逐服务重启；其他数据库、密钥等环境变量保持原部署值）：

```bash
SPRING_PROFILES_ACTIVE=direct MULTIREWARD_BASE_URL=http://localhost:8080 \
  java -jar Music_management/target/Music_management-0.0.1-SNAPSHOT.jar
SPRING_PROFILES_ACTIVE=direct PRIZECENTER_BASE_URL=http://localhost:8082 \
  java -jar Multi_reward/target/Multi_reward-0.0.1-SNAPSHOT.jar
SPRING_PROFILES_ACTIVE=direct MULTIREWARD_BASE_URL=http://localhost:8080 \
  java -jar ai_chatbot/target/ai_chatbot-0.0.1-SNAPSHOT.jar
```

Compose 中通过临时 override 文件为调用方增加 profile 和物理 URL，例如：

```yaml
services:
  music:
    environment:
      SPRING_PROFILES_ACTIVE: direct
      MULTIREWARD_BASE_URL: http://reward:8080
  reward:
    environment:
      SPRING_PROFILES_ACTIVE: direct
      PRIZECENTER_BASE_URL: http://prize:8082
  chatbot:
    environment:
      SPRING_PROFILES_ACTIVE: direct
      MULTIREWARD_BASE_URL: http://reward:8080
  prize:
    environment:
      SPRING_PROFILES_ACTIVE: direct
```

保存为 `compose.direct.yml` 后执行
`docker compose -f docker-compose.yml -f compose.direct.yml up -d music reward chatbot prize`。
直接寻址保留本次超时和上下文日志，也可恢复上版镜像完成整版本回滚；不需要回滚 schema。
恢复发现模式时移除 override 中的 profile/URL，重新部署并验证注册后再放量。

### 发布验收门槛

自动验证入口与覆盖范围见 [README](README.md#spring-cloud-migration-plan)。
上线前仍须在 staging 验证真实两个节点间的 DNS、防火墙、注册地址、滚动发布和回滚，
以及业务流量下的超时预算。Cloud 2024.0 已停止开源支持，Boot 3.4 服务的版本升级须单独安排。


## 10. Seata 分布式发奖事务

Reward 和 Prize 使用 `org.apache.seata:seata-spring-boot-starter:2.5.0`，
协调器使用同版本 `apache/seata-server:2.5.0.jdk17`。原 Boot / Cloud 版本不变。
无需引入 Spring Cloud Alibaba BOM：Reward 的 Feign interceptor 传递 `TX_XID`，
Prize 的 `SeataTransactionFilter` 仅在内部 `POST /prize/send_reward` 上绑定 XID，
并在 `finally` 清理线程上下文。Reward 不接受外部请求头指定全局事务。

`SyncServiceImpl.sendPrize()` 委托独立的 `RewardGrantService.sendPrize()`：

1. `@GlobalTransactional(rollbackFor=Exception.class, timeoutMills=15000)` 开启全局事务。
2. Prize 的 `@Transactional` 扣库存、写 Prize 流水，Seata 数据源代理写入该库的 `undo_log`。
3. Reward 的本地 `@Transactional` 写 Reward 流水。
4. 整体成功后全局提交；业务错误、HTTP 错误或 Reward 写入失败都会触发全局回滚。
5. 异常离开事务代理后才进入现有 Kafka 重试路径。每次重试用新 XID，保持原 `out_biz_no`。

独立 Bean 同时修复了原来 `sync()` 同类调用绕过事务代理的问题。不要把方法重新移回同类
并依赖 `this.sendPrize()` 触发注解。Prize 的独立库存修改及流水删除使用 `@GlobalLock` 检查全局锁，
防止普通本地写入覆盖待回滚的数据。幂等检查前使用 `SELECT ... FOR UPDATE` 检查全局锁，
不能把其他全局事务尚未结束的阶段一流水当成已经成功。全局事务不包含播放流水、每日汇总、Kafka 消息发布、
Redis、S3 或外部模型请求；播放事实仍保留，发奖失败继续走原有补偿路径。

### 本地启动与已有数据库升级

已有 `.env.local` 可以继续使用，默认 TC 地址为 `127.0.0.1:18091`。
如需换端口，在 `.env.local` 同时设置 `SEATA_PORT` 和 `SEATA_SERVER_ADDR`，例如
`SEATA_PORT=28091`、`SEATA_SERVER_ADDR=127.0.0.1:28091`。

```bash
./scripts/local-infra.sh start
./scripts/start-all.sh
./scripts/status.sh
```

`local-infra.sh start` 等待 TC/MySQL 就绪并同步执行 `sql/08_seata_undo_log.sql`。
脚本只向 `multi_reward`、`prize_center` 添加 `undo_log`，可以重复执行，不清空业务表。
生产 Compose 通过 `seata-schema` 一次性服务执行同一个脚本，并以迁移成功作为 Reward/Prize
的启动依赖。不要向已有库重新导入 `01`、`02` 初始化脚本，它们含有 DROP TABLE。

独立部署数据库时，在启动新应用前使用数据库管理员连接执行 `08_seata_undo_log.sql`。
应用账号需要读写本库业务表和 `undo_log`；两个客户端的 `SEATA_SERVER_ADDR` 必须指向同一个 TC，
并保持相同的 `reward_tx_group` → `default` 映射。TC 是事务控制基础设施，业务 Feign 仍经 Eureka
与 LoadBalancer 分发；`direct` profile 只切换服务寻址，仍保留 Seata 事务。

### 持久化、故障与部署边界

当前 Compose 使用单节点 TC 和持久化的 `seata-data` 卷，适用于本仓库的单机部署。
TC 没有暴露到公网：开发模式只绑定 localhost，生产 Compose 仅容器内网可达。
不要删除 TC 卷或 `undo_log` 中尚未结束事务的数据。该配置不是 TC 高可用集群；正式生产 HA
需要另行部署受监控的协调器集群及共享持久化存储，并验证故障恢复。

AT 在第一阶段提交本地事务，第二阶段按日志补偿。网络故障时回滚可能延迟，需要 TC 与 RM
恢复后重试；普通 SELECT 可能暂时看到全局事务尚未完成的数据，不能将 AT 宣称为全程串行隔离。
本实现从数据库读取库存，避免跨全局回滚的 Redis 库存缓存；直接 SQL 修改库存也必须遵循全局锁，
不要绕过应用代理修改正在参与事务的行。

Seata 默认启用且关闭自动降级；TC 不可达时不会静默按本地事务继续发奖，冷启动可能失败，
运行中的失败交给原重试/DLT。`SEATA_ENABLED=false` 仅供明确接受失去分布式原子性的隔离测试使用。
回滚应用版本前先停止发奖入口并等待全局事务与重试队列处理完毕；保留 TC 卷和 undo 表供恢复。

### 验收

```bash
./scripts/verify-discovery.sh --full
```

脚本创建独立的 MySQL、Redis、Kafka、MinIO 和 TC，使用随机本地端口，结束后只清理该次测试资源。
`SeataTransactionIT` 使用真实 TC、两个 MySQL schema 和独立 Prize JVM，故障注入仅存在于测试源码。
覆盖双方提交、Reward 写入后异常触发跨库回滚、库存不足、重复业务单号、正常 sync 的代理边界、
重试入口创建新 XID、并发重放与独立流水删除的全局锁保护。HTTP 单元测试另验证 XID 传播和异常后线程清理。
完整测试还保留 Eureka/Feign/轮询、多实例并发扣库存和注册中心故障恢复验收。
日志分别位于 `Multi_reward/target/seata-it/prize.log`、`Multi_reward/target/failsafe-reports/` 和
`target/discovery-smoke/`。

配置与 AT 语义参考 [Apache Seata AT 文档](https://seata.apache.org/docs/dev/mode/at-mode/) 和
[2.5 升级说明](https://seata.apache.org/docs/v2.5/ops/upgrade/)。
