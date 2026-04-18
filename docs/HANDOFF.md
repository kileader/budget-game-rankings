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

- Date: 2026-04-16
- **Product direction (2026-04-14):** optional **shopping assistant** (runtime context + user message first; **RAG** over the nightly cache when retrieval is needed). **Game detail** route ahead of wishlist; **"No thanks"** hide list; **commerce-first cover** shipped (deal → Steam → IGDB; **`steamAppId`** on rankings JSON); **affiliate** still deferred (see `docs/NEXT_STEPS.md`).
- **Vercel:** `@vercel/analytics` + `@vercel/speed-insights` mounted in `frontend/src/main.tsx`. Enable **Web Analytics** and **Speed Insights** for the project in the Vercel dashboard (deployments on Vercel only; local builds are effectively inert).
- **Rankings UI (Leaf / hybrid filters):** Release year and price use **number inputs only** (no dual-range sliders). **Playtime** keeps **dual-range + min/max inputs**, grouped in a highlighted block. **Title search:** debounced **300ms**, no submit/Enter; **Clear** button; **recent searches** dropdown (localStorage `bgr_search_recent`). **Apply filters** for all other fields. **Grid/table loading:** placeholder **skeletons** instead of “Loading…”. **Game cards:** title area fixed to **two lines** (`min-height` + 2-line clamp + ellipsis).
- **Rankings platform line (2026-04-14):** Card and table **platform** row is **one line** (`…` when overflow). **Order:** applied platform filter order (if any), else **onboarding “My Setup”** platform order, then the rest by catalog `sort_order`. **Links:** PC/Mac/Linux/PC VR use **CheapShark deal** (when present) then **Steam**; PlayStation / Xbox / Nintendo / mobile / Quest use **first-party store search** URLs (not price-ranked—backend only tracks PC deal). Unknown families fall back to **IGDB**. With an **applied** platform filter, only those platforms are listed; with **Any**, all game platforms show.
- **Rankings polish (2026-04-16):** If title search / pick / clear cannot apply (validation), **title input reverts** to the last applied query title. **Debounced** failure skips the next title effect (no redundant timer). **`SET_SORT`** clears `validationError`. **Search field** no longer advertises incomplete combobox roles; **`aria-describedby`** hint. **`storage`** listener refreshes recent list from other tabs. **`getRankings`** uses **AbortSignal**; rankings effect **aborts** in-flight fetch when `appliedQuery` changes (avoids stale overwrites). **`api.get`** second arg is now `{ token?, signal? }` (`listConfigs` updated).
- **"No thanks" + card links (frontend):** Hidden game IDs in **`localStorage` key `bgr_hidden_games`**; filter client-side after fetch; toolbar **Show hidden** clears list; `storage` event syncs tabs. **Cover (grid + table):** **deal URL → Steam (`steamAppId`) → IGDB** (`primaryCoverHref` in `RankingsPage.tsx`); **title** still **IGDB**. **×** on cover hides card (hover/focus on desktop; visible on touch). **HLTB** = playtime links to search. **Table:** **No thanks** column. **Filter bar:** 12-col grid + shorter search hint.
- **Rankings API:** `RankingResultDto` includes **`steamAppId`** (maps `game_cache.steam_app_id`); Jackson emits `steamAppId` in JSON.
- **Rankings API:** **`platformIds`** (IGDB) on each result; UI maps to names via existing **`GET /metadata/platforms`** (grid + table **Platforms** column / card footer).
- **Content rating:** `game_cache.age_rating_display` (V10) filled on IGDB sync from `age_ratings.rating_category` + organization; API field **`ageRatingDisplay`** (e.g. `ESRB · Teen`, prefers ESRB then PEGI). Shown in table **Content** column and under card title when present.
- **Rankings query:** `excludeAdultRated=true` drops games where `age_rating_display` matches **`AdultAgeRatingClassifier`** (Mature, AO, PEGI 18, etc.); **null label = keep**. UI checkbox **Hide Mature / 18+ labels** (Apply filters).
- **Price fallback (`PriceEstimationService`):** tier map now includes **PC / Mac / Linux (6, 14, 3)** at **$14.99**; aggregation uses **minimum** matched tier (not max console MSRP) so multi-platform games without a CheapShark row don’t show **$69.99**. **CheapShark still wins** when `cheapshark_price_cents` is set. Re-run **`estimateAll`** (nightly job or admin) to refresh `estimated_price_cents` in DB.
- Date: 2026-04-12
- Branch: `main`
- HLTB client fixed: endpoint changed from `/api/finder` → `/api/find`; HLTB now requires honeypot headers/body (`hpKey`/`hpVal`) from `/api/find/init`. Client auto-refreshes token on 403. Full resync running via `POST /admin/hltb-resync` (10,642 games, ~91% match rate verified).
- Value score caps playtime at 200 hrs to prevent live-service games (e.g. War Thunder at 832 hrs) dominating rankings.
- Game card grid view added as default; table view still available via toggle. Cards show cover art, value score, rating, price, rank badge.
- Default year filter set to 2000 (from `defaultFilters` and `initialState`).
- Platforms sorted by era via `sort_order` column (V7 migration). Modern platforms (PC, PS5, Switch, etc.) at top.
- `POST /admin/hltb-resync` admin endpoint added — clears all `lastHltbSync` and re-runs HLTB sync.
- **Onboarding modal** (V8): 4-step first-visit wizard — platform picker (searchable, grouped), year range preset, free-to-play toggle, multiplayer-only toggle. Persists to `localStorage` key `bgr_onboarding`. `OnboardingContext` manages open/close state and exposes `prefs` app-wide. "My Setup" button in Nav reopens wizard. RankingsPage pre-populates filters from prefs on load and re-applies on update. For logged-in users, completing the wizard upserts a saved config named "My Setup".
- **Search by title**: `?title=` query param on `GET /rankings` (case-insensitive substring). Frontend text input in FilterBar.
- **V8 migration**: adds Nintendo Switch 2 (612), iOS (39), Android (34), Meta Quest (385), PSVR2 (390), PC VR (163), PlayStation VR (165) to `platform_ref`.
- **MultiSelect extracted** to `components/MultiSelect.tsx` with searchable + grouped props.
- **Scoring weights + free/multiplayer inclusion** (V9): power-law formula `rating^rW * playtime^pW / price^prW` with user-adjustable weights **0.0–2.0** (validated on `GET /rankings` params and saved-config JSON; `RankingService.validateQuery` enforces the same range). Free games use $1.00 nominal price when included. Multiplayer-only games use existing playtime data. Weight sliders in collapsed "Advanced Scoring" (hint to click Apply). Include checkboxes in main filter bar. Weights on `ranking_config`. When either include flag is set, **`findAllForRanking(includeFree, includeMultiplayer)`** loads only the expanded slice (not all free games when only multiplayer is enabled). `ConstraintViolationException` →400 in `GlobalExceptionHandler`.
- **`GET /metadata/platforms`**: full `platform_ref` ordered by `sort_order` (not only platforms on rankable cached games), so new platforms (e.g. Switch 2) appear in the picker immediately.

## Files Recently Relevant

- `frontend/src/main.tsx` + `frontend/package.json` — Vercel Analytics & Speed Insights
- `frontend/src/pages/RankingsPage.tsx` + `RankingsPage.css` — filters grid, hidden games, commerce-first cover links, HLTB on playtime, table actions
- `frontend/src/types/index.ts` — `RankingResult.steamAppId`
- `backend/.../dto/ranking/RankingResultDto.java` — `steamAppId`; `RankingService.toRankingResult`
- `frontend/src/components/OnboardingModal.tsx` + `.css` — 4-step wizard, localStorage helpers
- `frontend/src/context/OnboardingContext.tsx` — prefs state, modal open/close, upsertMySetup
- `frontend/src/components/MultiSelect.tsx` + `.css` — extracted, searchable, grouped
- `frontend/src/components/Nav.tsx` + `Nav.css` — "My Setup" button
- `frontend/src/pages/RankingsPage.tsx` — onboarding prefs wired into initial state + APPLY_ONBOARDING, title filter, SET_TITLE
- `frontend/src/api/rankingConfigs.ts` — added updateConfig
- `frontend/src/types/index.ts` — OnboardingPrefs type, title on RankingQuery
- `backend/.../dto/ranking/RankingQueryDto.java` — title field
- `backend/.../controller/RankingController.java` — title param
- `backend/.../service/RankingService.java` — title filter in matchesFilters
- `backend/src/main/resources/db/migration/V8__add_new_platforms.sql` — 7 new platforms
- `backend/src/main/resources/db/migration/V9__add_scoring_weights_to_ranking_config.sql` — rating/playtime/price weight columns
- `backend/.../dto/ranking/ScoringWeightConstraints.java` — shared 0.0–2.0 bounds
- `backend/.../repository/GameCacheRepository.java` — `findAllForRanking(includeFree, includeMultiplayer)`
- `backend/.../repository/PlatformRefRepository.java` + `PlatformRef` entity — platform metadata
- `backend/.../service/RankingService.java` — weighted `computeValueScore`, `effectivePriceCents`, free/multiplayer filtering
- `frontend/src/pages/RankingsPage.tsx` — weight sliders (Advanced Scoring), include checkboxes, new reducer actions
- `frontend/src/pages/RankingsPage.css` — checkbox, scoring slider, advanced details styles

## Verification

- `backend/mvnw.cmd test` — all passing (includes `RankingResultDto.steamAppId` JSON assertions).
- `frontend/npm run build` passes clean (commerce-first cover + optional `steamAppId` type).

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

## Open Risks / Notes (updated)

- HLTB resync (10,642 games) was still running at handoff. Check Railway logs for final `matched`/`fallback` count.
- HLTB sessions expire after ~300 requests — 403 auto-refresh is now implemented; should handle the full run.
- Virtual console / port detection deferred — IGDB has `version_parent` field but we don't store it yet.
- Free/multiplayer scoring not yet implemented — scoring design decision still open.
- War Thunder and similar live-service games: playtime capped at 200hrs for scoring, but `is_multiplayer_only` flag may not be set correctly in DB for all games.

## Next Sensible Step

1. **Deploy V8 + V9 migrations** — push to Railway; verify platforms picker and `ranking_config` weight columns.
2. **Weight persistence in onboarding** — localStorage + "My Setup" saved config.
3. **Mobile-first CSS pass** — flip `max-width` media queries to `min-width`.
4. **Planned (not blocking core):** game detail page; affiliate links + disclosure if pursued; optional assistant / RAG slice when ready.
