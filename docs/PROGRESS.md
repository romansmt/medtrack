# Implementation progress

Detailed writeup of what each task in [TASKS.md](TASKS.md) actually built, and the reasoning behind non-obvious decisions.

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

## TASK-07 · `PrescriptionService.getPrescriptionsForPatient`

Done — all 8 tests pass (`Tests run: 8, Failures: 0, Errors: 0`), `BUILD SUCCESS`.

* `PrescriptionResponse.java` — a record (`drug`, `dosage`, `doctorName`, `issuedDate`, `status`), matching TASK-08's exact field list. Placed in a new `com.medtrack.application.dto` package rather than `api.dto` (as the architecture doc's structure would suggest) — TASK-07 explicitly says the *service* "maps to DTO," so the DTO has to live somewhere `application` can depend on without `application` reaching into `api`, which would create a circular package dependency once TASK-08's controller needs to depend on `application` too.
* `PrescriptionService.java` — new `application.service` package. Calls `EHealthCardPort.lookupBySvnr` first (validates the SVNR, discarding the returned session — same "call for its throwing side effect" pattern as `MockEHealthCardAdapter`), then `PrescriptionRepository.findByPatientSvnr`, then maps each `Prescription` to a `PrescriptionResponse`.
* `@Transactional(readOnly = true)` on the service method — not optional here: `Prescription.getDrug()`/`.getDoctor()` are `@ManyToOne(fetch = LAZY)`, and `application.yml` sets `open-in-view: false`, so without an explicit transaction spanning the whole method, accessing those lazy associations during DTO mapping (after the repository call's own transaction already closed) would throw `LazyInitializationException`.
* `PrescriptionServiceTest.java` — Mockito unit test, mocking both `EHealthCardPort` and `PrescriptionRepository`. Two cases: a valid SVNR maps correctly to a `PrescriptionResponse`; an invalid SVNR (port throws `PatientNotFoundException`) propagates the exception *and* never even calls `PrescriptionRepository.findByPatientSvnr` (verified with `verify(..., never())`) — confirming the "validate before fetch" ordering actually holds, not just that some exception eventually surfaces.

## TASK-08 · REST controller

Done — verified against a live, packaged instance of the app (not just `mvn test`), matching the DoD's literal "curl returns correct JSON, and 404 for a bogus SVNR" wording.

* `PrescriptionController.java` (`api.controller`) — `GET /api/oegk/{svnr}/prescriptions`, delegates straight to `PrescriptionService.getPrescriptionsForPatient`. Swagger annotations: `@Tag` on the class, `@Operation` + `@ApiResponses` (200 and 404) on the method.
* `ApiExceptionHandler.java` (`api`, top level) — `@RestControllerAdvice` translating `PatientNotFoundException` into a bare 404. Kept as a global advice rather than a controller-local `@ExceptionHandler`, since `PatientNotFoundException` is a domain-wide concept any future controller could throw, not something specific to this one endpoint.
* Added `springdoc-openapi-starter-webmvc-ui` as a new dependency — nothing existing covers OpenAPI/Swagger. **Version note:** the latest release (2.8.6) broke the entire test suite at context startup — its Swagger UI module references `LiteWebJarsResourceResolver`, a Spring Framework class that doesn't exist in the Spring Framework version Spring Boot 3.3.5 ships with (`NoClassDefFoundError`). Dropped to `2.6.0`, which works cleanly with 3.3.5. If Spring Boot ever gets upgraded past 3.3.x here, this version pin is worth revisiting.
* Verified end-to-end: packaged the jar, ran it against the real local Postgres (port `5434`, already seeded from TASK-02/03) on a scratch port (`8082` — port `8080` was again occupied by a leftover process, sidestepped rather than touched, same approach as TASK-03), then:
  * `GET /api/oegk/1234010190/prescriptions` → `200`, returned exactly Anna Gruber's 2 prescriptions from the seed data.
  * `GET /api/oegk/0000000000/prescriptions` → `404`.
  * `GET /v3/api-docs` → `200`, with the generated spec correctly showing the tag, summary, and both documented response codes.

## TASK-09 · Testcontainers integration test

Done — all 10 tests pass (`Tests run: 10, Failures: 0, Errors: 0`), `BUILD SUCCESS`.

* `PrescriptionControllerIntegrationTest.java` (`api`, test source) — a genuine end-to-end test: real Postgres via Testcontainers (`@ServiceConnection`, same pattern as `MedtrackApplicationTests`), real Flyway migration (V1 schema + V2 seed data, both applied automatically on context startup), a real embedded Tomcat on a random port (`@SpringBootTest(webEnvironment = RANDOM_PORT)`), and real HTTP GET requests via `TestRestTemplate`. This is the automated version of the manual curl verification done for TASK-08.
* Two cases: a known SVNR (`1234010190`, Anna Gruber) returns 200 with her exact 2 seeded prescriptions — asserted by deserializing straight into `PrescriptionResponse[]` and comparing structurally (`containsExactlyInAnyOrder`), reusing the DTO record's own equality rather than fragile substring-matching on raw JSON; an unknown SVNR returns 404, confirming the whole `PatientNotFoundException` → `ApiExceptionHandler` → HTTP chain holds end-to-end, not just in the isolated unit tests from TASK-06/07.
* Kept as its own test class rather than folded into `MedtrackApplicationTests` — that one stays a minimal smoke test for `/actuator/health`, matching its original TASK-00 scope, since TASK-09 is its own distinct backlog item testing the ÖGK feature specifically.

---

# Phase 2 — ApoScout + ApoApp feature build

Extends the ÖGK slice with the pharmacy-locator, medication-availability, pricing, and adherence-tracking features from the architecture doc, combining the functionality/UX of two real Austrian pharmacy apps (ApoScout, ApoApp). Scope decisions for this phase: no real login yet (a demo patient-selector stands in), desktop/laptop-first frontend (not phone-styled), reminders computed live with no persisted `Notification`/scheduler. See [TASKS.md](TASKS.md) for the full Phase 2 backlog. No more Dev A/Dev B split — solo, sequential.

## TASK-12 · Pharmacy domain entities

Done — compiles clean; schema match confirmed once TASK-13/14's migrations were verified (see below).

* `Pharmacy`, `PharmacyOpeningHours`, `PharmacyInventory` in `com.medtrack.domain`, plus a small `OpeningHoursKind` enum (`REGULAR` / `ON_CALL`) and a `Coordinates` record (`latitude`, `longitude`) shared with TASK-15's `GeoPort`.
* Same style as the existing entities: JPA-annotated directly in `domain` (no separate persistence layer — consistent with the TASK-11-deferred simplification), constructor + explicit getters/setters, no Lombok.
* `PharmacyOpeningHours` models both a pharmacy's regular weekly hours and its on-call/emergency-duty (Bereitschaftsdienst) hours in one table, distinguished by `kind` — a shift spanning midnight is stored as two same-day rows rather than one cross-midnight range, matching how the reference app itself displays it.
* `PharmacyInventory.price` is `BigDecimal` (`precision = 10, scale = 2`), unlike most of the codebase's plain types — money arithmetic on `double` is a real correctness risk, worth the one exception.
* `PharmacyInventory.lastUpdated` (`Instant`) exists specifically so TASK-16's `StockRefreshJob` has something to touch, simulating a live per-pharmacy stock feed.

## TASK-13 · Flyway pharmacy schema + seed data (`V3`, `V5`)

Done — verified via `mvn test`: all 5 migrations (`V1`–`V5`) apply cleanly against a fresh Testcontainers Postgres, and Hibernate's `ddl-auto: validate` passes against the new entities.

* `V3__pharmacy_schema.sql` — `pharmacy`, `pharmacy_opening_hours`, `pharmacy_inventory` tables, following `V1`'s conventions (snake_case columns, named `pk_`/`fk_` constraints, FK-dependency ordering). Indexes on both FK columns of `pharmacy_inventory` and on `pharmacy_opening_hours.pharmacy_id`, since both are looked up by pharmacy/drug id in the hot paths (`PharmacyService`, `AvailabilityService`).
* `V5__pharmacy_seed_data.sql` (not `V4` — see the numbering note below) — 8 fictional Vienna-area pharmacies (original invented names/addresses, not the real pharmacies visible in the ApoScout reference screenshots), regular Mon–Fri/Sat hours for all 8, plus a 7-pharmacy on-call rotation covering every overnight/Sunday gap so "who's on duty right now" always has an answer regardless of when the app is run. 4 "full service" pharmacies stock all 12 catalog drugs, 4 smaller ones stock a subset; a handful of rows are deliberately `in_stock = FALSE` (carried but currently out) rather than omitted (not carried at all) — both states are meaningful for the availability-check feature.
* **Numbering note:** `V5` seeds pharmacy inventory by joining against `drug.pzn`, which only exists once `V4` (TASK-14's drug catalog extension) has run — so the seed data had to be sequenced *after* the catalog extension, not before it as originally planned. Renumbered to `V3` = schema, `V4` = drug extension (TASK-14), `V5` = pharmacy seed, rather than keeping TASK-13's two pieces adjacent.
* **Bug caught and fixed during verification:** the first inventory `INSERT` block (`Apotheke Zum Goldenen Loewen`) referenced the seed price column as `d.base_price` instead of `base.base_price` — a copy-paste-forward typo from an early draft that the other 7 blocks didn't repeat. Failed with Postgres error `42703 column d.base_price does not exist`; caught by `mvn test` (Testcontainers, random port), which is what proved a subsequent `mvn spring-boot:run` failure against the real docker-compose DB was hitting the *same* bug rather than a port collision, even though the symptom briefly looked identical to one.

## TASK-14 · Drug catalog extension (`V4`)

Done — same verification as TASK-13 (part of the same `mvn test` run).

* Extended `Drug` (previously just `name`/`activeSubstance`) with `form`, `packSize`, `manufacturer`, `prescriptionRequired`, `pzn` (fake Pharmazentralnummer, unique), and `packageLeafletText` — needed because real medication-availability search results show multiple catalog rows per drug name (different strengths/pack sizes/manufacturers), and `PricingService` (TASK-20) needs to branch on prescription-required vs. OTC.
* `V4__drug_catalog_extension.sql` — additive `ALTER TABLE` (nullable columns first), backfills the existing 6 seeded drugs with real-shaped values, adds `NOT NULL`/`UNIQUE(pzn)` constraints after backfill, then inserts 6 new catalog rows (a second Paracetamol pack size, an infusion-solution variant, 3 new active substances) for search variety.
* Updated the two existing tests that constructed `Drug` directly (`PrescriptionRepositoryTest`, `PrescriptionServiceTest`) to the new 8-arg constructor rather than keeping a legacy 2-arg overload around.

## TASK-15 · `GeoPort` + `NominatimGeoAdapter` + Haversine helper

Done — compiles clean; the Nominatim call itself was exercised manually (see DEVELOPMENT.md §4.4), not by an automated test, since asserting on a real third-party API's response isn't a meaningful unit/integration test.

* `GeoPort` (`application.port`) — one method, `Optional<Coordinates> geocode(String address)`.
* `NominatimGeoAdapter` (`infrastructure.geo`) — the project's first **real**, non-mocked integration: calls the public Nominatim/OpenStreetMap geocoding API directly via Spring's `RestClient` (no new dependency — already transitively available via `spring-boot-starter-web` since Boot 3.2). Sends an identifying `User-Agent` with no personal data in it (Nominatim's usage policy asks for one), and is only ever called on an explicit "set location" submission from the frontend — never per keystroke, respecting the ~1 req/sec usage policy.
* `DistanceCalculator` (`application.service`) — stateless Haversine implementation, used by `PharmacyService` to compute/sort by distance from a given `Coordinates` origin.
* Browser geolocation (planned for TASK-26+) covers "use my current location" without touching this port at all — `GeoPort` exists specifically for the manual "type an address" path.

## TASK-16 · `PharmacyStockPort` + `PharmacyService` + `StockRefreshJob`

Done — compiles clean, manually verified via the `/api/pharmacies/nearby`, `/on-call`, and `/{id}` endpoints (DEVELOPMENT.md §4.4).

* `PharmacyStockPort`/`MockPharmacyStockAdapter` (`infrastructure.stock`) — thin repository wrapper, same shape as `EHealthCardPort`/`MockEHealthCardAdapter`: even a simple repository-backed lookup gets a port/adapter pair, keeping the "this could be swapped for a real feed later" boundary explicit.
* `PharmacyService` (`application.service`) — `findNearby` (radius filter + distance sort), `findOnCallNow` (filters to pharmacies with an `ON_CALL` opening-hours row matching right now), `getDetail` (pharmacy + full sorted opening-hours list), `geocode` (thin pass-through to `GeoPort`). "Open now"/"on call now" are computed by checking the current day-of-week/time against each pharmacy's `PharmacyOpeningHours` rows — no separate "is open" flag stored anywhere.
* `StockRefreshJob` (`api.scheduler`, `@Scheduled(fixedRate = 60_000)`) — simulates a live per-pharmacy stock feed (the architecture doc's "stock refresh" cross-cutting concern) by jittering ~10% of inventory rows' `in_stock` flag and touching `lastUpdated` every minute, so the frontend's future "last updated" freshness line actually moves during a demo. Added `@EnableScheduling` to `MedtrackApplication` for this.

## TASK-17 · `AvailabilityService`

Done — compiles clean, manually verified via `POST /api/availability/check` (DEVELOPMENT.md §4.4).

* Takes a "selection list" (`List<AvailabilityRequestItem>`, i.e. drug id + quantity pairs) and an origin, and returns one result per *nearby* pharmacy (reuses `PharmacyService.findNearby` as the base set, matching how the reference app lists all nearby pharmacies and overlays availability rather than only showing pharmacies that happen to stock something).
* Per pharmacy: an `allAvailable` flag plus a per-item breakdown (drug name/form/pack size, requested quantity, in-stock, price). A missing inventory row (pharmacy doesn't carry that drug at all) and an `in_stock = false` row (carried but currently out) both resolve to "not available" for the item, but stay distinguishable in the underlying data.

## TASK-18 · REST controllers — Patient/Pharmacy/Drug/Availability

Done — verified against a real running instance (DEVELOPMENT.md §4.4): all new endpoints return the expected shapes, unknown-id lookups return 404.

* `PatientController` (`GET /api/patients`) — new; nothing previously exposed a way to list patients at all. Needed for the frontend's planned "pick a demo patient" selector, the lightweight stand-in for real login for this phase.
* `DrugController` — search (`GET /api/drugs/search?q=`), detail (`GET /api/drugs/{id}`), leaflet (`GET /api/drugs/{id}/leaflet`).
* `PharmacyController` — nearby (`GET /api/pharmacies/nearby`), on-call (`GET /api/pharmacies/on-call`), detail (`GET /api/pharmacies/{id}`), geocode (`GET /api/pharmacies/geocode`).
* `AvailabilityController` — `POST /api/availability/check`.
* Extended `ApiExceptionHandler` with a `NoSuchElementException` → 404 handler (thrown by `PharmacyService`/`DrugService` for unknown ids), alongside the existing `PatientNotFoundException` handler.
* All four follow the existing `PrescriptionController` shape exactly: thin controller delegating to a service, Swagger `@Tag`/`@Operation` annotations, one `@RequestMapping` base path per resource.

**Troubleshooting note worth keeping:** while verifying this phase, `mvn spring-boot:run` failed twice for two *unrelated* reasons that briefly looked like the same problem — first the real `V5` SQL bug above, then (after fixing it) a stale `java` process from ~18 hours earlier still squatting on port 8080. `mvn test` (Testcontainers, always a random port) was the deciding diagnostic both times: it isolates "the code is actually broken" from "something local/environmental is in the way." See DEVELOPMENT.md §7 for the generalized version of this.

## TASK-19 · Testcontainers integration test for the availability flow

Done — all 12 tests pass (`Tests run: 12, Failures: 0, Errors: 0`), `BUILD SUCCESS`.

* `AvailabilityControllerIntegrationTest.java` (`api`, test source) — same shape as `PrescriptionControllerIntegrationTest`: real Postgres via Testcontainers, real Flyway migrations, real embedded Tomcat on a random port, real HTTP via `TestRestTemplate` (`postForEntity` this time, since the endpoint is a POST with a JSON body).
* Two cases: (1) a known drug (Aspirin, id 1) checked from the exact seeded coordinates of "Apotheke Zum Goldenen Loewen" returns all 8 seeded pharmacies within a 10km radius, sorted by distance, with the closest one asserted by name/distance/price/in-stock against the actual `V5` seed values — not just "some response came back"; (2) an unknown drug id returns 404.
* **Bug caught and fixed while writing this test:** `AvailabilityService.checkAvailability` looked up requested drugs via `drugRepository.findAllById(...)` but never checked whether every requested id actually came back — an unknown drug id would silently produce a `null` `Drug` that then NPE'd inside `toItemResult`, surfacing as a bare 500 rather than a clean 404. Fixed by comparing the resolved-drug count against the requested-id count upfront and throwing the same `NoSuchElementException` that `PharmacyService`/`DrugService` already use for unknown ids (already wired to 404 via `ApiExceptionHandler` since TASK-18) — no new exception type needed.
