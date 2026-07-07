-- ============================================================================
-- music 表覆盖索引:服务列表页 digest 查询,避免回表读取含 lyrics(TEXT) 的整行
-- ----------------------------------------------------------------------------
-- 目标查询(MusicMapper.queryRecentDigest):
--   SELECT id, title, artist, tags FROM music
--   WHERE status='ONLINE' ORDER BY id DESC LIMIT n
-- 索引 (status, id, title, artist, tags):
--   * status 等值过滤 → 前缀命中
--   * 同一 status 内按 id 有序 → ORDER BY id DESC 免排序(反向扫描)
--   * title/artist/tags 并入索引(MySQL 无 INCLUDE,以复合列实现覆盖)
--   → EXPLAIN Extra 应出现 "Using index"(不回表)
-- 长度核算(utf8mb4):status(130)+id(4)+title(258)+artist(130)+tags(258) ≈ 780B < 3072B 上限 ✅
-- 另:搜索 '%kw%' 无法 seek,但优化器可扫此窄索引代替扫聚簇整行(仍覆盖);
--     /rank/* 按 PK IN 点查,不依赖此索引(收益在列裁剪)。
-- 幂等:经 information_schema 判断,已存在则跳过(MySQL 无 DROP INDEX IF EXISTS)。
-- ============================================================================

USE `test`;

SET @idx_exists := (SELECT COUNT(*) FROM information_schema.statistics
                    WHERE table_schema = 'test' AND table_name = 'music'
                      AND index_name = 'idx_music_online_digest');
SET @ddl := IF(@idx_exists = 0,
               'ALTER TABLE `music` ADD INDEX `idx_music_online_digest` (`status`, `id`, `title`, `artist`, `tags`)',
               'SELECT ''idx_music_online_digest already exists'' AS note');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
