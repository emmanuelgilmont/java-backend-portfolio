# elasticsearch-formation-search

> **Portfolio project — be.gate25**  
> Full-text document search over an FSCrawler-indexed NAS — Spring Boot 3, Spring Data Elasticsearch, Testcontainers.

---

## What it does

- Full-text search over document content and metadata (title, author, keywords)
- Filter by file extension and date range
- Highlight matching excerpts in results
- Browse documents by directory path
- List all indexed directories

The index is built and maintained by [FSCrawler](https://fscrawler.readthedocs.io/) — this service is the Java/REST layer on top of it.

---

## Architecture

```
REST client
│
▼
┌──────────────────────────────────────────────────┐
│  Spring Boot 3 — elasticsearch-formation-search  │
│                                                  │
│  GET /v1/search?q=&extension=&from=&to=          │
│  GET /v1/browse?path=                            │
│  GET /v1/paths                                   │
│       │                                          │
│  SearchController                                │
│       │                                          │
│  SearchService                                   │
│  ├── FormationRepository  (simple queries)       │
│  └── ElasticsearchOperations                     │
│      (full-text, highlight, aggregations)        │
│       │                                          │
└───────┼──────────────────────────────────────────┘
│
▼
Elasticsearch (FSCrawler index: formations)
```

---

## API

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/v1/search` | Full-text search with optional filters |
| `GET` | `/v1/browse` | List documents under a given path |
| `GET` | `/v1/paths` | List all indexed directories |

### `/v1/search` parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `q` | String | Full-text query (content + title, title boosted x3) |
| `extension` | String | Filter by file extension (e.g. `pdf`, `mp4`) |
| `from` | Instant | Last modified — range start (ISO-8601) |
| `to` | Instant | Last modified — range end (ISO-8601) |

### Sample response

```json
[
  {
    "id": "445d5c22023151130cc7f6fc94d19d",
    "filename": "introduction-spring-boot.pdf",
    "title": "Introduction to Spring Boot",
    "extension": "pdf",
    "path": "/formations/spring",
    "lastModified": "2024-03-15T10:30:00Z",
    "highlight": "...a <em>Spring Boot</em> application starts with..."
  }
]
```

### Examples

```bash
# Full-text search
curl "http://localhost:8080/v1/search?q=spring+boot"

# Filter by extension
curl "http://localhost:8080/v1/search?q=kafka&extension=pdf"

# Date range
curl "http://localhost:8080/v1/search?from=2023-01-01T00:00:00Z&to=2024-01-01T00:00:00Z"

# Browse a directory
curl "http://localhost:8080/v1/browse?path=/webinaires"

# List all directories
curl "http://localhost:8080/v1/paths"
```

---

## FSCrawler index structure

Key fields used from the `formations` index:

| Field | Type | Usage |
|-------|------|-------|
| `content` | text | Full-text search |
| `meta.title` | text | Full-text search (boost x3) |
| `meta.author` | text | Metadata |
| `meta.language` | keyword | Filter |
| `file.extension` | keyword | Filter |
| `file.last_modified` | date | Range filter |
| `path.virtual` | keyword | Browse |
| `path.virtual.tree` | text (fscrawler_path) | Directory aggregation |

---

## Running locally

**Prerequisites:** Java 21, Maven, Elasticsearch reachable on `http://192.168.2.30:9200`.

```bash
./mvnw spring-boot:run
```

---

## Tests

Two layers — same principle as all projects in this portfolio:
**test behaviour locally, test infrastructure in CI.**

**Layer 1 — local (no Docker required)**
Not applicable here — this service has no behaviour that can be tested
without a real Elasticsearch instance.

**Layer 2 — CI (Docker required)**
Testcontainers spins up a real Elasticsearch 8.19.8 container.
`@Testcontainers(disabledWithoutDocker = true)` ensures the test is
skipped cleanly on machines without Docker.

```bash
mvn test
```

[![CI](https://github.com/emmanuelgilmont/java-backend-portfolio/actions/workflows/ci-elasticsearch.yml/badge.svg)](https://github.com/emmanuelgilmont/java-backend-portfolio/actions/workflows/ci-elasticsearch.yml)

---

## Module structure
```
elasticsearch-formation-search/
├── src/main/java/be/gate25/search/
│   ├── controller/
│   │   └── SearchController.java     # REST endpoints
│   ├── document/
│   │   └── FormationDocument.java    # @Document — FSCrawler index mapping
│   ├── dto/
│   │   ├── SearchRequest.java        # Query parameters (record)
│   │   └── SearchResult.java         # API response (record)
│   ├── repository/
│   │   └── FormationRepository.java  # Simple queries
│   └── service/
│       └── SearchService.java        # Full-text, highlight, aggregations
├── src/test/
│   └── SearchServiceIntegrationTest.java  # Testcontainers
└── application.properties
```
---