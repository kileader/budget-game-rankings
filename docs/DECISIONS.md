# Decisions

## 2026-04-19

- **Ranking price assembly:** `GameCache.getEffectivePriceCents()` **prefers `cheapshark_price_cents` when set**, else tier estimate. (Earlier **`min(cs, est)`** incorrectly made the **$14.99 PC tier** beat higher Steam deal prices.) **`priceIsTrackedDeal`** and **`cheapshark_deal_url`** apply when the **displayed** cent value equals the CheapShark column. Nominal **free** substitute when `includeFreeToPlay` uses **$1.00** and is **not** flagged as a tracked deal.
- **Tier estimation:** `PriceEstimationService` unions IGDB **`platform_ids`** with **Windows (6)** whenever **`steam_app_id`** is present (Steam catalog ⇒ PC tier participates in the **lowest-tier pick** across platforms), including when IGDB omits PC from platforms. If **`platform_ids` is empty** but **`steam_app_id`** is set, only the PC tier is used (~$14.99 baseline). (Not the same as the old **`min(cheapshark, estimate)`** effective price.)
- **IGDB Steam id:** `IgdbClient.extractSteamAppId` uses the **first** Steam (`category` 1) row from IGDB after filter (same as before pricing work). Numeric min/max across ids was considered and rejected — newer titles have **larger** app ids than older bundles/demos, so min would pick the wrong row. Wrong or missing ids still require good IGDB data.

## 2026-04-18

- **Handoff refresh:** `docs/HANDOFF.md` and `docs/NEXT_STEPS.md` rewritten for current rankings UI (grid meta row, favicon value score, conditional HLTB/IGDB/price links, `hltbFound` on API). Next product focus: **Wishlist Watchtower v1** (entity/DB exist; API/UI TBD).
- **US pricing scope:** Improve **US** trust (CheapShark + estimates + honest labeling) before multi-region or per-store API sprawl. Aligns with existing **USD canonical** baseline (2026-04-03).
- **HLTB outbound links:** Only when **`hltb_found`** is true in DB; genre (or other) fallback hours stay **plain text** so users are not sent to irrelevant HLTB search results.
- **My Setup + weights:** Onboarding save merges **Advanced Scoring** weights from `localStorage` (`bgr_last_ranking_filters`) or keeps existing **My Setup** config weights so `updateConfig` no longer resets them to 1.

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
