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

**Quick recheck** — reach for this shorter sequence (rather than the granular 4.1–4.4 walkthrough below) specifically when you just want fast end-to-end confirmation that nothing is actually broken: after clearing a stale port/process (see §7), after a machine restart, after pulling changes, or any time something *just* got fixed and you want to confirm it stuck. It assumes Postgres is already running (`docker compose ps` shows `healthy`) — if it isn't, start with `docker compose up -d` first.

```powershell
$env:JAVA_HOME = "C:\Users\rivay\.jdks\ms-21.0.11"
$mvn = "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.4\plugins\maven\lib\maven3\bin\mvn.cmd"
```

```powershell
& $mvn test
```
Expect: `Tests run: 10, Failures: 0, Errors: 0`, `BUILD SUCCESS`.

```powershell
& $mvn spring-boot:run
```
Expect Tomcat to start cleanly on port 8080 — no "port already in use" error.

In a second terminal, hit the actual endpoint:
```powershell
Invoke-WebRequest http://localhost:8080/api/oegk/1234010190/prescriptions | Select-Object -ExpandProperty Content
```
Expect:
```json
[{"drug":"Aspirin","dosage":"1 tablet daily","doctorName":"Dr. Julia Steiner","issuedDate":"2026-06-01","status":"OPEN"},{"drug":"Paracetamol","dosage":"500mg every 6 hours as needed","doctorName":"Dr. Marie Huber","issuedDate":"2026-06-10","status":"REDEEMED"}]
```

```powershell
try {
    Invoke-WebRequest http://localhost:8080/api/oegk/0000000000/prescriptions
} catch {
    Write-Output "STATUS=$($_.Exception.Response.StatusCode.value__)"
}
```
Expect: `STATUS=404`.

Then check Swagger UI in a browser: `http://localhost:8080/swagger-ui/index.html` — should show "ÖGK prescriptions" with the one GET endpoint, documenting both the 200 and 404 responses.

For the detailed, one-piece-at-a-time version of the same checks (compiling alone, starting Postgres from scratch, what each step actually proves), see 4.1–4.4 below.

### 4.1 Compile

```powershell
& $mvn compile
```
Expect: `BUILD SUCCESS`.

### 4.2 Run the automated test suite (the reliable check)

```powershell
& $mvn test
```
Expect: `Tests run: 10, Failures: 0, Errors: 0` and `BUILD SUCCESS`.

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

### 4.4 Verifying Phase 2A (pharmacy locator, drug catalog, availability check)

Same "app running for real" setup as 4.3 (`docker compose up -d`, `& $mvn spring-boot:run` in one terminal), then run these in a **second** terminal. Coordinates below (`48.2082, 16.3719`) are central Vienna, near the seeded "Apotheke Zum Goldenen Loewen".

```powershell
Invoke-WebRequest http://localhost:8080/api/patients | Select-Object -ExpandProperty Content
```
Expect: the 4 seeded demo patients (id, svnr, name).

```powershell
Invoke-WebRequest "http://localhost:8080/api/drugs/search?q=Paracetamol" | Select-Object -ExpandProperty Content
```
Expect: multiple catalog rows (different pack sizes/manufacturers), not just one.

```powershell
Invoke-WebRequest http://localhost:8080/api/drugs/1 | Select-Object -ExpandProperty Content
Invoke-WebRequest http://localhost:8080/api/drugs/1/leaflet | Select-Object -ExpandProperty Content
```
Expect: Aspirin's catalog detail, then its simulated leaflet text.

```powershell
Invoke-WebRequest "http://localhost:8080/api/pharmacies/nearby?lat=48.2082&lng=16.3719&radiusKm=10" | Select-Object -ExpandProperty Content
```
Expect: all 8 seeded pharmacies, sorted by `distanceKm` ascending.

```powershell
Invoke-WebRequest "http://localhost:8080/api/pharmacies/on-call?lat=48.2082&lng=16.3719" | Select-Object -ExpandProperty Content
```
Expect: at least one pharmacy, regardless of what day/time it is now — the seeded on-call rotation covers every hour of every day.

```powershell
Invoke-WebRequest http://localhost:8080/api/pharmacies/1 | Select-Object -ExpandProperty Content
```
Expect: pharmacy detail with its full sorted weekly + on-call hours list.

```powershell
Invoke-WebRequest "http://localhost:8080/api/pharmacies/geocode?address=Stephansplatz%201%20Wien" | Select-Object -ExpandProperty Content
```
Expect: a real lat/lng from the live Nominatim API (this is the one real external network call in the project — requires internet access, and is slower than the others).

```powershell
$body = @{ items = @(@{ drugId = 3; quantity = 1 }, @{ drugId = 1; quantity = 2 }); latitude = 48.2082; longitude = 16.3719; radiusKm = 10 } | ConvertTo-Json -Depth 5
Invoke-WebRequest -Uri http://localhost:8080/api/availability/check -Method POST -Body $body -ContentType "application/json" | Select-Object -ExpandProperty Content
```
Expect: one entry per nearby pharmacy, each with an `allAvailable` flag and a 2-item breakdown (drug id 3 = Paracetamol, drug id 1 = Aspirin).

```powershell
try { Invoke-WebRequest http://localhost:8080/api/pharmacies/9999 } catch { Write-Output "STATUS=$($_.Exception.Response.StatusCode.value__)" }
```
Expect: `STATUS=404`.

Then check Swagger UI (`http://localhost:8080/swagger-ui/index.html`) — current full tag list (grows as later phases land; see §4.5+ below): ÖGK prescriptions, Patients, Drugs, Pharmacies, Availability, Favorites, Reservations, Medication schedule.

### 4.5 Verifying Phase 2B (pricing & comparison)

Same setup as 4.3/4.4.

```powershell
Invoke-WebRequest "http://localhost:8080/api/drugs/1/compare?lat=48.2082&lng=16.3719&radiusKm=10" | Select-Object -ExpandProperty Content
```
Expect: 8 entries, cheapest `VitaNova-Apotheke` at `4.30`, priciest `Apotheke Zur Alten Muehle` at `4.75` — Aspirin is OTC, so price genuinely varies by pharmacy.

```powershell
Invoke-WebRequest "http://localhost:8080/api/drugs/4/compare?lat=48.2082&lng=16.3719&radiusKm=10" | Select-Object -ExpandProperty Content
```
Expect: 6 entries (2 of the 8 pharmacies don't carry Amoxicillin at all, so they're absent from the list, not shown as unavailable), every `price` is `7.55` (the flat Rezeptgebühr, since it's prescription-required — unlike Aspirin, the price does *not* vary by pharmacy), and `Stadtapotheke Wien-Mitte`'s entry shows `"inStock":false` while the rest show `true`.

```powershell
try { Invoke-WebRequest "http://localhost:8080/api/drugs/999999/compare?lat=48.2082&lng=16.3719" } catch { Write-Output "STATUS=$($_.Exception.Response.StatusCode.value__)" }
```
Expect: `STATUS=404`.

Swagger UI should show `/compare` listed under the existing **Drugs** tag (no new tag — this phase only added an endpoint to an existing controller).

### 4.6 Verifying Phase 2C (favorites & reservations)

Same setup as 4.3/4.4. Patient: Anna Gruber (`1234010190`). Pharmacy id `1` ("Apotheke Zum Goldenen Loewen") supports reservations; pharmacy id `4` ("Apotheke Zur Blauen Schwalbe") doesn't — used below to exercise the rejection case.

Favorite pharmacies:
```powershell
Invoke-WebRequest "http://localhost:8080/api/patients/1234010190/favorite-pharmacies" | Select-Object -ExpandProperty Content
```
Expect: `[]`.

```powershell
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/patients/1234010190/favorite-pharmacies/1" -Method POST
Write-Output "STATUS=$($response.StatusCode)"
```
Expect: `STATUS=204`.

```powershell
Invoke-WebRequest "http://localhost:8080/api/patients/1234010190/favorite-pharmacies" | Select-Object -ExpandProperty Content
```
Expect: one entry, "Apotheke Zum Goldenen Loewen".

```powershell
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/patients/1234010190/favorite-pharmacies/1" -Method POST
Write-Output "STATUS=$($response.StatusCode)"
```
Expect: `STATUS=204` again — re-favoriting the same pharmacy is idempotent, not an error and not a duplicate.

```powershell
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/patients/1234010190/favorite-pharmacies/1" -Method DELETE
Write-Output "STATUS=$($response.StatusCode)"
Invoke-WebRequest "http://localhost:8080/api/patients/1234010190/favorite-pharmacies" | Select-Object -ExpandProperty Content
```
Expect: `STATUS=204`, then `[]` again.

Favorite drugs (same pattern, briefer):
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/patients/1234010190/favorite-drugs/1" -Method POST
Invoke-WebRequest "http://localhost:8080/api/patients/1234010190/favorite-drugs" | Select-Object -ExpandProperty Content
Invoke-WebRequest -Uri "http://localhost:8080/api/patients/1234010190/favorite-drugs/1" -Method DELETE
```
Expect: after the `POST`, the list shows Aspirin; after the `DELETE` it's empty again.

Reservations — create one, noting the returned `id` for the cancel step below:
```powershell
$body = @{ pharmacyId = 1; drugId = 1; quantity = 2 } | ConvertTo-Json
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/patients/1234010190/reservations" -Method POST -Body $body -ContentType "application/json"
Write-Output "STATUS=$($response.StatusCode)"
Write-Output $response.Content
```
Expect: `STATUS=200`, body shows `"status":"REQUESTED"` at "Apotheke Zum Goldenen Loewen".

```powershell
Invoke-WebRequest "http://localhost:8080/api/patients/1234010190/reservations" | Select-Object -ExpandProperty Content
```
Expect: the reservation just created.

Rejected at a pharmacy that doesn't support reservations:
```powershell
$body = @{ pharmacyId = 4; drugId = 1; quantity = 1 } | ConvertTo-Json
try {
    Invoke-WebRequest -Uri "http://localhost:8080/api/patients/1234010190/reservations" -Method POST -Body $body -ContentType "application/json"
} catch {
    Write-Output "STATUS=$($_.Exception.Response.StatusCode.value__)"
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    Write-Output "BODY=$($reader.ReadToEnd())"
}
```
Expect: `STATUS=400`, body mentioning "Apotheke Zur Blauen Schwalbe".

Cancel the reservation created above:
```powershell
$reservationId = 1   # set this to the id the create response actually returned, not a placeholder
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/patients/1234010190/reservations/$reservationId" -Method DELETE
Write-Output "STATUS=$($response.StatusCode)"
Invoke-WebRequest "http://localhost:8080/api/patients/1234010190/reservations" | Select-Object -ExpandProperty Content
```
Expect: `STATUS=204`; the reservation is still listed, now with `"status":"CANCELLED"` (cancelling keeps history, doesn't delete the row).

Error cases:
```powershell
try { Invoke-WebRequest "http://localhost:8080/api/patients/0000000000/favorite-pharmacies" } catch { Write-Output "STATUS=$($_.Exception.Response.StatusCode.value__)" }
try { Invoke-WebRequest -Uri "http://localhost:8080/api/patients/1234010190/favorite-pharmacies/9999" -Method POST } catch { Write-Output "STATUS=$($_.Exception.Response.StatusCode.value__)" }
```
Expect: both `STATUS=404`.

Swagger UI should show new **Favorites** and **Reservations** tags.

### 4.7 Verifying Phase 2D (medication schedule / adherence tracking)

Same setup as 4.3/4.4. Patients: Anna Gruber (`1234010190`, once-daily Aspirin, 3 days of seeded history), Max Bauer (`2345020285`, twice-daily Metformin), Paul Wagner (`4567040475`, no schedule at all — the empty-state case).

```powershell
Invoke-WebRequest "http://localhost:8080/api/patients/1234010190/medication-schedule" | Select-Object -ExpandProperty Content
Invoke-WebRequest "http://localhost:8080/api/patients/4567040475/medication-schedule" | Select-Object -ExpandProperty Content
```
Expect: Anna Gruber shows one schedule (Aspirin, "1 Tablette", one `08:00:00` time); Paul Wagner returns `[]`.

```powershell
Invoke-WebRequest "http://localhost:8080/api/patients/1234010190/medication-schedule/due-today" | Select-Object -ExpandProperty Content
Invoke-WebRequest "http://localhost:8080/api/patients/2345020285/medication-schedule/due-today" | Select-Object -ExpandProperty Content
```
Expect: Anna Gruber shows one entry, `"status":"PENDING"` (the seeded history only covers 3 earlier days, not today). Max Bauer shows **two** entries (`08:00:00` and `20:00:00`), both `PENDING` — confirms the twice-daily case works.

Confirm today's dose — use the `scheduleTimeId` from Anna Gruber's due-today response above (`1` on a freshly migrated database):
```powershell
$body = @{ status = "TAKEN" } | ConvertTo-Json
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/patients/1234010190/medication-schedule/1/intake" -Method POST -Body $body -ContentType "application/json"
Write-Output "STATUS=$($response.StatusCode)"
Write-Output $response.Content
```
Expect: `STATUS=200`, body shows `"status":"TAKEN"` with a real `confirmedAt`.

Prove it's an upsert, not a duplicate (change your mind — same slot, same day):
```powershell
$body = @{ status = "SKIPPED" } | ConvertTo-Json
Invoke-WebRequest -Uri "http://localhost:8080/api/patients/1234010190/medication-schedule/1/intake" -Method POST -Body $body -ContentType "application/json" | Select-Object -ExpandProperty Content
Invoke-WebRequest "http://localhost:8080/api/patients/1234010190/medication-schedule/due-today" | Select-Object -ExpandProperty Content
```
Expect: both show `"status":"SKIPPED"` — the same entry updated in place, not a second one.

Create a new schedule (Paul Wagner, drug id 2 = Nurofen), note the generated id, then deactivate it:
```powershell
$body = @{ drugId = 2; doseText = "1 Tablette"; startDate = "2026-07-28"; endDate = $null; times = @("12:00:00") } | ConvertTo-Json
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/patients/4567040475/medication-schedule" -Method POST -Body $body -ContentType "application/json"
Write-Output "STATUS=$($response.StatusCode)"
Write-Output $response.Content
```
Expect: `STATUS=200`, a new schedule with a generated `id` in the body — read it from the output before the next step.

```powershell
$scheduleId = 4   # set this to the id the previous response actually returned, not a placeholder
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/patients/4567040475/medication-schedule/$scheduleId" -Method DELETE
Write-Output "STATUS=$($response.StatusCode)"
Invoke-WebRequest "http://localhost:8080/api/patients/4567040475/medication-schedule" | Select-Object -ExpandProperty Content
Invoke-WebRequest "http://localhost:8080/api/patients/4567040475/medication-schedule/due-today" | Select-Object -ExpandProperty Content
```
Expect: `STATUS=204`; the schedule list still shows it, now `"active":false` (soft-stop, not a delete — same pattern as `ReservationService.cancelReservation`); due-today no longer includes it.

Error cases:
```powershell
try { Invoke-WebRequest "http://localhost:8080/api/patients/0000000000/medication-schedule" } catch { Write-Output "STATUS=$($_.Exception.Response.StatusCode.value__)" }
$body = @{ status = "NOT_A_REAL_STATUS" } | ConvertTo-Json
try { Invoke-WebRequest -Uri "http://localhost:8080/api/patients/1234010190/medication-schedule/1/intake" -Method POST -Body $body -ContentType "application/json" } catch { Write-Output "STATUS=$($_.Exception.Response.StatusCode.value__)" }
```
Expect: `STATUS=404` for the unknown patient, `STATUS=400` for the invalid status string.

Look at the seeded intake history directly:
```powershell
$env:PGPASSWORD = "medtrack"
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -h localhost -p 5434 -U medtrack -d medtrack -c "SELECT scheduled_date, status, confirmed_at FROM medication_intake_log ORDER BY scheduled_date;"
```
Expect: the 3 seeded rows (07-25 TAKEN, 07-26 SKIPPED, 07-27 TAKEN) plus whatever you confirmed above for today.

### 4.8 Verifying the registration flow (Phase 2G — mock ID Austria + e-card, hardened with real registry verification and credential-based login)

Unlike 4.4–4.7, this feature has a real frontend UI built around it, not just a REST API — the table below is primarily for driving that UI by hand, with the equivalent raw API calls underneath for backend-only checks.

**Registration is only possible through ID Austria — there is no standalone email/password signup.** The UI lives in `frontend/src/pages/AuthModal.tsx` (the Anmelden/Registrieren overlay) and `frontend/src/pages/AccountSheet.tsx` (the "Mein Konto" account menu, reachable from the header's account icon). Neither is ever forced open on load — App.tsx opens them only from a deliberate action: the header's account button, the "not linked" banner's "ID Austria verknüpfen" button, or the "Anmelden" row inside Mein Konto. Inside the modal:
- **Registrieren** only offers "Mit ID Austria" (the inner method toggle is hidden — there is no choice to make): name + birthdate → e-card scan/confirm, where the submitted SVNR + 6 other fields are compared against the (mock) **ID-Austria registry** (`id_austria_registry` table, migration `V11__id_austria_registry.sql`, only ever read by `MockIdAustriaAuthAdapter`/`MockEHealthCardAdapter` — simulating that MedTrack has no direct access to ID Austria's own data). A wrong SVNR or any other mismatched field (name, birthdate, card serial, carrier number, insurer, expiry) is rejected with a specific message naming what's wrong, not silently accepted. Only once every field matches does the wizard move to a final **"Konto abschließen"** step, where you set the email + password that becomes this account's Standard-login credential (`POST /api/registration/complete-registration`). Attempting to register an identity that already has an account (a `Patient` row with `password_hash` set) is rejected with a 409 "already exists" alert.
- **Anmelden** offers both **Mit ID Austria** (same registry verification; if the identity already has an account it logs straight in via `POST /api/registration/login-with-id-austria`, no password needed — if not, it tells you to register first) and **Standard**: Vorname, Nachname, E-Mail, Passwort, checked for real against the account created during registration (`POST /api/registration/standard-login`, BCrypt-hashed `patient.password_hash`). Unknown email → 404 "no such user"; known email but wrong name/password → 401 generic "wrong credentials" (deliberately not field-specific, unlike registration).

**Demo data for all four seeded patients.** The ID-Austria registry holds every field a real e-card would carry back, seeded verbatim by `V11__id_austria_registry.sql` (not computed on the fly anymore):

| Patient | MedTrack-ID | SVNR | Vorname | Nachname | Geburtsdatum | Kennnummer der Karte | Kennnummer des Trägers | Versicherung | Ablaufdatum |
|---|---|---|---|---|---|---|---|---|---|
| Anna Gruber | `MT-10000001` | `1234010190` | Anna | Gruber | 01/01/1990 | `12340101900910104321` | `4711` | ÖGK | 31/12/2031 |
| Max Bauer | `MT-10000002` | `2345020285` | Max | Bauer | 02/02/1985 | `23450202855820205432` | `4711` | ÖGK | 31/12/2031 |
| Lena Hofer | `MT-10000003` | `3456030380` | Lena | Hofer | 03/03/1980 | `34560303800830306543` | `4711` | ÖGK | 31/12/2031 |
| Paul Wagner | `MT-10000004` | `4567040475` | Paul | Wagner | 04/04/1975 | `45670404755740407654` | `4711` | ÖGK | 31/12/2031 |

Three of the four (Anna, Max, Lena — **not Paul**) are also **pre-registered example accounts**: `DemoAccountSeeder` (an idempotent `ApplicationRunner`) fills in a demo email + password for them at startup, so Standard login works immediately without registering first. See `docs/TEST_CREDENTIALS.md` for the exact emails/password. Paul Wagner is deliberately left without credentials — he's the one seeded identity meant for manually walking through the full registration flow (verify → set credentials).

Golden path — register Paul Wagner (the one seeded identity without credentials yet):
1. Click the account icon in the header to open the overlay, switch to "Registrieren" (only "Mit ID Austria" is offered).
2. Enter Vorname `Paul`, Nachname `Wagner`, Geburtsdatum `04/04/1975`, submit.
3. On the e-card step, use "Kein Foto zur Hand? Werte stattdessen manuell eingeben" and type the rest of Paul's row above into the confirm form exactly.
4. Confirm. Expect: no error, advances to "Konto abschließen" — set an email + password.
5. Submit. Expect: lands on the Home dashboard, header shows "Paul Wagner", no "not linked" banner.

Standard login path — log in as Anna Gruber with her pre-seeded demo credentials (see `docs/TEST_CREDENTIALS.md`):
1. Open the overlay, "Anmelden" + "Standard".
2. Enter Vorname `Anna`, Nachname `Gruber`, her demo E-Mail and Passwort, submit.
3. Expect: lands in the app as Anna, header shows "Anna Gruber" — the "not linked" banner **is** showing, since Standard login never sets `isLinked`.

Deliberate error cases:
1. **Wrong SVNR during registration**: on the confirm step, use an SVNR that isn't any seeded patient's (but still encodes the asserted birthdate, so the client-side check doesn't intercept it first — e.g. `9999040475` for a `04/04/1975` birthdate). Expect: *"Es wurde keine ID-Austria-Identität mit der SVNR ... gefunden. Bitte überprüfen Sie die SVNR."*
2. **Field mismatch during registration**: use Anna's real SVNR `1234010190` but type a wrong Nachname or Ablaufdatum. Expect: *"Folgende Angaben stimmen nicht mit dem ID-Austria-Register überein: Nachname, Ablaufdatum"* (or whichever fields you changed) — every mismatch is listed, not just the first.
3. **Already registered**: try to register Anna Gruber's real data again (she already has an account). Expect: *"Für diese Identität existiert bereits ein MedTrack-Konto. Bitte melden Sie sich stattdessen an."*
4. **Wrong Standard-login password**: Anna's name/email with any password other than her demo one. Expect: *"Die eingegebenen Daten stimmen nicht mit dem gespeicherten Konto überein."*
5. **Unknown Standard-login email**: any name/email/password combination for an email nobody registered. Expect: *"Kein Konto mit dieser E-Mail gefunden..."*

Raw API equivalents, if you want to check the backend alone without the UI:
```powershell
$body = @{ fullName = "Anna Gruber"; dateOfBirth = "1990-01-01" } | ConvertTo-Json
Invoke-WebRequest -Uri "http://localhost:8080/api/registration/id-austria-login" -Method POST -Body $body -ContentType "application/json" | Select-Object -ExpandProperty Content
```
Expect: `200`, echoes back the same name/birthdate plus a fresh `authenticatedAt` - this step always succeeds, it's just the wizard's entry point; the real check is verify-identity below.

```powershell
$body = @{ svnr = "1234010190"; firstName = "Anna"; lastName = "Gruber"; dateOfBirth = "1990-01-01"; cardSerialNumber = "12340101900910104321"; carrierNumber = "4711"; carrierName = "ÖGK"; expiryDate = "2031-12-31" } | ConvertTo-Json
Invoke-WebRequest -Uri "http://localhost:8080/api/registration/verify-identity" -Method POST -Body $body -ContentType "application/json; charset=utf-8" | Select-Object -ExpandProperty Content
```
Expect: `200`, `{"verified": {...}, "alreadyRegistered": true}` for Anna (already has a demo account).

```powershell
$body = @{ firstName = "Anna"; lastName = "Gruber"; email = "anna.gruber@demo.medtrack.local"; password = "MedTrack2026!" } | ConvertTo-Json
Invoke-WebRequest -Uri "http://localhost:8080/api/registration/standard-login" -Method POST -Body $body -ContentType "application/json" | Select-Object -ExpandProperty Content

$body = @{ firstName = "Anna"; lastName = "Gruber"; email = "anna.gruber@demo.medtrack.local"; password = "wrong" } | ConvertTo-Json
try { Invoke-WebRequest -Uri "http://localhost:8080/api/registration/standard-login" -Method POST -Body $body -ContentType "application/json" } catch { Write-Output "STATUS=$($_.Exception.Response.StatusCode.value__)" }
```
Expect: the first returns Anna's `AccountResponse`; the second returns `STATUS=401` (see `docs/TEST_CREDENTIALS.md` if this password has changed).

```powershell
Invoke-WebRequest "http://localhost:8080/api/patients" | Select-Object -ExpandProperty Content
```
Expect: all patients including the four seeded ones, each with a `medtrackId` matching the table above — `patient` rows are untouched by this feature; only their `email`/`password_hash` are new.

### 4.9 Admin mode (patient switcher access)

Switching between patients via the header dropdown is an admin-only capability - everyone else gets a plain read-only display of their own current name (`frontend/src/layout/PatientSelector.tsx`), with no dropdown and no way to discover that other patients even exist. This is a demo/testing convenience, not a real authorization system: it doesn't gate the API in any way (every endpoint above already works for any SVNR you pass it), it only controls whether the *frontend* shows the switcher.

**Admin access code: `MEDTRACK-ADMIN-2026`** (`MockAdminAuthAdapter.ACCESS_CODE`). Like every other mock credential in this project, it's intentionally public - documented here so anyone testing MedTrack can use it, not meant to imply real security.

To activate it: open "Mein Konto" (the account icon in the header) → "Admin-Zugang" → enter the code above → "Bestätigen". The header's patient dropdown appears immediately, no reload needed. To deactivate: open "Mein Konto" again → "Admin-Modus aktiv - Tippen zum Beenden". The flag is purely a local `localStorage` value (`medtrack.isAdmin`) - it doesn't survive `localStorage.clear()` and isn't tied to whichever patient is currently selected.

Raw API check:
```powershell
$body = @{ code = "MEDTRACK-ADMIN-2026" } | ConvertTo-Json
Invoke-WebRequest -Uri "http://localhost:8080/api/admin/verify-code" -Method POST -Body $body -ContentType "application/json"

$body = @{ code = "wrong-code" } | ConvertTo-Json
try { Invoke-WebRequest -Uri "http://localhost:8080/api/admin/verify-code" -Method POST -Body $body -ContentType "application/json" } catch { Write-Output "STATUS=$($_.Exception.Response.StatusCode.value__)" }
```
Expect: `204` for the correct code, `STATUS=401` for anything else. No session or token is returned either way - see `AdminController`.

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
| 5434 | Local Postgres (docker-compose) — **not** the Postgres default 5432. Persists across sessions; stays up until you `docker compose down`. |
| 8080 | The Spring Boot app, default. Override with `$env:SERVER_PORT = "8081"` beforehand, or `& $mvn spring-boot:run` with `-Dspring-boot.run.arguments=--server.port=8081`, if it's occupied and you don't want to deal with whatever's on it. |
| *(random, different every run)* | Postgres during `mvn test`. Testcontainers always picks a free port automatically — this is why test runs never collide with 5434 or with each other, even run back to back. |

**Why 5434 and not 5432:** this machine already runs two native PostgreSQL Windows services unrelated to this project — PostgreSQL 13 on port 5432, PostgreSQL 17 on port 5433 (a second PostgreSQL version installed via the EDB installer defaults to the next free port). If MedTrack's container tried to use either of those ports, connections would go to the wrong database and fail with a *misleading* `password authentication failed` error instead of a clear "connection refused." Testcontainers-based tests were never affected, since they always pick a random free port automatically.

**Why 8080 keeps needing troubleshooting:** unlike the database port above, 8080 isn't fighting a *permanent* service — it's fighting **leftover copies of this same app** that were started at some point and never explicitly stopped. See the Troubleshooting section below for how to recognize and clear this.

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

**A new endpoint 404s, or the app otherwise seems to ignore code you just changed**
This almost always means you're actually talking to an **old copy of the app that's still running from an earlier session**, not your latest code. `mvn spring-boot:run` (and any `java -jar ...`) keeps running in the background indefinitely once started — closing the terminal window doesn't reliably kill it, an IDE stop button not being clicked doesn't either, and it happily survives across completely unrelated work days later. Meanwhile, endpoints that exist in *every* version of the app (`/actuator/health` above all) keep responding normally the whole time, which is exactly what makes a stale process look perfectly healthy right up until you hit something new.

Check what's actually bound to the port, and — critically — *when it started*:
```powershell
$conn = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($conn) { Get-Process -Id $conn.OwningProcess | Select-Object Id, ProcessName, StartTime }
```
If `StartTime` predates your latest code change, that's your answer — you're talking to stale code, no matter how fine everything else looks.

If a request 404s and you're not sure why, check the *response body*, not just the status code — PowerShell's `Invoke-WebRequest` hides the body by default on any non-2xx response:
```powershell
try {
    Invoke-WebRequest http://localhost:8080/some/endpoint
} catch {
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    Write-Output "STATUS=$($_.Exception.Response.StatusCode.value__)"
    Write-Output "BODY=$($reader.ReadToEnd())"
}
```
A body like `{"timestamp":...,"status":404,"error":"Not Found","path":"..."}` means Spring found **no matching route at all** — a stale build (most likely) or a genuine routing bug. A completely *empty* 404 body means the route matched fine and the app's own code (`ApiExceptionHandler`, for `PatientNotFoundException`) deliberately returned 404 for a business reason, e.g. an unknown SVNR.

To fix it: stop the old process — `Ctrl+C` in whichever terminal is actually running it, or IntelliJ's stop button if that's how it was launched. If you don't know where it's running:
```powershell
$conn = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($conn) { Stop-Process -Id $conn.OwningProcess -Force }
```
A full machine restart also clears this unconditionally, since no background process survives a reboot — but it's overkill for just this.

**`mvn spring-boot:run` fails fast with a generic `MojoExecutionException` / `Process terminated with exit code: 1`, and the pasted output doesn't show why**
This is just Maven's own summary line — the real cause is a Spring Boot exception earlier in the *same* console output, easy to miss if only the tail got pasted or piped through something like `Select-Object -Last N`. Two distinct causes produce this identical-looking failure:

1. **Port 8080 already bound** (see the stale-process entry above) — Tomcat fails to bind and the app exits within a couple seconds.
2. **A Flyway migration is actually broken** (bad SQL, a bad column reference, etc.) — fails during context startup, also within a couple seconds.

The fastest way to tell them apart: run `& $mvn test` instead. It uses Testcontainers, which always picks a random free port, so it's immune to cause #1 — if `mvn test` also fails, the problem is a genuine bug in the code/migrations, not a local port collision, and the real Postgres error (Flyway prints the failing SQL, the exact error, and the line number) will be in that output instead of buried under a generic Maven summary.

**A `$response = Invoke-WebRequest ...` command in one of the verification sections above "succeeds" with a status code that doesn't match what you expected**
Two easy mistakes cause this, both worth checking before assuming the API is wrong:
1. **A placeholder in the URL was never replaced with a real value.** `<` and `>` aren't valid URL characters, so a literal `.../medication-schedule/<scheduleId>` fails immediately — but since the failure happens *before* `$response` gets reassigned, `$response` still holds whatever it held from the last successful call. Checking `$response.StatusCode` afterward then silently shows that old, unrelated result instead of an error. Always substitute the actual id/value from a previous response's body, not a bracketed placeholder — the verification sections above use a `$scheduleId = 4  # set this to the id...` pattern specifically to make this harder to miss.
2. **`Select-Object -ExpandProperty Content` only prints the response body, never the status code.** If a command in this doc doesn't split into `$response = ...` first, the *absence* of a thrown error is your proof of success (2xx), not any visible "200" text — `Invoke-WebRequest` throws on any non-2xx response. If you want the code printed explicitly, assign the response to a variable first and print `$response.StatusCode` separately, as most snippets in §4.5 do.

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
