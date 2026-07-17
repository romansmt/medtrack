# Local Development Guide

How to build, run, test, and troubleshoot MedTrack on this machine.

## 1. Prerequisites

| Tool | Version | Notes |
|---|---|---|
| JDK | 21 | Not on system PATH — see [Environment setup](#2-environment-setup) below |
| Maven | 3.9.x | Not installed standalone — bundled with IntelliJ, see below |
| Docker Desktop | any recent | Must be running before `docker compose` or `mvn test` |

## 2. Environment setup

`java` and `mvn` are not on this machine's system PATH. Rather than installing them separately, use the copies already bundled with IntelliJ. At the start of every new PowerShell session where you'll run Maven commands, set these two variables first:

```powershell
$env:JAVA_HOME = "C:\Users\rivay\.jdks\ms-21.0.11"
$mvn = "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.4\plugins\maven\lib\maven3\bin\mvn.cmd"
```

Every Maven command below assumes you've run those two lines first, and is invoked as `& $mvn <goal>`.

**Alternative:** open the project in IntelliJ and use the Maven tool window (right side panel) or the green run arrow on `MedtrackApplication` — this sidesteps the PATH issue entirely and is the easiest option for day-to-day work. The terminal commands below are for quick, scriptable checks.

**Permanent fix (optional):** if you'd rather not set those variables every session, add `C:\Users\rivay\.jdks\ms-21.0.11\bin` and `C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.4\plugins\maven\lib\maven3\bin` to your user PATH via *Settings → System → About → Advanced system settings → Environment Variables*. Do this yourself; it's a system settings change.

## 3. Quick reference — common commands

| Task | Command |
|---|---|
| Compile | `& $mvn compile` |
| Run tests | `& $mvn test` |
| Full build (compile + test + package) | `& $mvn verify` |
| Start local Postgres | `docker compose up -d` |
| Stop local Postgres | `docker compose down` |
| Stop and wipe DB data | `docker compose down -v` |
| Run the app | `& $mvn spring-boot:run` |
| Check container status | `docker compose ps` |
| Tail container logs | `docker logs medtrack-postgres` |

## 4. Verifying the project works

### 4.1 Compile

```powershell
& $mvn compile
```
Expect: `BUILD SUCCESS`.

### 4.2 Run the automated test suite (the reliable check)

```powershell
& $mvn test
```
Expect: `Tests run: 2, Failures: 0, Errors: 0` and `BUILD SUCCESS`.

This test uses **Testcontainers**: it starts its own throwaway Postgres container, runs Flyway migrations against it, boots the full Spring context, and checks that `/actuator/health` returns `200`. A pass here is strong evidence the whole stack works, independent of anything below.

### 4.3 Run the app for real, end-to-end

```powershell
docker compose up -d
docker compose ps          # confirm medtrack-postgres shows "healthy"
& $mvn spring-boot:run
```

In a **second** terminal, once it's finished starting:

```powershell
Invoke-WebRequest http://localhost:8080/actuator/health
```

Expect: status `200`, body `{"status":"UP"}`. Stop the app with `Ctrl+C` in the first window, then `docker compose down` when you're done.

## 5. Project structure

```
src/main/java/com/medtrack/
  domain/          entities — pure Java, no framework dependencies
  application/     services and ports (interfaces)
  infrastructure/  adapters implementing those ports (persistence, mocks)
  api/             REST controllers, DTOs, config

src/main/resources/
  application.yml
  db/migration/    Flyway SQL migrations (V1__..., V2__..., empty for now)

src/test/java/com/medtrack/
  MedtrackApplicationTests.java   Testcontainers-backed integration test
```

## 6. Ports used by this project

| Port | Used for |
|---|---|
| 5434 | Local Postgres (docker-compose) — **not** the Postgres default 5432 |
| 8080 | The Spring Boot app |

**Why 5434 and not 5432:** this machine already runs two native PostgreSQL Windows services unrelated to this project — PostgreSQL 13 on port 5432, PostgreSQL 17 on port 5433 (a second PostgreSQL version installed via the EDB installer defaults to the next free port). If MedTrack's container tried to use either of those ports, connections would go to the wrong database and fail with a *misleading* `password authentication failed` error instead of a clear "connection refused." Testcontainers-based tests were never affected, since they always pick a random free port automatically.

## 7. Troubleshooting

**`mvn : term not recognized`**
`mvn` isn't on PATH. Re-run the two setup lines from [§2](#2-environment-setup) in your current terminal — they don't persist across sessions unless you did the permanent PATH fix.

**`password authentication failed for user "medtrack"` when running `mvn spring-boot:run`, but `mvn test` passes**
This means the app is very likely talking to the *wrong* Postgres server — almost always a port collision with something else already listening on that port. Check what's actually bound to it:

```powershell
netstat -ano | Select-String ":5434 "
Get-Process -Id <PID from the output above>
```

Also check for other native Postgres installs:
```powershell
Get-Service | Where-Object { $_.Name -like "*postgres*" }
```
If something unexpected owns the port, change `docker-compose.yml`'s port mapping and `application.yml`'s `DB_PORT` default to a free port, then retry.

**`docker compose up` fails, or containers won't start**
Check Docker Desktop is actually running:
```powershell
docker info
```
If this errors out, open Docker Desktop and wait for it to finish starting (whale icon in the tray, no longer showing "starting").

**Health check times out / connection refused on `localhost:8080`**
The app likely failed to start — check its console output for a stack trace (it fails fast on startup if it can't reach the database, so this is almost always the same DB-connection issue above, not a separate app bug).

**Container shows `unhealthy` in `docker compose ps`**
```powershell
docker logs medtrack-postgres
```
Read the tail of the log for the actual Postgres startup error.
