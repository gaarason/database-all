# noinspection SqlNoDataSourceInspectionForFile

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `test`;
CREATE TABLE `test`
(
    `id`         varchar(50) NOT NULL DEFAULT 'no_id',
    `name`       varchar(20) NOT NULL DEFAULT '' COMMENT '姓名',
    `age`        tinyint(2) unsigned NOT NULL DEFAULT '0' COMMENT '年龄',
    `sex`        tinyint(2) unsigned NOT NULL DEFAULT '1' COMMENT '性别1男2女',
    `subject`    varchar(500) NOT NULL DEFAULT '' COMMENT '科目',
    `created_at` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
    `updated_at` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='test';
INSERT INTO `test`
VALUES ('po213jwqb-1', '小明', '16', '2', '数学', '2009-03-14 17:15:23', '2010-04-24 22:11:03');
INSERT INTO `test`
VALUES ('qwe-1', '谭明佳', '44', '1', '数学', '2009-03-14 17:15:23', '2010-04-22 07:11:03');
INSERT INTO `test`
VALUES ('1-22', '小佳', '16', '2', '数学', '2009-03-14 17:15:23', '2010-04-24 21:11:03');