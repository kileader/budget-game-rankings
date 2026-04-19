# Next Steps

## Start here (2026-04-18)

1. **Wishlist Watchtower (v1)** — `wishlist_entry` + `WishlistEntry` exist; add backend API, wire auth, minimal frontend (list + add/remove from rankings). Price alerts / CheapShark alerts = later slice.
2. **Game detail page** (`/game/:id`) — pairs with wishlist; Leaf feedback. Can trail v1 wishlist or follow immediately after.
3. **US pricing trust** — core CheapShark sync bug fixed (2026-04-19; was deserializing array as single object). After deploying, `POST /admin/sync` to populate real prices. Remaining: `steam_app_id` coverage gaps, store name on tooltip, Steam list price research.

## Active queue (maintenance)

- **Production migrations** — confirm Railway (or host) has applied through **V11** (`exclude_adult_rated`) and prior ranking-config migrations.
- **Mobile-first CSS** — flip `max-width` → `min-width` where appropriate.
- **Onboarding vs weights** — **done:** My Setup upsert reads weights from `bgr_last_ranking_filters` or preserves existing config.

## Planned features

- **"No thanks"** — v1 shipped (localStorage). Optional: DB sync when logged in; hidden-list UI.
- **Commerce-first cover** — shipped (deal → Steam → IGDB). Deferred: affiliate params + disclosure.
- **HLTB deep links (optional)** — `hltb_game_id` on cache + DTO for direct game URLs (today: search URL when `hltbFound`).
- **Shopping assistant (optional)** — opt-in; RAG over nightly cache when needed (`docs/DECISIONS.md`).
- **Sale sniper / alerts** — CheapShark alert API; pairs with wishlist.
- **Community tagging** — backlog.

## Deferred / known gaps

- `POST /admin/sync` synchronous.
- Token revocation on deactivate — deferred.
- Token in localStorage — known XSS tradeoff.

## Usage

- Keep this file short; replace completed bullets instead of stacking stale history.
