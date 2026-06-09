# Insurance Sample

**Spring Boot 3.3 · Java 21 · MySQL · Hexagonal Architecture**

APAC Commercial Insurance Platform — Policy BFF Service. Contract-first, clean architecture, with optional Kafka and Caffeine caching.

---

## Quick Start (H2, no Docker needed)

```bash
mvn spring-boot:run
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console  (JDBC URL: `jdbc:h2:mem:my_sql`)
- Health: http://localhost:8080/actuator/health

Database: H2 in MySQL-compatibility mode, auto-seeded with **220+ APAC policy records** via Flyway.

---

## Full Stack (MySQL + Kafka)

```bash
docker compose up --build
```

| Service | Port |
|---|---|
| Insurance Sample API | 8080 |
| MySQL | 3306 |
| Kafka | 9092 |
| Kafka UI | 8090 |

---

## API Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/policies` | Paginated list with filters & search |
| GET | `/api/v1/policies/{id}` | Single policy by UUID |
| PATCH | `/api/v1/policies/flag` | Bulk flag for review |
| GET | `/api/v1/policies/summary` | Aggregated statistics |

### Query parameters for `GET /api/v1/policies`

| Param | Type | Example |
|---|---|---|
| `page` | int | `0` |
| `size` | int (1-100) | `20` |
| `sort` | string | `premiumAmount,desc` |
| `status` | enum | `Active`, `Expired`, `Pending`, `Cancelled` |
| `lineOfBusiness` | enum | `Property`, `Casualty`, `A&H`, `Marine` |
| `region` | string | `Singapore` |
| `effectiveDateFrom` | date | `2024-01-01` |
| `effectiveDateTo` | date | `2024-12-31` |
| `search` | string | `DBS` (searches policyNumber, policyholderName, underwriter) |

---

## MySQL Configuration

Set these environment variables to connect to a real MySQL instance:

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/my_sql?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=secret
SPRING_DATASOURCE_DRIVER=com.mysql.cj.jdbc.Driver
JPA_DIALECT=org.hibernate.dialect.MySQLDialect
```

The schema name is `my_sql` and the table is `policies`. Flyway manages all migrations.

---

## Architecture

```
api/              ← Controllers, DTOs, MapStruct mappers
application/      ← Use case interfaces (ports), PolicyService
  port/in/        ← PolicyUseCase, PolicyFilter
  port/out/       ← PolicyRepository, PolicyEventPublisher
domain/           ← Policy, PolicyStatus, LineOfBusiness, events (zero framework deps)
infrastructure/   ← JPA adapter, Kafka producer/consumer, Caffeine cache, exception handler
```

Dependencies point strictly inward. The domain layer has zero Spring or JPA imports.

---

## Caching (Caffeine)

| Cache | TTL | Max entries | Evicted on |
|---|---|---|---|
| `policy-list` | 60s | 200 | `PATCH /flag` |
| `policy-detail` | 300s | 500 | `PATCH /flag` |
| `policy-summary` | 120s | 1 | `PATCH /flag` |

---

## Kafka (optional)

Disabled by default. Enable with `APP_KAFKA_ENABLED=true`.

| Topic | Direction |
|---|---|
| `policy.flagged` | Producer — fires on each flag operation |
| `policy.status-changed` | Consumer — updates policy status; idempotent |

---

## Running Tests

```bash
mvn test
```

Test classes:

| Class | Type |
|---|---|
| `PolicyDomainTest` | Pure domain unit tests |
| `PolicyServiceTest` | Application service with Mockito |
| `PolicyControllerTest` | MockMvc integration (Spring context, H2) |
| `PolicyPersistenceAdapterTest` | Persistence integration (H2 + Flyway) |

---

## Observability

| Endpoint | Description |
|---|---|
| `/actuator/health` | Liveness + DB |
| `/actuator/metrics` | JVM & HTTP metrics |
| `/actuator/prometheus` | Prometheus scrape |
| `/actuator/caches` | Cache stats |
