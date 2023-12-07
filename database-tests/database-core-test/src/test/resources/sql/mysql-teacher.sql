# noinspection SqlNoDataSourceInspectionForFile

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher`
(
    `id`         bigint(1) unsigned NOT NULL AUTO_INCREMENT,
    `name`       varchar(20) NOT NULL DEFAULT '' COMMENT '姓名',
    `age`        tinyint(2) unsigned NOT NULL DEFAULT '0' COMMENT '年龄',
    `sex`        tinyint(2) unsigned NOT NULL DEFAULT '1' COMMENT '性别1男2女',
    `subject`    varchar(20) NOT NULL DEFAULT '' COMMENT '科目',
    `created_at` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
    `updated_at` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 18
  DEFAULT CHARSET = utf8mb4 COMMENT ='教师表';
INSERT INTO `teacher`
VALUES ('1', '张淑明', '22', '2', '会计', '2009-03-14 20:15:23', '2009-04-24 22:11:03');
INSERT INTO `teacher`
VALUES ('2', '腾腾', '26', '1', '计算机', '2009-03-15 20:15:23', '2009-04-24 22:11:03');
INSERT INTO `teacher`
VALUES ('6', '谭明佳', '22', '2', '会计', '2009-03-16 20:15:23', '2009-04-24 22:11:03');
INSERT INTO `teacher`
VALUES ('8', '文松', '22', '2', '会计', '2009-03-17 20:15:23', '2009-04-24 22:11:03');