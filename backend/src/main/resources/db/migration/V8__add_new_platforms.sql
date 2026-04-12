-- New platforms: current-gen, mobile, VR
INSERT INTO platform_ref (igdb_platform_id, name, sort_order) VALUES
(612, 'Nintendo Switch 2', 4),
(39,  'iOS',              8),
(34,  'Android',          9),
(385, 'Meta Quest',       10),
(390, 'PlayStation VR2',  11),
(163, 'PC VR',            12),
(165, 'PlayStation VR',   13);

-- Shift existing platforms to accommodate new entries
UPDATE platform_ref SET sort_order = 5  WHERE igdb_platform_id = 130;  -- Nintendo Switch: 4 → 5
UPDATE platform_ref SET sort_order = 6  WHERE igdb_platform_id = 48;   -- PS4: 5 → 6
UPDATE platform_ref SET sort_order = 7  WHERE igdb_platform_id = 49;   -- Xbox One: 6 → 7
UPDATE platform_ref SET sort_order = 14 WHERE igdb_platform_id = 14;   -- macOS: 10 → 14
UPDATE platform_ref SET sort_order = 15 WHERE igdb_platform_id = 3;    -- Linux: 11 → 15
