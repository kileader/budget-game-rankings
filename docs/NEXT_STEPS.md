# Next Steps

## Active Queue

1. **Deploy V8 + V9 migrations** — push and verify new platforms and scoring weight columns are live.
2. **Mobile-first CSS pass** — flip `max-width` media queries to `min-width`.
3. **Weight persistence in onboarding** — save weight prefs to localStorage and "My Setup" config so they survive reload.

## Planned Features

- **"No thanks" / hidden games** — per-user hide list (localStorage v1; optional DB sync when logged in later). Filter hidden IDs after fetch; undo toast; screen or panel to unhide. Complements wishlist (yes vs no).
- **Game detail page** — in-app route (e.g. `/game/:id`); primary card action when shipped. Pairs with wishlist; Leaf feedback.
- **Card outbound links by target** — title → IGDB (existing). **Cover** also links (same IGDB URL until in-app detail exists, then pick one consistent behavior). **CheapShark** — low lift: `cheapsharkDealUrl` is already on `RankingResult`; icon/label link only when non-null. **HLTB** — API has hours but not HLTB page id today; v1 = search URL from title, or add `hltb_game_id` (or equivalent) to `game_cache` + DTO for stable `/game/{id}` links after sync.
- **Optional shopping assistant** — off by default or gated; user message + rankings/API context first, then retrieval (RAG) over the nightly game cache when token limits or breadth require it. Leaf: treat the DB as the KB.
- Wishlist Watchtower — Kevin's reframe: ranking engine stays as core, wishlist + price alerts become the prominent user-facing angle.
- Sale sniper / price alerts — CheapShark has a price alert API; pairs with wishlist ("alert me when this hits $X") — Hunziboi feedback.
- Community tagging — "worth at full price" / "wait for sale", Steam-style — Hunziboi feedback; non-trivial scope (DB, UI, moderation), backlog.

## Deferred / Known Gaps

- Mobile-first CSS pass (currently desktop-first).
- `POST /admin/sync` is synchronous; make async if timeout becomes a problem.
- Token revocation / immediate effect of deactivation — deferred until there's a reason to add complexity.
- Token in localStorage — acceptable, known XSS tradeoff, no plans to change.

## Usage

- Keep this file short.
- Replace completed items instead of accumulating stale history.
