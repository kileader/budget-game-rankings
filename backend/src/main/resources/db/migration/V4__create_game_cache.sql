CREATE TABLE game_cache (
    igdb_game_id            BIGINT        PRIMARY KEY,
    title                   VARCHAR(255)  NOT NULL,
    igdb_rating             NUMERIC(5,2),
    igdb_rating_count       INTEGER       NOT NULL DEFAULT 0,
    platform_ids            INTEGER[]     NOT NULL DEFAULT '{}',
    genre_ids               INTEGER[]     NOT NULL DEFAULT '{}',
    first_release_date      DATE,
    cover_image_url         VARCHAR(500),
    igdb_url                VARCHAR(500),
    hltb_hours              NUMERIC(6,2),
    hltb_found              BOOLEAN       NOT NULL DEFAULT FALSE,
    cheapshark_price_cents  INTEGER,
    estimated_price_cents   INTEGER,
    is_free                 BOOLEAN       NOT NULL DEFAULT FALSE,
    is_multiplayer_only     BOOLEAN       NOT NULL DEFAULT FALSE,
    steam_app_id            INTEGER,
    cheapshark_deal_url     VARCHAR(500),
    last_igdb_sync          TIMESTAMPTZ,
    last_hltb_sync          TIMESTAMPTZ,
    last_price_sync         TIMESTAMPTZ,
    created_at              TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_game_cache_platform_ids  ON game_cache USING GIN (platform_ids);
CREATE INDEX idx_game_cache_genre_ids     ON game_cache USING GIN (genre_ids);
CREATE INDEX idx_game_cache_is_free       ON game_cache (is_free);
CREATE INDEX idx_game_cache_release_date  ON game_cache (first_release_date);
CREATE INDEX idx_game_cache_rating_count  ON game_cache (igdb_rating_count);
