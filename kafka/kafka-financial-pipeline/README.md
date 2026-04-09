# kafka-financial-pipeline

> **Portfolio project — be.gate25**
> Demonstrates a production-grade Kafka financial event pipeline with Dead Letter Queue, Testcontainers integration tests and observability hooks.

---

## Domain

Simulated **trade event pipeline** for a back-office processing system.

A trading front-end emits raw `TradeEvent` messages (buy/sell orders).
The pipeline validates, enriches and routes them. Invalid or unprocessable events are sent to a Dead Letter Queue for later inspection/replay.

```
                         ┌─────────────────────────────────────────────────────┐
                         │               kafka-financial-pipeline              │
                         │                                                     │
  ┌──────────────┐       │  ┌─────────────┐    ┌───────────────────────────┐   │
  │ TradeProducer│──────▶│  │   trade-    │    │   TradeEventConsumer      │   │
  │  (REST /     │       │  │  events     │───▶│   - validation            │   │
  │   Scheduler) │       │  │  (topic)    │    │   - enrichment            │   │
  └──────────────┘       │  └─────────────┘    │   - routing               │   │
                         │                     └───────────┬───────────────┘   │
                         │                                 │                   │
                         │              ┌──────────────────┴──────────────┐    │
                         │              │                                 │    │
                         │    ┌─────────▼──────────┐     ┌───────────────▼─┐   │
                         │    │  trade-events-     │     │  trade-events-  │   │
                         │    │  processed (topic) │     │  dlq (topic)    │   │
                         │    └────────────────────┘     └─────────────────┘   │
                         └─────────────────────────────────────────────────────┘
```

---

## Architecture decisions

| Decision | Choice | Rationale |
|---|---|---|
| Messaging | Apache Kafka | Durable, partitioned, replayable — standard in financial back-office |
| Framework | Spring Boot 3 / Spring Kafka | Production-grade auto-configuration, `@KafkaListener`, `@KafkaHandler` |
| DLQ strategy | Dedicated topic + manual ack | Explicit, auditable — avoids silent data loss |
| Serialization | JSON (Jackson) | Simple for the demo; Avro/Schema Registry in optional extension |
| Tests | Testcontainers (Kafka) | Real broker in CI, no mocking of the transport layer |
| Containerization | Docker Compose | Local dev parity; broker + app in one command |

---

## Topics

| Topic | Partitions | Purpose |
|---|---|---|
| `trade-events` | 3 | Raw inbound trade events from producers |
| `trade-events-processed` | 3 | Successfully validated & enriched events |
| `trade-events-dlq` | 1 | Failed / unprocessable events for inspection |

---

## TradeEvent model (draft)

```json
{
  "eventId": "uuid",
  "symbol": "BEL20:UCB",
  "side": "BUY",
  "quantity": 500,
  "price": 142.30,
  "currency": "EUR",
  "traderId": "T-0042",
  "timestamp": "2026-04-09T08:00:00Z"
}
```

---

## Module structure (planned)

```
kafka-financial-pipeline/
├── docker/
│   ├── docker-compose.yml  ← infra server only
│   └── Dockerfile
├── src/
│   ├── main/java/be/gate25/kafka/
│   │   ├── model/          # TradeEvent, EnrichedTradeEvent
│   │   ├── producer/       # TradeEventProducer
│   │   ├── consumer/       # TradeEventConsumer
│   │   ├── enrichment/     # EnrichmentService
│   │   ├── dlq/            # DlqPublisher
│   │   └── config/         # KafkaConfig, TopicConfig
│   └── test/java/be/gate25/kafka/
│       └── integration/    # KafkaPipelineIntegrationTest (Testcontainers)
├── pom.xml
└── README.md
```

---

## Running locally

> Docker files live in `docker/` — they are intended for the infrastructure server, not the dev machine.

```bash
# From the docker/ directory on the infra server
cd docker
docker compose up -d

# Build the image explicitly (context = project root)
docker build -f docker/Dockerfile .

# Run the application locally (dev machine, no Docker)
./mvnw spring-boot:run

# Produce a test event (once REST endpoint is wired)
curl -X POST http://localhost:8080/trade \
  -H "Content-Type: application/json" \
  -d '{"symbol":"BEL20:UCB","side":"BUY","quantity":500,"price":142.30,"currency":"EUR","traderId":"T-0042"}'
```

---

## Status

- [x] Architecture README
- [ ] Maven skeleton (`be.gate25`)
- [ ] Docker Compose (Kafka + Zookeeper)
- [ ] TradeEvent model
- [ ] Producer (REST trigger)
- [ ] Consumer + validation + enrichment
- [ ] DLQ publisher
- [ ] Testcontainers integration test
- [ ] Kafdrop (optional monitoring)
- [ ] Kafka Streams + windowing (optional)
- [ ] Avro / Schema Registry (optional)

---

## Commit conventions

This project uses [Conventional Commits](https://www.conventionalcommits.org/):
`feat:`, `fix:`, `test:`, `docs:`, `chore:`, `refactor:`