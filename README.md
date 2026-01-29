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
