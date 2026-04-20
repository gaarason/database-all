SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `m1` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS `m2` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS `s1` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS `s2` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `m1`;
DROP TABLE IF EXISTS `订单商品`;
DROP TABLE IF EXISTS `商品`;
DROP TABLE IF EXISTS `订单_12`;
DROP TABLE IF EXISTS `订单_11`;
DROP TABLE IF EXISTS `订单_10`;
DROP TABLE IF EXISTS `订单_9`;
DROP TABLE IF EXISTS `订单_8`;
DROP TABLE IF EXISTS `订单_7`;
DROP TABLE IF EXISTS `订单_6`;
DROP TABLE IF EXISTS `订单_5`;
DROP TABLE IF EXISTS `订单_4`;
DROP TABLE IF EXISTS `订单_3`;
DROP TABLE IF EXISTS `订单_2`;
DROP TABLE IF EXISTS `订单_1`;
DROP TABLE IF EXISTS `用户`;
CREATE TABLE `用户` (
    `id` bigint NOT NULL,
    `country` varchar(32) NOT NULL DEFAULT '',
    `name` varchar(64) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE `商品` (
    `id` bigint NOT NULL,
    `name` varchar(64) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE `订单_1` (
    `id` bigint NOT NULL,
    `user_id` bigint NOT NULL,
    `order_no` varchar(64) NOT NULL DEFAULT '',
    `order_month` char(7) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE `订单_2` LIKE `订单_1`;
CREATE TABLE `订单_3` LIKE `订单_1`;
CREATE TABLE `订单_4` LIKE `订单_1`;
CREATE TABLE `订单_5` LIKE `订单_1`;
CREATE TABLE `订单_6` LIKE `订单_1`;
CREATE TABLE `订单_7` LIKE `订单_1`;
CREATE TABLE `订单_8` LIKE `订单_1`;
CREATE TABLE `订单_9` LIKE `订单_1`;
CREATE TABLE `订单_10` LIKE `订单_1`;
CREATE TABLE `订单_11` LIKE `订单_1`;
CREATE TABLE `订单_12` LIKE `订单_1`;
CREATE TABLE `订单商品` (
    `id` bigint NOT NULL,
    `order_id` bigint NOT NULL,
    `product_id` bigint NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `用户` (`id`, `country`, `name`) VALUES (101, 'China', 'm1_user_101');
INSERT INTO `用户` (`id`, `country`, `name`) VALUES (102, 'Japan', 'm1_user_102_miss');
INSERT INTO `商品` (`id`, `name`) VALUES (501, 'm1_product_phone');
INSERT INTO `商品` (`id`, `name`) VALUES (502, 'm1_product_watch');
INSERT INTO `订单_3` (`id`, `user_id`, `order_no`, `order_month`) VALUES (10001, 101, 'M1-202603-001', '2026-03');
INSERT INTO `订单_3` (`id`, `user_id`, `order_no`, `order_month`) VALUES (10002, 101, 'M1-202604-001', '2026-04');
INSERT INTO `订单商品` (`id`, `order_id`, `product_id`) VALUES (90001, 10001, 501);
INSERT INTO `订单商品` (`id`, `order_id`, `product_id`) VALUES (90002, 10001, 502);
INSERT INTO `订单商品` (`id`, `order_id`, `product_id`) VALUES (90003, 10002, 502);

USE `m2`;
DROP TABLE IF EXISTS `订单商品`;
DROP TABLE IF EXISTS `商品`;
DROP TABLE IF EXISTS `订单_12`;
DROP TABLE IF EXISTS `订单_11`;
DROP TABLE IF EXISTS `订单_10`;
DROP TABLE IF EXISTS `订单_9`;
DROP TABLE IF EXISTS `订单_8`;
DROP TABLE IF EXISTS `订单_7`;
DROP TABLE IF EXISTS `订单_6`;
DROP TABLE IF EXISTS `订单_5`;
DROP TABLE IF EXISTS `订单_4`;
DROP TABLE IF EXISTS `订单_3`;
DROP TABLE IF EXISTS `订单_2`;
DROP TABLE IF EXISTS `订单_1`;
DROP TABLE IF EXISTS `用户`;
CREATE TABLE `用户` (
    `id` bigint NOT NULL,
    `country` varchar(32) NOT NULL DEFAULT '',
    `name` varchar(64) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE `商品` (
    `id` bigint NOT NULL,
    `name` varchar(64) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE `订单_1` (
    `id` bigint NOT NULL,
    `user_id` bigint NOT NULL,
    `order_no` varchar(64) NOT NULL DEFAULT '',
    `order_month` char(7) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE `订单_2` LIKE `订单_1`;
CREATE TABLE `订单_3` LIKE `订单_1`;
CREATE TABLE `订单_4` LIKE `订单_1`;
CREATE TABLE `订单_5` LIKE `订单_1`;
CREATE TABLE `订单_6` LIKE `订单_1`;
CREATE TABLE `订单_7` LIKE `订单_1`;
CREATE TABLE `订单_8` LIKE `订单_1`;
CREATE TABLE `订单_9` LIKE `订单_1`;
CREATE TABLE `订单_10` LIKE `订单_1`;
CREATE TABLE `订单_11` LIKE `订单_1`;
CREATE TABLE `订单_12` LIKE `订单_1`;
CREATE TABLE `订单商品` (
    `id` bigint NOT NULL,
    `order_id` bigint NOT NULL,
    `product_id` bigint NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `用户` (`id`, `country`, `name`) VALUES (101, 'China', 'm2_user_101_miss');
INSERT INTO `用户` (`id`, `country`, `name`) VALUES (102, 'Japan', 'm2_user_102');
INSERT INTO `商品` (`id`, `name`) VALUES (601, 'm2_product_laptop');
INSERT INTO `商品` (`id`, `name`) VALUES (602, 'm2_product_mouse');
INSERT INTO `订单_3` (`id`, `user_id`, `order_no`, `order_month`) VALUES (20001, 102, 'M2-202603-001', '2026-03');
INSERT INTO `订单_3` (`id`, `user_id`, `order_no`, `order_month`) VALUES (20002, 102, 'M2-202604-001', '2026-04');
INSERT INTO `订单商品` (`id`, `order_id`, `product_id`) VALUES (91001, 20001, 601);
INSERT INTO `订单商品` (`id`, `order_id`, `product_id`) VALUES (91002, 20001, 602);
INSERT INTO `订单商品` (`id`, `order_id`, `product_id`) VALUES (91003, 20002, 602);

USE `s1`;
DROP TABLE IF EXISTS `订单商品`;
DROP TABLE IF EXISTS `商品`;
DROP TABLE IF EXISTS `订单_12`;
DROP TABLE IF EXISTS `订单_11`;
DROP TABLE IF EXISTS `订单_10`;
DROP TABLE IF EXISTS `订单_9`;
DROP TABLE IF EXISTS `订单_8`;
DROP TABLE IF EXISTS `订单_7`;
DROP TABLE IF EXISTS `订单_6`;
DROP TABLE IF EXISTS `订单_5`;
DROP TABLE IF EXISTS `订单_4`;
DROP TABLE IF EXISTS `订单_3`;
DROP TABLE IF EXISTS `订单_2`;
DROP TABLE IF EXISTS `订单_1`;
DROP TABLE IF EXISTS `用户`;
CREATE TABLE `用户` (
    `id` bigint NOT NULL,
    `country` varchar(32) NOT NULL DEFAULT '',
    `name` varchar(64) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE `商品` (
    `id` bigint NOT NULL,
    `name` varchar(64) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE `订单_1` (
    `id` bigint NOT NULL,
    `user_id` bigint NOT NULL,
    `order_no` varchar(64) NOT NULL DEFAULT '',
    `order_month` char(7) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE `订单_2` LIKE `订单_1`;
CREATE TABLE `订单_3` LIKE `订单_1`;
CREATE TABLE `订单_4` LIKE `订单_1`;
CREATE TABLE `订单_5` LIKE `订单_1`;
CREATE TABLE `订单_6` LIKE `订单_1`;
CREATE TABLE `订单_7` LIKE `订单_1`;
CREATE TABLE `订单_8` LIKE `订单_1`;
CREATE TABLE `订单_9` LIKE `订单_1`;
CREATE TABLE `订单_10` LIKE `订单_1`;
CREATE TABLE `订单_11` LIKE `订单_1`;
CREATE TABLE `订单_12` LIKE `订单_1`;
CREATE TABLE `订单商品` (
    `id` bigint NOT NULL,
    `order_id` bigint NOT NULL,
    `product_id` bigint NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

USE `s2`;
DROP TABLE IF EXISTS `订单商品`;
DROP TABLE IF EXISTS `商品`;
DROP TABLE IF EXISTS `订单_12`;
DROP TABLE IF EXISTS `订单_11`;
DROP TABLE IF EXISTS `订单_10`;
DROP TABLE IF EXISTS `订单_9`;
DROP TABLE IF EXISTS `订单_8`;
DROP TABLE IF EXISTS `订单_7`;
DROP TABLE IF EXISTS `订单_6`;
DROP TABLE IF EXISTS `订单_5`;
DROP TABLE IF EXISTS `订单_4`;
DROP TABLE IF EXISTS `订单_3`;
DROP TABLE IF EXISTS `订单_2`;
DROP TABLE IF EXISTS `订单_1`;
DROP TABLE IF EXISTS `用户`;
CREATE TABLE `用户` (
    `id` bigint NOT NULL,
    `country` varchar(32) NOT NULL DEFAULT '',
    `name` varchar(64) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE `商品` (
    `id` bigint NOT NULL,
    `name` varchar(64) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE `订单_1` (
    `id` bigint NOT NULL,
    `user_id` bigint NOT NULL,
    `order_no` varchar(64) NOT NULL DEFAULT '',
    `order_month` char(7) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE `订单_2` LIKE `订单_1`;
CREATE TABLE `订单_3` LIKE `订单_1`;
CREATE TABLE `订单_4` LIKE `订单_1`;
CREATE TABLE `订单_5` LIKE `订单_1`;
CREATE TABLE `订单_6` LIKE `订单_1`;
CREATE TABLE `订单_7` LIKE `订单_1`;
CREATE TABLE `订单_8` LIKE `订单_1`;
CREATE TABLE `订单_9` LIKE `订单_1`;
CREATE TABLE `订单_10` LIKE `订单_1`;
CREATE TABLE `订单_11` LIKE `订单_1`;
CREATE TABLE `订单_12` LIKE `订单_1`;
CREATE TABLE `订单商品` (
    `id` bigint NOT NULL,
    `order_id` bigint NOT NULL,
    `product_id` bigint NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
