# Budget Game Rankings

Ranks video games by value — not just rating, not just price, but both combined with playtime.

**Value score:** `(IGDB rating × hours to beat) / lowest current price`

Filter by platform, genre, release window, price range, and playtime. The result is a ranked list of games that are actually worth your money.

---

## Status

V2 is in active development. V1 was a Java school project (Madison College, Spring 2021) — working but built on outdated patterns. V2 is a full rebuild.

[V1 video demo](https://youtu.be/8lMcARkRcuE)

---

## V2 Stack

**Backend**
- Java, Spring Boot
- Spring Security + BCrypt + JWT (24-hour tokens)
- Spring Data JPA + PostgreSQL
- Flyway for database migrations
- Hosted on Railway

**Frontend**
- React + TypeScript
- Vite
- Hosted on Vercel

**Data Sources**
- [IGDB](https://api-docs.igdb.com/) — game metadata, ratings, platform/genre data (Twitch OAuth)
- [CheapShark](https://apidocs.cheapshark.com/) — lowest current price across PC storefronts (Steam, GOG, Epic, etc.)
- [HowLongToBeat](https://howlongtobeat.com/) — playtime estimates

---

## How the Ranking Works

1. Game data is cached nightly from IGDB, CheapShark, and HowLongToBeat
2. User sets filters (platform, genre, price range, playtime range, release window)
3. Backend queries the cache, computes a value score for each matching game, and returns a ranked list
4. No live API calls during a user request — everything comes from the cache

**Value score formula:**
```
resolved_hours = hltb_hours if available, else genre average playtime
value_score = (igdb_rating × resolved_hours) / price_in_dollars
```

**Exclusions from main ranking:**
- Free / freemium games (shown in a separate category)
- Multiplayer-only games (no meaningful "hours to beat")
- Games with fewer than 10 IGDB user ratings
- Games with no price data and no platform-tier estimate

**Console-only pricing:** CheapShark covers PC storefronts only. Console-exclusive games use a fixed estimated price by platform generation.

---

## V2 Features

- Public ranking page — no account required
- User accounts — signup, login, JWT auth
- Saved ranking configurations — save and reuse your filter settings
- Wishlist — track specific games, run the value ranking on your wishlist
- Admin panel — user management, manual cache refresh, sync status

---

## Architecture

```
frontend/ (React + TypeScript)  →  backend/ (Spring Boot REST API)  →  PostgreSQL
                                         ↑
                              Nightly cache refresh job
                              (IGDB → CheapShark → HowLongToBeat)
```

---

## V1 Technologies (for reference)

The original school project used: Tomcat JDBC Realm auth, MySQL 8, Hibernate 5, Maven WAR packaging, JSP/JSTL views, Bootstrap 4, jQuery DataTables, Log4j2, JUnit 4/5, hosted on AWS.
