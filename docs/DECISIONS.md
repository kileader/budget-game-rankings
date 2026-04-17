# Decisions

## 2026-04-14

- **Optional shopping assistant:** ship as an **opt-in** feature. First slice: **runtime context** (current filters + API/ranking payload + user message). Add **retrieval / RAG** over the existing nightly **game cache DB** when answers need broader catalog grounding or context would exceed practical token limits. Nightly refresh + admin resync already match a KB-style cadence.

## 2026-04-04

- Agent instructions: **`AGENTS.md` is the single source of truth** for handoff/update rules. Root **`CLAUDE.md`** is a short pointer so Claude Code (and similar) still has a conventional entry file without duplicating content.
- **`.claude/`** project folder removed from the repo (was duplicate + local permission JSON). Recreate locally if a tool needs it. **`/.claude/settings.json`** is gitignored so local Claude Code permissions never show up as untracked churn.
- **Root `/.vscode/`** is gitignored so local VS Code settings do not appear as churn; `frontend/.vscode` remains governed by `frontend/.gitignore` (e.g. optional `extensions.json`).
- **`.cursor/rules/`** is tracked so Cursor rules are shared, not only on one machine.

## 2026-04-03

- Shared cross-agent project memory lives in repo files, not tool-local chat/session memory.
- The default handoff files are `docs/HANDOFF.md`, `docs/DECISIONS.md`, and `docs/NEXT_STEPS.md`.
- Agents should read those files before non-trivial work and update them after meaningful work.
- Keep Railway as the backend host path for now; optimize costs through refresh cadence and efficient read paths rather than changing platform.
- Treat USD as the canonical pricing baseline for now; international price display/localization is deferred.
- Phase 2 is considered complete with platform-tier estimated pricing included.
- Phase 3 is considered planted once the public ranking API, filters, sorting, pagination, validation, and tests are in place.
- Phase 4 is considered planted once signup, login, JWT, `/auth/me`, and admin route protection are in place.
