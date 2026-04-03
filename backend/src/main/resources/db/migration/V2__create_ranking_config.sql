CREATE TABLE ranking_config (
    id                  BIGSERIAL    PRIMARY KEY,
    user_id             BIGINT       NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    name                VARCHAR(100) NOT NULL,
    platform_ids        INTEGER[]    NOT NULL DEFAULT '{}',
    genre_ids           INTEGER[]    NOT NULL DEFAULT '{}',
    release_year_min    INTEGER,
    release_year_max    INTEGER,
    min_price_cents     INTEGER      NOT NULL DEFAULT 0,
    max_price_cents     INTEGER,
    min_playtime_hours  NUMERIC(6,2),
    max_playtime_hours  NUMERIC(6,2),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
