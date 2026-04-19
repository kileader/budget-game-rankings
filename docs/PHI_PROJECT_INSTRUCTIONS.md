# Phi — project instructions (Budget Game Rankings)

Use this file together with `AGENTS.md`, `docs/HANDOFF.md`, `docs/DECISIONS.md`, and `docs/NEXT_STEPS.md`. If they conflict, **ask which source wins** or prefer **repo files** for facts, this file for **current intent and priorities**.

---

## What this repo is

- **BGR** — a **game value-ranking** product (ratings, playtime, price signals, filters, accounts, saved configs).
- **Stack:** React + Spring Boot + Postgres + IGDB / CheapShark / HLTB nightly-style sync. See `docs/HANDOFF.md` for the latest technical snapshot.

---

## Where product intent is right now

- **Pricing and storefront data are a known pain:** deal coverage depends on **Steam app ids** and sync health. Do **not** promise perfect dollars. Prefer **honest labeling** (deal vs estimate) and **admin tooling** (`docs/ADMIN_API.md`) over pretending the pipeline is omniscient.
- **Wishlist Watchtower** (wishlist API + UI) is **not** automatically the top priority — it was planned in `docs/NEXT_STEPS.md` but may be **deferred** if pricing trust isn’t there. Treat it as **V3 / expansion** unless the user explicitly pulls it into sprint scope.
- The maintainer may be **50/50** on continuing BGR vs **exploring other gaming app ideas**. When they say so: help **brainstorm** and **scope** new concepts; **reuse** this codebase/DB where it makes sense; **do not** merge unrelated products into BGR without an explicit decision.

---

## Default decision question

**What helps ship something users can trust next, and what should wait?**

For anything that depends on **accurate real-time prices** or **complete Steam coverage**, flag **high product + engineering risk**.

---

## What to do when you land in the repo

1. Read **`docs/HANDOFF.md`** (latest snapshot first), then **`docs/NEXT_STEPS.md`**, then **`docs/DECISIONS.md`**.
2. Check **`docs/ADMIN_API.md`** if the task involves cache sync, ops, or pricing refresh.
3. **Dates in docs:** Use the **user’s actual calendar date** when adding dated sections — do not invent “tomorrow’s” date from chat context.

---

## Practical next moves (pick with the user)

| If the goal is… | Lean toward… |
|-----------------|--------------|
| **More trustworthy prices in prod** | Deploy backend; run **`POST /admin/sync/igdb`** then **`POST /admin/sync/cheapshark`** (or full **`POST /admin/sync`**). Re-check **`steam_app_id`** counts after IGDB sync includes **`external.steam`**. |
| **Finish a shippable BGR slice** | Rankings polish, trust copy, filters, saved configs, mobile pass — **not** new pricing oracle logic unless scoped. |
| **Pivot or side project** | Short **problem / user / 2-week MVP**; optional reuse: same DB, new read models, or new repo. |
| **Wishlist** | Only after the user confirms priority; entity exists, API/UI do not. |

---

## Style

- Practical, concise, **scope-cutting**.
- Separate **V2 / now**, **V3 / later**, **out of scope**.
- No guilt about deferring features that depend on unreliable third-party data.
