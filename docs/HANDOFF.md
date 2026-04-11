# Handoff

## Current State

- Backend Phases 1–6 complete.
- Phase 7 complete: React + TypeScript + Vite frontend scaffolded.
- Phase 8 complete: public rankings page at `/`.
- Phase 9 complete: login, signup, saved ranking configs.
  - `AuthContext` wraps app; token/username/role stored in localStorage, exposed via `useAuth()`.
  - `LoginPage` at `/login`, `SignupPage` at `/signup` — both redirect to `/` on success.
  - `Nav` shows login/logout and username based on auth state.
  - `SavedConfigs` component on rankings page (visible when logged in): list, load, save, delete.
  - Loading a config populates filter bar and re-fetches. Saving sends current filter state as a new config.
  - Sort is not persisted in saved configs (backend doesn't store it).

## Latest Snapshot

- Date: 2026-04-06
- Branch: `main`
- Platform/genre multi-select pickers wired end-to-end:
  - Backend: `V6__create_platform_ref.sql` seeds platform names; `MetadataController` exposes `GET /metadata/platforms` and `GET /metadata/genres`; `SecurityConfig` allows public access.
  - Frontend: `MetadataItem` type added; `frontend/src/api/metadata.ts` fetches both endpoints on page mount; `Filters` extended with `platformIds[]` / `genreIds[]`; `SET_MULTI_FILTER` reducer action; `MultiSelect` dropdown component with checkboxes and clear button; `FilterBar` renders pickers above numeric range fields; `rankings.ts` serializes ids as repeated `platformIds`/`genreIds` params matching backend `@RequestParam List<Integer>`.
- TypeScript check passes with zero errors.

## Files Recently Relevant

- `frontend/src/main.tsx`
- `frontend/src/context/AuthContext.tsx`
- `frontend/src/components/Nav.tsx`
- `frontend/src/components/SavedConfigs.tsx`
- `frontend/src/pages/RankingsPage.tsx`
- `frontend/src/pages/LoginPage.tsx`
- `frontend/src/pages/SignupPage.tsx`
- `frontend/src/api/auth.ts`
- `frontend/src/api/rankingConfigs.ts`
- `frontend/src/types/index.ts`

## Verification

- `backend/mvnw.cmd test` succeeded on 2026-04-04. 36 tests passing.
- `frontend/npm run build` passes clean as of 2026-04-05.

## Open Risks / Notes

- `POST /admin/sync` is synchronous. Acceptable for now.
- `listUsers()` has no pagination. Fine at current scale.
- JWT sessions not invalidated on deactivate/role change. Known tradeoff.
- Ranking filter/sort is in-memory after cache fetch.
- Platform/genre pickers show "Loading…" until metadata resolves; errors are silently swallowed (acceptable for metadata).
- CSS is functional but not polished; all styles are desktop-first (`max-width` queries). Flip to mobile-first (`min-width`) in a styling pass.
- Token stored in localStorage — acceptable for this app, but XSS-accessible. No plans to change.

## paper-mcp

Project page ID: `jd7fbgc841fk9pt764973gwvax84nxy6`
Read or post at [paper.ruixen.app](https://paper.ruixen.app) — give this ID to any agent for instant project context.

## Next Sensible Step

- Deploy and smoke-test the platform/genre pickers against the live backend.
- Consider sliders for numeric range filters (year, price, playtime) — Oli's feedback.
- Phase 10: deployment and hardening (Vercel for frontend, Railway already in place for backend).
