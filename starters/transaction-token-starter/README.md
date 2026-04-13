# transaction-token-starter

> **Portfolio project — be.gate25**
> A lightweight Spring Boot Starter that brings **automatic transaction log correlation** to any REST application — zero boilerplate for developers.

## The problem

In a typical Spring Boot REST app, log output from concurrent requests is interleaved in log files. Tracing a single failing transaction means grepping through thousands of unrelated lines. The naive solution — manually passing a token through every method — is tedious, error-prone, and pollutes the code.

## The solution

This starter automatically:

1. **Generates a unique UUID token** for every incoming HTTP request (or reuses an existing `X-Correlation-ID` header for microservice chaining).
2. **Injects the token into SLF4J MDC** — every `log.xxx()` call anywhere in the application automatically includes the token, with zero code change.
3. **Echoes the token in the `X-Correlation-ID` response header**.
4. **Returns the token in the error response body** when an exception reaches the client, so the helpdesk can grep the logs instantly.
5. **Always cleans up the MDC** at the end of the request, preventing token leakage across thread-pool reuse.

## What the developer writes

```java
// In a service — no token, no boilerplate
log.info("Processing order {}", orderId);
log.debug("Step 1 complete");
throw new RuntimeException("Something went wrong");
```

## What appears in the logs

```
14:32:01.123 [a1b2c3d4-e5f6-7890-abcd-ef1234567890] INFO  OrderService - Processing order 42
14:32:01.124 [a1b2c3d4-e5f6-7890-abcd-ef1234567890] DEBUG OrderService - Step 1 complete
14:32:01.125 [a1b2c3d4-e5f6-7890-abcd-ef1234567890] ERROR GlobalExceptionHandler - Unhandled exception [token=a1b2c3d4-...]
```

## What the client receives on error

```json
{
  "status": 500,
  "message": "An unexpected error occurred. Please contact the helpdesk with your token.",
  "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

The helpdesk runs:
```bash
grep "a1b2c3d4-e5f6-7890-abcd-ef1234567890" application.log
```
And gets the full transaction trace instantly.

---

## Getting Started — running the example

The repository contains two modules: the starter itself and a ready-to-run example application that demonstrates all three scenarios (200, 400, 500).

**Step 1 — Install the starter into your local Maven repository**

```bash
cd transaction-token-starter
mvn install
```

**Step 2 — Start the example application**

```bash
cd transaction-token-example
mvn spring-boot:run
```

The application starts on `http://localhost:8080`.

**Step 3 — Hit the endpoints**

```bash
# Happy path — returns a greeting, token visible in the X-Correlation-ID response header
curl -v http://localhost:8080/api/greet?name=Alice

# 400 Bad Request — blank name, token returned in the JSON error body
curl http://localhost:8080/api/fail-gracefully

# 500 Internal Server Error — unexpected exception, stack trace logged server-side only
curl http://localhost:8080/api/fail-hard
```

**Step 4 — Observe the logs**

Every log line for a given request shares the same token:

```
14:32:01.123 [a1b2c3d4-e5f6-7890-abcd-ef1234567890] INFO  GreetingController - greet() received request for name='Alice'
14:32:01.124 [a1b2c3d4-e5f6-7890-abcd-ef1234567890] DEBUG GreetingService    - greet() called with name='Alice'
14:32:01.125 [a1b2c3d4-e5f6-7890-abcd-ef1234567890] INFO  GreetingService    - greet() returning: 'Hello, Alice!'
```

To replay any transaction, the helpdesk greps the log file with the token provided by the client:

```bash
grep "a1b2c3d4-e5f6-7890-abcd-ef1234567890" logs/application.log
```

---

## Using the starter in your own project

### 1. Deploy to your Nexus (or install locally)

```bash
# Local only — available on this machine
cd transaction-token-starter && mvn install

# Shared — push to your Nexus instance
cd transaction-token-starter && mvn deploy
```

### 2. Add the dependency

```xml
<dependency>
    <groupId>be.gate25</groupId>
    <artifactId>transaction-token-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 3. Configure the log pattern

In your `logback-spring.xml`, add `%X{transactionToken}` to the pattern:

```xml
<pattern>%d{HH:mm:ss.SSS} [%X{transactionToken}] %-5level %logger - %msg%n</pattern>
```

**That's it.** No `@Import`, no `@ComponentScan`, no configuration class.

---

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `token-context.exception-handler.enabled` | `true` | Set to `false` to disable the built-in `@RestControllerAdvice` and use your own |

---

## Accessing the token explicitly

The MDC handles logging transparently. If you ever need the token value itself (e.g. in a custom error handler):

```java
import be.gate25.tokencontext.TokenContext;

String token = TokenContext.getToken();
```

---

## Microservice chaining

When service A calls service B, pass the token in the outgoing request:

```java
restClient.get()
    .uri("http://service-b/api/something")
    .header("X-Correlation-ID", TokenContext.getToken())
    .retrieve()
    ...
```

Service B will reuse the same token — the full cross-service trace is correlated in the logs.

---

## Module structure

```
transaction-token-starter/
├── TokenFilter.java                    # Servlet filter: init + cleanup
├── TokenContext.java                   # Static facade for explicit token access
├── ErrorResponse.java                  # Standard error response body
├── GlobalExceptionHandler.java         # @RestControllerAdvice: logs + safe error response
└── TokenContextAutoConfiguration.java  # Spring Boot auto-configuration entry point

transaction-token-example/
├── GreetingController.java             # REST endpoints: happy path, 400, 500
├── GreetingService.java                # Service with natural logging (no token code)
├── logback-spring.xml                  # Pattern with %X{transactionToken}
└── application.properties
```

---

## Note on virtual threads

If your application uses Spring Boot virtual threads (`spring.threads.virtual.enabled=true`), the MDC-based approach works correctly for standard request handling — each virtual thread processing a request carries its own MDC context. Be cautious if you spawn child threads manually inside a request: explicitly copy the MDC context to child threads using `MDC.getCopyOfContextMap()`.