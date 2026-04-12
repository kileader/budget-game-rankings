# Next Steps

## Active Queue

1. Investigate HLTB data quality — most games show 50.0 hrs (RPG genre fallback). Trigger sync, read logs, check matched vs fallback ratio. If HLTB is blocking Railway IP or API changed, fix or find alternative.
2. Game card grid view — cover art cards instead of table; show value score, rating, price, platform tags.
3. Search by title — backend `title` query param + frontend text input.
4. Include free/freemium games — adjusted scoring (skip price component).
5. Include multiplayer-only games — adjusted scoring (skip hours-to-beat component).

## Planned Features

- Game card grid view — cover art grid as alternative to table; Leaf feedback.
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
