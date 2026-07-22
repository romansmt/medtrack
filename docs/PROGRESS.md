# Implementation progress

Detailed writeup of what each task in [TASKS.md](TASKS.md) actually built, and the reasoning behind non-obvious decisions. See [DEVELOPMENT.md](DEVELOPMENT.md#8-implementation-status) for the quick-glance status table.

## TASK-00 · Scaffold

Done (predates detailed tracking here).

* Spring Boot 3.3.5 project on Java 21, with the four packages (`domain`, `application`, `infrastructure`, `api`) stubbed out via `package-info.java` files.
* `docker-compose.yml` — Postgres 16 (alpine), published on host port `5434` instead of the default `5432` (this machine already runs two native Postgres services on `5432`/`5433` — see DEVELOPMENT.md's ports section).
* Flyway wired up (`flyway-core`, `flyway-database-postgresql`), pointed at `classpath:db/migration` — empty at this point, filled in starting TASK-02.
* `.github/workflows/ci.yml` — GitHub Actions: JDK 21 (Temurin), `mvn -B verify` on push/PR to `main`.
* `MedtrackApplicationTests` — Testcontainers-backed (`@ServiceConnection` + `PostgreSQLContainer`), checks `/actuator/health` returns 200.

DoD (`mvn spring-boot:run` boots, `docker-compose up` provides the DB, health check responds 200) reconfirmed repeatedly while working later tasks.

## TASK-01 · Domain entities

Done — `BUILD SUCCESS` on `mvn compile`.

* `Patient.java`, `Doctor.java`, `Drug.java`, `Prescription.java` (+ `PrescriptionStatus` enum) in `com.medtrack.domain`.
* Constructors match the exact signatures given in the backlog (`Patient(svnr, name)`, etc.); plain JavaBean-style getters/setters, no Lombok (not a project dependency).
* `Patient.svnr` — `@Column(unique = true, length = 10)`, matching the Austrian SVNR's fixed 10-digit format.
* `Prescription`'s three `@ManyToOne` relations to `Patient`/`Doctor`/`Drug` are unidirectional (no back-reference collections — not asked for), explicitly `fetch = FetchType.LAZY` to avoid implicit eager joins/N+1 queries, and mapped via named `@JoinColumn`s (`patient_id`, `doctor_id`, `drug_id`).
* `status` uses `@Enumerated(EnumType.STRING)` rather than ordinal, so the DB stores readable values (`OPEN`/`REDEEMED`) instead of fragile integers.
* No Bean Validation annotations (`@NotNull` etc.) — `spring-boot-starter-validation` isn't a project dependency, so nullability is expressed purely via JPA's `@Column(nullable = false)`.

**Architectural note:** these entities are JPA-annotated directly in `domain`, rather than split into a separate persistence-model + mapper layer as the original `MedTrack_Architecture.doc` describes (it puts JPA entities under `infrastructure/persistence`, keeping `domain` framework-free). This was a deliberate, discussed tradeoff — keep it simple now, revisit later — tracked as the deferred `TASK-11`, not an oversight.

## TASK-02 · Flyway `V1__init_schema.sql`

Done — verified by actually applying the migration to a real, fresh Postgres (via the project's existing Testcontainers-backed test), not just reviewing the SQL.

* Four tables, created in dependency order (`patient`, `doctor`, `drug`, then `prescription` last, since it has FKs into all three).
* `patient.svnr` gets a `UNIQUE` constraint (`uq_patient_svnr`) — Postgres backs every `UNIQUE` constraint with an index automatically, so this single constraint satisfies both "one SVNR per patient" and the task's "index on patient.svnr" requirement. No separate, redundant `CREATE INDEX`.
* Column lengths/types mirror the JPA mapping exactly, including the *implicit* JPA default of `VARCHAR(255)` for `String` fields that don't specify a `length`.
* All constraints (`PRIMARY KEY`, `UNIQUE`, `FOREIGN KEY`) are explicitly named (`pk_*`, `uq_*`, `fk_*`) rather than left to Postgres's auto-generated names, for readability in future migrations.
* Deliberately skipped: a `CHECK` constraint on `status`, and indexes on `prescription`'s FK columns — neither was asked for in the DoD.

Verified via `mvn test`: Flyway logged `Successfully applied 1 migration to schema "public", now at version v1` against a fresh Testcontainers Postgres, and Hibernate's `ddl-auto: validate` passed — meaning the schema matches the TASK-01 entities exactly.

## TASK-03 · Flyway `V2__seed_demo_data.sql`

Done — verified against the real local `docker-compose` Postgres (port `5434`), not just Testcontainers, since the DoD specifically calls out `docker-compose up`.

* 4 patients, 3 doctors, 6 drugs, 9 prescriptions (5 `OPEN` / 4 `REDEEMED`) — within the backlog's specified ranges (3–5 / 2–3 / 5–7 / 8–10).
* Prescription rows use subqueries on natural keys (`(SELECT id FROM patient WHERE svnr = '...')`, matched on doctor/drug `name`) to resolve foreign keys, instead of hardcoded numeric IDs — more robust and self-documenting than assuming a specific identity-sequence starting value.
* Drug names/active substances are real, common, public pharmacological terms (Aspirin, Ibuprofen, Metformin, etc.) — not sensitive data, and distinct from the "fake data only" constraint that applies specifically to patient identities/SVNRs.
* Verified two ways: (1) `mvn test` (Testcontainers) proves the migration applies without constraint violations; (2) actually booted the app against the real local Postgres (`docker-compose up -d`, then `mvn spring-boot:run -Dspring-boot.run.fork=false`) and queried it directly via a native `psql` client (found at `C:\Program Files\PostgreSQL\17\bin\psql.exe`, connecting on port `5434`) to confirm the exact row counts, status split, and a full joined view of patient/doctor/drug/prescription data.

## TASK-04 · Spring Data repositories

Done — new `@DataJpaTest` passes alongside the existing suite.

* `PatientRepository.java` — plain `JpaRepository<Patient, Long>`, no extra methods (none specified in the DoD; `findBySvnr` was added later in TASK-06 once it was actually needed).
* `PrescriptionRepository.java` — adds `findByPatientSvnr(String svnr)`, a Spring Data derived query method that traverses `Prescription.patient.svnr`.
* Both live in a new `com.medtrack.infrastructure.persistence` package — the `infrastructure` package-info already named "persistence" as one of its concerns, and this also matches the original architecture doc's layout for repositories.
* `PrescriptionRepositoryTest.java` — `@DataJpaTest` + `@Testcontainers`, mirroring the existing test's `@ServiceConnection` pattern. Since this project has no embedded-DB dependency (H2/HSQL), added `@AutoConfigureTestDatabase(replace = Replace.NONE)` so it uses the real Testcontainers Postgres instead of `@DataJpaTest`'s default (which would otherwise fail looking for an embedded driver). Two tests: a patient's own prescriptions are all returned, and an unknown SVNR returns empty — both meaningful since the seeded demo data (9 prescriptions across 4 other patients) coexists in the same schema, so a broken `WHERE` clause would have been caught.

## TASK-05 · `EHealthCardPort`

Done — `BUILD SUCCESS` on compile.

* `EHealthCardSession.java` — a minimal record (`svnr`, `establishedAt`), plain domain value type, no JPA (nothing in the backlog persists sessions to the DB, so no tension with the domain-purity question this time).
* `EHealthCardPort.java` — the interface itself, one method: `EHealthCardSession lookupBySvnr(String svnr)`, in a new `com.medtrack.application.port` package as the DoD specifies.
* Deliberately left out: `PatientNotFoundException` and any implementation — those belong to TASK-06 (`MockEHealthCardAdapter`), not this task. No test either, since the DoD only asks for the contract to exist, and there's nothing to implementation-test yet.

## TASK-06 · `MockEHealthCardAdapter`

Done — all 6 tests pass (`Tests run: 6, Failures: 0, Errors: 0`), `BUILD SUCCESS`.

* `PatientRepository.findBySvnr(String svnr)` — added an `Optional<Patient>` lookup method, the piece deliberately deferred from TASK-04 since nothing needed it until now.
* `PatientNotFoundException.java` — unchecked (`RuntimeException`), placed in `domain` rather than `infrastructure`, since the future REST layer (TASK-08) will need to catch it, and per the hexagonal dependency direction outer layers (`api`) should depend on `domain`/`application`, not reach into `infrastructure` directly.
* `MockEHealthCardAdapter.java` — `@Component` implementing `EHealthCardPort`, in a new `com.medtrack.infrastructure.ehealthcard` package (kept separate from `infrastructure.persistence`, matching the architecture doc's structure, since this is a mock-integration adapter that happens to use a repository, not a repository itself). Looks the patient up by SVNR, throws `PatientNotFoundException` if absent, otherwise returns an `EHealthCardSession`.
* `MockEHealthCardAdapterTest.java` — a genuine Mockito unit test (`@ExtendWith(MockitoExtension.class)`, mocked `PatientRepository`), not a database-backed one — matching the DoD's specific wording ("unit test", unlike TASK-04's `@DataJpaTest`). Ran in about 1.5 seconds with no Docker involved at all, confirming it's truly isolated. Covers both the found and not-found branches.
