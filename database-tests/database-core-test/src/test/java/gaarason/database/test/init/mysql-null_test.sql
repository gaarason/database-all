# noinspection SqlNoDataSourceInspectionForFile

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `null_test`;
CREATE TABLE `null_test`
(
    `id`                 bigint(1) unsigned AUTO_INCREMENT,
    `name`               varchar(20) DEFAULT NULL COMMENT '姓名',
    `is_deleted`         tinyint(1) DEFAULT NULL,
    `json_array_column`  json COMMENT 'json array 类型字段',
    `json_object_column` json COMMENT 'json object 类型字段',
    `time_column`        time        DEFAULT NULL COMMENT 'time类型字段',
    `date_column`        date        DEFAULT NULL COMMENT 'date类型字段',
    `datetime_column`    datetime    DEFAULT NULL COMMENT 'datetime类型字段',
    `timestamp_column`   timestamp   DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'timestamp类型字段',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='NULL测试表';
INSERT INTO `null_test` (`id`, `name`, `is_deleted`, `time_column`, `date_column`, `datetime_column`,
                         `timestamp_column`)
VALUES (1, null, null, null, null, null, null);
INSERT into null_test (`json_array_column`, `json_object_column`, `is_deleted`)
VALUES ('[
  1,
  2,
  3
]', '{
  "name": "zhan",
  "age": 12
}', 0);
