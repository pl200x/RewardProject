-- Jamendo 真实音乐种子(音频在 MinIO bucket `music`, key = `<title>.mp3`)
-- 由 scripts/fetch-jamendo-music.py 生成;幂等(固定 id + INSERT IGNORE)。
-- 来源 track: 438223, 1369721, 1827871, 1209633, 1367340, 1439391, 1062048, 357243, 1700955, 702497

USE `test`;

INSERT IGNORE INTO `music` (`id`, `title`, `artist`, `release_year`, `tags`, `lyrics`, `status`) VALUES
  (201, 'Sadista - Husljuk', 'Žznić Crew', 2020, 'Pop', NULL, 'ONLINE'),
  (202, 'One For The Road', 'John Russell', 2020, 'Rock', NULL, 'ONLINE'),
  (203, 'All or Nothing (Light version)', 'Roman Batiuk', 2020, 'Hip-Hop/Rap', NULL, 'ONLINE'),
  (204, 'Drama', 'Matti Paalanen', 2020, 'Electronic Dance Music (EDM)', NULL, 'ONLINE'),
  (205, 'Matrix', 'Zixidi', 2020, 'Classical', NULL, 'ONLINE'),
  (206, 'Charles Chickens', 'Igor Lenik', 2020, 'Jazz', NULL, 'ONLINE'),
  (207, 'Something I''ve already heard', 'Delenda', 2020, 'Country', NULL, 'ONLINE'),
  (208, 'Shah Mat', 'Kudai', 2020, 'R&B', NULL, 'ONLINE'),
  (209, 'We want Freedom', 'Martin Eigenmann', 2020, 'Pop', NULL, 'ONLINE'),
  (210, 'Wo_fuehrt_das_hin(Revocalized)', 'Device Transition', 2020, 'Rock', NULL, 'ONLINE');
