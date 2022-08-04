# noinspection SqlNoDataSourceInspectionForFile

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `relationship_student_teacher`;
CREATE TABLE `relationship_student_teacher`
(
    `id`         int(1) unsigned NOT NULL AUTO_INCREMENT,
    `student_id` int(1) unsigned NOT NULL DEFAULT '0' COMMENT '学生id',
    `teacher_id` int(1) unsigned NOT NULL DEFAULT '0' COMMENT '教师id',
    `note`       varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
    `created_at` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
    `updated_at` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 30
  DEFAULT CHARSET = utf8mb4 COMMENT ='学生与老师的关系表';
INSERT INTO `relationship_student_teacher`
VALUES ('1', '1', '1', '无备注', '2009-03-14 22:15:23', '2009-04-24 22:22:03');
INSERT INTO `relationship_student_teacher`
VALUES ('2', '1', '2', '无备注', '2009-03-14 22:15:23', '2009-04-24 22:22:03');
INSERT INTO `relationship_student_teacher`
VALUES ('3', '2', '1', '无备注', '2009-03-14 22:15:23', '2009-04-24 22:22:03');
INSERT INTO `relationship_student_teacher`
VALUES ('4', '2', '2', '无备注', '2009-03-14 22:15:23', '2009-04-24 22:22:03');
INSERT INTO `relationship_student_teacher`
VALUES ('5', '3', '1', '无备注', '2009-03-14 22:15:23', '2009-04-24 22:22:03');
INSERT INTO `relationship_student_teacher`
VALUES ('6', '3', '2', '无备注', '2009-03-14 22:15:23', '2009-04-24 22:22:03');
INSERT INTO `relationship_student_teacher`
VALUES ('7', '4', '6', '无备注', '2009-03-14 22:15:23', '2009-04-24 22:22:03');
INSERT INTO `relationship_student_teacher`
VALUES ('8', '4', '2', '无备注', '2009-03-14 22:15:23', '2009-04-24 22:22:03');
INSERT INTO `relationship_student_teacher`
VALUES ('9', '5', '6', '无备注', '2009-03-14 22:15:23', '2009-04-24 22:22:03');
INSERT INTO `relationship_student_teacher`
VALUES ('10', '5', '2', '无备注', '2009-03-14 22:15:23', '2009-04-24 22:22:03');
INSERT INTO `relationship_student_teacher`
VALUES ('11', '6', '6', '无备注', '2009-03-14 22:15:23', '2009-04-24 22:22:03');
INSERT INTO `relationship_student_teacher`
VALUES ('12', '6', '2', '无备注', '2009-03-14 22:15:23', '2009-04-24 22:22:03');
INSERT INTO `relationship_student_teacher`
VALUES ('13', '7', '6', '无备注', '2009-03-14 22:15:23', '2009-04-24 22:22:03');
INSERT INTO `relationship_student_teacher`
VALUES ('14', '7', '2', '无备注', '2009-03-14 22:15:23', '2009-04-24 22:22:03');
INSERT INTO `relationship_student_teacher`
VALUES ('15', '8', '6', '无备注', '2009-03-14 22:15:23', '2009-04-24 22:22:03');
INSERT INTO `relationship_student_teacher`
VALUES ('16', '8', '2', '无备注', '2009-03-14 22:15:23', '2009-04-24 22:22:03');
INSERT INTO `relationship_student_teacher`
VALUES ('17', '9', '6', '无备注', '2009-03-14 22:15:23', '2009-04-24 22:22:03');
INSERT INTO `relationship_student_teacher`
VALUES ('18', '9', '2', '无备注', '2009-03-14 22:15:23', '2009-04-24 22:22:03');
INSERT INTO `relationship_student_teacher`
VALUES ('19', '10', '8', '无备注', '2009-03-14 22:15:23', '2009-04-24 22:22:03');
INSERT INTO `relationship_student_teacher`
VALUES ('20', '10', '2', '无备注', '2009-03-14 22:15:23', '2009-04-24 22:22:03');