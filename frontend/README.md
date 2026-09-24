# MedTrack Frontend

## Stack

| | |
|---|---|
| Language | TypeScript |
| UI framework | React 19 |
| Build tool | Vite |
| Routing | React Router (`react-router-dom`) |
| Server state / data fetching | TanStack Query (`@tanstack/react-query`) |
| Maps | Leaflet + `react-leaflet` (used from TASK-29 onward) |
| Styling | Plain CSS with CSS custom properties for theming — no CSS framework, no CSS-in-JS |
| Backend | Spring Boot REST API at `../` (this repo's root), reached over CORS — see [`VITE_API_BASE_URL`](#environment-variables) below |

Desktop/laptop and mobile phone browsers are both first-class targets (not desktop-only) — every page must work at phone width (~375px) as well as laptop width. See [`AppShell.css`](src/layout/AppShell.css) for the responsive breakpoint (768px).

## Project structure

```
frontend/
├── src/
│   ├── api/                One file per backend resource, matching a controller 1:1 (e.g.
│   │                        pharmacies.ts <-> PharmacyController). Each exports typed
│   │                        use*Query/use*Mutation hooks (TanStack Query) wrapping client.ts.
│   │                        Mutations that change server state invalidate the relevant
│   │                        query keys on success.
│   │   ├── client.ts        Shared fetch wrapper (apiGet/apiSend) + ApiError.
│   │   ├── availability.ts  <-> AvailabilityController
│   │   ├── drugs.ts         <-> DrugController
│   │   ├── favorites.ts     <-> FavoriteController
│   │   ├── medicationSchedule.ts  <-> MedicationScheduleController
│   │   ├── patients.ts      <-> PatientController
│   │   ├── pharmacies.ts    <-> PharmacyController
│   │   ├── prescriptions.ts <-> PrescriptionController
│   │   └── reservations.ts  <-> ReservationController
│   ├── components/          Reusable UI shared across pages - not layout/nav (that's layout/)
│   │   │                    and not a whole route's own content (that's pages/).
│   │   ├── DrugSearchCombobox.tsx   Search-as-you-type drug picker (Search page, add-schedule flow).
│   │   ├── FavoriteButton.tsx       Heart-icon toggle; dumb (isFavorite + onToggle props).
│   │   ├── LocationPicker.tsx       Sets UserLocationContext via the geocode endpoint.
│   │   ├── NextOpenPharmacyCard.tsx
│   │   ├── PharmacyCard.tsx         The shared expandable pharmacy card - see below.
│   │   ├── PharmacyContactActions.tsx  Call/route/email/web links.
│   │   ├── PharmacyHours.tsx        Weekly + on-call hours, from PharmacyCard's detail fetch.
│   │   ├── PharmacyMap.tsx          react-leaflet wrapper; fixes Leaflet's marker-icon/bundler gotcha.
│   │   ├── QuickLinkTile.tsx
│   │   └── SearchBar.tsx
│   ├── context/              React context providers needed app-wide.
│   │   ├── PatientContext.tsx        Demo patient selector (no auth) - see TASKS.md TASK-26.
│   │   └── UserLocationContext.tsx   The user's chosen address/coordinates (TASK-28+).
│   │                                 Named useUserLocation, NOT useLocation - see Conventions.
│   ├── hooks/
│   │   └── useConsent.ts     localStorage-backed onboarding-seen flag. Not a context - only
│   │                         App.tsx reads/writes it.
│   ├── layout/                App shell: header, responsive nav, icons. Not page content.
│   │   ├── AppShell.tsx / .css
│   │   ├── Icon.tsx           The IconName union lives here - see "Allowed icon tags" below.
│   │   ├── navItems.ts        Single source of truth for nav routes/labels/icons.
│   │   └── PatientSelector.tsx
│   ├── pages/                  One component per route (wired up in App.tsx) - see "Routes" below.
│   │   └── onboardingSlides.ts  Data (not a component) for ConsentPage's 4-slide carousel.
│   ├── utils/
│   │   └── format.ts          germanDayName/formatTime/formatDistance/formatPrice/formatDateTime/
│   │                          formatDate. Backend LocalDate/LocalTime/Instant fields arrive as
│   │                          plain ISO strings - these only slice/relabel them, no Date parsing.
│   ├── App.tsx                 Route table + the consent gate.
│   ├── main.tsx                 Provider composition (QueryClient, BrowserRouter,
│   │                            PatientProvider, UserLocationProvider).
│   ├── theme.css                 CSS custom properties (design tokens) - see below.
│   └── index.css
├── .env.development           Committed, no secrets: dev-time VITE_API_BASE_URL default.
└── .env.example
```

## Routes

| Path | Page | Notes |
|---|---|---|
| `/` | `HomePage` | Search bar, location picker, quick-link tiles, next-open-pharmacy card |
| `/search` | `SearchPage` | Selection list + availability check + map split-view (TASK-29) |
| `/pharmacies` | `PharmaciesPage` | Nearby/on-call list + map + favorite toggle (TASK-30) |
| `/drugs/:drugId/compare` | `PriceComparisonPage` | Not in the nav - reached from a drug row on `/search` (TASK-31) |
| `/medication-plan` | `MedicationPlanPage` | Due-today, schedule list, add-schedule form (TASK-32) |
| `/favorites` | `FavoritesPage` | Apotheken/Medikamente tabs (TASK-33) |
| `/reservations` | `ReservationsPage` | TASK-33 |
| `/prescriptions` | `PrescriptionsPage` | Wires up the original ÖGK slice's endpoint (TASK-34) |

Before any route renders, `App.tsx` gates on `useConsent()` - see `ConsentPage`.

## Conventions

- **Pages are dumb containers.** A page component (`src/pages/*.tsx`) owns layout and composes hooks/components; it doesn't itself define fetch logic — that goes in `src/api/*.ts`.
- **One `api/*.ts` file per backend resource** (matching the backend's controller boundaries, e.g. `patients.ts` ↔ `PatientController`), each exporting typed TanStack Query hooks, never a raw fetch call from inside a component.
- **Context is for cross-cutting app state only** (currently the selected demo patient and the user's chosen location). Don't reach for context for state that's local to one page.
- All demo-patient-scoped API calls need the currently selected patient's `svnr` from `usePatient()` (`src/context/PatientContext.tsx`) — there is no auth/session, the patient selector in the header is the entire "login."
- The user's chosen address/coordinates (for nearby-pharmacy features) live in `useUserLocation()` (`src/context/UserLocationContext.tsx`), deliberately **not** named `useLocation` - that name is already `react-router-dom`'s hook for the current URL, and shadowing it would be a confusing landmine for anyone reaching for router state.
- **Backend `String`-typed status fields have no compiler safety net.** `ReservationResponse.status`, `PrescriptionResponse.status`, `DueMedicationResponse.status` etc. are plain Java `String`s wrapping an enum's `.name()`, not the enum itself - TypeScript can't check them against the real backend enum for you. Before hardcoding a status literal (e.g. `"OPEN"`), open the actual `domain.*Status` enum in the backend and copy its real values. Getting this wrong doesn't fail to compile or throw - it just silently filters everything out (see `docs/PROGRESS.md`'s TASK-29 "Bugs found and fixed" entry, where this happened once already with `ReservationStatus`).
- **An expand/collapse `<button>` must never contain another interactive `<button>`.** Nested buttons are invalid HTML; browsers silently "recover" from it visually, so nothing looks wrong on screen, but React logs a hydration-error warning in the console and screen readers/keyboard nav break. `PharmacyCard` hit this once (its favorite-toggle button was nested inside the expand button) - see the same PROGRESS.md entry. If a card needs both a big "expand" click zone and a smaller "action" button inside its header, they must be siblings, not parent/child.

## Allowed icon tags

Icons are a closed set defined in [`Icon.tsx`](src/layout/Icon.tsx) as the `IconName` union (consumed by both `navItems.ts` and `onboardingSlides.ts`), each with one hand-drawn inline SVG path — there is no icon library dependency. **Do not reference an icon name that isn't in this list; extend `IconName` and the `paths` map in `Icon.tsx` before using a new one.**

Currently allowed: `home`, `search`, `pill`, `store`, `heart`, `bookmark`, `doc`, `more`, `shield`.

## Design tokens (CSS custom properties)

Defined once in [`theme.css`](src/theme.css), consumed via `var(--token-name)` everywhere else. Don't hardcode colors, radii, or the shell's layout dimensions outside this file — add a new token here instead.

| Token | Value | Purpose |
|---|---|---|
| `--color-bg` | `#f7f5ee` | Page background |
| `--color-surface` | `#ffffff` | Cards, header, nav, dropdowns |
| `--color-primary` | `#d9432c` | Brand red — active nav state, primary buttons/links |
| `--color-primary-contrast` | `#ffffff` | Text/icon color on top of `--color-primary` |
| `--color-text` | `#1c1c1c` | Default body text |
| `--color-text-muted` | `#6b6b6b` | Secondary/help text |
| `--color-border` | `#e3e0d6` | Hairline borders/dividers |
| `--color-success` | `#2f9e5c` | "Open now" / positive-status text |
| `--color-success-bg` | `#e4f4ea` | Soft tint behind a success/"eingelöst" status badge |
| `--color-primary-bg` | `#fbe9e6` | Soft tint behind a warning/"offen" status badge |
| `--radius-card` | `16px` | Cards, sheets, panels |
| `--radius-pill` | `999px` | Buttons, the patient selector, chips |
| `--shell-max-width` | `960px` | Max content width on wide screens |
| `--bottom-nav-height` | `64px` | Mobile bottom tab bar height |
| `--header-height` | `56px` | Header height |

## Environment variables

| Variable | Default (`.env.development`) | Purpose |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080` | Base URL the frontend fetches the Spring Boot API from. Override per-environment (e.g. a deployed backend) via a `.env.local` or the hosting platform's env config — never commit a non-localhost value. |

## Running locally

Requires the backend running separately (`mvn spring-boot:run` from the repo root, with Postgres up via `docker compose up -d`) — see the repo root [`docs/DEVELOPMENT.md`](../docs/DEVELOPMENT.md). The backend's CORS config (`app.cors.allowed-origins` in `application.yml`) must include this dev server's origin (`http://localhost:5173` by default — already the case).

```bash
npm install
npm run dev      # starts the Vite dev server on http://localhost:5173
npm run build     # type-checks (tsc -b) then produces a production build in dist/
```

## Where things live (for tracking down a feature)

See [`docs/FEATURE_MAPPING.md`](../docs/FEATURE_MAPPING.md) for a table mapping every reference-app feature to its exact backend service and frontend page/component. Start there when you know *what* is broken but not *where*.

For *why* a piece of code looks the way it does, check [`docs/PROGRESS.md`](../docs/PROGRESS.md) - every task's entry explains the non-obvious decisions, not just what was built.

## Troubleshooting

**Every page shows a network/CORS error, or data never loads.**
The backend almost certainly isn't running, or is running but Postgres isn't. Check `curl http://localhost:8080/actuator/health` first (expect `{"status":"UP"}`) - if that fails, see the repo root [`docs/DEVELOPMENT.md`](../docs/DEVELOPMENT.md)'s troubleshooting section, this is a backend/DB problem, not a frontend one. If the backend answers but the browser console shows a CORS error specifically, check `app.cors.allowed-origins` in `application.yml` includes `http://localhost:5173` (or whatever origin you're actually running on - `--host` binds Vite to your LAN IP too, which is a *different* origin CORS will reject).

**A list page is empty even though you know the backend has data (e.g. Pharmacies, Search results).**
Check `UserLocationContext` first - most nearby/availability endpoints are `enabled` only when a location is set (`localStorage['medtrack.userLocation']`). The page usually prompts for this via `LocationPicker`, but if you're mid-development on a page that doesn't render `LocationPicker` yet, the query just silently never fires.

**A status badge, filter, or list never matches anything, but the raw API response (checked via `curl`) clearly has matching rows.**
You're almost certainly comparing against the wrong string literal for a backend enum. See the "Backend `String`-typed status fields" convention above - `curl` the actual endpoint and read the real value rather than assuming it.

**The map doesn't render, or markers are invisible/broken icons.**
`leaflet/dist/leaflet.css` must be imported once globally (it's in `main.tsx`) - without it the map container has zero height and nothing shows. If the map shows but markers are missing/broken image icons, check `PharmacyMap.tsx`'s `L.Icon.Default.mergeOptions(...)` block is still intact - this is Leaflet's well-known "marker icons break under a bundler" issue, and removing that setup (e.g. during a refactor) silently reintroduces it.

**A `console.error` about `<button> cannot be a descendant of <button>` (or any nested-interactive-element hydration warning).**
Real bug, not noise - see the nested-button convention above. `read_console_messages`/`console` output catches this; a screenshot alone won't, since the browser visually "fixes" the invalid HTML.

**IntelliJ (or `git status`) doesn't show recent changes.**
This is an IDE cache/VCS-watcher issue, not a code issue - `File → Reload All from Disk`, or reopen the project if that doesn't help. Verify the actual state with `git log`/`git status` in a terminal first before assuming work was lost.
