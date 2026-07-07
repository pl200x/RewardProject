
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

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `test` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `test`;
DROP TABLE IF EXISTS `music`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `music` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(64) NOT NULL,
  `artist` varchar(32) NOT NULL,
  `release_year` int NOT NULL,
  `tags` varchar(64) DEFAULT NULL,
  `lyrics` text,
  `status` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_music_title` (`title`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `music` WRITE;
/*!40000 ALTER TABLE `music` DISABLE KEYS */;
INSERT INTO `music` VALUES (1,'title1','artist1',2023,'tag1',NULL,'online'),(3,'Rolling in the Deep','Adele',1999,'Must Listen',NULL,'ONLINE'),(4,'Shape of you  ','Ed sheeran',1999,'Must Listen','666','ONLINE'),(6,'iphone11','Jobs',2015,'gaming,eating,reading','I like .....','ONLINE'),(7,'iphone4','Jobs',2011,'gaming,eating,reading','I like Steven.....','ONLINE'),(13,'android1','Google',2005,NULL,'let\'s see','ONLINE'),(14,'android10086','Google',2005,NULL,'let\'s see','ONLINE'),(15,'android555','Google',25555,NULL,'let\'s see','ONLINE'),(16,'huawei','Google',25555,NULL,'let\'s see','ONLINE'),(17,'Motorola','Google',1900,NULL,'let\'s see','ONLINE'),(18,'galaxy','samsung',1900,NULL,'let\'s see','ONLINE'),(19,'galaxy A15','samsung',1900,NULL,'let\'s see','ONLINE'),(20,'Mobile TV','samsung',1900,NULL,'let\'s see','ONLINE'),(21,'Mob','samsung',1900,NULL,'let\'s see','ONLINE'),(22,'Rock11','samsung',1900,NULL,'let\'s see','ONLINE'),(23,'Rock1145','sam',1900,NULL,'let\'s see','ONLINE'),(24,'Rock14','sam',1900,NULL,'let\'s see','ONLINE'),(25,'Electronic11','sam',1900,NULL,'let\'s see','ONLINE'),(26,'Electronic134561','sam',1900,NULL,'let\'s see','ONLINE'),(27,'Elect','sam',1900,NULL,'let\'s see','ONLINE'),(28,'May30','newTry',1900,NULL,'let\'s see','OFFLINE'),(29,'June 1','ErTong',2025,NULL,'let\'s see','ONLINE'),(30,'June 111','ErTong',2025,'gaming','let\'s see','ONLINE'),(31,'June 25','pellecr',2025,'eating','let\'s see','ONLINE'),(32,'Ju789','pellecr',2025,'eating','let\'s see','ONLINE'),(33,'Jfhjkl9','pellecr',2025,'eating','let\'s see','ONLINE'),(34,'yosane','pellecr',2025,'eating','let\'s see','ONLINE');
/*!40000 ALTER TABLE `music` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type` varchar(32) NOT NULL,
  `detail` varchar(64) DEFAULT NULL,
  `event_time` datetime DEFAULT NULL,
  `sender` varchar(32) DEFAULT NULL,
  `receiver` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
INSERT INTO `notification` VALUES (1,'LIKE','User 1has liked music: 22','2025-06-23 22:16:20','System','aaEmail@123.com'),(2,'LIKE','User 1has liked music: 4','2025-06-25 17:13:45','demo-sender@example.com','demo-user@example.com'),(3,'LIKE','User 1has liked music: 4','2025-06-25 17:13:51','demo-sender@example.com','demo-user@example.com');
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NOT NULL,
  `password` varchar(64) NOT NULL,
  `age` int NOT NULL,
  `gender` int NOT NULL,
  `email` varchar(64) NOT NULL,
  `job` varchar(16) NOT NULL,
  `interest` varchar(255) NOT NULL DEFAULT '',
  `avatar` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
-- 密码为 BCrypt 哈希(明文分别是 111 / jjjjj)
INSERT INTO `user` VALUES (1,'aa','$2a$10$Cb1MUoJYUZ5AEdQas.gSuusUqZ/uQCx9qpFcZb43L3uopipPGal/e',23,1,'demo-user@example.com','java','gaming,eating',2),(2,'wer','$2a$10$rIWCEJ5tZRYn52VKYwm2M.7Cgdf9Sa0eO/rcc4AzFe1kHf8fgxj46',12,1,'345@gmail.com','jiooi','gaming,reading',7);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

