ALTER TABLE game_cache
    ADD COLUMN age_rating_display VARCHAR(128);

COMMENT ON COLUMN game_cache.age_rating_display IS 'Human-readable content rating from IGDB sync (e.g. ESRB Teen), for parent-facing UI';
