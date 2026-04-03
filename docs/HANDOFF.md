# Handoff

## Current State

- Backend Phase 2 cache population is complete with IGDB, estimated pricing, CheapShark, and HLTB sync services.
- Phase 3 core public ranking API is in place with filtering, sorting, pagination, validation, and tests.
- Phase 4 auth baseline is now in place with signup, login, JWT issuance/parsing, `/auth/me`, and admin route protection.
- Public rankings remain open at `/rankings`; auth routes remain open at `/auth/**`; `/admin/**` requires `ADMIN`.

## Latest Snapshot

- Date: 2026-04-03
- Branch: `main`
- Known uncommitted work: project-level AI workflow files in `AGENTS.md`, `.claude/CLAUDE.md`, and `docs/`

## Files Recently Relevant

- `backend/src/main/java/com/kevinleader/bgr/controller/RankingController.java`
- `backend/src/main/java/com/kevinleader/bgr/controller/AuthController.java`
- `backend/src/main/java/com/kevinleader/bgr/service/RankingService.java`
- `backend/src/main/java/com/kevinleader/bgr/service/AuthService.java`
- `backend/src/main/java/com/kevinleader/bgr/security/JwtService.java`
- `backend/src/main/java/com/kevinleader/bgr/security/JwtAuthenticationFilter.java`
- `backend/src/main/java/com/kevinleader/bgr/config/SecurityConfig.java`
- `backend/src/main/java/com/kevinleader/bgr/service/PriceEstimationService.java`

## Verification

- `backend\\mvnw.cmd test` succeeded on 2026-04-03.
- Current suite size: 18 tests, all passing.

## Open Risks / Notes

- Estimated pricing is heuristic and based on a hard-coded platform tier map.
- CheapShark still takes precedence via `GameCache.getEffectivePriceCents()`.
- `/admin/**` is protected by role, but there is not yet a dedicated 401/403 response polish layer beyond default behavior.
- Ranking filtering/sorting is still mostly in-memory after fetching the rankable set; acceptable for current scale, but a future optimization target.
- The controller tests use standalone MockMvc and currently emit deprecation warnings for `MappingJackson2HttpMessageConverter` in test code only.

## Next Sensible Step

- Treat Phase 4 as planted and either do auth polish (401/403 behavior, role-route tests) or move into Phase 5 user features.
