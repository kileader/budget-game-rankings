# Decisions

## 2026-04-03

- Shared cross-agent project memory lives in repo files, not tool-local chat/session memory.
- The default handoff files are `docs/HANDOFF.md`, `docs/DECISIONS.md`, and `docs/NEXT_STEPS.md`.
- Agents should read those files before non-trivial work and update them after meaningful work.
- Keep Railway as the backend host path for now; optimize costs through refresh cadence and efficient read paths rather than changing platform.
- Treat USD as the canonical pricing baseline for now; international price display/localization is deferred.
- Phase 2 is considered complete with platform-tier estimated pricing included.
- Phase 3 is considered planted once the public ranking API, filters, sorting, pagination, validation, and tests are in place.
- Phase 4 is considered planted once signup, login, JWT, `/auth/me`, and admin route protection are in place.
