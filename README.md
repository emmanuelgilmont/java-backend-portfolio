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

<table style="width:100%">
  <tr><th style="width:20%">Project</th><th style="width:80%">Description</th></tr>
  <tr><td><a href="./spring-boot/discord-service">discord-service</a></td><td>Discord bot gateway — send private DMs and public channel messages via REST API, multi-user support, zero secrets in source</td></tr>
  <tr><td><a href="./spring-boot/elasticsearch-formation-search">elasticsearch-formation-search</a></td><td>Full-text document search over an FSCrawler-indexed NAS — Spring Data Elasticsearch, highlight, aggregations, Testcontainers</td></tr>
  <tr><td><a href="./spring-boot/fx-rate-service">fx-rate-service</a></td><td>FX rate lookup with Redis cache-aside — <code>@Cacheable</code>/<code>@CacheEvict</code>, per-cache TTL, Testcontainers integration test, Prometheus/Grafana observability</td></tr>
  <tr><td><a href="./spring-boot/weather-api">weather-api</a></td><td>Spring Boot service consuming OpenWeatherMap — REST client, DTO mapping, error handling </td></tr>
</table>

### Quarkus

<table style="width:100%">
  <tr><th style="width:20%">Project</th><th style="width:80%">Description</th></tr>
  <tr><td><a href="./quarkus/q-weather">q-weather</a></td><td>Quarkus equivalent of weather-api — same domain, different stack. MicroProfile REST Client, Caffeine cache, human-readable endpoint</td></tr>
</table>

### Starters & Libraries

<table style="width:100%">
  <tr><th style="width:20%">Project</th><th style="width:80%">Description</th></tr>
  <tr><td><a href="./starters/transaction-token-starter">transaction-token-starter</a></td><td>Spring Boot Starter for automatic MDC-based transaction log correlation — drop-in, zero boilerplate</td></tr>
</table>

### Kafka

<table style="width:100%">
  <tr><th style="width:20%">Project</th><th style="width:80%">Description</th></tr>
  <tr><td><a href="./kafka/kafka-financial-pipeline">kafka-financial-pipeline</a></td><td>🚧 In progress — trade event pipeline with DLQ and Testcontainers (Spring Boot 3, Kafka)</td></tr>
</table>

### Batch

<table style="width:100%">
  <tr><th style="width:20%">Project</th><th style="width:80%">Description</th></tr>
  <tr><td><a href="./batch/trade-settlement-batch">trade-settlement-batch</a></td><td>EOD trade settlement batch — <code>FlatFileItemReader</code>, <code>CompositeItemWriter</code> (DB + CSV), T+2 calculation, two-layer testing strategy (H2 / Testcontainers PostgreSQL)</td></tr>
</table>

### gRPC

<table style="width:100%">
  <tr><th style="width:20%">Project</th><th style="width:80%">Description</th></tr>
  <tr><td><a href="./grpc/grpc-price-service">grpc-price-service</a></td><td>gRPC unary service — financial price lookup with REST→gRPC bridge and MDC correlation propagation (Spring Boot 3)</td></tr>
</table>

### Automation

<table style="width:100%">
  <tr><th style="width:20%">Project</th><th style="width:80%">Description</th></tr>
  <tr><td><a href="./automation/gmail-automation">gmail-automation</a></td><td>Twice-daily Gmail triage — Claude Haiku classifies job opportunities, deduplicates against 7-day history, logs to Google Sheets, notifies via Telegram</td></tr>
</table>

---

## Tech stack

| Layer | Tech                                              |
|---|---------------------------------------------------|
| Language | Java 8 → 21                                       |
| Framework | Spring Boot 2 / 3, Quarkus 3                      |
| Build | Maven                                             |
| Data | PostgreSQL, Oracle, Elasticsearch                |
| Messaging | Kafka, gRPC                                       |
| Tooling | Docker, Jenkins, SonarQube, Nexus / Artifactory   |
| Testing | JUnit, Mockito, Testcontainers                    |
| Cache   | Redis, Caffeine                                   |
| Monitoring | Prometheus, Grafana                               |
| Batch   | Spring Batch 5                                    |

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