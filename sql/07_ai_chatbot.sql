-- The ai_chatbot database is created in the RewardProject MySQL instance.
CREATE DATABASE IF NOT EXISTS `ai_chatbot`
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `ai_chatbot`;

CREATE TABLE IF NOT EXISTS `chat_context` (
  `id` int NOT NULL AUTO_INCREMENT,
  `session_id` varchar(128) NOT NULL,
  `user_id` int NOT NULL,
  `message_content` text NOT NULL,
  `message_sent_time` datetime NOT NULL,
  `read_status` boolean NOT NULL DEFAULT false,
  PRIMARY KEY (`id`),
  KEY `idx_chat_context_user_session_time`
    (`user_id`, `session_id`, `message_sent_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
