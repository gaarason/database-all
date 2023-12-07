# noinspection SqlNoDataSourceInspectionForFile

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student`
(
    `id`         int(1) unsigned NOT NULL AUTO_INCREMENT,
    `name`       varchar(20) NOT NULL DEFAULT '' COMMENT '姓名',
    `age`        tinyint(2) unsigned NOT NULL DEFAULT '0' COMMENT '年龄',
    `sex`        tinyint(2) unsigned NOT NULL DEFAULT '1' COMMENT '性别1男2女',
    `teacher_id` int(1) unsigned NOT NULL DEFAULT '0' COMMENT '教师id',
    `is_deleted` tinyint(1) NOT NULL default 0,
    `created_at` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
    `updated_at` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 20
  DEFAULT CHARSET = utf8mb4 COMMENT ='学生表';
INSERT INTO `student`
VALUES ('1', '小明', '6', '2', '6', 0, '2009-03-14 17:15:23', '2010-04-24 22:11:03');
INSERT INTO `student`
VALUES ('2', '小张', '11', '2', '6', 0, '2009-03-14 15:15:23', '2010-04-24 22:11:03');
INSERT INTO `student`
VALUES ('3', '小腾', '16', '1', '6', 0, '2009-03-14 15:11:23', '2010-04-24 22:11:03');
INSERT INTO `student`
VALUES ('4', '小云', '11', '2', '6', 0, '2009-03-14 15:15:23', '2010-04-24 22:11:03');
INSERT INTO `student`
VALUES ('5', '小卡卡', '11', '2', '1', 0, '2009-03-14 17:15:23', '2010-04-24 22:11:03');
INSERT INTO `student`
VALUES ('6', '非卡', '16', '1', '1', 0, '2009-03-14 17:15:23', '2010-04-24 22:11:03');
INSERT INTO `student`
VALUES ('7', '狄龙', '17', '1', '2', 0, '2009-03-14 18:15:23', '2010-04-24 22:11:03');
INSERT INTO `student`
VALUES ('8', '金庸', '17', '1', '2', 0, '2009-03-14 18:18:23', '2010-04-24 22:11:03');
INSERT INTO `student`
VALUES ('9', '莫西卡', '17', '1', '8', 0, '2009-03-15 22:15:23', '2010-04-24 22:11:03');
INSERT INTO `student`
VALUES ('10', '象帕', '15', '1', '0', 0, '2009-03-15 12:15:23', '2010-04-24 22:11:03');