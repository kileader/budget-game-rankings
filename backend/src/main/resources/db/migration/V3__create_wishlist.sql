CREATE TABLE wishlist_entry (
    id           BIGSERIAL    PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    igdb_game_id BIGINT       NOT NULL,
    game_name    VARCHAR(255) NOT NULL,
    added_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, igdb_game_id)
);
