# Next Steps

## Active Queue

1. **Deploy V8 + V9 migrations** — push and verify new platforms and scoring weight columns are live.
2. **Mobile-first CSS pass** — flip `max-width` media queries to `min-width`.
3. **Weight persistence in onboarding** — save weight prefs to localStorage and "My Setup" config so they survive reload.

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
