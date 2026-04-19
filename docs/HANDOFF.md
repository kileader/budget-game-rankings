# Handoff

## Latest snapshot (2026-04-20)

**Admin API:** Full reference and **when to use each sync endpoint** — `docs/ADMIN_API.md`. Partial sync: `POST /admin/sync/cheapshark`, `/admin/sync/hltb`, `/admin/sync/igdb`, `/admin/sync/price-estimation`; full pipeline `POST /admin/sync`; HLTB reset+full `POST /admin/hltb-resync`. All share the cache lock (**409** if busy).

---

## Previous snapshot (2026-04-19)

**CheapShark sync bug fixed:** `CheapSharkClient` was calling `GET /games?steamAppID={id}` and deserializing the JSON array response as a single `CheapSharkGameDto`. Jackson silently failed; `cheapshark_price_cents` was never written for any game; every game with a Steam ID fell back to the $14.99 PC tier estimate. Fixed with a two-step lookup: (1) search by Steam App ID to get CheapShark `gameID`, (2) fetch deals by that ID (`GET /games?id={gameID}`). Added `CheapSharkSearchResultDto` to model the search response array element.

**Files touched:**
- `backend/src/.../client/CheapSharkClient.java` — two-step lookup replacing single call
- `backend/src/.../dto/cheapshark/CheapSharkSearchResultDto.java` — new record for search result array element

**Verification:** `./mvnw compile` clean. No existing CheapShark tests.

**Required action:** `POST /admin/sync` after deploying to populate real `cheapshark_price_cents` values. All current DB rows have null — estimates are the only prices right now.

---

## Previous snapshot (2026-04-18)

**Pricing fix (2026-04-19):** `GameCache.getEffectivePriceCents()` **prefers `cheapshark_price_cents` when set**, else tier estimate (removed **`min(cs, est)`** that forced ~**$14.99** over higher Steam deals). Deploy backend only; no migration. If UI still shows mostly **Est.** / **$14.99**, **`cheapshark_price_cents`** is often null — check sync logs / DB coverage, not only app code (`docs/DECISIONS.md`).

**Rankings / UI**

- **Grid cards:** Title (2-line fixed height) → **meta row**: platforms **left**, content rating **right** (or —). **Stats row:** site **favicon** + value score (first), ⭐ IGDB rating, price, playtime; value is not a giant hero number. **Links:** price → CheapShark deal if present, else **Steam** when `steamAppId` and `priceCents` > 0; ⭐ → IGDB only when `igdbUrl` is non-empty; **HLTB** only when API sends **`hltbFound: true`** (real HLTB match, not genre fallback). Table price uses same link rules.
- **Playtime filter:** Min/Max **number inputs only** (dual-range **slider removed**). Advanced Scoring still uses **range inputs** for weights.
- **Saved configs:** `exclude_adult_rated` (V11); onboarding **merges** into current filters (weights preserved). `SavedConfigs` summary includes hide-M/18+ when set.
- **Favicon:** `frontend/index.html` + multi-format assets in `frontend/public/` (`favicon.ico`, `bgr_favicon.svg`, PNGs, apple-touch).
- **About copy:** “How does the value score work?” includes a **Grid cards** paragraph (icons, when links appear, HLTB rule).

**API**

- `RankingResultDto` / JSON: `steamAppId`, `platformIds`, `ageRatingDisplay`, **`hltbFound`**, **`priceIsTrackedDeal`** (true when displayed price is from CheapShark, false when tier estimate; suppressed for nominal free substitute). Grid/table show **Deal** / **Est.** badges. **Deploy backend + frontend together** when adding DTO fields.

**Pricing (US-first, product intent)**

- Canonical display/ranking price: **CheapShark** when set, else **`estimated_price_cents`**. Steam store link is for **navigation**, not necessarily the number shown. **No** multi-region pricing slice shipped; USD baseline per `docs/DECISIONS.md`. Trust issues (e.g. wrong deal / estimate) = **sync + coverage + UI labeling** (“deal” vs “est.”), not solved by this handoff.

**Wishlist**

- DB: **`wishlist_entry`** (V3) + **`WishlistEntry`** entity. **No REST/UI** wired yet — logical start for **Wishlist Watchtower** (`docs/NEXT_STEPS.md`).

## Files recently relevant

- `frontend/src/pages/RankingsPage.tsx` + `RankingsPage.css` — filters, cards, table, `cardPriceHref`, `canLinkHltbSearch`, `igdbPageUrl`, meta row
- `frontend/src/types/index.ts` — `RankingResult` (`hltbFound`, etc.)
- `frontend/index.html` + `frontend/public/bgr_favicon*` — favicon set
- `backend/.../dto/ranking/RankingResultDto.java` — includes `hltbFound`
- `backend/.../service/RankingService.java` — `toRankingResult` maps `hltbFound`
- `backend/.../entity/WishlistEntry.java` + `V3__create_wishlist.sql` — wishlist persistence only

## Verification (last known)

- `backend/mvnw.cmd test` — green (includes `hltbFound` JSON assertions in `RankingControllerTest`).
- `frontend/npm run build` — green.

## Open risks / notes

- Rankings are in-memory after cache fetch; `POST /admin/sync` synchronous.
- JWT not revoked on deactivate/role change.
- Desktop-first CSS; mobile pass still backlog.
- HLTB: long resyncs / session limits — see logs if odd playtime coverage.
- **My Setup:** `upsertMySetup` now sends **rating/playtime/price weights** from `bgr_last_ranking_filters` when present, else preserves existing saved weights, else defaults to 1 — avoids wiping weights on wizard save.

## Next sensible step (new chat)

1. **Wishlist Watchtower v1** — repository + service + `GET/POST/DELETE` (or similar) for wishlist; minimal UI from rankings (e.g. “Add” + `/wishlist` page). Pair with **game detail** later if desired (`docs/NEXT_STEPS.md`).
2. **US price trust** — audit CheapShark coverage / `steam_app_id`; optional UI badge for deal vs estimate; optional Steam list-price research spike (separate from link).

## paper-mcp

Project page ID: `jd7fbgc841fk9pt764973gwvax84nxy6`  
Read or post at [paper.ruixen.app](https://paper.ruixen.app)
