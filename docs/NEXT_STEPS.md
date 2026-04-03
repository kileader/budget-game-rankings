# Next Steps

## Active Queue

1. Phase 10: deployment and hardening (Vercel for frontend, Railway already in place for backend).
2. Add backend metadata endpoints (GET /platforms, GET /genres) to enable platform/genre filter UI.
3. Phase 10: deployment and hardening (Vercel for frontend, Railway already in place for backend).

## Deferred / Known Gaps

- Wishlist feature deferred — only useful once price alert infrastructure exists.
- `POST /admin/sync` is synchronous; make async if timeout becomes a problem.
- Token revocation / immediate effect of deactivation — deferred until there's a reason to add complexity.
- Platform/genre filter UI blocked on backend metadata endpoints.

## Usage

- Keep this file short.
- Replace completed items instead of accumulating stale history.
