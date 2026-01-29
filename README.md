# Voting API (Java 21 + Spring Boot + Oracle + RabbitMQ)

CPF is the **only** voter identifier.

## Run dependencies (Oracle + RabbitMQ)
```bash
docker compose up -d
```

## Run app
```bash
./gradlew bootRun
```

Swagger: http://localhost:8080/swagger-ui.html

## Quick cURL
Create agenda:
```bash
curl -X POST http://localhost:8080/api/v1/agendas   -H "Content-Type: application/json"   -d '{"title":"My agenda","description":"..."}'
```

Open session (default 60s):
```bash
curl -X POST http://localhost:8080/api/v1/agendas/1/sessions   -H "Content-Type: application/json"   -d '{"durationSeconds":60}'
```

Vote:
```bash
curl -X POST http://localhost:8080/api/v1/agendas/1/votes   -H "Content-Type: application/json"   -d '{"cpf":"12345678909","choice":"YES"}'
```

Result:
```bash
curl http://localhost:8080/api/v1/agendas/1/result
```

## CPF validation
Uses external service:
- `GET {CPF_VALIDATION_BASE_URL}/users/{cpf}`
- 404 => invalid CPF (API returns 404)
- status != ABLE_TO_VOTE => 422

Override base url:
```bash
export CPF_VALIDATION_BASE_URL=http://localhost:8089
```

## Tests + Coverage
```bash
./gradlew test
```
JaCoCo HTML report:
- `build/reports/jacoco/test/html/index.html`

Coverage gate:
- minimum 80% (config in Gradle)

## Messaging (RabbitMQ)
Publishes events to exchange `voting.events`:
- `session.opened`
- `session.closed`
- `vote.cast`


---

## Interview-ready notes (architecture & trade-offs)

### Architecture
- **Layers**: `api` (controllers/DTOs) → `domain` (services/entities/exceptions) → `infra` (repositories/http/messaging).
- **Database**: Oracle via Spring Data JPA + Flyway migrations.
- **Messaging**: RabbitMQ publisher is optional (`VOTING_MESSAGING_ENABLED=true|false`) to keep the core domain independent.

### SOLID / SRP
To improve maintainability, controllers were split by responsibility:
- `AgendaCommandController`: write operations (create agenda, open session, cast vote)
- `AgendaQueryController`: read operations (get agenda, list, current session, result)

This keeps each controller focused and makes it easier to evolve endpoints independently.

### Modern Java
DTOs were migrated to **Java records**:
- less boilerplate
- immutable request/response contracts
- better signal of intent (data carriers)

### Reduced conditionals
Some domain rules were encapsulated in entities to avoid scattered `if/else`.
Example: `VotingSession#isOpenAt(now)` centralizes session-open logic.

### Observability
The app exposes endpoints via Spring Boot Actuator:
- `/actuator/health`
- `/actuator/metrics`
- `/actuator/prometheus` (Prometheus scraping)

### Testing strategy
- Unit tests for domain services (business rules)
- Web layer tests using `@WebMvcTest` for controllers
- E2E integration test using Testcontainers (Oracle) + WireMock (CPF validation)
