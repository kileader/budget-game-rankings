# Admin API

All paths below are under the API base URL (e.g. `https://<host>`). They require an **`Authorization: Bearer <jwt>`** header for a user whose role is **`ADMIN`** (Spring: `ROLE_ADMIN`).

Non-admin authenticated users **cannot** call `/admin/**`; those routes return **403**.

---

## Cache sync jobs (shared lock)

Every **POST** under `/admin/sync/…`, **`POST /admin/sync`**, and **`POST /admin/hltb-resync`** uses the same **`CacheRefreshJob`** lock. Only **one** of these operations may run at a time (including the **nightly cron** if it fires during a manual run).

- **409 Conflict** — another job is already running (or the lock could not be acquired). Body is a short plain-text message.
- **200 OK** — body is plain text describing completion.

**When to use which**

| Endpoint | When to use |
|----------|-------------|
| **`POST /admin/sync`** | **Default / full refresh.** Runs the same pipeline as the nightly job: **IGDB** → **price estimation (tier)** → **CheapShark** → **HLTB**. Use after deploys that affect data shape, or when you want everything brought forward in order. **Longest** run. |
| **`POST /admin/sync/igdb`** | Pull **new/updated games from IGDB** (titles, platforms, Steam ids, ratings, etc.) **without** re-tiering prices or touching CheapShark/HLTB. Use when IGDB credentials or query behavior changed, or you need catalog rows before a separate price/HLTB pass. |
| **`POST /admin/sync/price-estimation`** | Recompute **`estimated_price_cents`** (platform-tier fallbacks) for all applicable rows. Use after changing **tier map logic** in `PriceEstimationService`, or if IGDB platform data was fixed and you need tiers refreshed **without** hitting CheapShark or HLTB. |
| **`POST /admin/sync/cheapshark`** | Refresh **Steam-linked deal prices** only (`cheapshark_price_cents`, deal URLs). Use when **pricing looks wrong**, after fixing the **CheapShark client**, or to pick up storefront changes **without** a full IGDB/HLTB run. **Most targeted** for “deals still missing / still Est.” issues. |
| **`POST /admin/sync/hltb`** | **Incremental** HowLongToBeat pass: only games that **still need** an HLTB sync per repository rules. Use to **fill or update hours** without clearing existing HLTB data. Safer for routine “catch up playtime” runs. |
| **`POST /admin/hltb-resync`** | **Destructive for HLTB scheduling:** clears HLTB sync markers so **every** game is eligible again, then runs a **full** HLTB sync. Use when **HLTB logic/title matching** changed, or data looks systematically wrong and you need a **full re-fetch** of hours. **Heavy** (rate limits / time). |

**Order note:** Nightly job order is IGDB → tiers → CheapShark → HLTB. If you run **partial** endpoints, you are responsible for dependencies (e.g. **CheapShark** needs **`steam_app_id`** from IGDB; **tiers** need platform/Steam data).

---

## User management

| Method | Path | Body | Purpose |
|--------|------|------|---------|
| **GET** | `/admin/users` | — | List all users (`AdminUserDto[]`). |
| **GET** | `/admin/users/{id}` | — | Single user by id. |
| **PATCH** | `/admin/users/{id}/active` | `{ "active": true \| false }` | Enable or disable login for that account. |
| **PATCH** | `/admin/users/{id}/role` | `{ "role": "<role>" }` | Set role (non-blank string; must match app conventions, e.g. `ADMIN` / `USER`). Validated with `@NotBlank`. |

`PATCH` handlers take the **acting admin** from the JWT (`@AuthenticationPrincipal`) for audit-style rules inside `AdminUserService`.

---

## Quick reference: all `/admin` routes

| Method | Path |
|--------|------|
| POST | `/admin/sync` |
| POST | `/admin/sync/igdb` |
| POST | `/admin/sync/price-estimation` |
| POST | `/admin/sync/cheapshark` |
| POST | `/admin/sync/hltb` |
| POST | `/admin/hltb-resync` |
| GET | `/admin/users` |
| GET | `/admin/users/{id}` |
| PATCH | `/admin/users/{id}/active` |
| PATCH | `/admin/users/{id}/role` |

---

## Not admin-only (for comparison)

These require **authentication** but **not** the `ADMIN` role:

- `/auth/me`
- `/users/me/ranking-configs` (saved ranking configs CRUD)

Public (no auth): `/auth/signup`, `/auth/login`, `/rankings/**`, `/health`, `/metadata/**` (see `SecurityConfig`).
