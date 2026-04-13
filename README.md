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
- Kafka messaging
- Elasticsearch integration
- External API consumption
- Docker-based local execution
- JUnit / Mockito testing
- Code quality with SonarQube

---

## Projects

### Spring Boot

|     Project                                                                        | Description |
|------------------------------------------------------------------------------------|---|
| [discord-service](./spring-boot/discord-service)                               | Discord bot gateway — send private DMs and public channel messages via REST API, multi-user support, zero secrets in source  |
| [elasticsearch-formation-search](./elasticsearch/elasticsearch-formation-search) | Full-text document search over an FSCrawler-indexed NAS — Spring Data Elasticsearch, highlight, aggregations, Testcontainers |
| [fx-rate-service](./spring-boot/fx-rate-service)                               | FX rate lookup with Redis cache-aside — `@Cacheable`/`@CacheEvict`, per-cache TTL, Testcontainers integration test           |
| [weather-service](./spring-boot/weather-service)                               | Spring Boot service consuming OpenWeatherMap — REST client, DTO mapping, error handling                                      |

### Quarkus

| Project | Description |
|---|---|
| [q-weather](./quarkus/q-weather) | Quarkus equivalent of weather-api — same domain, different stack. MicroProfile REST Client, Caffeine cache, human-readable endpoint |

### Starters & Libraries

| Project                                                           | Description |
|-------------------------------------------------------------------|---|
| [transaction-token-starter](./starters/transaction-token-starter) | Spring Boot Starter for automatic MDC-based transaction log correlation — drop-in, zero boilerplate |

### Kafka

|     Project                                                      | Description |
|------------------------------------------------------------------|---|
| [kafka-financial-pipeline](./kafka/kafka-financial-pipeline) | 🚧 In progress — trade event pipeline with DLQ and Testcontainers (Spring Boot 3, Kafka) |


### gRPC

|      Project                                         | Description |
|------------------------------------------------------|---|
| [grpc-price-service](./grpc/grpc-price-service) | gRPC unary service — financial price lookup with REST→gRPC bridge and MDC correlation propagation (Spring Boot 3) |

---

## Tech stack

| Layer | Tech                                                 |
|---|-------------------------------------------------------|
| Language | Java 8 → 21                                       |
| Framework | Spring Boot 2 / 3, Quarkus 3                     |
| Build | Maven                                                |
| Data | PostgreSQL, Oracle, Elasticsearch                     |
| Messaging | Kafka, gRPC                                      |
| Tooling | Docker, Jenkins, SonarQube, Nexus / Artifactory    |
| Testing | JUnit, Mockito, Testcontainers                     |
| Cache   | Redis, Caffeine                                    |

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
[gate25.be](https://gate25.be) |
[code@gate25.be](mailto:code@gate25.be)

---

## License

MIT — see [LICENSE](LICENSE)