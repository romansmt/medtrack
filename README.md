# medtrack

MedTrack - Austrian health system simulator (study/portfolio project).

- [docs/TASKS.md](docs/TASKS.md) — current backlog
- [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) — local setup, running, testing, and troubleshooting

## Quick start

```
docker-compose up -d
mvn spring-boot:run
```

Health check: `GET http://localhost:8080/actuator/health` should return `200`.

```
mvn verify
```

runs the test suite (uses Testcontainers, so `docker-compose up` isn't required just to test).

If `mvn`/`java` aren't on your PATH, or you hit a database connection error, see [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md).
