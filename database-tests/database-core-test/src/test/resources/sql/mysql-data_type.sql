SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `data_type`;
CREATE TABLE `data_type`
(
    `id`           bigint(1) unsigned NOT NULL AUTO_INCREMENT,
    `name`         varchar(20)    NOT NULL DEFAULT '' COMMENT '姓名',
    `age`          tinyint(2) unsigned NOT NULL DEFAULT '0' COMMENT '年龄',
    `sex`          tinyint(2) unsigned NOT NULL DEFAULT '1' COMMENT '性别1男2女',
    `sex_2`        tinyint(1) unsigned NOT NULL DEFAULT '1' COMMENT 'test',
    `subject`      varchar(20)    NOT NULL DEFAULT '' COMMENT '科目',
    `created_at`   timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
    `updated_at`   timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_time` datetime       NOT NULL,
    `updated_time` datetime       NOT NULL,
    `is_deleted`   tinyint(1) NOT NULL,
    `char_char`    char(255)      NOT NULL,
    `integer`      int(11) NOT NULL,
    `numeric`      decimal(10, 0) NOT NULL,
    `bigint`       bigint(20) NOT NULL,
    `binary`       binary(0) NOT NULL DEFAULT '',
    `bit`          bit(1)         NOT NULL DEFAULT b'0',
    `blob`         blob           NOT NULL,
    `date`         date           NOT NULL,
    `decimal`      decimal(10, 0) NOT NULL,
    `double_d`     double         NOT NULL,
    `point`        point          NOT NULL,
    `linestring`   linestring     NOT NULL,
    `geometry`     geometry       NOT NULL COMMENT '支付结果\nSUCCESS—支付成功\nREFUND—转入退款',
    `text`         text           NOT NULL COMMENT '问题状态\r\n现"场检"查 enum(''''待提交'''',''''待整改'''',''''待复验'''',''''已通过'''',''''已作废'''',''''已关闭'''')\r\n实测实量一级问题 enum(''''检查中'''',''''待整改'''',''''已整改'''')\r\n实测实量二级问题 enum(''''检查中'''',''''检查完毕'''', ''''已整改'''')',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;