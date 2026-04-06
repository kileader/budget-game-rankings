# Next Steps

## Active Queue

1. Phase 10: deployment and hardening (Vercel for frontend, Railway already in place for backend).
2. Add backend metadata endpoints (GET /platforms, GET /genres) to enable platform/genre filter UI.

## Deferred / Known Gaps

- Wishlist feature deferred — only useful once price alert infrastructure exists.
- Sale sniper / price alerts — CheapShark has a price alert API; pairs naturally with the wishlist ("alert me when this game hits $X"). Build alongside wishlist.
- Community tagging ("worth at full price", "wait for sale") — user-generated signal on top of the value score. Real feature, non-trivial scope (DB, UI, moderation). Backlog.
- `POST /admin/sync` is synchronous; make async if timeout becomes a problem.
- Token revocation / immediate effect of deactivation — deferred until there's a reason to add complexity.
- Platform/genre filter UI blocked on backend metadata endpoints.

## Usage

- Keep this file short.
- Replace completed items instead of accumulating stale history.
