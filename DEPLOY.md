# AWS 部署手册(EC2 单机 + Docker Compose)

面试演示场景的最优解:**一台 EC2 跑全部组件**(3 后端 + 前端 nginx + MySQL/Redis/Kafka/MinIO),
`docker compose up` 一条命令拉起,月成本 ~$15-30,不演示时停机只付磁盘钱(每月不到 $3)。

> 为什么不用 RDS/MSK/ElastiCache 等托管服务?演示项目流量为零,托管服务最低配也要
> $15(RDS)+ $70(MSK)+ $12(ElastiCache)/月,且拆开部署对展示系统设计没有加分——
> 面试时可以主动讲"生产会换成托管服务,这里为成本选择单机 compose",这本身就是架构取舍能力。

## 一、创建 EC2

1. 控制台 → EC2 → Launch instance:
   - **AMI**: Ubuntu Server 24.04 LTS
   - **机型**: `t3.medium`(4GB,舒适)或 `t3.small`(2GB,需加 swap,见下)
   - **磁盘**: 25 GB gp3
   - **Key pair**: 新建并下载 `.pem`
2. **安全组**(入站规则):
   | 端口 | 来源 | 用途 |
   |------|------|------|
   | 22 | 仅你的 IP | SSH |
   | 80 | 0.0.0.0/0 | 前端 + API |
   | 9000 | 0.0.0.0/0 | MinIO 预签名音频直连(播放器 `<audio src>`) |
3. **弹性 IP**(Elastic IP):分配并绑定实例——否则每次重启公网 IP 会变,
   预签名 URL 主机名(`PUBLIC_HOST`)就失效了。

## 二、装 Docker

```bash
ssh -i your-key.pem ubuntu@<弹性IP>

curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu && exit   # 重新 ssh 进来使组权限生效
```

(t3.small 才需要)加 2GB swap,防止构建/启动时 OOM:

```bash
sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile
sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

## 三、拉代码 + 配置

```bash
git clone https://github.com/pl200x/RewardProject.git && cd RewardProject
cp .env.example .env && vim .env
```

`.env` 必改项:

| 变量 | 改成什么 |
|------|----------|
| `DB_PASSWORD` | **新的强密码**(compose 首次启动会用它初始化 MySQL root,别用本地那个) |
| `PUBLIC_HOST` | 你的**弹性 IP**(或域名) |
| `FRONTEND_PORT` | `80` |
| `MINIO_PUBLIC_PORT` | `9000` |
| `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` | 换掉 minioadmin 默认值(9000 对公网开放) |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | 可留空——点赞通知邮件会发不出、消息进 `NOTIFICATION.DLT` 死信(顺便演示 DLQ);要演示邮件就填 SMTP 授权码 |

## 四、启动 + 验收

```bash
docker compose up -d --build     # 首次 5-10 分钟(maven/npm 构建)
docker compose ps                # 全部 Up,minio-init 是 Exited(0) 才对
docker compose logs -f --tail=50 # 观察启动
```

验收清单(本机浏览器/终端):

```bash
curl http://<弹性IP>/api/prize/prize/getallprize   # 奖品 JSON
curl http://<弹性IP>/api/music/music/list          # 音乐 JSON
curl -I http://<弹性IP>/minio/avatars/0.svg        # 200,默认头像
# 浏览器打开 http://<弹性IP> → 注册/登录 → 播放测试
```

## 五、日常运维

```bash
docker compose logs -f reward          # 看某个服务日志(music/prize/web/mysql/kafka)
docker compose restart music           # 重启单个服务
git pull && docker compose up -d --build   # 更新代码后重建
docker compose down                    # 停(保留数据卷)
docker compose down -v                 # 停并清空数据(慎用:删库)
```

**成本控制**:不面试的日子在控制台 Stop 实例(数据在 EBS 上不丢,弹性 IP 挂着不另收费——
注:实例停止期间弹性 IP 每小时 $0.005,约 $3.6/月,可接受;Start 后 `docker compose up -d` 即恢复)。

## 常见问题

| 症状 | 原因 |
|------|------|
| 页面能开但音频播不了 | 安全组没放行 9000,或 `.env` 的 `PUBLIC_HOST` 不是弹性 IP(预签名 URL 主机名不可达) |
| 点赞后 `NOTIFICATION.DLT` 出现死信 | `MAIL_*` 没配,邮件发送失败重试耗尽——预期行为,配上即消失 |
| 首次启动 mysql 卡住 | 正在导 `sql/` 种子数据,等 healthcheck 通过,`docker compose logs mysql` 可见进度 |
| t3.small 构建时卡死 | 没加 swap,见第二步 |
