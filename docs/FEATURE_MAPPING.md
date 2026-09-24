# Feature mapping: ApoScout/ApoApp → MedTrack

MedTrack's Phase 2 ("ApoScout + ApoApp feature build", see [TASKS.md](TASKS.md)) combines the functionality of two real Austrian pharmacy apps into one. This table is the fast lookup for "where does feature X live in this codebase" - use it before searching, not after.

| Reference feature | Backend (package `com.medtrack.*`) | Frontend route / component |
|---|---|---|
| Apotheken in der Nähe finden (pharmacy locator) | `application.service.PharmacyService`, `infrastructure.geo.NominatimGeoAdapter` (`GeoPort`) | [`/pharmacies`](../frontend/src/pages/PharmaciesPage.tsx), Home's [`NextOpenPharmacyCard`](../frontend/src/components/NextOpenPharmacyCard.tsx) |
| Nachtdienste & Bereitschaftsdienste (on-call/emergency duty) | `PharmacyService.findOnCallNow` → `GET /api/pharmacies/on-call` | `PharmaciesPage`'s "Nur Bereitschaftsdienst" filter |
| Standort festlegen (manual location search) | `PharmacyService.geocode` → `GET /api/pharmacies/geocode` (real Nominatim/OSM call, not mocked) | [`LocationPicker`](../frontend/src/components/LocationPicker.tsx) + [`UserLocationContext`](../frontend/src/context/UserLocationContext.tsx) |
| Verfügbarkeit von Medikamenten (multi-item availability check) | `application.service.AvailabilityService` → `POST /api/availability/check` | [`/search`](../frontend/src/pages/SearchPage.tsx) |
| Medikamentensuche (drug catalog search) | `application.service.DrugService.search` → `GET /api/drugs/search` | [`DrugSearchCombobox`](../frontend/src/components/DrugSearchCombobox.tsx) (used on `/search` and `/medication-plan`) |
| Preisvergleich (cross-pharmacy price comparison) | `application.service.ComparisonService` + `PricingService` → `GET /api/drugs/{id}/compare` | [`/drugs/:drugId/compare`](../frontend/src/pages/PriceComparisonPage.tsx) |
| Reservierung (reserve a medication at a pharmacy) | `application.service.ReservationService` | Reserve buttons on `/search`'s results, listed/cancelled at [`/reservations`](../frontend/src/pages/ReservationsPage.tsx) |
| Favoriten (favorite pharmacies/medications) | `application.service.FavoriteService` | [`/favorites`](../frontend/src/pages/FavoritesPage.tsx), [`FavoriteButton`](../frontend/src/components/FavoriteButton.tsx) |
| Mein Einnahmeplan (medication plan / adherence tracking) | `application.service.MedicationScheduleService` | [`/medication-plan`](../frontend/src/pages/MedicationPlanPage.tsx) |
| Meine Rezepte (ÖGK prescriptions) | `application.service.PrescriptionService` (the original vertical slice, TASK-05–09) | [`/prescriptions`](../frontend/src/pages/PrescriptionsPage.tsx) |
| Sicherheit/Datenschutz-Einstieg (security/consent onboarding) | *(none - frontend-only, no data is actually collected)* | [`ConsentPage`](../frontend/src/pages/ConsentPage.tsx) |
| Demo-Patientenauswahl (stands in for real login) | `application.service.PatientService` → `GET /api/patients` | [`PatientSelector`](../frontend/src/layout/PatientSelector.tsx) + [`PatientContext`](../frontend/src/context/PatientContext.tsx) |

## Deliberately not implemented

These appear in the reference apps' screenshots but were intentionally left out - documented here so nobody re-investigates "is this missing by accident" from scratch:

- **Pollenflug (pollen forecast widget).** TASK-28's own DoD lists it as an explicit stretch goal. There is no backend data source or service for it anywhere in the architecture doc or `TASKS.md`, so building it would mean fabricating fake pollen numbers with nothing behind them - pure decoration, not a real feature. Skipped; see `docs/PROGRESS.md`'s TASK-28 entry.
- **Gesundheitsnachrichten (health news article feed).** Shown in the ApoApp reference screenshots' home screen but never appears in `TASKS.md`, the architecture doc's service list, or any backend controller. Out of scope entirely, not a cut corner.
- **Real ID Austria / e-card login.** Explicitly out of scope for the whole project (see `docs/DEVELOPMENT.md` / the architecture doc) - the demo patient selector stands in for it everywhere.

## How to use this table when something breaks

1. Find the reference feature (or its German label - the frontend's UI copy is German throughout, matching the reference apps) in the left column.
2. The backend column gives the exact service class and endpoint - start there if data looks wrong or an API call fails.
3. The frontend column links straight to the page or component - start there if the UI itself is broken, styled wrong, or a button doesn't work.
4. Check `docs/PROGRESS.md` for the task that built it - each entry documents *why* non-obvious decisions were made, which is usually faster than re-deriving the reasoning from the code alone.
