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
│   ├── api/            # One file per backend resource. Each exports a `use*Query`/`use*Mutation`
│   │                    hook (TanStack Query) that wraps a plain fetch from client.ts.
│   │   ├── client.ts    # Shared fetch wrapper (apiGet/apiSend) + ApiError.
│   │   ├── patients.ts
│   │   └── pharmacies.ts
│   ├── components/      # Reusable UI shared across pages, but not layout/nav (that's `layout/`)
│   │   │                and not a route's own top-level content (that's `pages/`).
│   │   ├── LocationPicker.tsx     # Sets UserLocationContext via the geocode endpoint.
│   │   ├── NextOpenPharmacyCard.tsx
│   │   ├── QuickLinkTile.tsx
│   │   └── SearchBar.tsx
│   ├── context/         # React context providers that need to be available app-wide.
│   │   ├── PatientContext.tsx       # Demo patient selector (no auth) — see TASKS.md TASK-26.
│   │   └── UserLocationContext.tsx  # The user's chosen address/coordinates (TASK-28+).
│   ├── layout/          # App shell: header, responsive nav, icons. Not page content.
│   │   ├── AppShell.tsx
│   │   ├── AppShell.css
│   │   ├── Icon.tsx      # See "Allowed icon tags" below.
│   │   ├── navItems.ts   # The single source of truth for nav routes/labels/icons.
│   │   └── PatientSelector.tsx
│   ├── pages/           # One component per route, wired up in App.tsx. This is where each
│   │                    TASK-27+ feature's actual UI lives.
│   ├── App.tsx           # Route table.
│   ├── main.tsx          # Provider composition (QueryClient, BrowserRouter, PatientProvider,
│   │                      UserLocationProvider).
│   ├── theme.css          # CSS custom properties (design tokens) — see below.
│   └── index.css
├── .env.development      # Committed, no secrets: dev-time VITE_API_BASE_URL default.
└── .env.example
```

## Conventions

- **Pages are dumb containers.** A page component (`src/pages/*.tsx`) owns layout and composes hooks/components; it doesn't itself define fetch logic — that goes in `src/api/*.ts`.
- **One `api/*.ts` file per backend resource** (matching the backend's controller boundaries, e.g. `patients.ts` ↔ `PatientController`), each exporting typed TanStack Query hooks, never a raw fetch call from inside a component.
- **Context is for cross-cutting app state only** (currently the selected demo patient and the user's chosen location). Don't reach for context for state that's local to one page.
- All demo-patient-scoped API calls need the currently selected patient's `svnr` from `usePatient()` (`src/context/PatientContext.tsx`) — there is no auth/session, the patient selector in the header is the entire "login."
- The user's chosen address/coordinates (for nearby-pharmacy features) live in `useUserLocation()` (`src/context/UserLocationContext.tsx`), deliberately **not** named `useLocation` - that name is already `react-router-dom`'s hook for the current URL, and shadowing it would be a confusing landmine for anyone reaching for router state.

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
