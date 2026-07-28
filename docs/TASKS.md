# Backlog: "ÖGK — My Prescriptions" slice

### TASK-00 · Scaffold (joint, ~0.5 day, anyone can do it)
Spring Boot project, packages domain/application/infrastructure/api, Docker Compose + Postgres, Flyway wired up, CI (build+test).
DoD: `mvn spring-boot:run` starts up, `docker-compose up` provides the DB, an empty health-check endpoint responds 200.

---

### Dev A — Data Layer

TASK-01 · Domain entities
Patient(svnr, name), Doctor(name, specialty), Drug(name, activeSubstance), Prescription(patient, doctor, drug, dosage, issuedDate, status).
DoD: compiles, JPA annotations in place, relationships (`@ManyToOne`) are correct.

TASK-02 · Flyway `V1__init_schema.sql`
Tables for the entities from TASK-01 + an index on patient.svnr.
DoD: migration applies cleanly on an empty database.
*Depends on TASK-01.*

TASK-03 · Flyway `V2__seed_demo_data.sql`
3–5 patients, 2–3 doctors, 5–7 drugs, 8–10 prescriptions (with different statuses: open/redeemed).
DoD: after `docker-compose up`, realistic test data is visible in the database.
*Depends on TASK-02.*

TASK-04 · Spring Data repositories
PatientRepository, PrescriptionRepository with a findByPatientSvnr(String svnr) method.
DoD: a unit/slice test (`@DataJpaTest`) for lookup by SVds on TASK-01.*

---

### Dev B — Access & API
*(can start in parallel with Dev A — working against interfaces/stub entities, without waiting for the database to be ready)*

TASK-05 · `EHealthCardPort` (interface)
EHealthCardSession lookupBySvnr(String svnr) — contract only, no implementation.
DoD: interface exists in application/port.

TASK-06 · `MockEHealthCardAdapter`
Port implementation: looks up the patient by SVNR via PatientRepository, throws PatientNotFoundException if not found.
DoD: unit test for found/not-found cases.
*Depends on TASK-04 (repository) and TASK-05.*

TASK-07 · `PrescriptionService.getPrescriptionsForPatient(svnr)`
Uses EHealthCardPort to validate the SVNR, then PrescriptionRepository to fetch data, maps to DTO.
*Depends on TASK-04, TASK-06.*

TASK-08 · REST controller
GET /api/oegk/{svnr}/prescriptions → PrescriptionResponse[] (drug, dosage, doctorName, issuedDate, status). 404 for an unknown SVNR, Swagger annotations.
DoD: curl returns correct JSON, and 404 for a bogus SVNR.
*Depends on TASK-07.*

---

### Joint (once both branches are ready, ~1 day)

TASK-09 · Testcontainers integration test
Spins up Postgres, runs Flyway, seeds data, hits /api/oegk/{svnr}/prescriptions, checks the response body.

TASK-10 · Merge + manual verification
Merge branches, run through Postman/curl for the "existing SVNR" and "non-existing SVNR" scenarios.

---

Order: TASK-00 → TASK-01 blocks TASK-02/03/04; Dev B can write TASK-05/07/08 against mocks without waiting for Dev A, but TASK-06 is coupled to TASK-04. That's usually enough to keep both branches from being idle.

---

### Deferred — domain/persistence split

TASK-11 · Split domain model from JPA persistence
`Patient`, `Doctor`, `Drug`, `Prescription` are JPA-annotated directly in `domain/` (TASK-01's simplification) instead of the layering in `MedTrack_Architecture.doc` (pure domain objects + separate `@Entity` classes/mappers under `infrastructure/persistence`). Revisit once the ÖGK slice works end-to-end: pull the JPA annotations out into persistence-only entities, add mappers, repoint TASK-04's repositories at the persistence entities.
DoD: `domain` package has zero `jakarta.persistence` imports; all existing tests still pass.
*Not blocking TASK-02–10 — deliberately deferred, tracked here so it isn't forgotten.*

---

## Phase 2 — ApoScout + ApoApp feature build

Extends the ÖGK slice with the pharmacy-locator, medication-availability, pricing, and adherence-tracking features from the architecture doc (features 1/4/6/7 plus "adherence tracking"), combining the functionality/UX of two real Austrian pharmacy apps (ApoScout, ApoApp). Scope for this phase: no real login yet (a demo patient-selector stands in for it), desktop/laptop-first web frontend (not phone-styled), reminders computed live with no persisted `Notification`/scheduler. Solo, sequential — no more Dev A/Dev B split. Detailed writeups: [PROGRESS.md](PROGRESS.md).

### Phase 2A — Pharmacy & availability core

TASK-12 · Pharmacy domain entities
`Pharmacy`, `PharmacyOpeningHours` (regular + on-call hours in one table), `PharmacyInventory`, plus `Coordinates` and `OpeningHoursKind`.
DoD: compiles, JPA annotations correct.

TASK-13 · Flyway pharmacy schema + seed data
Schema for the TASK-12 entities, plus 6–8 fictional Vienna-area pharmacies with hours (incl. an on-call rotation) and inventory.
DoD: migrations apply cleanly on an empty database.
*Depends on TASK-12, TASK-14 (seed data needs the extended drug catalog).*

TASK-14 · Drug catalog extension
Adds form/packSize/manufacturer/prescriptionRequired/pzn/packageLeafletText to `Drug`, backfills existing rows, adds a few new catalog rows.
DoD: migration applies cleanly, backfill covers all pre-existing drugs.

TASK-15 · `GeoPort` + `NominatimGeoAdapter` + Haversine helper
Real (not mocked) call to the public Nominatim API for the manual "set location" search; a distance-calculation helper.
DoD: compiles; manually verified against the live Nominatim API.

TASK-16 · `PharmacyStockPort` + `PharmacyService` + `StockRefreshJob`
Nearby/on-call-now/detail lookups; a `@Scheduled` job simulating a live stock feed.
DoD: compiles; manually verified via the nearby/on-call/detail endpoints.
*Depends on TASK-13, TASK-15.*

TASK-17 · `AvailabilityService`
Multi-item ("selection list") cross-pharmacy availability check.
DoD: compiles; manually verified via the availability-check endpoint.
*Depends on TASK-16.*

TASK-18 · REST controllers
`PatientController` (list demo patients), `PharmacyController`, `DrugController`, `AvailabilityController`.
DoD: curl/Postman returns correct JSON for each; unknown ids return 404.
*Depends on TASK-14, TASK-17.*

TASK-19 · Testcontainers integration test
End-to-end test covering the availability flow, matching `PrescriptionControllerIntegrationTest`'s pattern.
*Depends on TASK-18.*

---

### Phase 2B — Pricing & comparison

TASK-20 · `PricingService` + `ComparisonService`
Flat Rezeptgebühr for prescription-required drugs, OTC price otherwise; cross-pharmacy price comparison per drug.
*Depends on TASK-14.*

TASK-21 · Price/compare REST endpoints
Exposed on `DrugController`.
*Depends on TASK-20.*

---

### Phase 2C — Favorites & reservations

TASK-22 · `FavoritePharmacy`/`FavoriteDrug`/`Reservation` entities
Plus a Flyway migration.

TASK-23 · Favorite + reservation logic and endpoints
Favorite toggle/list, `ReservationService`, new `ReservationController`.
*Depends on TASK-22.*

---

### Phase 2D — Medication plan / adherence
*(independent of 2A–2C — only needs Patient + Drug, can be built any time)*

TASK-24 · `MedicationSchedule`/`MedicationScheduleTime`/`MedicationIntakeLog` entities
Plus Flyway schema + seed migrations with demo schedules for existing patients.

TASK-25 · `MedicationScheduleService` + `MedicationScheduleController`
CRUD + a computed "due today" query; confirm-intake endpoint.
*Depends on TASK-24.*

---

### Phase 2E — Frontend
`frontend/`, Vite + React + TypeScript, desktop/laptop-first (not phone-styled)

TASK-26 · Scaffold + CORS + patient context
Vite + React + TypeScript + Router + TanStack Query + react-leaflet; CORS config on the backend; a patient-context provider (demo selector, no auth).

TASK-27 · Consent/landing screen
Desktop layout, privacy copy accurate to MedTrack's fake-data-only scope (not "stored only on your phone" like the real reference app).

TASK-28 · Home dashboard
Search bar + quick-link tiles + next-open-pharmacy card + optional pollen widget (stretch).

TASK-29 · Search & Availability page
Selection list + results list with expandable per-pharmacy detail (contact actions, hours + on-call hours, freshness line, reserve/recheck) + map split-view (Leaflet/OSM).

TASK-30 · Pharmacies page
List/map, on-call-now filter, favorite toggle.

TASK-31 · Price comparison view
Per-drug cross-pharmacy price list.

TASK-32 · My Medication Plan page
Schedule list, confirm-intake action, add-schedule flow.

TASK-33 · Favorites + Reservations pages
Favorites (pharmacies/medications tabs) and reservations list.

TASK-34 · My Prescriptions page
Wires up the existing, already-shipped `/api/oegk/{svnr}/prescriptions` endpoint so the whole app feels unified.

---

### Phase 2F — Polish

TASK-35 · Docs
Update README/TASKS/PROGRESS; add a short doc mapping ApoScout/ApoApp features → MedTrack services.

---

Status: TASK-12–23 done (see PROGRESS.md for the detailed writeup); TASK-24 onward not started. Phase 2A-2C (backend: pharmacy locator, availability, pricing/comparison, favorites/reservations) complete.
