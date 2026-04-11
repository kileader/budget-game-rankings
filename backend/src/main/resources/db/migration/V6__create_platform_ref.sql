CREATE TABLE platform_ref (
    igdb_platform_id  INTEGER      PRIMARY KEY,
    name              VARCHAR(100) NOT NULL
);

INSERT INTO platform_ref (igdb_platform_id, name) VALUES
(6,   'PC (Windows)'),
(3,   'Linux'),
(14,  'macOS'),
(48,  'PlayStation 4'),
(167, 'PlayStation 5'),
(7,   'PlayStation 3'),
(46,  'PlayStation Vita'),
(38,  'PlayStation Portable'),
(49,  'Xbox One'),
(169, 'Xbox Series X|S'),
(12,  'Xbox 360'),
(11,  'Xbox'),
(130, 'Nintendo Switch'),
(21,  'Nintendo DS'),
(20,  'Nintendo 64'),
(18,  'Nintendo GameCube'),
(5,   'Wii'),
(41,  'Wii U'),
(22,  'Game Boy Advance'),
(24,  'Game Boy Color'),
(33,  'Game Boy'),
(8,   'PlayStation'),
(9,   'PlayStation 2'),
(37,  'Nintendo 3DS'),
(4,   'Nintendo Entertainment System'),
(19,  'Super Nintendo Entertainment System'),
(32,  'Sega Mega Drive/Genesis'),
(29,  'Sega Dreamcast');
