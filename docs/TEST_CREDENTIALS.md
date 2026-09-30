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
here after a `flyway clean` + re-migrate. Log in via "Mit ID Austria" (Anmelden or Registrieren tab,
either works) with the Vorname/Nachname/Geburtsdatum columns, then scan/enter the rest on the
confirm screen.

| Patient | MedTrack-ID | SVNR | Vorname | Nachname | Geburtsdatum | Kennnummer der Karte | Kennnummer des Trägers | Versicherung | Ablaufdatum |
|---|---|---|---|---|---|---|---|---|---|
| Anna Gruber | `MT-10000001` | `1234010190` | Anna | Gruber | 01/01/1990 | `12340101900910104321` | `4711` | ÖGK | 31/12/2031 |
| Max Bauer | `MT-10000002` | `2345020285` | Max | Bauer | 02/02/1985 | `23450202855820205432` | `4711` | ÖGK | 31/12/2031 |
| Lena Hofer | `MT-10000003` | `3456030380` | Lena | Hofer | 03/03/1980 | `34560303800830306543` | `4711` | ÖGK | 31/12/2031 |
| Paul Wagner | `MT-10000004` | `4567040475` | Paul | Wagner | 04/04/1975 | `45670404755740407654` | `4711` | ÖGK | 31/12/2031 |

Password/email: none - ID Austria accounts never have either. See DEVELOPMENT.md §4.8 for exactly how
each column is derived (SVNR encodes the birthdate; the card fields are computed on request, not
stored).

## Patients - created ad hoc in this dev database

These were created by registering through the running app while testing/building the feature (this
session and earlier ones) - they are **not** part of any migration. They only exist in *this*
database; a fresh clone or a `flyway clean` will not recreate them. Pulled live from
`GET /api/patients` and decoded the same way as the table above, so this reflects whatever is
actually in the database right now, not what was typed in at registration time.

| Patient | MedTrack-ID | SVNR | Geburtsdatum | Registered via | Email |
|---|---|---|---|---|---|
| John Smith | `MT-10000005` | `3863150695` | 15/06/1995 | Mit ID Austria (pre-dates the Standard path) | none |
| Test Tester | `MT-10000006` | `5872200700` | 20/07/2000 | Mit ID Austria ("Registrieren" tab) | none |
| Julia Novak | `MT-10000007` | `9580150595` | 15/05/1995 | Standard registration | `julia.novak@example.com` |
| Stefan Huber | `MT-10000008` | `8661100388` | 10/03/1988 | Standard registration | `stefan.huber@example.com` |

- **John Smith** and **Test Tester** were registered through "Mit ID Austria" - log in with Vorname/Nachname/Geburtsdatum exactly like the seeded four, then scan/enter their SVNR above.
- **Julia Novak** and **Stefan Huber** were registered through the **Standard** (email/password) path - neither is verified via ID Austria on the backend. Log in with just their email; **any password is accepted** (nothing is checked server-side, see DEVELOPMENT.md §4.8). Either can be upgraded via "ID Austria verknüpfen" using their name + the Geburtsdatum above. Note "linked" (the not-linked banner) is a per-browser `localStorage` flag, not something stored on the patient record - it resets for anyone logging in from a different browser/device regardless of what's been done here before.
- Card serial numbers for these four aren't listed since they're computed on the fly from the SVNR the same way as the seeded patients (`svnr + reverse(svnr)`, carrier `4711`/ÖGK, expiry = today + 5 years forced to 31 December) - see `MockEHealthCardAdapter.java` if you need one precomputed.

## Registering your own

Any name/birthdate not already in the tables above works for a fresh "Registrieren" - both paths are
open-ended, there's no fixed list of "valid" new accounts. See DEVELOPMENT.md §4.8 for the full golden
path and the deliberate-error (birthdate mismatch) test case.
