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

- Date: 2026-04-04
- Branch: `main`
- Tooling cleanup: removed duplicate `.claude/` files; canonical agent workflow is `AGENTS.md` with root `CLAUDE.md` pointer for Claude Code. Root `/.vscode/` and `/.claude/settings.json` gitignored; `.cursor/rules/00-core-repo.mdc` tracked.

## Files Recently Relevant

- `frontend/src/main.tsx`
- `frontend/src/context/AuthContext.tsx`
- `frontend/src/components/Nav.tsx`
- `frontend/src/components/SavedConfigs.tsx`
- `frontend/src/pages/RankingsPage.tsx`
- `frontend/src/pages/LoginPage.tsx`
- `frontend/src/pages/SignupPage.tsx`
- `frontend/src/api/auth.ts`
- `frontend/src/api/rankingConfigs.ts`
- `frontend/src/types/index.ts`

## Verification

- `backend/mvnw.cmd test` succeeded on 2026-04-03. 36 tests passing.
- `frontend/npm run build` passes clean as of 2026-04-03.

## Open Risks / Notes

- `POST /admin/sync` is synchronous. Acceptable for now.
- `listUsers()` has no pagination. Fine at current scale.
- JWT sessions not invalidated on deactivate/role change. Known tradeoff.
- Ranking filter/sort is in-memory after cache fetch.
- Platform/genre filter UI needs backend metadata endpoints before it can be built.
- CSS is functional but not polished; all styles are desktop-first (`max-width` queries). Flip to mobile-first (`min-width`) in a styling pass.
- Token stored in localStorage — acceptable for this app, but XSS-accessible. No plans to change.

## Next Sensible Step

- Phase 10: deployment and hardening (Vercel for frontend, Railway already in place for backend).
