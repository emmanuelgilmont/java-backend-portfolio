# trade-settlement-batch

> **Portfolio project — be.gate25**
> EOD trade settlement batch — Spring Batch 5, Java 21, PostgreSQL, two-layer testing strategy.

---

## Domain

End-of-day settlement processing for a simulated financial back-office.

A daily CSV file of raw trade records is processed to produce:
- a **settlement report** inserted into a PostgreSQL `settlements` table
- a **daily CSV output file** (`settlements_YYYYMMDD.csv`)

CANCELLED trades are filtered out. PENDING trades are enriched with a T+2 settlement date and a computed settlement amount (quantity × price).

```
trades_YYYYMMDD.csv
    │
    ▼
┌─────────────────────────────────────────────────────┐
│  Spring Batch — settlementJob                       │
│                                                     │
│  FlatFileItemReader<TradeRecord>                    │
│      │  (1 item per CSV row, skip header)           │
│      ▼                                              │
│  SettlementProcessor                                │
│      │  CANCELLED → null (filtered, no write)       │
│      │  PENDING   → Settlement (T+2, qty × price)   │
│      ▼                                              │
│  CompositeItemWriter<Settlement>                    │
│      ├── JdbcBatchItemWriter → settlements table    │
│      └── FlatFileItemWriter  → settlements_*.csv    │
└─────────────────────────────────────────────────────┘
```

---

## Job parameters

| Parameter   | Example                            | Description                    |
|-------------|------------------------------------|--------------------------------|
| `inputFile` | `/data/input/trades_20260521.csv`  | Absolute path to input CSV     |
| `runDate`   | `2026-05-21`                       | Run date (used for output name)|
| `outputDir` | `/data/output`                     | Output directory for CSV report|

---

## Input CSV format

```csv
tradeId,symbol,side,quantity,price,currency,tradeDate,status
T001,BEL20:UCB,BUY,100,134.50,EUR,2026-05-21,PENDING
T002,BEL20:KBC,SELL,200,72.30,EUR,2026-05-21,CANCELLED
```

`status` is either `PENDING` (processed) or `CANCELLED` (filtered out, not written).

---

## Testing strategy — two layers

The dev machine runs Windows without Docker (intentional — mirrors an acceptance/production environment). Tests are split into two independent layers.

### Layer 1 — `SettlementJobLocalTest` (local, no Docker)

```bash
mvn test
```

Active profile `test` → **H2** in-memory for both the `JobRepository` (BATCH_* tables) and the `settlements` business table. Identical Spring Batch application context, different DataSource.

| Assertion | What it proves |
|---|---|
| `status == COMPLETED` | Job runs end-to-end without error |
| `readCount == 5` | All CSV rows (excluding header) are read |
| `filterCount == 1` | CANCELLED trade is correctly filtered |
| `writeCount == 4` | 4 settlements are written |
| Output CSV exists with 5 lines | FlatFileItemWriter produces correct output |

### Layer 2 — `SettlementJobPostgresIT` (CI, Docker required)

```bash
# Skipped automatically if Docker is not available
mvn test
```

`@Testcontainers(disabledWithoutDocker = true)` starts `postgres:16-alpine` on a random port. `@DynamicPropertySource` wires the container URL into the Spring context.

What this layer adds on top of H2:

| Aspect | H2 | Real PostgreSQL |
|---|---|---|
| Spring Batch BATCH_* tables | ✓ (H2 dialect) | ✓ (PostgreSQL dialect) |
| Settlements persisted | ✓ | ✓ |
| Real PostgreSQL DATE / TIMESTAMP | ✗ | ✓ |
| T+2 date queryable via SQL | ✗ | ✓ |

---

## Module structure

```
trade-settlement-batch/
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
├── src/
│   ├── main/
│   │   ├── java/be/gate25/batch/
│   │   │   ├── TradeBatchApplication.java
│   │   │   ├── config/
│   │   │   │   └── BatchConfig.java          # Job, Step, Reader, Writers
│   │   │   ├── domain/
│   │   │   │   ├── TradeRecord.java           # Input DTO (from CSV)
│   │   │   │   ├── Settlement.java            # Output domain object
│   │   │   │   └── SettlementStatus.java      # SETTLED / CANCELLED / FAILED
│   │   │   ├── processor/
│   │   │   │   └── SettlementProcessor.java   # Filter + enrich
│   │   │   └── listener/
│   │   │       └── JobCompletionListener.java # Summary log after job
│   │   └── resources/
│   │       ├── application.properties         # PostgreSQL (prod)
│   │       └── db/schema.sql                 # settlements DDL (H2 + PostgreSQL)
│   └── test/
│       ├── java/be/gate25/batch/
│       │   ├── SettlementJobLocalTest.java    # Layer 1: H2, no Docker
│       │   └── SettlementJobPostgresIT.java   # Layer 2: Testcontainers PostgreSQL
│       └── resources/
│           ├── application-test.properties    # H2 config
│           └── test-data/trades_test.csv      # 5 trades, 1 CANCELLED
└── pom.xml
```

---

## Running locally

```bash
# Layer 1 — local tests only (no Docker)
mvn test

# Build the JAR
mvn package -DskipTests

# Copy JAR to docker/ and transfer to the Docker server via SCP
scp target/trade-settlement-batch-1.0.0-SNAPSHOT.jar user@homelab:/path/to/docker/

# On the Docker server — start PostgreSQL + run the batch
cd docker
mkdir -p input output
cp /path/to/trades_20260521.csv input/
docker compose up --build
```

---

## Architecture decisions

| Decision | Choice | Rationale |
|---|---|---|
| Spring Batch 5 | No `@EnableBatchProcessing` | Boot 3 auto-configures `JobRepository` and `PlatformTransactionManager`; the annotation *disables* this |
| Schema init | `spring.batch.jdbc.initialize-schema=always` + `spring.sql.init` | No Flyway dependency — keeps scope focused on Batch (Flyway is project #4) |
| Local test DB | H2 in-memory | No Docker on dev machine; same Spring context, same DDL |
| CI test DB | Testcontainers PostgreSQL 16 | Real DB types, real BATCH_* schema, skipped without Docker |
| `@StepScope` on reader + CSV writer | Job parameter injection | `inputFile`, `outputDir`, `runDate` resolved at step start, not at context load |
| `BigDecimal` for all amounts | Financial precision | Never `double` for monetary values |
| T+2 settlement | `tradeDate.plusDays(2)` | Standard equity settlement convention |
| Output CSV | `FlatFileItemWriter` + `BeanWrapperFieldExtractor` | Standard Spring Batch, no external dependency |

---

## Conventional Commits used

```
chore: add Maven skeleton (trade-settlement-batch)
feat: add domain model (TradeRecord, Settlement, SettlementStatus)
feat: add SettlementProcessor with T+2 logic and CANCELLED filtering
feat: add BatchConfig (Job, Step, FlatFileItemReader, CompositeItemWriter)
feat: add JobCompletionListener with execution summary
test: add SettlementJobLocalTest (H2, Layer 1)
test: add SettlementJobPostgresIT (Testcontainers, Layer 2)
docs: add README with architecture and testing strategy
chore: add Dockerfile and docker-compose
```