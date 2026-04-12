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
- Platform/genre multi-select pickers wired end-to-end (backend MetadataController + frontend MultiSelect).
- Dual-range sliders for year, price, playtime filters (replaced plain number inputs).
- Click-to-sort column headers with ASC/DESC toggle (backend `SortDirection` enum, frontend `SortableHeader` component). Sort dropdown removed.
- Save config fix: 403 now triggers logout (stale JWT); success shows "Saved ✓" flash.
- Release year max defaults to `new Date().getFullYear()`.
- Frontend deployed to Vercel, backend on Railway.

## Files Recently Relevant

- `frontend/src/pages/RankingsPage.tsx` — filters, sort headers, dual-range sliders, MultiSelect
- `frontend/src/pages/RankingsPage.css` — filter bar layout, slider and multi-select styles
- `frontend/src/components/SavedConfigs.tsx` — save/delete error handling, 403 logout
- `frontend/src/api/rankings.ts` — serializes platformIds/genreIds/sortDirection
- `frontend/src/api/metadata.ts` — fetches platforms and genres
- `frontend/src/types/index.ts` — MetadataItem, SortDirection, extended RankingQuery
- `backend/.../dto/ranking/SortDirection.java` — ASC/DESC enum
- `backend/.../controller/RankingController.java` — accepts sortDirection param
- `backend/.../service/RankingService.java` — buildComparator uses SortDirection
- `backend/.../service/HltbSyncService.java` — HLTB sync + genre fallback logic
- `backend/.../client/HltbClient.java` — HLTB token + search client
- `backend/.../dto/hltb/HltbGameResult.java` — comp_main / comp_plus fields
- `backend/src/main/resources/db/migration/V5__create_genre_hltb_fallback.sql` — genre avg_hours seed

## Verification

- `backend/mvnw.cmd test` — 36+ tests passing (last run 2026-04-06).
- `frontend/npm run build` passes clean (Vercel deploys succeed).

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

1. **Investigate HLTB data quality** — many games show exactly 50.0 hrs playtime because HLTB title matching is failing and falling back to `genre_hltb_fallback` (RPG = 50 hrs). Need to trigger `POST /admin/sync`, read logs for matched vs fallback count. If matched count is low, HLTB may be blocking Railway's IP or the API endpoint has changed. The `hltb_found` flag on `GameCache` already tracks this — consider exposing it in the DTO/UI so users can see which hours are estimated.
2. **Game card grid view** — replace table with cards showing cover art, title, value score, rating, price, platform tags. Frontend-only change (IGDB already provides `cover_url` on GameCache).
3. **Search by title** — backend param + frontend text input. Small lift.
4. **Include free/freemium games** — scoring adjustment: skip price component, rank by rating×hours or similar. Needs design decision.
5. **Include multiplayer-only games** — scoring adjustment: skip hours-to-beat component. Needs design decision.
