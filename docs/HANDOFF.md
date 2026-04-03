# Handoff

## Current State

- Backend Phases 1–6 complete.
- Phase 7 complete: React + TypeScript + Vite frontend scaffolded in `frontend/`.
  - `react-router-dom` installed and wired.
  - Folder structure: `src/api/`, `src/components/`, `src/pages/`, `src/types/`.
  - Base API client at `src/api/client.ts` (fetch wrapper, Bearer token support).
  - Domain types at `src/types/index.ts` (Game, RankedGame, RankingFilters, Auth, RankingConfig).
  - Root layout in `App.tsx` (header + `<Outlet />`).
  - Stub `HomePage` at `/`.
  - `VITE_API_URL` env var; `.env.example` committed, `.env.local` gitignored.
  - Vite boilerplate removed. Build passes clean.

## Latest Snapshot

- Date: 2026-04-03
- Branch: `main`
- All changes committed and pushed.

## Files Recently Relevant

- `frontend/src/main.tsx`
- `frontend/src/App.tsx`
- `frontend/src/api/client.ts`
- `frontend/src/types/index.ts`
- `frontend/src/pages/HomePage.tsx`

## Verification

- `backend/mvnw.cmd test` succeeded on 2026-04-03. 36 tests passing.
- `frontend/npm run build` passes clean as of 2026-04-03.

## Open Risks / Notes

- `POST /admin/sync` is synchronous and will block the HTTP connection for the full cache refresh duration (potentially minutes). Acceptable for a single admin user with log access.
- `listUsers()` uses `findAll()` with no pagination. Fine at current scale.
- Deactivating a user or changing their role has no immediate effect on active JWT sessions. Tokens remain valid until natural expiry (24h). Known tradeoff, no token revocation mechanism in place.
- Ranking filter/sort is still in-memory after fetching the rankable set.
- Domain types in `src/types/index.ts` are based on expected backend DTOs — verify field names match when wiring Phase 8.

## Next Sensible Step

- Phase 8: frontend ranking page (public, no auth required).
