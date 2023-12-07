# noinspection SqlNoDataSourceInspectionForFile
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `datetime_test`;
CREATE TABLE `datetime_test`
(
    `id`               bigint(1) unsigned NOT NULL AUTO_INCREMENT,
    `name`             varchar(20) NOT NULL DEFAULT '' COMMENT '姓名',
    `is_deleted`       tinyint(1) NOT NULL default 0,
    `time_column`      time        NOT NULL DEFAULT "00:00:00" COMMENT 'time类型字段',
    `date_column`      date        NOT NULL DEFAULT "0001-01-01" COMMENT 'date类型字段',
    `datetime_column`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'datetime类型字段',
    `timestamp_column` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'timestamp类型字段',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='时间测试表';
INSERT INTO `datetime_test` (`id`, `name`, `is_deleted`, `time_column`, `date_column`, `datetime_column`,
                             `timestamp_column`)
VALUES (1, 'test', 0, '17:15:23', '2010-04-24', '2009-03-14 17:15:23', '2010-04-24 22:11:03');