# discord-service

> **Portfolio project — be.gate25**
> A lightweight Spring Boot microservice that acts as a Discord bot gateway.  
It exposes a simple REST API to send private messages (DM) to one or more users, and public messages to a configured channel — without embedding any Discord logic in the caller.

---

## Features

- Send a private Discord DM to a single user or a comma-separated list of users
- Send a public message to a pre-configured Discord channel
- Liveness check endpoint that pings the admin via DM
- Zero secrets in source — all credentials are injected via environment variables

---

## Tech stack

- Java 21
- Spring Boot 3.x
- Discord REST API v10

---

## API Endpoints

### `GET /v1/discord/ping`

Sends a DM to the configured admin user to confirm the bot is alive.

```bash
curl http://localhost:8081/v1/discord/ping
```

---

### `POST /v1/discord/private`

Sends a private message (DM) to one or more Discord users.

| Parameter | Type   | Description                                          |
|-----------|--------|------------------------------------------------------|
| `userId`  | String | A single Discord user ID or a comma-separated list   |
| `message` | String | The message to send                                  |

```bash
# Single user
curl -X POST "http://localhost:8081/v1/discord/private" \
     --data-urlencode "userId=548604749919027222" \
     --data-urlencode "message=Hello from the bot!"

# Multiple users
curl -X POST "http://localhost:8081/v1/discord/private" \
     --data-urlencode "userId=394416569767165953,548604749919027222" \
     --data-urlencode "message=Hello everyone!"
```

---

### `POST /v1/discord/public`

Sends a message to the pre-configured Discord channel.

| Parameter | Type   | Description            |
|-----------|--------|------------------------|
| `message` | String | The message to send    |

```bash
curl -X POST "http://localhost:8081/v1/discord/public" \
     --data-urlencode "message=Hello channel!"
```

---

## Configuration

All secrets are provided via environment variables. No secrets are stored in source.

| Environment variable  | Description                                      |
|-----------------------|--------------------------------------------------|
| `DISCORD_TOKEN`       | Discord bot token                                |
| `DISCORD_CHANNEL_ID`  | Target channel ID for public messages            |
| `DISCORD_ADMIN`       | Discord user ID of the admin (used by `/ping`)   |

---

## Running locally

```bash
export DISCORD_TOKEN=your_bot_token
export DISCORD_CHANNEL_ID=your_channel_id
export DISCORD_ADMIN=your_user_id

mvn spring-boot:run
```

---

## Docker deployment

The `docker/` folder contains a ready-to-use `docker-compose.yml`.

1. Copy the JAR into the `docker/` folder:
   ```bash
   mvn package -DskipTests
   cp target/discord-0.0.1-SNAPSHOT.jar docker/
   ```

2. Create `docker/.env` (**never commit this file**):
   ```
   DISCORD_TOKEN=your_bot_token
   DISCORD_CHANNEL_ID=your_channel_id
   DISCORD_ADMIN=your_user_id
   ```

3. Start the service:
   ```bash
   cd docker
   docker compose up -d
   ```

The service will be available on port `8766` (mapped to internal port `8081`).

> **Note:** this service uses an external Docker network named `bot-network`.  
> Create it once with: `docker network create bot-network`

---

## Project structure

```
discord/
├── docker/
│   ├── docker-compose.yml
│   └── .env                  ← not committed, see .gitignore
├── src/
│   ├── main/
│   │   ├── java/be/gate25/discord/
│   │   │   ├── DiscordApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── HeartbeatController.java
│   │   │   │   └── PublicController.java
│   │   │   └── service/
│   │   │       └── DiscordService.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/be/gate25/discord/
│           ├── DiscordApplicationTests.java
│           └── controller/
│               ├── HeartbeatControllerTest.java
│               └── PublicControllerTest.java
└── pom.xml
```