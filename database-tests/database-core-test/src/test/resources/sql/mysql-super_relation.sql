# noinspection SqlNoDataSourceInspectionForFile

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS super_relation;
CREATE TABLE super_relation
(
    `id`         bigint(1) unsigned NOT NULL AUTO_INCREMENT,
    `relation_one_type` varchar(200) NOT NULL DEFAULT '' COMMENT '',
    `relation_one_value` bigint(1) unsigned NOT NULL default 0 COMMENT '',
    `relation_two_type` varchar(200) NOT NULL DEFAULT '' COMMENT '',
    `relation_two_value` bigint(1) unsigned NOT NULL default 0  COMMENT '',
    `created_at` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
    `updated_at` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='超级关系表';

INSERT INTO `super_relation`
VALUES ('1', '', 2, '', 1,'2009-03-14 20:15:23', '2009-04-24 22:11:03');
INSERT INTO `super_relation`
VALUES ('2', '', 2, '', 2,'2009-03-14 20:15:23', '2009-04-24 22:11:03');
INSERT INTO `super_relation`
VALUES ('3', 'post', 3, 'image', 3,'2009-03-14 20:15:23', '2009-04-24 22:11:03');
INSERT INTO `super_relation`
VALUES ('4', 'post', 3, 'image', 4,'2009-03-14 20:15:23', '2009-04-24 22:11:03');
INSERT INTO `super_relation`
VALUES ('5', 'comment', 1, 'image', 1,'2009-03-14 20:15:23', '2009-04-24 22:11:03');
INSERT INTO `super_relation`
VALUES ('6', 'post', 3, '', 5,'2009-03-14 20:15:23', '2009-04-24 22:11:03');
INSERT INTO `super_relation`
VALUES ('7', 'post', 3, '', 6,'2009-03-14 20:15:23', '2009-04-24 22:11:03');
INSERT INTO `super_relation`
VALUES ('8', '', 3, '', 7,'2009-03-14 20:15:23', '2009-04-24 22:11:03');
INSERT INTO `super_relation`
VALUES ('9', '', 3, 'image', 8,'2009-03-14 20:15:23', '2009-04-24 22:11:03');