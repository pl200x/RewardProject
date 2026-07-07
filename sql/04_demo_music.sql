-- ============================================================================
-- Music Map · demo 音乐种子(带真实曲风 tag)
-- ----------------------------------------------------------------------------
-- 目的:原有 music 表的 tags 是 gaming/eating/Must Listen/NULL 等,和前端兴趣预设
--       (Pop/Rock/Jazz/EDM…)对不上,导致"按兴趣推荐"永远匹配不到。
--       这里插入一批带标准曲风 tag 的歌,让首页 Top/Recent/推荐三列能展示区分明显的真实数据。
--
-- tag 词表与前端 InterestSelectionPage 的预设标签保持完全一致(逐字匹配才能命中分榜):
--   Pop / Rock / Hip-Hop/Rap / Electronic Dance Music (EDM) / Classical / Jazz / Country / R&B
--
-- 幂等:固定 id(101+,避开现有 ≤34 的数据) + INSERT IGNORE,可重复导入。
-- 用法:mysql -uroot -p test < sql/04_demo_music.sql
-- 导入后重启 Music_management,GenreRankSeeder 会把这些歌灌入各 genre 分榜。
-- ============================================================================

USE `test`;

INSERT IGNORE INTO `music` (`id`, `title`, `artist`, `release_year`, `tags`, `lyrics`, `status`) VALUES
  -- Pop
  (101, 'Blinding Lights',        'The Weeknd',        2019, 'Pop',                            NULL, 'ONLINE'),
  (102, 'Levitating',             'Dua Lipa',          2020, 'Pop',                            NULL, 'ONLINE'),
  (103, 'As It Was',              'Harry Styles',      2022, 'Pop',                            NULL, 'ONLINE'),
  -- Rock
  (104, 'Bohemian Rhapsody',      'Queen',             1975, 'Rock',                           NULL, 'ONLINE'),
  (105, 'Smells Like Teen Spirit','Nirvana',           1991, 'Rock',                           NULL, 'ONLINE'),
  (106, 'Numb',                   'Linkin Park',       2003, 'Rock',                           NULL, 'ONLINE'),
  -- Hip-Hop/Rap
  (107, 'Lose Yourself',          'Eminem',            2002, 'Hip-Hop/Rap',                    NULL, 'ONLINE'),
  (108, 'SICKO MODE',             'Travis Scott',      2018, 'Hip-Hop/Rap',                    NULL, 'ONLINE'),
  (109, 'HUMBLE.',                'Kendrick Lamar',    2017, 'Hip-Hop/Rap',                    NULL, 'ONLINE'),
  -- Electronic Dance Music (EDM)
  (110, 'Titanium',               'David Guetta',      2011, 'Electronic Dance Music (EDM)',   NULL, 'ONLINE'),
  (111, 'Wake Me Up',             'Avicii',            2013, 'Electronic Dance Music (EDM)',   NULL, 'ONLINE'),
  (112, 'Clarity',                'Zedd',              2012, 'Electronic Dance Music (EDM)',   NULL, 'ONLINE'),
  -- Classical
  (113, 'Clair de Lune',          'Claude Debussy',    1905, 'Classical',                      NULL, 'ONLINE'),
  (114, 'Nocturne Op.9 No.2',     'Frederic Chopin',   1832, 'Classical',                      NULL, 'ONLINE'),
  (115, 'Moonlight Sonata',       'Ludwig van Beethoven',1801,'Classical',                     NULL, 'ONLINE'),
  -- Jazz
  (116, 'So What',                'Miles Davis',       1959, 'Jazz',                           NULL, 'ONLINE'),
  (117, 'Take Five',              'Dave Brubeck',      1959, 'Jazz',                           NULL, 'ONLINE'),
  (118, 'Feeling Good',           'Nina Simone',       1965, 'Jazz',                           NULL, 'ONLINE'),
  -- Country
  (119, 'Country Roads',          'John Denver',       1971, 'Country',                        NULL, 'ONLINE'),
  (120, 'Jolene',                 'Dolly Parton',      1973, 'Country',                        NULL, 'ONLINE'),
  (121, 'The Gambler',            'Kenny Rogers',      1978, 'Country',                        NULL, 'ONLINE'),
  -- R&B
  (122, 'No Scrubs',              'TLC',               1999, 'R&B',                            NULL, 'ONLINE'),
  (123, 'Adorn',                  'Miguel',            2012, 'R&B',                            NULL, 'ONLINE'),
  (124, 'Redbone',                'Childish Gambino',  2016, 'R&B',                            NULL, 'ONLINE');
