# RewardProject 前端设计与实现文档

> 本文件是前端的设计依据与实现说明,边开发边更新。目标:具有独立唱片店与音乐杂志气质的听歌奖励平台。
> 技术栈:**Vite + React 18 + TypeScript + React Router 6 + axios + 手写 CSS**。

### 目标用户

- **音乐听众**:发现新歌、建立兴趣偏好,并通过有效收听解锁奖励。
- **音乐创作者**:上传 MP3、歌曲资料与歌词,让作品进入平台的发现与推荐链路。
- **平台运营人员**:管理奖品库存、奖励规则、音乐下架与发奖流水。
- **次要受众**:关注奖励链路、消息重试和幂等发放实现的开发者与项目评审者。

---

## 1. 目录结构

```
frontend/
├── index.html
├── package.json
├── vite.config.ts            # 开发代理(转发到 5 个后端端口)
├── tsconfig.json
├── DESIGN.md                 # 本文档
└── src/
    ├── main.tsx              # 入口,挂载 Router + AuthProvider
    ├── App.tsx               # 路由表与全局提示/会话过期层
    ├── index.css             # 全局基础样式
    ├── musicmap.css          # 用户端与管理后台的统一视觉系统
    ├── api/
    │   ├── client.ts         # 5 个 axios 实例 + 统一响应/错误拦截
    │   ├── types.ts          # BaseVO / 各 VO、Cmd 的 TS 类型
    │   ├── music.ts          # Music_management 接口
    │   ├── reward.ts         # Multi_reward 接口
    │   └── prize.ts          # PrizeCenter ×3 接口
    ├── auth/
    │   ├── AuthContext.tsx   # 登录态 Context + Provider
    │   └── useAuth.ts        # 读取/操作登录态的 hook
    ├── components/           # 通用组件(Shelf 横向卡片、StatCard、PageHeader、ServiceBadge…)
    └── pages/                # 各页面(见第 5 节)
```

---

## 2. 后端接口映射

### 2.1 开发代理(Vite proxy,规避跨域,零后端改动)

后端**没有 CORS 配置**,因此开发期用 Vite 代理。`vite.config.ts` 里把前缀转发到对应端口并 rewrite 去掉前缀:

| 前缀 | 目标端口 | 服务 | axios 实例 |
|------|----------|------|-----------|
| `/api/music`  | 8081 | Music_management | `musicClient` |
| `/api/reward` | 8080 | Multi_reward     | `rewardClient` |
| `/api/prize`  | 8082 | PrizeCenter(coin/gem/coupon 单服务) | `prizeClient` |

> 下文接口清单/页面里写的 `/m`、`/r`、`/p-*` 为上述前缀简写(实际 `/api/music` 等)。
> 前缀刻意写长以避免与客户端路由(如 `/music`)冲突。生产部署换 nginx 同前缀反代(第 4 步)。

### 2.2 统一响应结构

所有接口返回都带 `baseVO`:

```ts
interface BaseVO { success: boolean; code: number; duration?: number; time?: number; errorMessage: string | null; }
// 列表类例:{ prizeVOList: Prize[];  baseVO: BaseVO }
//          { musicDigestVOList: MusicDigest[]; baseVO: BaseVO }
//          { configurationVoList: Configuration[]; baseVO: BaseVO }
```

axios 响应拦截器统一:取 `data`,若 `baseVO.success===false` 用 antd `message.error(errorMessage)` 提示并抛出,业务层只处理成功路径。

### 2.3 完整接口清单

**Music_management `/m` (8081)**
| 方法 | 路径 | 入参 | 说明 |
|------|------|------|------|
| POST | /user/register | RegisterCmd{name,password,age,gender,email,job,interest} | 注册 |
| GET  | /user/login | userName, password | 登录,返回 LoginVO{token,userId,userName,baseVO} |
| DELETE | /user/logout | userId | 登出 |
| POST | /user/change-tag | userId, interest | 改兴趣 |
| GET  | /interest | interest | 按兴趣找用户 |
| GET  | /music/list | — | 推荐音乐列表 |
| GET  | /music/id | id | 查单首 |
| POST | /music/add | MusicCmd{title,artist,releaseYear,tags,lyrics} | 新增音乐 |
| DELETE | /music/id | musicId | 删除音乐 |
| POST | /music/play | PlayMusicCmd{userId,musicId,syncTime,duration,scene,code} | 听歌上报(触发奖励链) |
| POST | /like/add | userId, musicId | 点赞 |
| DELETE | /like/delete | userId, musicId | 取消赞 |
| GET  | /like/getuserall | userId | 某用户点赞的歌 |
| GET  | /like/getmusicall | musicId | 某歌被谁赞 |
| GET  | /rank/top | N | 播放排行 Top N(⚠️ 空数据会 500) |
| GET  | /producer/send | content | Kafka 发消息(测试) |

**Multi_reward `/r` (8080)**
| 方法 | 路径 | 入参 | 说明 |
|------|------|------|------|
| GET  | /config/searchall | — | 所有奖励规则(JEXL) |
| GET  | /config/search | code | 查单条规则 |
| POST | /config/add | ConfigurationCmd{code,rule,description} | 新增规则 |
| PUT  | /config/update | code, rule, description | 改规则 |
| DELETE | /config/delete | code | 删规则 |
| GET  | /analysis/id | userId, summaryDate | 用户某天累计时长分析 |
| GET  | /playrecord/id | id | 查单条播放记录 |
| GET  | /playrecord/today | (日期/用户参数) | 当天播放记录 |
| GET  | /prizerecord/by-outbizno | outBizNo | 按业务单号查发奖 |
| GET  | /prizerecord/by-user-and-date | userId, prizeDate | 按用户+日期查发奖 |
| POST | /sync | PlayCmd | (内部,Music 调用,前端一般不直接用) |

**PrizeCenter `/p` (8082,coin/gem/coupon 按奖品 code 区分)**
| 方法 | 路径 | 入参 | 说明 |
|------|------|------|------|
| GET  | /prize/getallprize | — | 奖品列表 |
| GET  | /prize/search | code | 查奖品 |
| POST | /prize/add | PrizeCmd{code,name,description,storage} | 新增奖品 |
| PUT  | /prize/update | id, storage, name, description | 改奖品/库存 |
| DELETE | /prize/delete | id | 删奖品 |
| POST | /prize/send_reward | SendRewardCmd{code,amount,outbizno} | 手动发奖(幂等) |
| GET  | /prize_reward/get_all | — | 发奖记录列表 |
| GET  | /prize_reward/count | — | 发奖总数 |
| GET  | /prize_reward/out_biz | outbizno | 按单号查 |
| GET  | /prize_reward/record-by-minutes | — | 近 N 分钟发奖 |
| POST | /prize_reward/add | PrizeRecordCmd | 新增发奖记录 |
| DELETE | /prize_reward/delete_by_id | id | 删记录 |

> 注:曾为 coin/gem/coupon 三副本服务(8082/8083/8084,共用 prize_center 库),已合并为单一 PrizeCenter;流水页的类型 Tab 现为客户端按 code 筛选。

---

## 3. 登录态设计(核心,JWT)

> 已采用**方案 A:轻量 JWT,仅 Music_management**。后端已实现并验证通过。

### 后端鉴权(已实现,仅 Music_management,不动其他服务)
- `GET /m/user/login?userName=&password=` 成功返回 **`LoginVO { token, userId, userName, baseVO }`**;token 为 HS384 JWT,内含 `sub=userId`、`userName`,默认有效期 1 天。
- `JwtAuthFilter` 仅校验这些**受保护接口**的 `Authorization: Bearer <token>`,缺失/无效 → **401**:
  `/music/play`、`/like/add`、`/like/delete`、`/user/change-tag`、`/user/logout`。
- 其余浏览/登录/注册接口无需 token。
- 关键类:`security/JwtUtil`(签发/校验)、`security/JwtAuthFilter`、`controller/vo/LoginVO`;密钥读 `jwt.secret`(默认值仅 demo)。

### 前端登录态
- **存储**:`AuthContext`(全局内存)+ `localStorage`(键 `reward.auth`),刷新/重开不丢。
- **数据结构**:`interface Auth { token: string; userId: number; userName: string }`
- **登录流程**(`/login` 页):
  1. 表单仅需 `userName` + `password`(**不再手填 userId**;保留 aa/wer 快捷填充账号)。
  2. 提交 → `GET /m/user/login`;`baseVO.success===true` → 存 `{token,userId,userName}`。
  3. 跳转回来源页或首页。
- **请求头注入**:`musicClient` 请求拦截器自动加 `Authorization: Bearer <token>`。
- **401 处理**:响应拦截器遇 401 → 清登录态 + 跳 `/login`,提示"登录已过期"。
- **使用**:`useAuth()` 暴露 `{ auth, login, logout, isLoggedIn }`;顶栏显示当前用户 + 退出;听歌/点赞/改兴趣自动用 `auth.userId`。
- **路由守卫**:浏览类页面免登录可看;需要身份的动作未登录时引导去 `/login`。
- **退出**:`DELETE /m/user/logout?userId=`(带 token)+ 清 localStorage。

### 安全说明
- JWT 密钥目前为演示默认值,生产应放环境变量;token 存 localStorage,demo 可接受。

---

## 4. 视觉风格(独立唱片店 × 音乐杂志)

### 设计语言

- **整体**:暖色、浅色、内容优先;用纸张、唱片封套和编辑排版的感觉替代科技控制台风格。
- **品牌色**:陶土红 `#A63F2D`,是唯一的产品主强调色;hover/按下使用 `#8E3425`。
- **背景与表面**:页面背景为暖米色 `#F3EFE7`,主要内容表面为暖白 `#FFFCF6`,次级表面为燕麦灰 `#EAE3D8`。
- **文字**:主要文字使用暖黑 `#26221D`,次级文字使用暖灰 `#625C54`;重要标题保持实色,不使用渐变文字。
- **卡片**:使用细边框、中性阴影和 8–14px 的克制圆角;不使用装饰性毛玻璃或霓虹光。
- **排版**:界面控件使用系统无衬线字体,品牌和主要标题使用 Georgia/宋体回退,形成唱片内页的编辑感。
- **音乐封面**:占位封面从六个深色大地色中稳定取色,不再为每首歌曲生成彩虹渐变。
- **欢迎页唱机**:红木纹理只用于欢迎页的材质背景;唱片旋转、唱臂和播放状态组成可点击的播放/暂停动画,并遵循系统的减少动态效果设置。
- **后台**:用户端与管理后台共享同一套颜色、文字和组件表面,通过信息密度而不是另一套暗色主题区分场景。

### 颜色 token

```css
--mm-bg: #f3efe7;
--mm-surface: #fffcf6;
--mm-surface-subtle: #eae3d8;
--mm-text: #26221d;
--mm-muted: #625c54;
--mm-border: #d5ccbe;
--mm-accent: #a63f2d;
--mm-accent-strong: #8e3425;
--mm-accent-soft: #f2dfd8;
--mm-accent-ink: #7e2f20;
```

### 可访问性约定

- 暖黑文字/米色背景对比度 `13.78:1`。
- 次级文字/米色背景对比度 `5.76:1`。
- 白色文字/陶土红按钮对比度 `6.23:1`。
- 深陶土文字/淡陶土背景对比度 `7.05:1`。
- 焦点使用清晰的陶土色外轮廓;轻微阴影仅用于焦点、弹窗层级和可点击卡片反馈。
- 成功、警告与错误颜色仅表达对应状态,不作为品牌装饰色。

---

## 5. 路由与页面清单

侧边栏分 5 组,共 11 页:

| 分组 | 路由 | 页面 | 主要接口 |
|------|------|------|---------|
| 概览 | `/` | **Dashboard 控制台** | 各服务探活、`/p-*/prize_reward/count`、`/p-*/prize/getallprize`、`/m/music/list` |
| 核心流程 | `/play` | **听歌上报**(亮点,带动画反馈) | `/m/music/play`、`/m/music/list` |
| 核心流程 | `/prize-records` | **发奖记录** | `/p-*/prize_reward/get_all`、`/r/prizerecord/by-outbizno`、`/r/prizerecord/by-user-and-date` |
| 核心流程 | `/analysis` | **用户分析** | `/r/analysis/id`、`/r/playrecord/today` |
| 音乐与用户 | `/music` | **音乐管理** | `/m/music/list`、`/m/music/id`、`/m/music/add`、删除 |
| 音乐与用户 | `/users` | **用户中心**(登录/注册/改兴趣/按兴趣找人) | `/m/user/*`、`/m/interest` |
| 音乐与用户 | `/likes` | **点赞** | `/m/like/*` |
| 音乐与用户 | `/rank` | **播放排行** | `/m/rank/top`(空数据兜底) |
| 后台配置 | `/config` | **奖励规则(JEXL)** | `/r/config/*` |
| 后台配置 | `/prizes` | **奖品库存**(三中心 Tab) | `/p-*/prize/*` |
| 工具 | `/login` | **登录页** | `/m/user/login` |

> Kafka 测试作为"工具"小入口并入 Dashboard 或独立 `/kafka`(待定)。

---

## 6. 通用约定
- **请求封装**:每个服务一个 axios 实例(见 2.1);GET 查询参数走 `params`,POST body 类用 JSON。
- **错误处理**:响应拦截器统一弹 `message.error`;页面 loading 用 antd `Spin`/表格 `loading`。
- **表单**:新增/编辑用 `Modal` + antd `Form`;提交成功 `message.success` 并刷新列表。
- **空/异常兜底**:排行榜等空数据显示 `Empty` 占位而非报错。

## 7. 已知后端限制(前端需兜底)
1. ~~登录不返回 userId/token~~ → **已解决**:已加 JWT,login 返回 token+userId(第 3 节)。
2. `GET /rank/top` 空排行数据返回 500 → 前端捕获后显示"暂无排行数据"。
3. 三个 PrizeCenter 实际同库,数据一致属正常。

## 8. 实施阶段
1. 脚手架 + 代理 + 主题 + 布局/路由 + api/auth 骨架
2. Dashboard + 听歌上报(核心)
3. 音乐 / 用户 / 点赞 / 排行
4. 奖励规则 / 奖品库存 / 发奖记录 / 用户分析
5. 样式打磨 + 兜底 + 联调
