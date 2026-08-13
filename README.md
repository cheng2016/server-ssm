# server-ssm

一个能跑通、能讲清楚的 **游戏 / 实时服务器脚手架**。

从 2016 年的 SSM + Netty 学习项目演进而来：现在用 Spring Boot 3、Java 21、Netty、Protobuf、MyBatis-Plus，把「HTTP 管账号和运营、长连接管实时」这条后台主线拆开讲明白。

适合想入门游戏服 / 长连接后台的开发者：先按步骤理解为什么这样分层，再动手跑通 Login / Heartbeat / Chat。

---

## 后台开发在做什么

普通 Web 后台常见路径是：浏览器 → HTTP → Controller → Service → 数据库。

实时游戏服多了一条路：客户端一直连着服务器，消息要自己定协议、自己路由、自己管会话。本项目把两条路放在同一个进程里：

```text
客户端 / 浏览器
    │
    ├─ HTTP（注册、登录、查用户、踢人、广播）──► Spring MVC
    │                                              │
    └─ TCP / WebSocket（登录、心跳、聊天）──────► Netty
                                                   │
                              ┌────────────────────┤
                              ▼                    ▼
                           MySQL                 Redis
                         （账号数据）          （在线集合）
```

记住三个重点就够：

1. **协议先于代码**：双方先约定「一帧长什么样、msgId 是什么」，再写业务。
2. **短连接管身份，长连接管在线**：HTTP 登录发 JWT；TCP/WS 再用这个 token 绑定玩家会话。
3. **模块按依赖方向切**：协议不依赖 Spring；网络层不写业务 SQL；启动模块只做编排。

---

## 一步步怎么构建（建议按这个顺序读代码）

### 第 1 步：先定模块边界

不要把所有类丢进一个 WAR。本仓库按「谁依赖谁」拆成 5 个 Maven 模块：

```text
server-ssm/
├── game-common        # 错误码、统一响应、消息 ID（最底层，谁都能用）
├── game-protocol      # .proto 文件，编译成 Java 消息类
├── game-persistence   # 表结构、Entity、Mapper、Flyway 迁移
├── game-network       # Netty 启动、拆包粘包、会话、消息分发
└── game-app           # Spring Boot 入口：REST、鉴权、游戏 Handler
```

**重点**：上层可以依赖下层，下层不要反过来依赖 `game-app`。这样换数据库、换协议时，不会把整棵树拽垮。

### 第 2 步：先写协议，再写业务

长连接不能靠 URL 路由。我们约定二进制帧：

```text
int32 length | int16 msgId | protobuf 内容
length = 2 + 内容长度
```

| msgId | 消息 | 方向 | 作用 |
|------:|------|------|------|
| 1001 | LoginRequest | 客户端 → 服务器 | 带上 HTTP 拿到的 JWT |
| 1002 | LoginResponse | 服务器 → 客户端 | 登录结果、玩家 ID |
| 1003 | HeartbeatRequest | 客户端 → 服务器 | 保活，避免空闲被踢 |
| 1004 | HeartbeatResponse | 服务器 → 客户端 | 回服务器时间 |
| 1005 | ChatRequest | 客户端 → 服务器 | 发一句聊天 |
| 1006 | ChatMessage | 服务器 → 全员 | 广播聊天 |

协议定义在 [`game-protocol/src/main/proto/game.proto`](game-protocol/src/main/proto/game.proto)，消息号在 [`MsgIds`](game-common/src/main/java/com/cheng/game/common/protocol/MsgIds.java)。

**重点**：拆包粘包必须有长度字段；`msgId` 相当于「长连接版的 URL」。换业务只加消息，不改帧格式。

### 第 3 步：把账号落库（HTTP 闭环）

先做能存用户的短连接能力，再谈实时：

- Flyway 建表：[`V1__init_user.sql`](game-persistence/src/main/resources/db/migration/V1__init_user.sql)
- MyBatis-Plus 映射 `user` 表
- REST：`POST /api/auth/register`、`POST /api/auth/login` 返回 JWT

**重点**：密码只存哈希；配置走环境变量 / `application.yml`，不要再手写 `jdbc.properties`。表结构变更用迁移脚本，不要改线上库「随手 ALTER」。

### 第 4 步：鉴权打通两条通道

HTTP 登录成功后下发 JWT。客户端连上 TCP/WS 后，发 `LoginRequest.token`，服务器校验 JWT，把 `playerId` 绑到这条 Channel 上（见 `SessionManager`）。

```text
① HTTP 注册/登录  →  得到 JWT
② 建立 TCP 或 WebSocket
③ 发 LoginRequest(token=JWT)
④ SessionManager.bind(playerId, Channel)
⑤ 之后的心跳、聊天都认这条连接上的玩家
```

**重点**：长连接本身不认「Cookie 会话」。身份必须显式绑定；顶号时关掉旧 Channel。运营踢人走 HTTP，最终还是关的这条连接。

### 第 5 步：用 Netty 接住长连接

[`game-network`](game-network) 做三件事：

1. **编解码**：把字节流切成 `GamePacket(msgId, payload)`，避免半包。
2. **生命周期**：Spring `SmartLifecycle` 随应用启动/关闭 Netty（TCP `9000`，WebSocket `9001/ws`）。
3. **会话**：`playerId ↔ Channel`，支持单播、广播、踢人。

TCP 和 WebSocket **共用同一套 Handler**，浏览器和游戏客户端走同一协议。

**重点**：Netty 的 I/O 线程不要做重活；空闲超时用 `IdleStateHandler`；连接断开要解绑会话，并同步 Redis 在线集合。

### 第 6 步：用注解写游戏逻辑（像写 Controller）

业务不要堆在一个巨型 `channelRead` 里。扫描 `@GameHandler(msgId=...)`，按消息号分发：

```java
@GameMessageController
public class GameMessageHandlers {

    @GameHandler(msgId = MsgIds.LOGIN_REQ)
    public GamePacket login(ChannelHandlerContext ctx, byte[] payload) { ... }

    @GameHandler(msgId = MsgIds.CHAT_REQ)
    public GamePacket chat(ChannelHandlerContext ctx, byte[] payload) { ... }
}
```

实现见 [`GameMessageHandlers`](game-app/src/main/java/com/cheng/game/app/game/GameMessageHandlers.java)。

**重点**：加新玩法 = 加 proto + 加 msgId + 加一个方法。这是后台「可扩展」的关键，而不是复制一套新的 ServerBootstrap。

### 第 7 步：给运营一条 HTTP 通道

实时通道给玩家；运营用 REST：

- `GET /api/ops/online` 在线人数
- `POST /api/ops/kick/{playerId}` 踢人
- `POST /api/ops/broadcast` 全服广播

请求头：`X-Ops-Token`（开发默认 `dev-ops-token`）。

**重点**：后台常把「玩家协议」和「运营接口」分开。前者追求低延迟，后者追求可审计、好调用。

### 第 8 步：工程化，让别人也能跑起来

分层写完还不够，别人 clone 下来要能启动、能测、能看：

| 能力 | 位置 | 解决什么问题 |
|------|------|----------------|
| 配置外置 | `.env.example`、`application.yml` | 换环境不改代码 |
| 一键依赖 | `docker-compose.yml` | MySQL / Redis / 应用一起起 |
| 文档接口 | `/swagger-ui.html` | HTTP 不用猜字段 |
| 浏览器调试 | `/debug.html` | 不用先写客户端也能测 WS |
| 构建包装 | `./mvnw` | 不要求本机预装 Maven |
| 回归 | `GameServerE2ETest` | 注册 → 登录 → TCP 聊天 |
| CI | `.github/workflows/ci.yml` | 推代码自动编译测试 |

**重点**：能演示、能回归，脚手架才有人敢用。健康检查看 `/actuator/health`。

---

## 快速开始

需要：JDK 21、Docker（推荐）。

```bash
docker compose up -d --build
```

| 入口 | 地址 |
|------|------|
| 调试页 | http://localhost:8080/debug.html |
| Swagger | http://localhost:8080/swagger-ui.html |
| TCP | `localhost:9000` |
| WebSocket | `ws://localhost:9001/ws` |

调试页路径：注册 / 登录 → Connect WS → Send Login → Heartbeat / Chat。

只用本机跑应用时：

```bash
docker compose up -d mysql redis
cp .env.example .env
./mvnw -pl game-app -am spring-boot:run
```

测试（需要 Docker，会起 MySQL / Redis 容器）：

```bash
./mvnw -B verify
```

### HTTP 示例

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"secret1","nickname":"Alice"}'

curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"secret1"}'

curl -s http://localhost:8080/api/ops/online -H "X-Ops-Token: dev-ops-token"
```

登录返回的 `token` 同时用于：

- HTTP：`Authorization: Bearer <token>`
- TCP / WS：`LoginRequest.token`

---

## 扩展一个新消息（最短路径）

1. 在 `game.proto` 增加消息，在 `MsgIds` 增加号段  
2. 在带 `@GameMessageController` 的类里加 `@GameHandler` 方法  
3. 用调试页或 `TcpDemoClient` 打一帧验证  

不必新开端口，也不必再写一套编解码。

---

## 本模板刻意没做的

完整场景、AOI、战斗、微服务拆分、大型管理后台。先把「协议、会话、鉴权、分层、工程化」跑通，再往上加玩法。

---

## 作者与协议

原作者 [cheng2016](https://github.com/cheng2016)。Apache License 2.0，见 [LICENSE](LICENSE)。
