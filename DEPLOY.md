# AWS 部署：EC2 + Docker Compose + S3

这套方案面向演示环境和低流量试运行：一台 EC2 运行前端、四个后端以及
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

不再开放 9000、3306、6379、8080、8081、8082、8083 或 9092。

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
