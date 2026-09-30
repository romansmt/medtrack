# Test credentials

Every value on this page is fake, generated, or hardcoded demo data for the MedTrack portfolio
project - there is no real person, no real government ID, and nothing here needs to be kept secret.
It's collected in one place so you don't have to dig through `docs/DEVELOPMENT.md` §4.8/§4.9 to find
a specific login. For *how* each flow works, what each field means, and raw API equivalents, see
those sections - this page is just the lookup table.

## Admin access code

Unlocks the patient switcher in the header (see [DEVELOPMENT.md §4.9](DEVELOPMENT.md)) - without it,
every account below can only ever see its own data.

| Access code |
|---|
| `MEDTRACK-ADMIN-2026` |

How to use it: header account icon → "Mein Konto" → "Admin-Zugang" → enter the code → "Bestätigen".
Toggle it off again the same way ("Admin-Modus aktiv - Tippen zum Beenden"). It's a local browser
flag only (`localStorage["medtrack.isAdmin"]`) - it doesn't gate the backend API itself, only whether
the frontend shows the switcher.

## Patients - original seeded demo data

These four are created by `V2__seed_demo_data.sql` and always exist on a freshly migrated database -
unlike everything further down this page, they aren't specific to this dev database and will still be
here after a `flyway clean` + re-migrate. Their official identity data (SVNR, card fields) additionally
lives in the separate ID-Austria registry (`V11__id_austria_registry.sql`), which registration/login
verification checks against - log in via "Mit ID Austria" (Anmelden or Registrieren tab, either works)
with the Vorname/Nachname/Geburtsdatum columns, then scan/enter the rest on the confirm screen.

| Patient | MedTrack-ID | SVNR | Vorname | Nachname | Geburtsdatum | Kennnummer der Karte | Kennnummer des Trägers | Versicherung | Ablaufdatum |
|---|---|---|---|---|---|---|---|---|---|
| Anna Gruber | `MT-10000001` | `1234010190` | Anna | Gruber | 01/01/1990 | `12340101900910104321` | `4711` | ÖGK | 31/12/2031 |
| Max Bauer | `MT-10000002` | `2345020285` | Max | Bauer | 02/02/1985 | `23450202855820205432` | `4711` | ÖGK | 31/12/2031 |
| Lena Hofer | `MT-10000003` | `3456030380` | Lena | Hofer | 03/03/1980 | `34560303800830306543` | `4711` | ÖGK | 31/12/2031 |
| Paul Wagner | `MT-10000004` | `4567040475` | Paul | Wagner | 04/04/1975 | `45670404755740407654` | `4711` | ÖGK | 31/12/2031 |

See DEVELOPMENT.md §4.8 for exactly how registration/login verification uses this table now (a real
comparison against `id_austria_registry`, not just an SVNR-birthdate consistency check).

### Pre-registered demo accounts (Standard login)

Three of the four - **not Paul Wagner** - already have a MedTrack account (email + password), seeded
idempotently at every app startup by `DemoAccountSeeder` so Standard login works immediately. Same
"fake and intentionally public" spirit as the admin access code below.

| Patient | E-Mail | Passwort |
|---|---|---|
| Anna Gruber | `anna.gruber@demo.medtrack.local` | `MedTrack2026!` |
| Max Bauer | `max.bauer@demo.medtrack.local` | `MedTrack2026!` |
| Lena Hofer | `lena.hofer@demo.medtrack.local` | `MedTrack2026!` |

**Paul Wagner has no password set** - he's deliberately left as the one seeded identity for manually
walking through the full registration flow (ID-Austria verification, then set your own email/password
on the "Konto abschließen" step). Once you register him, Standard login works for him too.

## Patients - created ad hoc in this dev database (pre-dates the registry hardening)

These were created by registering through the running app before registration required a real
ID-Austria registry match - they are **not** part of any migration and only exist in *this* database.
**None of these can log in anymore**: none of them has an `id_austria_registry` row (so "Mit ID
Austria" always fails with "wrong SVNR"), and none has a `password_hash` set (so Standard login always
fails with "wrong credentials", even Julia/Stefan whose email is still on file). Left here only as a
historical record of dev-database drift, not as working test accounts.

| Patient | MedTrack-ID | SVNR | Geburtsdatum | Originally registered via | Email |
|---|---|---|---|---|---|
| John Smith | `MT-10000005` | `3863150695` | 15/06/1995 | Mit ID Austria (invented SVNR, pre-registry) | none |
| Test Tester | `MT-10000006` | `5872200700` | 20/07/2000 | Mit ID Austria (invented SVNR, pre-registry) | none |
| Julia Novak | `MT-10000007` | `9580150595` | 15/05/1995 | Standard registration (path since removed) | `julia.novak@example.com` |
| Stefan Huber | `MT-10000008` | `8661100388` | 10/03/1988 | Standard registration (path since removed) | `stefan.huber@example.com` |

## Registering your own

Unlike before, you can **not** register an arbitrary invented name/birthdate anymore - registration
now requires a real match against the ID-Austria registry (`id_austria_registry`, seeded by
`V11__id_austria_registry.sql`), which only contains the four patients in the table above. **Paul
Wagner** is the one with no password set yet, so he's the one to use for a fresh end-to-end
registration walkthrough. See DEVELOPMENT.md §4.8 for the full golden path and the deliberate-error
(wrong SVNR / mismatched field) test cases.
