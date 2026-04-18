ALTER TABLE ranking_config
    ADD COLUMN exclude_adult_rated BOOLEAN NOT NULL DEFAULT FALSE;
