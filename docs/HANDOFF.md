# Handoff

## Current State

- Backend Phases 1–6 are complete.
- Phase 5: saved ranking config CRUD under `/users/me/ranking-configs` (list, get, create, update, delete).
- Phase 6: admin user management under `/admin/users` (list, get, activate/deactivate, role change). Admin self-modification is guarded. Role promotion no longer requires direct DB access.
- Auth polish applied: `/auth/me` returns 401 when unauthenticated, signup race condition returns 409, not-found cases return 404 via `NotFoundException`.
- Global `.gitattributes` added at `~/.gitattributes`; repo-level file removed as redundant.

## Latest Snapshot

- Date: 2026-04-03
- Branch: `main`
- All changes committed and pushed.

## Files Recently Relevant

- `backend/src/main/java/com/kevinleader/bgr/controller/RankingConfigController.java`
- `backend/src/main/java/com/kevinleader/bgr/service/RankingConfigService.java`
- `backend/src/main/java/com/kevinleader/bgr/controller/AdminController.java`
- `backend/src/main/java/com/kevinleader/bgr/service/AdminUserService.java`
- `backend/src/main/java/com/kevinleader/bgr/exception/NotFoundException.java`
- `backend/src/main/java/com/kevinleader/bgr/exception/GlobalExceptionHandler.java`
- `backend/src/main/java/com/kevinleader/bgr/config/SecurityConfig.java`

## Verification

- `backend/mvnw.cmd test` succeeded on 2026-04-03.
- Current suite size: 36 tests, all passing.

## Open Risks / Notes

- `POST /admin/sync` is synchronous and will block the HTTP connection for the full cache refresh duration (potentially minutes). Acceptable for a single admin user with log access.
- `listUsers()` uses `findAll()` with no pagination. Fine at current scale.
- Deactivating a user or changing their role has no immediate effect on active JWT sessions. Tokens remain valid until natural expiry (24h). Known tradeoff, no token revocation mechanism in place.
- Ranking filter/sort is still in-memory after fetching the rankable set.

## Next Sensible Step

- Phase 7: scaffold the React + TypeScript + Vite frontend in `frontend/` at the repo root.
