CREATE TABLE genre_hltb_fallback (
    igdb_genre_id  INTEGER      PRIMARY KEY,
    genre_name     VARCHAR(100) NOT NULL,
    avg_hours      NUMERIC(6,2) NOT NULL
);

-- Seed data: genre average playtimes (hours), sourced from HowLongToBeat aggregates.
-- Update periodically as the cache grows and real HLTB data accumulates.
INSERT INTO genre_hltb_fallback (igdb_genre_id, genre_name, avg_hours) VALUES
(2,  'Point-and-click',    8.0),
(4,  'Fighting',           12.0),
(5,  'Shooter',            10.0),
(7,  'Music',              6.0),
(8,  'Platform',           10.0),
(9,  'Puzzle',             8.0),
(10, 'Racing',             15.0),
(11, 'Real Time Strategy', 25.0),
(12, 'Role-playing (RPG)', 50.0),
(13, 'Simulator',          20.0),
(14, 'Sport',              15.0),
(15, 'Strategy',           25.0),
(16, 'Turn-based strategy',30.0),
(24, 'Tactical',           20.0),
(25, 'Hack and slash',     15.0),
(26, 'Quiz/Trivia',        5.0),
(31, 'Adventure',          12.0),
(32, 'Indie',              8.0),
(33, 'Arcade',             6.0),
(34, 'Visual Novel',       10.0),
(35, 'Card & Board Game',  10.0),
(36, 'MOBA',               500.0);
