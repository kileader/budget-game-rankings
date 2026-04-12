ALTER TABLE platform_ref ADD COLUMN sort_order INTEGER NOT NULL DEFAULT 999;

UPDATE platform_ref SET sort_order = 1  WHERE igdb_platform_id = 6;    -- PC (Windows)
UPDATE platform_ref SET sort_order = 2  WHERE igdb_platform_id = 167;  -- PlayStation 5
UPDATE platform_ref SET sort_order = 3  WHERE igdb_platform_id = 169;  -- Xbox Series X|S
UPDATE platform_ref SET sort_order = 4  WHERE igdb_platform_id = 130;  -- Nintendo Switch
UPDATE platform_ref SET sort_order = 5  WHERE igdb_platform_id = 48;   -- PlayStation 4
UPDATE platform_ref SET sort_order = 6  WHERE igdb_platform_id = 49;   -- Xbox One
UPDATE platform_ref SET sort_order = 10 WHERE igdb_platform_id = 14;   -- macOS
UPDATE platform_ref SET sort_order = 11 WHERE igdb_platform_id = 3;    -- Linux
UPDATE platform_ref SET sort_order = 20 WHERE igdb_platform_id = 7;    -- PlayStation 3
UPDATE platform_ref SET sort_order = 21 WHERE igdb_platform_id = 12;   -- Xbox 360
UPDATE platform_ref SET sort_order = 22 WHERE igdb_platform_id = 41;   -- Wii U
UPDATE platform_ref SET sort_order = 23 WHERE igdb_platform_id = 37;   -- Nintendo 3DS
UPDATE platform_ref SET sort_order = 24 WHERE igdb_platform_id = 9;    -- PlayStation 2
UPDATE platform_ref SET sort_order = 25 WHERE igdb_platform_id = 5;    -- Wii
UPDATE platform_ref SET sort_order = 26 WHERE igdb_platform_id = 46;   -- PlayStation Vita
UPDATE platform_ref SET sort_order = 27 WHERE igdb_platform_id = 38;   -- PlayStation Portable
UPDATE platform_ref SET sort_order = 30 WHERE igdb_platform_id = 11;   -- Xbox
UPDATE platform_ref SET sort_order = 31 WHERE igdb_platform_id = 18;   -- Nintendo GameCube
UPDATE platform_ref SET sort_order = 32 WHERE igdb_platform_id = 21;   -- Nintendo DS
UPDATE platform_ref SET sort_order = 33 WHERE igdb_platform_id = 22;   -- Game Boy Advance
UPDATE platform_ref SET sort_order = 40 WHERE igdb_platform_id = 8;    -- PlayStation
UPDATE platform_ref SET sort_order = 41 WHERE igdb_platform_id = 20;   -- Nintendo 64
UPDATE platform_ref SET sort_order = 42 WHERE igdb_platform_id = 29;   -- Sega Dreamcast
UPDATE platform_ref SET sort_order = 43 WHERE igdb_platform_id = 32;   -- Sega Mega Drive/Genesis
UPDATE platform_ref SET sort_order = 44 WHERE igdb_platform_id = 4;    -- Nintendo Entertainment System
UPDATE platform_ref SET sort_order = 45 WHERE igdb_platform_id = 19;   -- Super Nintendo Entertainment System
UPDATE platform_ref SET sort_order = 46 WHERE igdb_platform_id = 24;   -- Game Boy Color
UPDATE platform_ref SET sort_order = 47 WHERE igdb_platform_id = 33;   -- Game Boy
