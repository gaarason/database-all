# noinspection SqlNoDataSourceInspectionForFile

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post`
(
    `id`         bigint(1) unsigned NOT NULL AUTO_INCREMENT,
    `title`      varchar(20) NOT NULL DEFAULT '' COMMENT '标题',
    `content`    varchar(200) NOT NULL DEFAULT '' COMMENT '内容',
    `created_at` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
    `updated_at` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='帖子表';
INSERT INTO `post`
VALUES ('1', '初级帖子', '第一份帖子, 没有图片,  没有评论, 请多多关照~', '2009-03-14 20:15:23', '2009-04-24 22:11:03');

INSERT INTO `post`
VALUES ('2', 'zhong 级帖子', '第2份帖子,有图片,  但是还是没有没有评论, |_|', '2009-03-14 20:15:23', '2009-04-24 22:11:03');

INSERT INTO `post`
VALUES ('3', '高级级帖子', '第2份帖子,有图片, 有评论!', '2009-03-14 20:15:23', '2009-04-24 22:11:03');