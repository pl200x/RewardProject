
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `multi_reward` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `multi_reward`;
DROP TABLE IF EXISTS `configuration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `configuration` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(64) NOT NULL COMMENT '配置编码',
  `rule` text NOT NULL COMMENT '配置规则',
  `description` varchar(512) NOT NULL COMMENT '配置描述',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `configuration` WRITE;
/*!40000 ALTER TABLE `configuration` DISABLE KEYS */;
INSERT INTO `configuration` VALUES (1,'STAGE_RULE','stage = (totalDuration < 160)  ? 0 :(totalDuration < 320)  ? 1 :(totalDuration < 640)  ? 2 :(totalDuration < 960)  ? 3 :(totalDuration < 1280) ? 4 :(totalDuration < 1600) ? 5 :(totalDuration < 3200) ? 6 :(totalDuration < 6400) ? 7 : 8','阶段计算规则配置','2025-09-23 10:01:24','2025-09-23 10:01:24'),(2,'AMOUNT_RULE','amount = (totalDuration < 160)  ? 0 :(totalDuration < 320)  ? 1 :(totalDuration < 640)  ? 1 :(totalDuration < 960)  ? 2 :(totalDuration < 1280) ? 2 :(totalDuration < 1600) ? 2 :(totalDuration < 3200) ? 2 :(totalDuration < 6400) ? 10 : 10','奖品数量计算规则配置','2025-09-23 10:03:27','2025-09-23 10:03:27');
/*!40000 ALTER TABLE `configuration` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `playrecord`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `playrecord` (
  `id` int NOT NULL AUTO_INCREMENT,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `user_id` int DEFAULT NULL,
  `music_id` int DEFAULT NULL,
  `duration` int DEFAULT NULL,
  `sync_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id_music_id_sync_time` (`user_id`,`music_id`,`sync_time`),
  KEY `idx_user_id_sync_time` (`user_id`,`sync_time`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `playrecord` WRITE;
/*!40000 ALTER TABLE `playrecord` DISABLE KEYS */;
INSERT INTO `playrecord` VALUES (1,'2025-12-12 08:29:57','2025-12-12 08:29:57',1,1,30,'2025-12-12 21:41:30'),(2,'2025-12-12 08:30:02','2025-12-12 08:30:02',1,1,30,'2025-12-12 21:42:30'),(3,'2025-12-12 08:30:32','2025-12-12 08:30:32',1,1,30,'2025-12-12 21:43:30'),(4,'2025-12-12 08:30:37','2025-12-12 08:30:37',1,1,30,'2025-12-12 21:44:30'),(5,'2025-12-12 08:30:43','2025-12-12 08:30:43',1,1,30,'2025-12-12 21:45:30'),(6,'2025-12-12 08:30:49','2025-12-12 08:30:49',1,1,30,'2025-12-12 21:46:30'),(7,'2025-12-12 08:40:35','2025-12-12 08:40:35',1,1,30,'2025-12-12 21:47:30'),(8,'2025-12-12 08:40:45','2025-12-12 08:40:45',1,1,30,'2025-12-12 21:48:30'),(9,'2025-12-12 08:40:50','2025-12-12 08:40:50',1,1,30,'2025-12-12 21:49:30'),(10,'2025-12-12 08:40:56','2025-12-12 08:40:56',1,1,30,'2025-12-12 21:50:30'),(11,'2025-12-12 08:41:41','2025-12-12 08:41:41',1,1,30,'2025-12-12 21:52:30'),(12,'2025-12-13 07:05:38','2025-12-13 07:05:38',1,1,30,'2025-12-13 21:51:30'),(13,'2025-12-13 07:05:43','2025-12-13 07:05:43',1,1,30,'2025-12-13 21:52:30'),(14,'2025-12-13 07:05:51','2025-12-13 07:05:51',1,1,30,'2025-12-13 21:53:30'),(15,'2025-12-13 07:05:55','2025-12-13 07:05:55',1,1,30,'2025-12-13 21:54:30'),(16,'2025-12-13 07:06:00','2025-12-13 07:06:00',1,1,30,'2025-12-13 21:55:30'),(17,'2025-12-13 07:06:06','2025-12-13 07:06:06',1,1,30,'2025-12-13 21:56:30');
/*!40000 ALTER TABLE `playrecord` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `prize_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prize_record` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '用户id',
  `biz_scene` varchar(50) DEFAULT NULL COMMENT '业务场景 如：后台播放，每日登录',
  `prize_code` varchar(50) DEFAULT NULL COMMENT '奖励编码 如：金币1，优惠卷2',
  `prize_date` varchar(10) NOT NULL COMMENT '奖励日期',
  `prize_stage` int NOT NULL COMMENT '奖励阶段',
  `prize_amount` int NOT NULL COMMENT '记录每一次发送的奖励数量',
  `out_biz_no` varchar(256) NOT NULL COMMENT '外部业务号,用来做idempotency key',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_prize_record_out_biz_no` (`out_biz_no`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `prize_record` WRITE;
/*!40000 ALTER TABLE `prize_record` DISABLE KEYS */;
INSERT INTO `prize_record` VALUES (1,1,'Listening_Music','coin','2025-12-12',1,1,'Listening_Music_1_coin_2025-12-12_1','2025-12-12 08:30:51','2025-12-12 08:30:51'),(6,1,'Listening_Music','coin','2025-12-12',2,1,'Listening_Music_1_coin_2025-12-12_2','2025-12-12 08:41:41','2025-12-12 08:41:41'),(7,1,'Listening_Music','coin','2025-12-13',1,1,'Listening_Music_1_coin_2025-12-13_1','2025-12-13 07:06:07','2025-12-13 07:06:07'),(8,1,'Listening_Music','coin','2026-06-29',1,1,'Listening_Music_1_coin_2026-06-29_1','2026-06-29 13:59:01','2026-06-29 13:59:01');
/*!40000 ALTER TABLE `prize_record` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `user_analysis`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_analysis` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` int NOT NULL COMMENT '用户id',
  `summary_date` varchar(10) NOT NULL COMMENT '汇总日期',
  `total_duration` int NOT NULL COMMENT '总播放时长（秒）',
  `last_play_time` datetime NOT NULL COMMENT '最后播放时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_analysis_user_id_summary_date` (`user_id`,`summary_date`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `user_analysis` WRITE;
/*!40000 ALTER TABLE `user_analysis` DISABLE KEYS */;
INSERT INTO `user_analysis` VALUES (1,1,'2025-12-12',330,'2025-12-12 08:41:41','2025-12-12 08:29:57','2025-12-12 08:41:41'),(2,1,'2025-12-13',180,'2025-12-13 07:06:06','2025-12-13 07:05:38','2025-12-13 07:06:06');
/*!40000 ALTER TABLE `user_analysis` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

