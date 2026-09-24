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

## TASK-20 · `PricingService` + `ComparisonService`

Done — all 15 tests pass at this point (`Tests run: 15`), `BUILD SUCCESS`.

* `PricingService` (`application.service`) — one method, `getPrice(Drug, PharmacyInventory)`: `null` if the pharmacy doesn't carry the drug at all (no inventory row), a fixed `REZEPTGEBUEHR` constant (`7.55`, explicitly labeled illustrative/fictional, not real current Austrian pricing) if the drug is `prescriptionRequired`, otherwise the pharmacy's own `PharmacyInventory.price`. Deliberately doesn't check whether the *patient* actually holds a valid prescription — that's the separate ÖGK prescription slice; this phase only models the fixed-fee-vs-OTC pricing shape the architecture doc asks for.
* **Consistency fix while wiring this in:** `AvailabilityService` was reading `inventory.getPrice()` directly, which would have shown the raw OTC-style price for prescription-required drugs too, disagreeing with whatever `ComparisonService` shows for the same drug. Refactored `AvailabilityService.toItemResult` to go through `PricingService.getPrice(...)` as well, so there's exactly one place that decides "what price to display" for any drug, used everywhere pricing shows up.
* `ComparisonService` (`application.service`) — `compare(drugId, origin, radiusKm)`: reuses `PharmacyService.findNearby` as the base set (same pattern as `AvailabilityService`), attaches each nearby pharmacy's price via `PricingService`, drops pharmacies that don't carry the drug at all (`price == null`), sorts the rest ascending by price. A pharmacy that carries the drug but is currently out of stock still appears (with `inStock = false`) rather than being hidden, matching `AvailabilityService`'s "carries it vs doesn't carry it" distinction.
* `PricingServiceTest.java` — plain JUnit, no mocks (pure function, no framework dependency): Rx drug → flat fee, OTC drug → inventory price, no inventory → `null`.
* **Why no mocked `ComparisonServiceTest`:** `ComparisonService` groups pharmacy inventory by `Pharmacy.getId()`, which is `@GeneratedValue`-assigned and stays `null` on a plain `new Pharmacy(...)` never persisted through JPA — the same reason `AvailabilityService` (TASK-17) never got an isolated mocked unit test either. Verified via a real Testcontainers integration test instead (below), where ids are genuinely assigned.

## TASK-21 · Price/compare REST endpoint

Done — 17/17 tests pass, `BUILD SUCCESS`; also closes a pre-existing gap where `DrugController` had no automated test at all (search/detail/leaflet were only manually curled after TASK-18).

* `GET /api/drugs/{id}/compare?lat=&lng=&radiusKm=` on the existing `DrugController` (default radius `5.0`, matching `PharmacyController`/`AvailabilityController`), returning `List<PriceComparisonEntry>` (`pharmacy`, `price`, `inStock`) — no new controller needed.
* `DrugControllerIntegrationTest.java` (new — `api`, test source, Testcontainers pattern): compares Aspirin (drug id 1, OTC, stocked by all 8 seeded pharmacies) across a 10km radius from central Vienna. Asserts the cheapest (`VitaNova-Apotheke`, `4.30`) and priciest (`Apotheke Zur Alten Muehle`, `4.75`) entries by name and exact price against the real `V5` seed values, plus a full ascending-order sweep across all 8 results — not just spot-checking the ends. A second test confirms an unknown drug id returns 404.

## TASK-22 · `FavoritePharmacy`/`FavoriteDrug`/`Reservation` entities + migration

Done — `mvn test`: `V6` applies cleanly, Hibernate schema validation passes, all 17 existing tests still pass, `BUILD SUCCESS`.

* `FavoritePharmacy` (patient + pharmacy + `createdAt`) and `FavoriteDrug` (patient + drug + `createdAt`) — simple join entities, each with a `UNIQUE(patient_id, *_id)` constraint so a patient can't favorite the same pharmacy/drug twice. No seed data — unlike the pharmacy/drug catalog (which has to exist for search/availability to work at all), favorites are inherently patient-generated, so starting empty and creating them through the UI is more representative than faking some in.
* `Reservation` (patient + pharmacy + drug + quantity + `status` + `requestedAt`), with a small `ReservationStatus` enum (`REQUESTED`, `CANCELLED`) — enough states for TASK-23's `ReservationService` to support both "make a reservation" and "cancel it" without modeling a full fulfillment workflow (no pharmacy-side confirm/reject step - out of scope, matches the "simple reservation feature, no real fulfillment loop" plan decision).
* `V6__favorites_and_reservations_schema.sql` — three tables, named `pk_`/`fk_`/`uq_` constraints as usual, indexes on each table's `patient_id` (the expected lookup path: "my favorites", "my reservations").
* **Numbering note:** originally planned as `V8` (with `V6`/`V7` reserved for the medication-schedule tables), but since Phase 2C is being built before Phase 2D, `V6` was the actual next free version — same situation as the `V3`–`V5` reordering back in TASK-13, resolved the same way (use the real next number, note it here rather than leaving a gap).

## TASK-23 · Favorite + reservation logic and endpoints

Done — 28/28 tests pass, `BUILD SUCCESS`.

* `FavoriteService` (`application.service`) — add/remove/list for both favorite pharmacies and favorite drugs. `add*` is idempotent (checks `existsByPatientIdAnd*Id` first and no-ops rather than hitting the `UNIQUE` constraint and raising a `DataIntegrityViolationException`); `remove*` is a plain idempotent delete (removing something already-not-favorited is a no-op, not an error), matching normal REST DELETE semantics. `list*` resolves a patient's favorite ids, then delegates to `PharmacyService.findByIds`/`DrugService.getByIds` (both new — small additions reusing each service's existing private response-mapping logic) so a "favorites" list returns exactly the same `PharmacyResponse`/`DrugResponse` shape as a normal search/nearby result, for frontend reuse.
* `ReservationService` — `createReservation` validates patient/pharmacy/drug all exist, then rejects the request with a new `ReservationNotSupportedException` (→ 400, not 404 - a real, well-formed request being refused for a business reason, not a missing resource) if `Pharmacy.reservationSupported` is false, matching the real reference app's per-pharmacy "reservation not offered" behavior. `cancelReservation` sets `status = CANCELLED` rather than deleting the row (keeps reservation history); a reservation id that exists but belongs to a *different* patient is deliberately treated identically to a nonexistent one (`NoSuchElementException` → 404) rather than 403 — doesn't reveal that the id belongs to someone else.
* New endpoints: `FavoriteController` under `/api/patients/{svnr}/favorite-pharmacies` and `/favorite-drugs` (GET list, POST add, DELETE remove - `204 No Content` on the write ones); `ReservationController` under `/api/patients/{svnr}/reservations` (GET list, POST create, DELETE `/{id}` to cancel). Nested under `/api/patients/{svnr}/...` rather than under `PharmacyController`/`DrugController` as originally sketched in the plan - favorites/reservations are fundamentally the *patient's* data, and this matches the existing `/api/oegk/{svnr}/...` precedent of patient-owned resources living under a patient-scoped path.
* `FavoritePharmacyRepository`/`FavoriteDrugRepository`/`ReservationRepository` (new) - straightforward Spring Data derived queries (`findByPatientId`, `existsByPatientIdAndPharmacyId`, `deleteByPatientIdAndPharmacyId`, etc.).
* `ApiExceptionHandler` gained a `ReservationNotSupportedException` → 400 handler, with a body message (the other handlers return a bare body-less status) - a 400 caused by a business rule benefits from saying why, unlike a 404.
* `FavoriteServiceTest`/`ReservationServiceTest` (Mockito unit tests) - given `ComparisonService`'s earlier lesson about grouping/keying by real, JPA-only-assigned ids, these mock the *domain objects themselves* (`@Mock Patient`, `@Mock Pharmacy`, `@Mock Drug`) rather than constructing them with `new`, so each mock's `getId()` can be stubbed to a specific value with no risk of colliding-null-key collisions. Covers: idempotent add/no-op, an actual add's `save` call, not-found propagation without querying further, favorites-list id resolution, the reservation-not-supported rejection, and the cross-patient cancel-ownership check.

## Phase 2D — Medication plan / adherence

## TASK-24 · `MedicationSchedule`/`MedicationScheduleTime`/`MedicationIntakeLog` entities + migrations

Done — `mvn test`: `V7`/`V8` apply cleanly, Hibernate schema validation passes, all 28 existing tests still pass, `BUILD SUCCESS`.

* `MedicationSchedule` (patient + drug + `doseText` + `startDate`/`endDate` + `active`) — one row per "patient takes this drug on this general plan"; `endDate` nullable means "ongoing, no planned stop"; `active` allows pausing/stopping a schedule without losing its intake history (soft state, not a delete).
* `MedicationScheduleTime` — 1-to-many child of a schedule, one row per time-of-day it's taken. Modeled as a separate child table (not a CSV/array column on the schedule itself) specifically so a twice-(or more-)daily schedule is just multiple rows, matching how the rest of this codebase models 1-to-many relationships elsewhere (e.g. `PharmacyOpeningHours`).
* `MedicationIntakeLog` — deliberately **only created at the moment a patient confirms or skips a dose**, keyed by `(scheduleTime, scheduledDate)` with a `UNIQUE` constraint. A scheduled dose with no row yet is implicitly still-pending — there is no "PENDING" status ever persisted, and nothing pre-generates today's expected rows. This is what makes the earlier "reminders computed live, no scheduler" scope decision actually work end-to-end: TASK-25's "due today" query will be a live comparison of each active schedule's times against today's date, not a table scan of pre-seeded pending rows.
* `V7__medication_schedule_schema.sql` (schema) + `V8__medication_schedule_seed.sql` (demo data: Anna Gruber gets a once-daily Aspirin schedule, Max Bauer a **twice-daily** Metformin schedule specifically to exercise the multi-time-per-schedule case, Lena Hofer a once-daily Omeprazole schedule; Paul Wagner deliberately gets none, so there's a real empty state to build against). Anna Gruber's schedule also gets 3 days of seeded intake history (taken/skipped/taken) with today itself left with no row, so the frontend has real "due today" and "past history" data to show immediately without requiring manual confirms first.
* **Numbering note:** as planned this time - `V7`/`V8` were reserved for this phase back when TASK-13/TASK-22 got renumbered ahead of it, so no reordering surprise here.

## TASK-25 · `MedicationScheduleService` + `MedicationScheduleController`

Done — 33/33 tests pass, `BUILD SUCCESS`.

* `MedicationScheduleService` (`application.service`): `listSchedules` (all of a patient's schedules, active or not, each with its sorted list of times), `createSchedule` (new schedule + its times), `deactivateSchedule` (soft-stop via `active = false`, keeps intake history - same "no destructive delete" pattern as `ReservationService.cancelReservation`), `getDueToday` (the live computation this whole phase was designed around), `recordIntake` (confirm/skip today's dose).
* `getDueToday` logic: filter the patient's active schedules to ones whose `startDate`/`endDate` actually cover today, pull every `MedicationScheduleTime` for those schedules, look up any `MedicationIntakeLog` already recorded for `(scheduleTime, today)`, and report `"PENDING"` for any that don't have one yet. Nothing is ever inserted just by *viewing* this endpoint - a dose stays computed-PENDING until the patient actually acts on it.
* `recordIntake` is an upsert, not a plain insert: looks up `(scheduleTimeId, today)` first and updates the existing row's `status`/`confirmedAt` if one's already there, otherwise creates a new one. Lets a patient correct today's confirmation (e.g. accidentally marked skipped) without hitting the `UNIQUE(schedule_time_id, scheduled_date)` constraint from TASK-24. Ownership is checked the same way as `ReservationService.cancelReservation` - a schedule time belonging to a different patient reports `NoSuchElementException` (404), not 403.
* `MedicationScheduleController` under `/api/patients/{svnr}/medication-schedule`: `GET` (list), `POST` (create), `DELETE /{scheduleId}` (deactivate, `204`), `GET /due-today`, `POST /{scheduleTimeId}/intake` (body `{"status": "TAKEN"|"SKIPPED"}`). `ApiExceptionHandler` gained a generic `IllegalArgumentException` → 400 handler so an invalid status string (`IntakeStatus.valueOf` throwing) fails cleanly instead of surfacing as a 500.
* `MedicationScheduleServiceTest` - continues the mock-the-domain-objects-directly approach from `FavoriteServiceTest`/`ReservationServiceTest` (`@Mock MedicationSchedule`, `@Mock MedicationScheduleTime`, etc.) so ids can be stubbed without JPA. Covers: creating a new intake log, upserting an existing one, the cross-patient ownership rejection, PENDING-when-no-log, and a schedule being correctly excluded once its `endDate` has passed.

**Phase 2D complete. All backend phases (2A-2D) are now done** - pharmacy locator, availability, pricing/comparison, favorites/reservations, and the medication plan/adherence tracking are all implemented and tested. Remaining work is entirely the frontend (Phase 2E) and final docs polish (Phase 2F).

## Phase 2E — Frontend

## TASK-26 · Scaffold + CORS + patient context

Done — `npm run build` succeeds (`tsc -b && vite build`), verified live end-to-end against the running backend in a browser at both mobile and desktop viewport sizes.

* `frontend/` — Vite + React 19 + TypeScript, via `npm create vite@latest -- --template react-ts`. Added `react-router-dom`, `@tanstack/react-query`, `leaflet`/`react-leaflet` (for TASK-29's map) as the task specified, nothing beyond that.
* `src/api/client.ts` — thin `fetch` wrapper (`apiGet`/`apiSend`), base URL from `VITE_API_BASE_URL` (`.env.development` defaults it to `http://localhost:8080`). `ApiError` carries the HTTP status so callers can branch on 404 vs other failures later. No axios dependency — `fetch` is sufficient for this API surface.
* `src/context/PatientContext.tsx` — the "patient-context provider (demo selector, no auth)" the task asked for. `PatientProvider` fetches `/api/patients` via a `usePatientsQuery` TanStack Query hook, keeps the selected SVNR in state, persists it to `localStorage` (survives reloads), and falls back to the first patient if the stored SVNR no longer exists in the list (e.g. stale value from a wiped DB). `usePatient()` is the consuming hook; throws if called outside the provider rather than silently returning `undefined`, so a future page missing the provider fails loudly during development instead of rendering a blank state.
* `src/layout/AppShell.tsx` — the responsive app shell every later page will render inside, since the task screenshots (real ApoScout/ApoApp reference apps) made clear the frontend must work on both phone and laptop widths, not just desktop-first as the original TASKS.md phrasing suggested. Bottom tab bar (Home/Suche/Einnahmeplan/Mehr) below 768px, full horizontal nav in the header at 768px+, single breakpoint, mobile-first CSS. "Mehr" opens a small sheet with the four secondary routes (Apotheken/Favoriten/Reservierungen/Rezepte) rather than cramming seven items into the bottom bar — mirrors the real apps' own "Mehr" pattern from the reference screenshots.
* Seven routes wired in `App.tsx`, all pointing at placeholder page components except `/` (`HomePage`, which also proves the patient context works by rendering "Angemeldet als: {name} ({svnr})"). Placeholders exist only so the shell/routing/CORS chain is provably working end-to-end this task — their real content is each route's own later task (TASK-27 through TASK-34), not built here.
* Backend: `com.medtrack.api.config.WebConfig` (`WebMvcConfigurer`) adds a CORS mapping on `/api/**`, origins from the new `app.cors.allowed-origins` property (`application.yml`, defaults to `http://localhost:5173`, overridable via `CORS_ALLOWED_ORIGINS` env var for a later non-localhost deploy).
* `.claude/launch.json` added so the frontend dev server can be previewed (`npm --prefix frontend run dev -- --host`).
* Verified live, not just via `npm run build`: started `docker-compose`'s Postgres, ran the backend with Maven, ran the frontend dev server, and in a browser confirmed — `GET /api/patients` succeeds cross-origin (no CORS errors, zero console errors), all seven nav links route correctly, the patient dropdown switches patients and the choice survives a full page reload (localStorage), and at a 375px mobile width the bottom nav + "Mehr" sheet render and navigate correctly.

## TASK-27 · Consent/landing screen

Done — `npm run build` succeeds, verified live in-browser at both desktop and mobile (375px) widths.

* `frontend/README.md` rewritten from Vite's default scaffold text into real frontend docs — stack/structure/conventions up front as the entry point for anyone touching `frontend/`, plus two closed-list references (the allowed icon tags in `Icon.tsx`, and the design tokens in `theme.css`) so future tasks extend those lists instead of inventing one-off values.
* `src/hooks/useConsent.ts` — a tiny `localStorage`-backed hook (`medtrack.consentGiven`), same pattern as TASK-26's patient-selection persistence. Deliberately not a React Context: nothing besides `App.tsx` needs to read or change consent state, so a context would just be indirection.
* `src/pages/ConsentPage.tsx` — a 4-slide onboarding carousel matching the structure of the reference apps' own onboarding flow from the task's screenshots (three skippable feature-intro slides, then a mandatory final security/consent slide), rather than the single condensed screen built in an earlier pass. Slide content/copy (`src/pages/onboardingSlides.ts`) is original wording describing MedTrack's own features (pharmacy search, medication search, medication plan) plus the same corrected privacy framing as before on the final slide — not verbatim text from the reference apps' copyrighted onboarding copy, which described a different, real product. "Überspringen" (skip) jumps straight to the final consent slide rather than skipping past it entirely, since accepting the fictive-data disclaimer isn't optional; "Weiter" advances one slide; dot pagination reflects the current slide.
* `Icon.tsx` refactored so `IconName` is its own exported union (previously piggybacked on `NavItem["icon"]`), since the onboarding slides need icons outside the nav (`shield` added for the security slide) — `navItems.ts` and `onboardingSlides.ts` both now import `IconName` from `Icon.tsx`, which is the single source of truth the README's "Allowed icon tags" list points to.
* Wired into `App.tsx` as a gate in front of `<Routes>`: `if (!hasConsented) return <ConsentPage onAccept={giveConsent} />` — no route exists for it, so it can't be deep-linked around, and once accepted the router resumes at whatever URL was already requested rather than forcing a redirect to `/`.
* Responsive per the task's explicit "Desktop layout" callout: a centered card (max-width 420px, growing to 460px at 768px+) over the shared `--color-bg` background, not the reference screenshots' edge-to-edge phone layout — verified at both 375px and desktop widths in-browser, including stepping through all four slides, the skip shortcut, and the disabled-until-checked CTA.

Not started: TASK-28 onward.

## TASK-28 · Home dashboard

Done — `npm run build` succeeds, verified live end-to-end against the real backend (search bar navigation, geocode → nearby-pharmacy flow, both desktop and 375px mobile).

* `src/api/pharmacies.ts` — `useNearbyPharmaciesQuery(lat, lng)` (`GET /api/pharmacies/nearby`, `enabled` gated on both coordinates being present) and `useGeocodeMutation()` (`GET /api/pharmacies/geocode`, a mutation rather than a query since it's a one-shot user-triggered lookup, not cached/refetched data).
* `src/context/UserLocationContext.tsx` — new app-wide context for the address/coordinates the user sets (`medtrack.userLocation` in `localStorage`, same persistence pattern as `PatientContext`/`useConsent`). Deliberately named `useUserLocation`, not `useLocation` — that name already belongs to `react-router-dom`'s current-URL hook, and shadowing it would be a landmine for the next person who imports both. Wired into `main.tsx` alongside `PatientProvider`.
* New `src/components/` folder (added to `frontend/README.md`'s structure section) for UI reused across pages that isn't layout/nav and isn't a whole route's content:
  - `SearchBar.tsx` - controlled input; on submit, `navigate`s to `/search?q=<value>` rather than searching itself. The actual search logic is TASK-29's job; this just proves the entry point works and hands the query along via the URL.
  - `LocationPicker.tsx` - address input + "Standort festlegen" button, calls the geocode mutation and writes the result into `UserLocationContext`; once a location is set, collapses to a one-line "Standort: {label} · Ändern" so it doesn't dominate the page on repeat visits.
  - `NextOpenPharmacyCard.tsx` - reads `useUserLocation()`; with no location set, shows a prompt instead of guessing one (no default coordinates, no silent browser-geolocation fallback - TASK-15's geocode adapter is the only location source, matching the reference app's own "Standort festlegen" pattern). With a location, queries `/nearby` and picks the first result with `openNow === true` (the endpoint sorts by distance, not by open status, so this has to filter client-side).
  - `QuickLinkTile.tsx` - small `Link`-wrapped card (icon + label), reused 4 times for the quick-link grid.
* `src/pages/HomePage.tsx` rebuilt from the TASK-26 placeholder into the real dashboard: hero copy, `SearchBar`, `LocationPicker`, a 4-tile quick-link grid (`Geöffnete Apotheken in der Nähe` → `/pharmacies`, `Verfügbarkeit von Medikamenten` → `/search`, `Mein Einnahmeplan` → `/medication-plan`, `Meine Lieblingsapotheken` → `/favorites`), then `NextOpenPharmacyCard`. All four quick-link tiles reuse existing `IconName`s (`store`/`search`/`pill`/`heart`) - no new icon tags needed for this task.
* Pollen widget (the DoD's explicit stretch item) was skipped - there's no backend data source for it and MedTrack's scope doesn't include a pollen feature anywhere in TASKS.md/architecture doc, so faking one would be pure decoration with no real data behind it.
* One new design token, `--color-success` (`#2f9e5c`), added to `theme.css` for the "geöffnet" status text on the next-open-pharmacy card, rather than hardcoding the green - README's token table updated to match.
* Verified against the real running backend end-to-end, not just `npm run build`: geocoded `Freyung 7, Wien` via the live Nominatim-backed endpoint, confirmed the nearby-pharmacy query returned and rendered the correct nearest open pharmacy (`Apotheke Zum Goldenen Loewen`, matching a direct `curl` of the same endpoint), confirmed `localStorage` persistence of the chosen location, and confirmed the search bar's `Return`-to-navigate flow lands on `/search?q=...` with the typed value intact.

## Shared infrastructure built for TASK-29–34

Before building the remaining pages, the full remaining backend API surface (`DrugController`, `AvailabilityController`, `FavoriteController`, `MedicationScheduleController`, `ReservationController`, `PrescriptionController`) and every DTO it returns were read directly from source rather than guessed from the task descriptions, specifically to avoid type mismatches against the real backend. One mismatch slipped through anyway - see "Bugs found and fixed" below.

* **`src/api/drugs.ts`, `availability.ts`, `favorites.ts`, `medicationSchedule.ts`, `reservations.ts`, `prescriptions.ts`** - one file per controller, continuing TASK-26's convention. Mutations that change server state (`favorites`, `medicationSchedule`, `reservations`) call `queryClient.invalidateQueries` on success so the UI reflects the change without a manual refetch.
* **One backend DTO change, made deliberately, not as scope creep:** `AvailabilityItemResult` gained a `lastUpdated` field (`AvailabilityItemResult.java`, `AvailabilityService.java`). The domain-level `PharmacyInventory.lastUpdated` already existed, and `StockRefreshJob`'s own code comment says it exists *"so the UI's 'last updated' freshness line moves over time the way a real polled integration would"* - the freshness line is explicitly named in TASK-29's DoD, so this was finishing a half-wired feature, not adding a new one. No existing test constructed the record positionally, so nothing broke.
* **`src/components/PharmacyCard.tsx`** - the single shared expandable pharmacy card used by TASK-29's results, TASK-30's list, and TASK-33's favorite-pharmacies tab: header (name, distance, open/on-call/closed status, optional favorite toggle) that expands to address + contact actions + weekly/on-call hours (lazily fetched via `usePharmacyDetailQuery` only once expanded) + an optional `children` slot for page-specific extra content (TASK-29 puts its per-drug availability breakdown there).
* **`src/components/PharmacyContactActions.tsx`** - call/route/email/web links; "Route" opens OpenStreetMap centered on the pharmacy's coordinates (no Google Maps dependency).
* **`src/components/PharmacyHours.tsx`** - renders `OpeningHoursResponse[]` grouped by `REGULAR` vs `ON_CALL`, via `src/utils/format.ts`'s new `germanDayName`/`formatTime` helpers.
* **`src/components/PharmacyMap.tsx`** - a `react-leaflet` wrapper (`MapContainer`/`TileLayer`/`Marker`) with the standard Vite-bundler fix for Leaflet's default marker icons (re-pointing `L.Icon.Default`'s URLs at the bundled asset imports - without this, markers silently fail to render, a well-known Leaflet+bundler gotcha).
* **`src/components/DrugSearchCombobox.tsx`** - search-as-you-type drug picker (debounce-free; the seeded catalog is small enough that per-keystroke queries are fine), reused by TASK-29's selection list and TASK-32's add-schedule flow.
* **`src/utils/format.ts`** (new) - `germanDayName`, `formatTime`, `formatDistance`, `formatPrice`, `formatDateTime`, `formatDate`. All backend `LocalDate`/`LocalTime`/`Instant` fields arrive as plain ISO strings (Spring Boot's default Jackson config, confirmed by inspecting real responses, not assumed) - these helpers only ever slice/relabel those strings, never round-trip through `Date` parsing for anything timezone-sensitive.
* Two new design tokens in `theme.css`: `--color-success-bg` and `--color-primary-bg` (soft tint backgrounds for status badges), extending the `--color-success` token from TASK-28 rather than hardcoding new hex values - README's token table updated to match.

### Bugs found and fixed during live verification (documented here specifically so the same mistake isn't repeated elsewhere)

1. **Wrong reservation status literal.** `ReservationResponse.status` is a plain `String` in the DTO, so nothing at compile time caught that the frontend assumed `"OPEN"` when the actual backend enum (`domain.ReservationStatus`) is `REQUESTED`/`CANCELLED`. Reserving a medication silently worked (`POST` succeeded) but the Reservations page then showed "Keine Reservierungen" because its filter never matched anything. Found by checking the raw `GET /api/patients/{svnr}/reservations` response against what the UI rendered, not by assumption. Fixed in `src/api/reservations.ts` and `src/pages/ReservationsPage.tsx`. **Lesson for future DTOs with a bare `String` status field (there's no compiler safety net):** always read the actual Java enum before hardcoding its values in TypeScript, the same way the DTOs themselves were read.
2. **Doctor name duplication.** `PrescriptionsPage.tsx` prefixed `"Dr. " + prescription.doctorName`, but the seed data (`V2__seed_demo_data.sql`) already stores names as `"Dr. Julia Steiner"` - rendered as "Dr. Dr. Julia Steiner". Fixed by removing the frontend's own prefix.
3. **Invalid nested `<button>`.** `PharmacyCard`'s original structure put the whole expand/collapse header in a `<button>`, with `FavoriteButton` (also a `<button>`) nested inside it - invalid HTML, and React logs a hydration-error warning for it in the console even though Chromium's HTML parser silently "fixes" the DOM at runtime so it still visually worked. Caught by reading `read_console_messages`, not by visual inspection - a nested-interactive-element bug like this won't show up in a screenshot. Fixed by making `.pharmacy-card__header` a plain `<div>` containing a `.pharmacy-card__toggle` button (name/status/chevron only) as a sibling of the `FavoriteButton`, not a parent. **General lesson:** any card with both a big clickable "expand" area and a smaller clickable "action" area inside it needs the action button to be a *sibling*, never a *descendant*, of the expand button.

## TASK-29 · Search & Availability page

Done - `npm run build` succeeds; verified live against the real backend (added a drug, ran a real availability check, expanded a real pharmacy card, made and cancelled a real reservation, confirmed the map renders real markers) at both desktop (split view) and 375px mobile (stacked view) widths.

* `src/pages/SearchPage.tsx` - reads `?q=` from the URL (set by TASK-28's `SearchBar`) and surfaces it as a hint, but doesn't auto-populate the selection list from it - the query param is informational only; DoD didn't ask for a free-text drug-name search endpoint on this page, only the selection-list + availability-check flow. Selection list state (`{drug, quantity}[]`) is local component state, not persisted - it's a working list for one check, not a saved cart.
* "Verfügbarkeit prüfen" is a `POST` mutation (`useAvailabilityCheckMutation`), disabled until both a location is set and the selection list is non-empty. Results replace in place (`useState`, not derived from the query cache) so "Erneut prüfen" (recheck) is just calling the same mutate function again - one shared recheck action for the whole result set, not per-pharmacy, because the backend's `/availability/check` endpoint itself has no per-pharmacy variant to recheck against.
* Each result renders via the shared `PharmacyCard`, with the per-drug availability breakdown (name, ✓/✗, price, a "Reservieren" button when `pharmacy.reservationSupported && item.inStock`, or a "Reservierung wird nicht angeboten" pill when the pharmacy doesn't support it at all) and the freshness line passed in via `children`.
* "Reservieren" calls `useCreateReservationMutation` with that specific `{pharmacyId, drugId, quantity}` - reservations are inherently single-drug (`CreateReservationRequest` has no item list), so with multiple drugs selected, each in-stock item gets its own reserve button rather than one whole-pharmacy action.
* "Preisvergleich" next to each selection-list row links to `/drugs/{id}/compare` (TASK-31).
* Map split-view: `PharmacyMap` fed the current results' pharmacies, laid out via CSS flex - side-by-side on ≥768px (list flex 1.1, map flex 1, sticky), stacked with the map first on mobile (`flex-direction: column-reverse` puts the map above a scrollable list, since a fixed-height map below an unbounded list would push it off-screen on a phone).

## TASK-30 · Pharmacies page

Done - `npm run build` succeeds; verified live (on-call filter toggle, favorite toggle, confirmed on the Favorites page).

* `src/pages/PharmaciesPage.tsx` - toggles between `useNearbyPharmaciesQuery` and `useOnCallPharmaciesQuery` based on a local `onCallOnly` boolean (only one of the two is ever `enabled`, so switching the filter doesn't fire both endpoints). Reuses `PharmacyCard` with the favorite toggle wired to `useFavoritePharmaciesQuery`/`useToggleFavoritePharmacyMutation` - the favorite `Set<id>` is built once per render from the favorites list, not per-card, so all cards agree on favorite state without N separate queries.
* Same list/map split layout pattern as TASK-29 (deliberately not extracted into a shared layout component yet - two pages with a similar-but-separate small CSS block was judged simpler than a premature abstraction; revisit if a third page needs the same split).

## TASK-31 · Price comparison view

Done - `npm run build` succeeds; verified live (`/drugs/{id}/compare`, cheapest-first ordering confirmed against a direct `curl` of the same endpoint).

* `src/pages/PriceComparisonPage.tsx` at route `/drugs/:drugId/compare` - not in the bottom/top nav (neither reference app nor TASKS.md gives this its own nav entry; it's a drill-down from a specific drug, reached via the "Preisvergleich" link TASK-29 added to each selection-list row).
* Renders `GET /api/drugs/{id}/compare?lat&lng` (`usePriceComparisonQuery`) as-is, in the order the backend already returns it ("cheapest first" per the endpoint's own `@Operation` summary) - no client-side re-sorting. A "Günstigster Preis" badge is shown on the first row **only if it's actually in stock** (`i === 0 && entry.inStock`) - the cheapest price at a pharmacy that doesn't have it isn't a real offer, so it doesn't get the "best price" badge; whichever in-stock row happens to also be cheapest overall still gets no special "still-cheapest-among-in-stock" callout, since the DoD only asked for a plain per-drug cross-pharmacy list, not a second in-stock-only ranking.

## TASK-32 · My Medication Plan page

Done - `npm run build` succeeds; verified live end-to-end against real seeded data (Anna Gruber's existing Aspirin schedule) and a newly created schedule (confirmed intake, added a new Paracetamol schedule via the form, both appeared correctly in "Heute fällig" and "Meine Medikamente" immediately).

* `src/pages/MedicationPlanPage.tsx` - three sections: "Heute fällig" (`useDueTodayQuery`, only rendered if non-empty, each row's `PENDING` status shows "Einnahme bestätigen"/"Überspringen" buttons calling `useRecordIntakeMutation` with `TAKEN`/`SKIPPED`; `TAKEN`/`SKIPPED` rows show a plain status label instead), the active-schedule list with a "Stoppen" button per row (`useDeactivateScheduleMutation` - a soft deactivate per the backend's own design, not a delete, so intake history survives), and the empty state (matching the reference screenshot's copy: "Noch keine Medikamente eingetragen" + a CTA) shown only when there are zero *active* schedules.
* The add-schedule form (`AddScheduleForm`, a local sub-component in the same file - page-specific, single-use, not extracted to `components/`) uses `DrugSearchCombobox` for the drug, plain text for dose, native `<input type="date">`/`<input type="time">` for dates/times (their native string values already match the backend's expected ISO format, so no date-library dependency was needed), and a repeatable list of time-of-day rows (add/remove, at least one required) since `CreateScheduleRequest.times` is a list - a schedule can be more than once daily (the seed data's Max Bauer/Metformin schedule already exercises this on the backend side).

## TASK-33 · Favorites + Reservations pages

Done - `npm run build` succeeds; verified live (favorited a pharmacy from the Pharmacies page and confirmed it appeared here; created and cancelled a real reservation).

* `src/pages/FavoritesPage.tsx` - two tabs (Apotheken/Medikamente, matching the reference screenshot's tab labels exactly), each with its own empty-state copy ("Keine Stammapotheken gefunden" is the reference app's own exact wording, since that's genuinely accurate copy worth reusing verbatim - unlike the onboarding carousel's marketing copy, an empty-state label isn't the kind of creative/copyrighted content that needed rewording). The pharmacies tab reuses `PharmacyCard` (favorite already true, toggling removes it); the drugs tab is a plainer list since `PharmacyCard`'s hours/contact-details expansion doesn't apply to a drug - just name/form/pack size plus a `FavoriteButton`.
* `src/pages/ReservationsPage.tsx` - lists only `status === "REQUESTED"` reservations (see the "Bugs found and fixed" section above for why this isn't `"OPEN"`); "Stornieren" calls `useCancelReservationMutation`, which soft-cancels server-side (same non-destructive pattern as deactivating a medication schedule) and disappears from this list via query invalidation.

## TASK-34 · My Prescriptions page

Done - `npm run build` succeeds; verified live against the real seeded prescriptions for Anna Gruber (one `OPEN`, one `REDEEMED`).

* `src/pages/PrescriptionsPage.tsx` wires up the already-shipped `GET /api/oegk/{svnr}/prescriptions` endpoint (built back in the original ÖGK slice, TASK-05–09) - the simplest page in Phase 2E, exactly as TASKS.md's own description of this task predicted. Status badge uses the `--color-primary-bg`/`--color-success-bg` tokens for `OPEN`/`REDEEMED` respectively.

**Phase 2E (frontend) is now complete - TASK-26 through TASK-34 all done.** Every route in the nav (Home, Suche, Einnahmeplan, Apotheken, Favoriten, Reservierungen, Rezepte) plus the drill-down price-comparison view is wired to the real backend, not placeholders.

## TASK-35 · Docs

Done.

* [`docs/FEATURE_MAPPING.md`](FEATURE_MAPPING.md) (new) - the ApoScout/ApoApp → MedTrack feature table this task's DoD asked for: every reference-app feature mapped to its exact backend service class/endpoint and frontend route/component, plus an explicit "deliberately not implemented" section (pollen widget, health-news feed, real ID Austria login) so those known gaps don't get mistaken for oversights and re-investigated later.
* [`frontend/README.md`](../frontend/README.md) - structure section brought back in sync with the codebase as it actually stands after TASK-29–34 (every `api/`, `components/`, `context/`, `hooks/`, `utils/` file, not just what existed after TASK-28), plus two new sections: a **Routes** table (every path → page component, one line each) and a **Troubleshooting** section covering the failure modes actually hit while building Phase 2E (CORS/backend-down, a query silently disabled because no location is set, wrong backend status-enum literals, Leaflet's marker-icon bundler gotcha, the nested-button hydration warning, and IntelliJ's cache lagging behind real git state) - written so a future session (or the next person) can self-diagnose from the symptom instead of re-deriving the cause from scratch.
* `docs/TASKS.md`'s final status line rewritten to reflect the whole project's actual state (TASK-00 through TASK-35 all done) rather than needing another incremental bump.
* `docs/PROGRESS.md` (this file) already carried a detailed per-task writeup for every task done in this session, including a dedicated "Bugs found and fixed" section under the TASK-29 shared-infrastructure entry - that section exists specifically because of this task's "easy to find and solve the problem later" requirement, not as routine changelog noise.

**Every task in TASKS.md (TASK-00 through TASK-35) is now done.**

## TASK-36 · Mock ID Austria login + e-card scan registration flow

Done - `mvn test` passes (33/33, unchanged), frontend `npm run build` succeeds, verified live end-to-end three times: the happy path (correct name + matching birthdate → lands in the app with the right patient selected), the "unknown name" 404 path (a name that isn't one of the four seeded demo patients), and the birthdate-mismatch validation path (a real seeded name but a wrong asserted birthdate) - each produces the correct, distinct outcome.

**Why now, and why this shape:** the architecture doc always named a `MockIdAustriaAuthAdapter` as part of the target design (see TASK-01's note below), but it was never built - TASK-26's demo patient dropdown was an explicit, documented stand-in ("stands in for real login while auth is out of scope"). This task adds the real thing. Per explicit instruction, it is a **one-time registration step**, not a gate that reappears every session (like TASK-27's consent screen) and not a parallel optional flow that coexists with instant dropdown access - it runs once, is remembered via `localStorage`, and the existing patient-selector dropdown in the header is untouched, still available afterward for quickly previewing other demo patients.

**Backend** (hexagonal - two ports, two mock adapters, matching the project's established pattern):
* `domain/IdAustriaIdentity.java` (new) - `(fullName, dateOfBirth, authenticatedAt)`, a plain record, not persisted - same precedent as `EHealthCardSession` (TASK-05: "no expiry/window logic, since nothing needs it yet").
* `application/port/IdAustriaAuthPort.java` + `infrastructure/idaustria/MockIdAustriaAuthAdapter.java` (new) - `authenticate(fullName, dateOfBirth)`. A real ID Austria login is a federated redirect with nothing for a demo to check against, so the mock **always succeeds** - it simply asserts back whatever identity the frontend collected. This is intentional, not a shortcut: the actual identity check happens one step later (see below), matching how a real ID Austria + e-card system is really two independent trust boundaries, not one.
* `domain/ECardDetails.java` (new) + `EHealthCardPort` extended with `scanCard(String fullName)` (implemented by the existing `MockEHealthCardAdapter`, alongside its original `lookupBySvnr`) - extending the *existing* e-card port rather than inventing a separate one, since "scan a physical e-card" is an e-card-integration concern, the same as the original `lookupBySvnr`.
  * Looks up a seeded `Patient` by name (`PatientRepository.findByNameIgnoreCase`, new derived query) - 404s (`NoSuchElementException`, already mapped by `ApiExceptionHandler`) if no seeded demo patient matches. There is no way to register as a made-up person; only the four seeded demo patients have e-cards, which is the honest limit of a fake-data-only project and is stated plainly in the frontend's error copy rather than hidden.
  * **Birthdate is decoded from the SVNR itself**, not stored separately: Austrian SVNRs encode the holder's birthdate as `ddMMyy` in the last 6 digits, and the seed data (`V2__seed_demo_data.sql`) was already built this way (e.g. `1234010190` → day `01`, month `01`, year `90` → 1990-01-01) even though nothing surfaced that fact until now. Century inference uses the standard sliding-window heuristic (if `2000+yy` is in the future, it's `1900+yy`).
  * Card serial number (20 digits) and expiry date aren't stored anywhere (`Patient` only ever had `svnr`+`name`, per TASK-01) - generating them on the fly in the adapter (serial = svnr + reversed svnr, deterministic; expiry = a fixed number of years out) avoids a schema migration for fields nothing needs to query or persist, consistent with "everything fake, generated by the app."
  * `carrierNumber`/`carrierName` are a fixed fictional `"4711"` / `"ÖGK"` - deliberately not researched from any real OEGK identifier, matching the project's "never real data, even public real data" discipline.
* `application/service/RegistrationService.java` (new) orchestrates both ports and maps domain → DTO, matching every other service's controller→service→port→adapter layering exactly. `api/controller/RegistrationController.java` (new) exposes `POST /api/registration/id-austria-login` and `POST /api/registration/scan-card`.
* No new migration, no new persisted table - everything here is either a lookup against existing seed data or a value computed on the fly.

**Frontend:**
* `src/hooks/useRegistration.ts` - `localStorage`-backed (`medtrack.registrationComplete`), identical shape to `useConsent`.
* `src/api/registration.ts` - `useIdAustriaLoginMutation`, `useScanCardMutation`.
* `src/pages/RegistrationPage.tsx` orchestrates three step components under `src/pages/registration/` (split into separate files, unlike `AddScheduleForm`'s single-file sub-component, because each step here has enough independent form state and API logic to be worth its own file):
  1. `IdAustriaLoginStep.tsx` - Vorname/Nachname/Geburtsdatum form, styled in MedTrack's own red/cream visual language (not a copy of the real ID Austria government page's blue/white branding or logo - the reference screenshot was used for *concept*, not pixel/asset reproduction), explicitly labeled "(Demo)" and "Diese Anmeldung ist eine Demo-Simulation" so it's never mistaken for the real thing.
  2. `ECardScanStep.tsx` - a file input (`accept="image/*" capture="environment"`, opens the camera directly on a phone) wrapped in a styled drop-zone; on file selection, a ~1.2s simulated "Scanne…" delay (matching the honesty-about-being-fake tone of `StockRefreshJob`'s comment elsewhere in this codebase) before calling the scan endpoint. On a 404, shows the exact four demo-patient names as a hint rather than a bare error, since this is a demo meant to be explorable.
  3. `ConfirmCardDataStep.tsx` - all scanned fields editable and pre-filled; re-derives the birthdate from whatever SVNR is *currently in the form* (not the original scan response) using the same `ddMMyy` decode as the backend, so editing the SVNR field is itself covered by the check. On submit, compares that against the login step's asserted birthdate - mismatch blocks submission with an inline, specific error message and keeps the user on the step (verified live: typing a real seeded name with the *wrong* birthdate correctly produces this error rather than silently proceeding).
* `Icon.tsx` gained one more `IconName`: `camera` (used by the scan step) - extends the same closed list from TASK-27/28, README's "Allowed icon tags" table updated.
* Wired into `App.tsx` as a second one-time gate, after `ConsentPage` and before the router: `if (!isRegistered) return <RegistrationPage onComplete={completeRegistration} />`. On the confirm step's success, it calls the *existing* `usePatient().selectPatient(svnr)` - none of TASK-26's `PatientContext` or any of the 8 pages built in Phase 2E needed to change at all, since the registration flow's only job is to decide which SVNR to feed into infrastructure that already existed.
* `docs/FEATURE_MAPPING.md` updated: "real ID Austria / e-card login" moved out of the "deliberately not implemented" list (where TASK-35 had correctly placed it, since it was true at the time) and into the main feature table, now that it exists.

**Follow-up (same task, added right after):** the flow was initially mandatory-until-completed with no way out. Per explicit instruction, added a persistent "Ohne Anmeldung fortfahren" (continue without registering) control in `RegistrationPage`'s own top bar - shown across all three steps at once (one control, not three duplicated skip buttons, since "skippable at any stage" doesn't require re-implementing the skip action per step). It calls `onComplete()` directly, deliberately bypassing `selectPatient()` entirely rather than routing through it - the point is that nothing about the e-card/ID-Austria linkage runs at all, not just that it runs with default values. This falls through to `PatientContext`'s pre-existing auto-select-first-patient behavior (unchanged since TASK-26), so a user who skips still lands in a working app, exactly as anyone did before TASK-36 existed - the header's patient dropdown remains their only, and unchanged, way to pick who they are. A short line under the logo explains what skipping means and where to go instead (the demo-patient dropdown), since a bare "skip" button with no explanation would be confusing about what happens next. Verified live at both desktop and mobile widths, zero console errors on a fresh tab.

**Second follow-up: a way back in, and a better photo step.** Two more explicit asks on the same feature:

1. *Reopen registration after skipping (or to re-verify).* `useRegistration` now tracks two independent flags, not one: `isRegistered` (has the one-time gate been passed at all, by either path) and `isLinked` (was it passed specifically via a completed e-card scan, never by skipping). `completeRegistration(linked: boolean)` sets `isLinked` only when `linked` is true; `RegistrationPage`'s skip button calls `onComplete(false)`, the confirm step's success calls `onComplete(true)`. A new `reopenRegistration()` clears only the `isRegistered` flag (not `isLinked` - so a previously-linked identity isn't discarded just by reopening the flow), which flips `App.tsx`'s gate back on. The entry point is `IdentityLinkBanner` (new, `src/components/`), rendered in `AppShell` above `<Outlet />` whenever `!isLinked` - a slim, non-blocking strip on every page (not crammed into the header row, which is already tight on mobile with the logo + patient selector) explaining that the account isn't linked and offering a "ID Austria verknüpfen" button that calls `reopenRegistration()`. Once actually linked, the banner disappears for good (verified live: skip → banner appears → click it → back at step 1 of `RegistrationPage` → complete it for real → banner gone, `medtrack.identityLinked` now `true`).
2. *"Make the photo part optimal" (`ECardScanStep.tsx` only - the other two steps were deliberately left untouched, per the instruction's scope).* The step no longer auto-scans the instant a file is chosen; it now shows an actual image preview (`URL.createObjectURL`, properly revoked on file change/unmount via a `useEffect` cleanup, so repeated photo picks don't leak object URLs) with explicit "Anderes Foto wählen" (discard, go back to the picker) and "Foto scannen" (confirm, then runs the same simulated-delay scan as before) actions - letting the user actually see and confirm what they're about to submit, which the original auto-scan-on-select design didn't allow. Also added drag-and-drop (`onDragOver`/`onDrop` on the picker, with an `.is-dragging` hover state) as a desktop-friendly alternative to `capture="environment"`'s mobile-camera path, and a fix for the file `<input>` not firing `onChange` when the exact same file is re-selected after "Anderes Foto wählen" (`onClick` resets `e.currentTarget.value`, a standard file-input gotcha). Verified live: preview renders a real selected image, "Anderes Foto wählen" correctly resets to the empty picker, re-selecting and scanning still reaches the confirm step correctly.

**Third follow-up: the photo step was optional, not "optimal" - a wording slip in the instruction, caught by the user asking directly whether manual entry was actually possible.** It wasn't; fixed. `ECardScanStep` now takes an `onManualEntry` callback alongside `onSuccess`, exposed as a "Kein Foto zur Hand? Werte stattdessen manuell eingeben" link - shown on the empty picker state and again next to "Zurück" so it stays reachable even after a failed (404) scan, not just before picking a photo. Choosing it skips the photo/scan entirely and jumps straight to `ConfirmCardDataStep` with a blank `ECardDetails` (`RegistrationPage` constructs it) - blank for the fields only an e-card can provide (SVNR, card serial, carrier number, expiry), but firstName/lastName/dateOfBirth are still carried over from the ID-Austria login step (already typed once, no reason to ask twice) and `carrierName` still defaults to `"ÖGK"` (the only carrier this project models). `ConfirmCardDataStep`'s copy was adjusted from "review the *scanned* data" to "review and *complete*" so it reads correctly for both the scanned and the manual path, since it can no longer assume a scan happened. The same SVNR-vs-registered-birthdate validation applies unchanged either way (verified live: manual entry with a correct SVNR reaches the app and sets `isLinked`; manual entry with a mismatched SVNR is correctly rejected with the same inline error as the scanned path). One noteworthy non-issue hit while testing this: a "Rendered more hooks than during the previous render" console error appeared once, from Vite Fast Refresh hot-swapping `useRegistration` mid-session after its hook count changed (TASK-36's second follow-up added a second `useState` to it) - confirmed via a completely fresh tab (no HMR history) that this doesn't occur on an ordinary page load, so it wasn't a code bug, just a dev-server artifact of editing a hook's shape while the page was already running.

**Fourth follow-up: a real MedTrack account ID, and the registered name acting as a proper "username."** Two related asks: make registration "work like a usual login system" (your name/surname becomes your visible username right after registering), and give each account MedTrack's own unique identification number - separate from the government-issued SVNR next to it.

* **This is the first genuinely *persisted* addition to the registration feature** - everything else in TASK-36 (card serial, carrier number/name, expiry) is computed on the fly precisely because it doesn't need to be queried or remain stable in a way that matters for a demo. A MedTrack-ID is different: it's supposed to *be* your stable account number, so it has to actually live in the database, not be regenerated. New migration `V9__add_patient_medtrack_id.sql` adds `patient.medtrack_id` (`NOT NULL`, `UNIQUE`), backfilling the four seeded patients with fixed values (`MT-10000001`…`MT-10000004`) - same additive-migration-plus-backfill pattern as `V4`'s drug catalog extension.
* `Patient`'s constructor gained a third mandatory parameter (`medtrackId`) rather than a nullable/optional field, matching how `svnr`/`name` are already both constructor-required - the entity has no "partially constructed" state anywhere else, so this doesn't introduce one either. The three tests that call `new Patient(...)` directly (`PrescriptionServiceTest`, `MockEHealthCardAdapterTest`, `PrescriptionRepositoryTest`) were updated to pass a placeholder id; none of them assert on it.
* Threaded through both existing read paths, not just the registration flow: `PatientResponse`/`PatientService` (so `GET /api/patients` - already polled by `PatientContext` for the header dropdown - carries it for free) and `ECardDetails`/`ECardDetailsResponse`/`MockEHealthCardAdapter.scanCard` (so a scan surfaces it too, the same way a real card would).
* Frontend: `ConfirmCardDataStep` shows MedTrack-ID as a read-only field (matching how `Versicherung` was already read-only) right above SVNR. For the manual-entry path there's nothing to show yet (no scan happened, and the SVNR hasn't been validated), so it displays a literal placeholder string, *"(wird nach Bestätigung zugewiesen)"*, instead of leaving a blank box that looks like something the user forgot to fill in - it's genuinely not theirs to fill in, so it shouldn't look editable-but-empty. Once confirmed (either path), the header (`PatientSelector`, now wrapped in a two-line block - name in the dropdown, MedTrack-ID in small muted text underneath) and the Home page (a new `Angemeldet als **{name}** · MedTrack-ID: **{id}**` line above the hero) both show the real value immediately - "right after you register" is satisfied literally, since Home is the very next screen. `--header-height` bumped 56px→68px to fit the second line without cramping either breakpoint (verified at both).
* The "username" half of the ask was mostly already true by construction (the registered name has to exactly match a seeded patient to succeed at all, so what you type and what's stored are the same string) - the real gap was that nothing *displayed* it as your account identity afterward. That display gap is what this follow-up actually closes, not the underlying matching logic, which is unchanged.
* `docs/DEVELOPMENT.md`'s §4.8 demo-data table (added for the previous "give me testing samples" request) gained a MedTrack-ID column and a new verification snippet (`GET /api/patients`, confirming the id is a real stored column, not something only the registration endpoints know about) - kept in sync rather than left stale now that the table's own subject grew a field.
* Verified live end-to-end: `mvn test` still 33/33 after the migration; registered as Lena Hofer via manual entry, confirm screen showed the "not yet assigned" placeholder, and immediately after confirming both the header and Home page showed `Lena Hofer` / `MT-10000003` correctly, at both desktop and mobile widths, zero console errors on a fresh tab.

Not started: nothing outstanding in the current backlog.
