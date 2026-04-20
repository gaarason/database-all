# noinspection SqlNoDataSourceInspectionForFile

SET FOREIGN_KEY_CHECKS = 0;
CREATE DATABASE IF NOT EXISTS `gaarason_routing_a` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS `gaarason_routing_b` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
DROP TABLE IF EXISTS `gaarason_routing_a`.`student`;
CREATE TABLE `gaarason_routing_a`.`student`
(
    `id`         int unsigned          NOT NULL AUTO_INCREMENT,
    `name`       varchar(20)           NOT NULL DEFAULT '' COMMENT '姓名',
    `age`        tinyint unsigned      NOT NULL DEFAULT '0' COMMENT '年龄',
    `sex`        tinyint unsigned      NOT NULL DEFAULT '1' COMMENT '性别1男2女',
    `hobby`      bigint unsigned       NOT NULL DEFAULT '0' COMMENT '爱好',
    `teacher_id` int unsigned          NOT NULL DEFAULT '0' COMMENT '教师id',
    `is_deleted` tinyint(1)            NOT NULL DEFAULT '0',
    `created_at` timestamp             NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
    `updated_at` timestamp             NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='路由集成测试-库A';
INSERT INTO `gaarason_routing_a`.`student`
VALUES ('100', 'catalog_a', '20', '1', 0, '1', 0, '2009-03-14 17:15:23', '2010-04-24 22:11:03');
DROP TABLE IF EXISTS `gaarason_routing_b`.`student`;
CREATE TABLE `gaarason_routing_b`.`student`
(
    `id`         int unsigned          NOT NULL AUTO_INCREMENT,
    `name`       varchar(20)           NOT NULL DEFAULT '' COMMENT '姓名',
    `age`        tinyint unsigned      NOT NULL DEFAULT '0' COMMENT '年龄',
    `sex`        tinyint unsigned      NOT NULL DEFAULT '1' COMMENT '性别1男2女',
    `hobby`      bigint unsigned       NOT NULL DEFAULT '0' COMMENT '爱好',
    `teacher_id` int unsigned          NOT NULL DEFAULT '0' COMMENT '教师id',
    `is_deleted` tinyint(1)            NOT NULL DEFAULT '0',
    `created_at` timestamp             NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
    `updated_at` timestamp             NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='路由集成测试-库B';
INSERT INTO `gaarason_routing_b`.`student`
VALUES ('200', 'catalog_b', '21', '1', 0, '1', 0, '2009-03-14 17:15:23', '2010-04-24 22:11:03');
DROP TABLE IF EXISTS `student_rt_001`;
CREATE TABLE `student_rt_001`
(
    `id`         int unsigned          NOT NULL AUTO_INCREMENT,
    `name`       varchar(20)           NOT NULL DEFAULT '' COMMENT '姓名',
    `age`        tinyint unsigned      NOT NULL DEFAULT '0' COMMENT '年龄',
    `sex`        tinyint unsigned      NOT NULL DEFAULT '1' COMMENT '性别1男2女',
    `hobby`      bigint unsigned       NOT NULL DEFAULT '0' COMMENT '爱好',
    `teacher_id` int unsigned          NOT NULL DEFAULT '0' COMMENT '教师id',
    `is_deleted` tinyint(1)            NOT NULL DEFAULT '0',
    `created_at` timestamp             NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
    `updated_at` timestamp             NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='路由集成测试-分表001';
INSERT INTO `student_rt_001`
VALUES ('9001', 'shard001', '10', '1', 0, '1', 0, '2009-03-14 17:15:23', '2010-04-24 22:11:03');
DROP TABLE IF EXISTS `student_rt_002`;
CREATE TABLE `student_rt_002`
(
    `id`         int unsigned          NOT NULL AUTO_INCREMENT,
    `name`       varchar(20)           NOT NULL DEFAULT '' COMMENT '姓名',
    `age`        tinyint unsigned      NOT NULL DEFAULT '0' COMMENT '年龄',
    `sex`        tinyint unsigned      NOT NULL DEFAULT '1' COMMENT '性别1男2女',
    `hobby`      bigint unsigned       NOT NULL DEFAULT '0' COMMENT '爱好',
    `teacher_id` int unsigned          NOT NULL DEFAULT '0' COMMENT '教师id',
    `is_deleted` tinyint(1)            NOT NULL DEFAULT '0',
    `created_at` timestamp             NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
    `updated_at` timestamp             NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='路由集成测试-分表002';
INSERT INTO `student_rt_002`
VALUES ('9002', 'shard002', '11', '1', 0, '1', 0, '2009-03-14 17:15:23', '2010-04-24 22:11:03');
