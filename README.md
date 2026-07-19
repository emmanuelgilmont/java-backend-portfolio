# Emmanuel Gilmont — Backend Portfolio

25 years of Java. Still writing it by hand.

Freelance backend developer based in Belgium. This repository is a curated
collection of backend projects and technical explorations — not a highlight
reel of buzzwords, but a honest look at how I design, structure and ship
backend code.

---

## What you'll find here

Clean, production-minded backend code built around things I actually know:

- REST API design and implementation
- Spring Boot services (2 and 3)
- Microservices patterns
- API Gateway & service discovery (Spring Cloud Gateway, Eureka)
- Kafka messaging
- Elasticsearch integration
- External API consumption
- Docker-based local execution
- JUnit / Mockito testing
- Code quality with SonarQube

---

## Projects

### Spring Boot

| Project                                                                          | Description                                                                                                                                          |
|----------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| [discord-service](./spring-boot/discord-service)                                 | Discord bot gateway — send private DMs and public channel messages via REST API, multi-user support, zero secrets in source                          |
| [fx-rate-service](./spring-boot/fx-rate-service)                                 | FX rate lookup with Redis cache-aside — `@Cacheable`/`@CacheEvict`, per-cache TTL, Testcontainers integration test, Prometheus/Grafana observability |
| [weather-service](./spring-boot/weather-service)                                 | Spring Boot service consuming OpenWeatherMap — REST client, DTO mapping, error handling                                                              |

### Elasticsearch

| Project                          | Description                                                                                                                         |
|----------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| [elasticsearch-formation-search](./elasticsearch/elasticsearch-formation-search) | Full-text document search over an FSCrawler-indexed NAS — Spring Data Elasticsearch, highlight, aggregations, Testcontainers                         |

### Quarkus

| Project                          | Description                                                                                                                         |
|----------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| [q-weather](./quarkus/q-weather) | Quarkus equivalent of weather-api — same domain, different stack. MicroProfile REST Client, Caffeine cache, human-readable endpoint |

### Starters & Libraries

| Project                                                           | Description                                                                                         |
|-------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| [transaction-token-starter](./starters/transaction-token-starter) | Spring Boot Starter for automatic MDC-based transaction log correlation — drop-in, zero boilerplate |

### Kafka

| Project                                                      | Description                                                                              |
|--------------------------------------------------------------|------------------------------------------------------------------------------------------|
| [kafka-financial-pipeline](./kafka/kafka-financial-pipeline) | 🚧 In progress — trade event pipeline with DLQ and Testcontainers (Spring Boot 3, Kafka) |

### Batch

| Project                                                  | Description                                                                                                                                                       |
|----------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [trade-settlement-batch](./batch/trade-settlement-batch) | EOD trade settlement batch — `FlatFileItemReader`, `CompositeItemWriter` (DB + CSV), T+2 calculation, two-layer testing strategy (H2 / Testcontainers PostgreSQL) |

### gRPC

| Project                                         | Description                                                                                                       |
|-------------------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| [grpc-price-service](./grpc/grpc-price-service) | gRPC unary service — financial price lookup with REST→gRPC bridge and MDC correlation propagation (Spring Boot 3) |

### Automation

| Project                                           | Description                                                                                                                                            |
|---------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| [gmail-automation](./automation/gmail-automation) | Twice-daily Gmail triage — Claude Haiku classifies job opportunities, deduplicates against 7-day history, logs to Google Sheets, notifies via Telegram |

---

### Gateway

| Project                                    | Description                                                                                                                       |
|---------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| [eureka-server](./gateway/eureka-server)     | Netflix Eureka service registry — standalone, self-preservation disabled                                                          |
| [gateway-service](./gateway/gateway-service) | 🚧 In progress — Spring Cloud Gateway (WebFlux) routing to fx-rate-service, grpc-price-service and weather-service; static routes today, service-discovery routes planned |

---

## Tech stack

| Layer      | Tech                                            |
|------------|-------------------------------------------------|
| Language   | Java 8 → 25                                     |
| Framework  | Spring Boot 2 / 3, Quarkus 3                    |
| Framework  | Spring Boot 4.1 (gateway module — first Boot 4 / Java 25 in the repo) |
| Build      | Maven                                           |
| Data       | PostgreSQL, Oracle, Elasticsearch               |
| Messaging  | Kafka, gRPC                                     |
| Discovery / Gateway | Spring Cloud Gateway, Netflix Eureka   |
| Tooling    | Docker, Jenkins, SonarQube, Nexus / Artifactory |
| Testing    | JUnit, Mockito, Testcontainers                  |
| Cache      | Redis, Caffeine                                 |
| Monitoring | Prometheus, Grafana                             |
| Batch      | Spring Batch 5                                  |

---

## Version strategy

This isn't a fleet of production services that all need to stay on the latest patch —
it's a portfolio, and each project is a snapshot of what I was building with at the time.
A project written against a given Java / Spring Boot version a year ago stays there
deliberately; it documents what I used then, not a maintenance backlog. The version
spread below happened organically, not as a curated demonstration.

| Project | Java | Spring Boot | Notes |
|---|---|---|---|
| `weather-service`                | 21 | 4.0.2 | Pinned at time of writing |
| `fx-rate-service`                | 21 | 3.3.5 | Pinned at time of writing |
| `grpc-price-service`             | 21 | 3.2.5 | Pinned at time of writing |
| `q-weather`                      | 21 | Quarkus 3 | Pinned at time of writing |
| `elasticsearch-formation-search` | 21 | 3.5.13 | Pinned at time of writing |
| `trade-settlement-batch`         | 21 | 3.3.5 (Spring Batch 5) | Pinned at time of writing |
| `kafka-financial-pipeline`       | à confirmer | à confirmer | Pinned at time of writing |
| `transaction-token-starter`      | 17 | 3.4.3 | Pinned at time of writing |
| `discord-service`                | 21 | 3.5.12 | Pinned at time of writing |

---

## Engineering philosophy

I write code for the developer who maintains it six months later —
often me.

Clarity over cleverness. Explicit over implicit. Boring where it should be
boring, careful where it matters.

---

## About me

**Emmanuel Gilmont** — Freelance Java Backend Developer, Belgium
25+ years of backend experience. Currently available for contracts.

[LinkedIn](https://www.linkedin.com/in/emmanuelgilmont) |
[gate25.be](https://gate25.be) — built with Stitch, Claude & Claude Code |
[code@gate25.be](mailto:code@gate25.be)

---

## License

MIT — see [LICENSE](LICENSE)