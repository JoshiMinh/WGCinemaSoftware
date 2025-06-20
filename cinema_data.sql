-- Drop and recreate accounts table
DROP TABLE IF EXISTS `accounts`;
CREATE TABLE `accounts` (
  `account_email` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `gender` enum('Male','Female','Other') COLLATE utf8mb4_general_ci NOT NULL,
  `date_of_birth` date NOT NULL,
  `membership` tinyint(1) NOT NULL DEFAULT '0',
  `transactions` int unsigned NOT NULL DEFAULT '0',
  `password_hash` char(64) COLLATE utf8mb4_general_ci NOT NULL,
  `admin` tinyint(1) NOT NULL DEFAULT '0',
  `total_tickets_sold_current_month` int DEFAULT '0',
  `total_revenue_current_month` decimal(10,2) DEFAULT '0.00',
  `total_tickets_sold_last_month` int DEFAULT '0',
  `total_revenue_last_month` decimal(10,2) DEFAULT '0.00',
  `last_reset_date` date DEFAULT (curdate()),
  PRIMARY KEY (`account_email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Drop and recreate movies table
DROP TABLE IF EXISTS `movies`;
CREATE TABLE `movies` (
  `id` tinyint unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `director` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `release_date` date NOT NULL,
  `duration` int unsigned NOT NULL,
  `language` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `age_rating` enum('PG','PG-13','PG-16','R') COLLATE utf8mb4_general_ci DEFAULT NULL,
  `trailer` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `poster` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_general_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Drop and recreate showrooms table
DROP TABLE IF EXISTS `showrooms`;
CREATE TABLE `showrooms` (
  `showroom_id` tinyint unsigned NOT NULL AUTO_INCREMENT,
  `showroom_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `max_chairs` smallint unsigned NOT NULL,
  `rowCount` int DEFAULT NULL,
  `collumnCount` int DEFAULT NULL,
  PRIMARY KEY (`showroom_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Drop and recreate showtimes table
DROP TABLE IF EXISTS `showtimes`;
CREATE TABLE `showtimes` (
  `showtime_id` smallint unsigned NOT NULL AUTO_INCREMENT,
  `time` time NOT NULL,
  `movie_id` tinyint unsigned NOT NULL,
  `date` date NOT NULL,
  `showroom_id` tinyint unsigned NOT NULL,
  `reserved_chairs` smallint unsigned DEFAULT '0',
  `chairs_booked` varchar(255) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
  `regular_price` int NOT NULL DEFAULT '80000',
  `vip_price` int NOT NULL DEFAULT '85000',
  PRIMARY KEY (`showtime_id`),
  KEY `movie_id` (`movie_id`),
  KEY `showroom_id` (`showroom_id`),
  CONSTRAINT `showtimes_ibfk_1` FOREIGN KEY (`movie_id`) REFERENCES `movies` (`id`) ON DELETE CASCADE,
  CONSTRAINT `showtimes_ibfk_2` FOREIGN KEY (`showroom_id`) REFERENCES `showrooms` (`showroom_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Drop and recreate transactions table
DROP TABLE IF EXISTS `transactions`;
CREATE TABLE `transactions` (
  `transaction_id` int unsigned NOT NULL AUTO_INCREMENT,
  `transaction_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `movie_id` tinyint unsigned NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `seats_preserved` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `showroom_id` tinyint unsigned NOT NULL,
  `account_email` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `showtime_id` smallint unsigned NOT NULL,
  PRIMARY KEY (`transaction_id`),
  KEY `movie_id` (`movie_id`),
  KEY `showroom_id` (`showroom_id`),
  KEY `account_email` (`account_email`),
  KEY `showtime_id` (`showtime_id`),
  CONSTRAINT `transactions_ibfk_1` FOREIGN KEY (`movie_id`) REFERENCES `movies` (`id`) ON DELETE CASCADE,
  CONSTRAINT `transactions_ibfk_2` FOREIGN KEY (`showroom_id`) REFERENCES `showrooms` (`showroom_id`) ON DELETE CASCADE,
  CONSTRAINT `transactions_ibfk_3` FOREIGN KEY (`account_email`) REFERENCES `accounts` (`account_email`) ON DELETE CASCADE,
  CONSTRAINT `transactions_ibfk_4` FOREIGN KEY (`showtime_id`) REFERENCES `showtimes` (`showtime_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;