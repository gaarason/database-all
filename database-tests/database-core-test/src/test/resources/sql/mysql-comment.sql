# noinspection SqlNoDataSourceInspectionForFile

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`
(
    `id`         bigint(1) unsigned NOT NULL AUTO_INCREMENT,
    `p_type` varchar(200) NOT NULL DEFAULT '' COMMENT '回复的类型',
    `p_id`    bigint(1) unsigned NOT NULL DEFAULT '0' COMMENT '回复的类型的id',
    `content`    varchar(200) NOT NULL DEFAULT '' COMMENT '内容',
    `created_at` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
    `updated_at` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='评论表';
INSERT INTO `comment`
VALUES ('1', 'post', '3', '评论帖子', '2009-03-14 20:15:23', '2009-04-24 22:11:03');
INSERT INTO `comment`
VALUES ('2', 'comment', '1', '评论评论1', '2009-03-14 20:15:23', '2009-04-24 22:11:03');
INSERT INTO `comment`
VALUES ('3', 'comment', '2', '评论评论2', '2009-03-14 20:15:23', '2009-04-24 22:11:03');
INSERT INTO `comment`
VALUES ('4', 'comment', '3', '评论评论3', '2009-03-14 20:15:23', '2009-04-24 22:11:03');
INSERT INTO `comment`
VALUES ('5', 'comment', '4', '评论评论4', '2009-03-14 20:15:23', '2009-04-24 22:11:03');
INSERT INTO `comment`
VALUES ('6', 'comment', '4', '评论评论4 - 1', '2009-03-14 20:15:23', '2009-04-24 22:11:03');