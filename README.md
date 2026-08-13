# server-ssm

Modern **game / realtime server starter**: Spring Boot 3 + Java 21 + Netty + Protobuf + MyBatis-Plus.

Evolved from the classic SSM + Netty learning scaffold into a clean multi-module template you can run with Docker and extend with `@GameHandler` message routing.

## Features

- HTTP API: register / login (JWT), user profile, ops (online / kick / broadcast)
- Netty TCP (`9000`) + WebSocket (`9001/ws`) with one binary frame layout
- Protobuf sample protocol: Login / Heartbeat / Chat
- Spring-style `@GameHandler` dispatcher for game logic
- MySQL + Flyway, Redis online-set, Actuator, OpenAPI (`/swagger-ui.html`)
- Virtual threads for HTTP, Docker Compose, GitHub Actions CI
- Maven Wrapper, Testcontainers e2e, browser WebSocket debug page

## Modules

```text
server-ssm/
├── game-common        # ApiResponse, errors, MsgIds
├── game-protocol      # .proto + generated Java
├── game-persistence   # MyBatis-Plus entities/mappers + Flyway
├── game-network       # Netty bootstrap, codec, SessionManager, dispatcher
└── game-app           # Spring Boot app, REST, handlers
```

## Quick start

### 1) Infrastructure + server

```bash
docker compose up -d --build
```

- HTTP: http://localhost:8080
- Debug page: http://localhost:8080/debug.html
- Swagger: http://localhost:8080/swagger-ui.html
- TCP: `localhost:9000`
- WebSocket: `ws://localhost:9001/ws`

### 2) Register & login

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"secret1","nickname":"Alice"}'

curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"secret1"}'
```

Use the returned `token` for HTTP `Authorization: Bearer <token>` and for TCP `LoginRequest.token`.

### 3) Ops

```bash
curl -s http://localhost:8080/api/ops/online -H "X-Ops-Token: dev-ops-token"
curl -s -X POST http://localhost:8080/api/ops/broadcast \
  -H "X-Ops-Token: dev-ops-token" \
  -H "Content-Type: application/json" \
  -d '{"content":"hello everyone"}'
```

### Local Maven run

Requirements: JDK 21+ (Maven via `./mvnw`), MySQL 8, Redis.

```bash
docker compose up -d mysql redis
cp .env.example .env
./mvnw -pl game-app -am spring-boot:run
```

Open http://localhost:8080/debug.html → Register/Login → Connect WS → Send Login/Chat.

### Tests

Needs Docker (Testcontainers MySQL + Redis):

```bash
./mvnw -B verify
```

## Binary protocol

Frame:

```text
int32 length | int16 msgId | protobuf payload
length = 2 + payload.length
```

| msgId | Name | Direction |
|------:|------|-----------|
| 1001 | LoginRequest | C→S |
| 1002 | LoginResponse | S→C |
| 1003 | HeartbeatRequest | C→S |
| 1004 | HeartbeatResponse | S→C |
| 1005 | ChatRequest | C→S |
| 1006 | ChatMessage | S→C (broadcast) |

Add handlers with:

```java
@GameMessageController
public class MyHandlers {
  @GameHandler(msgId = MsgIds.CHAT_REQ)
  public GamePacket chat(ChannelHandlerContext ctx, byte[] payload) { ... }
}
```

## Architecture

```text
Client ──Protobuf TCP──► Netty (game-network)
Browser ──WebSocket────► Netty (same handlers)
Admin ──REST───────────► Spring MVC (game-app)
                              │
                              ├─ SessionManager
                              ├─ MySQL (users)
                              └─ Redis (online set)
```

## Author

Originally by [cheng2016](https://github.com/cheng2016) (Apache 2.0). Refactored into a modern game-server starter.

## License

Apache License 2.0 — see [LICENSE](LICENSE).
