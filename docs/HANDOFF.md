# Handoff

## Current State

- Backend Phases 1–6 complete.
- Phase 7 complete: React + TypeScript + Vite frontend scaffolded in `frontend/`.
- Phase 8 complete: public rankings page at `/`.
  - `RankingsPage` fetches `GET /rankings`, renders results table with cover images and external links.
  - Filters: release year range, price range (dollars ↔ cents conversion), playtime range, sort.
  - Offset/limit pagination (50 per page).
  - `ApiError` used for typed error handling.
  - Platform/genre filters deferred — no backend metadata endpoints yet.

## Latest Snapshot

- Date: 2026-04-03
- Branch: `main`
- Uncommitted changes in `frontend/`.

## Files Recently Relevant

- `frontend/src/pages/RankingsPage.tsx`
- `frontend/src/api/rankings.ts`
- `frontend/src/types/index.ts`
- `frontend/src/api/client.ts`
- `frontend/src/main.tsx`

## Verification

- `backend/mvnw.cmd test` succeeded on 2026-04-03. 36 tests passing.
- `frontend/npm run build` passes clean as of 2026-04-03.

## Open Risks / Notes

- `POST /admin/sync` is synchronous and will block the HTTP connection for the full cache refresh duration (potentially minutes). Acceptable for a single admin user with log access.
- `listUsers()` uses `findAll()` with no pagination. Fine at current scale.
- Deactivating a user or changing their role has no immediate effect on active JWT sessions. Tokens remain valid until natural expiry (24h). Known tradeoff, no token revocation mechanism in place.
- Ranking filter/sort is still in-memory after fetching the rankable set.
- `RankingConfig.filters` shape not yet verified against backend `RankingConfigDto` — check before Phase 9.
- Platform/genre filter UI needs backend metadata endpoints (`GET /platforms`, `GET /genres`) before it can be built.

## Next Sensible Step

- Phase 9: frontend user features (login/signup, saved ranking configs).
