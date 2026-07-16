# grpc-price-service

> **Portfolio project — be.gate25**
> Demonstrates a gRPC unary call with Spring Boot 3, REST→gRPC bridge, MDC propagation interceptors and integration tests.

---

## Domain

Simulated **price lookup service** for a financial back-office system.

A REST client queries the current price of a financial instrument by symbol.
The REST controller delegates to the local gRPC service via an internal stub.
The transaction token (correlation ID) is propagated transparently across the HTTP→gRPC thread boundary.

```
  HTTP client
      │
      ▼
  ┌─────────────────────────────────────────────────────────┐
  │  Spring Boot 3 — Hermes                                 │
  │                                                         │
  │  GET /price/{symbol}                                    │
  │       │                                                 │
  │  PriceController                                        │
  │  (Tomcat thread — MDC: transactionToken=uuid)           │
  │       │                                                 │
  │  MdcClientInterceptor ── x-correlation-id ──▶           │
  │       │                                    │            │
  │       ▼               gRPC (in-process)    │            │
  │  PriceServiceGrpc stub                     │            │
  │       │                          MdcServerInterceptor   │
  │       │                          (Netty thread)         │
  │       │                          MDC: transactionToken  │
  │       ▼                                    │            │
  │  PriceServiceImpl ◀────────────────────────┘            │
  │  (Netty thread — MDC: transactionToken=uuid)            │
  └─────────────────────────────────────────────────────────┘
```

---

## Contract (`price_service.proto`)

```protobuf
service PriceService {
  rpc GetPrice (PriceRequest) returns (PriceResponse);
}

message PriceRequest {
  string symbol = 1;  // e.g. "BEL20:UCB"
}

message PriceResponse {
  string symbol    = 1;
  double price     = 2;
  string currency  = 3;  // e.g. "EUR"
  string timestamp = 4;  // ISO-8601
}
```

---

## Architecture decisions

| Decision | Choice | Rationale |
|---|---|---|
| Framework | Spring Boot 3 + `grpc-spring-boot-starter` 3.1.0 | Auto-configuration, `@GrpcService`, `@GrpcGlobalServerInterceptor` |
| Client exposure | REST → gRPC bridge (`PriceController`) | Pattern réaliste — une gateway HTTP délègue à des services gRPC internes |
| Domain model | `Price.java` interne, découplé du proto | Séparation des couches — le domaine ne dépend pas du contrat de transport |
| MDC propagation | `MdcClientInterceptor` + `MdcServerInterceptor` | Token de corrélation propagé du thread Tomcat vers le thread Netty via gRPC Metadata |
| Tests | `grpc-testing` — `Server`/`ManagedChannel` via `@BeforeEach/@AfterEach` | Serveur in-process réel, JUnit 5 natif, pas de mock du transport |
| Containerization | Dockerfile uniquement | Pas de broker externe nécessaire |

---

## Module structure

```
grpc-price-service/
├── docker/
│   └── Dockerfile
├── src/
│   ├── main/
│   │   ├── java/be/gate25/grpc/
│   │   │   ├── GrpcPriceServiceApplication.java
│   │   │   ├── controller/
│   │   │   │   └── PriceController.java     # REST → gRPC bridge (GET /price/{symbol})
│   │   │   ├── interceptor/
│   │   │   │   ├── MdcClientInterceptor.java # @GrpcGlobalClientInterceptor — MDC → Metadata
│   │   │   │   └── MdcServerInterceptor.java # @GrpcGlobalServerInterceptor — Metadata → MDC
│   │   │   ├── repository/
│   │   │   │   └── PriceRepository.java      # Stub — hardcoded BEL20 prices
│   │   │   ├── service/
│   │   │   │   └── PriceServiceImpl.java     # @GrpcService — extends PriceServiceImplBase
│   │   │   └── model/
│   │   │       └── Price.java                # Internal domain model, decoupled from proto
│   │   ├── proto/
│   │   │   └── price_service.proto
│   │   └── resources/
│   │       └── application.properties
│   └── test/java/be/gate25/grpc/
│       ├── PriceServiceIntegrationTest.java  # In-process server, JUnit 5 — gRPC layer only
│       └── PriceServiceMdcPropagationIT.java # @SpringBootTest — verifies MDC propagation across the Tomcat/Netty boundary
├── pom.xml
└── README.md
```

---

## Running locally

```bash
# Dev machine — run the gRPC server + REST bridge
./mvnw spring-boot:run

# Test known symbol
curl http://localhost:8080/price/BEL20:UCB

# Test unknown symbol (expect 404)
curl http://localhost:8080/price/UNKNOWN:XYZ

# Build the image (infra server, from project root)
docker build -f docker/Dockerfile .
```

---

## Tests

Two complementary layers — same principle as the rest of the portfolio: fast, isolated
tests for the gRPC contract itself, plus a full-context test for anything that depends
on Spring Boot auto-configuration.

| Test | What it covers |
|---|---|
| `PriceServiceIntegrationTest` | In-process gRPC server (`InProcessServerBuilder`), no Spring context. Fast. Verifies `PriceServiceImpl` business logic and the `NOT_FOUND` error path. |
| `PriceServiceMdcPropagationIT` | Full `@SpringBootTest` on a random port. The interceptors (`MdcClientInterceptor`, `MdcServerInterceptor`) are only registered via Spring Boot auto-configuration, so this is the only test that exercises them — it confirms the transaction token set on the Tomcat thread is still present in MDC when `PriceServiceImpl` runs on the Netty thread. |

```bash
mvn test
```

---

## Status

- [x] Architecture README
- [x] Maven skeleton (`be.gate25`)
- [x] `price_service.proto` (contract)
- [x] `PriceServiceImpl` (`@GrpcService`)
- [x] `Price.java` (internal domain model)
- [x] `PriceRepository` (stub — BEL20 hardcoded prices)
- [x] `application.properties`
- [x] `PriceServiceIntegrationTest` — 2 tests verts (known symbol + NOT_FOUND)
- [x] Dockerfile
- [x] REST → gRPC bridge (`PriceController`)
- [x] Interceptors MDC — `MdcClientInterceptor` + `MdcServerInterceptor`
- [x] Integration test — MDC propagation verified end-to-end across the Tomcat → Netty boundary (`PriceServiceMdcPropagationIT`)
- [ ] Server streaming (optionnel)
- [ ] Client stub mocké dans les tests (optionnel)

---

## Commit conventions

This project uses [Conventional Commits](https://www.conventionalcommits.org/):
`feat:`, `fix:`, `test:`, `docs:`, `chore:`, `refactor:`