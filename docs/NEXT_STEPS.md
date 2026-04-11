# Next Steps

## Active Queue

1. Phase 10: deploy frontend to Vercel, smoke-test against Railway backend.
2. Column sort — click-to-sort ascending/descending on Title and numeric columns (was in V1, lost in rework — j00f feedback).
3. Sliders for numeric range filters — year, price, playtime — Leaf + Oli feedback.

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
