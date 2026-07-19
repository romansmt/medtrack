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
DoD: a unit/slice test (`@DataJpaTest`) for lookup by SVNR passes.
*Depends on TASK-01.*

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
