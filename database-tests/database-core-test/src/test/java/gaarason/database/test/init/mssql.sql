use test_master_0;

DROP TABLE IF EXISTS data_type;
CREATE TABLE data_type
(
    [id]         [bigint]       NOT NULL identity (1,1) primary key,
    name         varchar(20)    NOT NULL DEFAULT '',
    age          tinyint        NOT NULL DEFAULT '0',
    sex          tinyint        NOT NULL DEFAULT '1',
    subject      varchar(20)    NOT NULL DEFAULT '',
    created_at   datetime       NOT NULL,
    updated_at   DATETIME       NOT NULL,
    created_time datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted   tinyint        NOT NULL,
    char_char    char(255)      NOT NULL,
    integer      int            NOT NULL,
    numeric      decimal(10, 0) NOT NULL,
    bigint       bigint         NOT NULL,
    binary       binary(1)      NOT NULL,
    bit          bit            NOT NULL DEFAULT '0',
--     blob         blob                NOT NULL,
    date         date           NOT NULL,
    decimal      decimal(10, 0) NOT NULL,
--     double_d     double              NOT NULL,
--     point        point               NOT NULL,
--     linestring   linestring          NOT NULL,
--     geometry     geometry            NOT NULL ,
    text         text           NOT NULL,
);
set IDENTITY_INSERT data_type on;

set IDENTITY_INSERT data_type OFF;

DROP TABLE IF EXISTS test;

CREATE TABLE test
(
    id         varchar(12) NOT NULL DEFAULT 'no_id' ,
    name       varchar(20) NOT NULL DEFAULT '',
    age        tinyint     NOT NULL DEFAULT '0',
    sex        tinyint     NOT NULL DEFAULT '1',
    subject    varchar(20) NOT NULL DEFAULT '',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
-- set IDENTITY_INSERT test on;
INSERT INTO test (id, name, age, sex, subject, created_at, updated_at)
VALUES ('po213jwqb-1', N'小明', '16', '2', N'数学', DEFAULT, DEFAULT);
INSERT INTO test (id, name, age, sex, subject, created_at, updated_at)
VALUES ('qwe-1', N'谭明佳', '44', '1', N'数学', DEFAULT, DEFAULT);
INSERT INTO test (id, name, age, sex, subject, created_at, updated_at)
VALUES ('1-22', N'小佳', '16', '2', N'数学', DEFAULT, DEFAULT);
-- set IDENTITY_INSERT test OFF;

DROP TABLE IF EXISTS relationship_student_teacher;

CREATE TABLE relationship_student_teacher
(
    id         int          NOT NULL identity (30,1),
    student_id int          NOT NULL DEFAULT '0',
    teacher_id int          NOT NULL DEFAULT '0',
    note       varchar(255) NOT NULL DEFAULT '',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
set IDENTITY_INSERT relationship_student_teacher ON
INSERT INTO relationship_student_teacher (id, student_id, teacher_id, note, created_at, updated_at)
VALUES ('1', '1', '1', N'无备注', DEFAULT, DEFAULT);
set IDENTITY_INSERT relationship_student_teacher ON
INSERT INTO relationship_student_teacher (id, student_id, teacher_id, note, created_at, updated_at)
VALUES ('2', '1', '2', N'无备注', DEFAULT, DEFAULT);
set IDENTITY_INSERT relationship_student_teacher ON
INSERT INTO relationship_student_teacher (id, student_id, teacher_id, note, created_at, updated_at)
VALUES ('3', '2', '1', N'无备注', DEFAULT, DEFAULT);
set IDENTITY_INSERT relationship_student_teacher ON
INSERT INTO relationship_student_teacher (id, student_id, teacher_id, note, created_at, updated_at)
VALUES ('4', '2', '2', N'无备注', DEFAULT, DEFAULT);
set IDENTITY_INSERT relationship_student_teacher ON
INSERT INTO relationship_student_teacher (id, student_id, teacher_id, note, created_at, updated_at)
VALUES ('5', '3', '1', N'无备注', DEFAULT, DEFAULT);
set IDENTITY_INSERT relationship_student_teacher ON
INSERT INTO relationship_student_teacher (id, student_id, teacher_id, note, created_at, updated_at)
VALUES ('6', '3', '2', N'无备注', DEFAULT, DEFAULT);
set IDENTITY_INSERT relationship_student_teacher ON
INSERT INTO relationship_student_teacher (id, student_id, teacher_id, note, created_at, updated_at)
VALUES ('7', '4', '6', N'无备注', DEFAULT, DEFAULT);
set IDENTITY_INSERT relationship_student_teacher ON
INSERT INTO relationship_student_teacher (id, student_id, teacher_id, note, created_at, updated_at)
VALUES ('8', '4', '2', N'无备注', DEFAULT, DEFAULT);
set IDENTITY_INSERT relationship_student_teacher ON
INSERT INTO relationship_student_teacher (id, student_id, teacher_id, note, created_at, updated_at)
VALUES ('9', '5', '6', N'无备注', DEFAULT, DEFAULT);
set IDENTITY_INSERT relationship_student_teacher ON
INSERT INTO relationship_student_teacher (id, student_id, teacher_id, note, created_at, updated_at)
VALUES ('10', '5', '2', N'无备注', DEFAULT, DEFAULT);
set IDENTITY_INSERT relationship_student_teacher ON
INSERT INTO relationship_student_teacher (id, student_id, teacher_id, note, created_at, updated_at)
VALUES ('11', '6', '6', N'无备注', DEFAULT, DEFAULT);
set IDENTITY_INSERT relationship_student_teacher ON
INSERT INTO relationship_student_teacher (id, student_id, teacher_id, note, created_at, updated_at)
VALUES ('12', '6', '2', N'无备注', DEFAULT, DEFAULT);
set IDENTITY_INSERT relationship_student_teacher ON
INSERT INTO relationship_student_teacher (id, student_id, teacher_id, note, created_at, updated_at)
VALUES ('13', '7', '6', N'无备注', DEFAULT, DEFAULT);
set IDENTITY_INSERT relationship_student_teacher ON
INSERT INTO relationship_student_teacher (id, student_id, teacher_id, note, created_at, updated_at)
VALUES ('14', '7', '2', N'无备注', DEFAULT, DEFAULT);
set IDENTITY_INSERT relationship_student_teacher ON
INSERT INTO relationship_student_teacher (id, student_id, teacher_id, note, created_at, updated_at)
VALUES ('15', '8', '6', N'无备注', DEFAULT, DEFAULT);
set IDENTITY_INSERT relationship_student_teacher ON
INSERT INTO relationship_student_teacher (id, student_id, teacher_id, note, created_at, updated_at)
VALUES ('16', '8', '2', N'无备注', DEFAULT, DEFAULT);
set IDENTITY_INSERT relationship_student_teacher ON
INSERT INTO relationship_student_teacher (id, student_id, teacher_id, note, created_at, updated_at)
VALUES ('17', '9', '6', N'无备注', DEFAULT, DEFAULT);
set IDENTITY_INSERT relationship_student_teacher ON
INSERT INTO relationship_student_teacher (id, student_id, teacher_id, note, created_at, updated_at)
VALUES ('18', '9', '2', N'无备注', DEFAULT, DEFAULT);
set IDENTITY_INSERT relationship_student_teacher ON
INSERT INTO relationship_student_teacher (id, student_id, teacher_id, note, created_at, updated_at)
VALUES ('19', '10', '8', N'无备注', DEFAULT, DEFAULT);
set IDENTITY_INSERT relationship_student_teacher ON
INSERT INTO relationship_student_teacher (id, student_id, teacher_id, note, created_at, updated_at)
VALUES ('20', '10', '2', N'无备注', DEFAULT, DEFAULT);
set IDENTITY_INSERT relationship_student_teacher OFF;

DROP TABLE IF EXISTS teacher;
CREATE TABLE teacher
(
    id         int         NOT NULL identity (18,1),
    name       varchar(20) NOT NULL DEFAULT '',
    age        tinyint     NOT NULL DEFAULT '0',
    sex        tinyint     NOT NULL DEFAULT '1',
    subject    varchar(20) NOT NULL DEFAULT '',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
set IDENTITY_INSERT teacher on
INSERT INTO teacher (id, name, age, sex, subject, created_at, updated_at)
VALUES ('1', N'张淑明', '22', '2', N'会计', DEFAULT, DEFAULT);
set IDENTITY_INSERT teacher on
INSERT INTO teacher (id, name, age, sex, subject, created_at, updated_at)
VALUES ('2', N'腾腾', '26', '1', N'计算机', DEFAULT, DEFAULT);
set IDENTITY_INSERT teacher on
INSERT INTO teacher (id, name, age, sex, subject, created_at, updated_at)
VALUES ('6', N'谭明佳', '22', '2', N'会计', DEFAULT, DEFAULT);
set IDENTITY_INSERT teacher on
INSERT INTO teacher (id, name, age, sex, subject, created_at, updated_at)
VALUES ('8', N'文松', '22', '2', N'会计', DEFAULT, DEFAULT);
set IDENTITY_INSERT teacher OFF;

DROP TABLE IF EXISTS student;
CREATE TABLE student
(
    id         int         NOT NULL identity (20,1),
    name       varchar(20) NOT NULL DEFAULT '',
    age        tinyint     NOT NULL DEFAULT '0',
    sex        tinyint     NOT NULL DEFAULT '1',
    teacher_id int         NOT NULL DEFAULT '0',
    is_deleted tinyint     NOT NULL default 0,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
set IDENTITY_INSERT student on
INSERT INTO student (id, name, age, sex, teacher_id, is_deleted, created_at, updated_at)
VALUES ('1', N'小明', '6', '2', '6', 0, DEFAULT, DEFAULT);
set IDENTITY_INSERT student on
INSERT INTO student (id, name, age, sex, teacher_id, is_deleted, created_at, updated_at)
VALUES ('2', N'小张', '11', '2', '6', 0, DEFAULT, DEFAULT);
set IDENTITY_INSERT student on
INSERT INTO student (id, name, age, sex, teacher_id, is_deleted, created_at, updated_at)
VALUES ('3', N'小腾', '16', '1', '6', 0, DEFAULT, DEFAULT);
set IDENTITY_INSERT student on
INSERT INTO student (id, name, age, sex, teacher_id, is_deleted, created_at, updated_at)
VALUES ('4', N'小云', '11', '2', '6', 0, DEFAULT, DEFAULT);
set IDENTITY_INSERT student on
INSERT INTO student (id, name, age, sex, teacher_id, is_deleted, created_at, updated_at)
VALUES ('5', N'小卡卡', '11', '2', '1', 0, DEFAULT, DEFAULT);
set IDENTITY_INSERT student on
INSERT INTO student (id, name, age, sex, teacher_id, is_deleted, created_at, updated_at)
VALUES ('6', N'非卡', '16', '1', '1', 0, DEFAULT, DEFAULT);
set IDENTITY_INSERT student on
INSERT INTO student (id, name, age, sex, teacher_id, is_deleted, created_at, updated_at)
VALUES ('7', N'狄龙', '17', '1', '2', 0, DEFAULT, DEFAULT);
set IDENTITY_INSERT student on
INSERT INTO student (id, name, age, sex, teacher_id, is_deleted, created_at, updated_at)
VALUES ('8', N'金庸', '17', '1', '2', 0, DEFAULT, DEFAULT);
set IDENTITY_INSERT student on
INSERT INTO student (id, name, age, sex, teacher_id, is_deleted, created_at, updated_at)
VALUES ('9', N'莫西卡', '17', '1', '8', 0, DEFAULT, DEFAULT);
set IDENTITY_INSERT student on
INSERT INTO student (id, name, age, sex, teacher_id, is_deleted, created_at, updated_at)
VALUES ('10', N'象帕', '15', '1', '0', 0, DEFAULT, DEFAULT);
set IDENTITY_INSERT student OFF;

DROP TABLE IF EXISTS people;
CREATE TABLE people
(
    id         bigint      NOT NULL identity (20,1),
    name       varchar(20) NOT NULL DEFAULT '',
    age        tinyint     NOT NULL DEFAULT '0',
    sex        tinyint     NOT NULL DEFAULT '1',
    teacher_id int         NOT NULL DEFAULT '0',
    is_deleted tinyint     NOT NULL default 0,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
set IDENTITY_INSERT people ON
INSERT INTO people (id, name, age, sex, teacher_id, is_deleted, created_at, updated_at)
VALUES ('1', N'小明', '6', '2', '6', 0, DEFAULT, DEFAULT);
set IDENTITY_INSERT people ON
INSERT INTO people (id, name, age, sex, teacher_id, is_deleted, created_at, updated_at)
VALUES ('2', N'小张', '11', '2', '6', 0, DEFAULT, DEFAULT);
set IDENTITY_INSERT people ON
INSERT INTO people (id, name, age, sex, teacher_id, is_deleted, created_at, updated_at)
VALUES ('3', N'小腾', '16', '1', '6', 0, DEFAULT, DEFAULT);
set IDENTITY_INSERT people ON
INSERT INTO people (id, name, age, sex, teacher_id, is_deleted, created_at, updated_at)
VALUES ('4', N'小云', '11', '2', '6', 0, DEFAULT, DEFAULT);
set IDENTITY_INSERT people ON
INSERT INTO people (id, name, age, sex, teacher_id, is_deleted, created_at, updated_at)
VALUES ('5', N'小卡卡', '11', '2', '1', 0, DEFAULT, DEFAULT);
set IDENTITY_INSERT people ON
INSERT INTO people (id, name, age, sex, teacher_id, is_deleted, created_at, updated_at)
VALUES ('6', N'非卡', '16', '1', '1', 0, DEFAULT, DEFAULT);
set IDENTITY_INSERT people ON
INSERT INTO people (id, name, age, sex, teacher_id, is_deleted, created_at, updated_at)
VALUES ('7', N'狄龙', '17', '1', '2', 0, DEFAULT, DEFAULT);
set IDENTITY_INSERT people ON
INSERT INTO people (id, name, age, sex, teacher_id, is_deleted, created_at, updated_at)
VALUES ('8', N'金庸', '17', '1', '2', 0, DEFAULT, DEFAULT);
set IDENTITY_INSERT people ON
INSERT INTO people (id, name, age, sex, teacher_id, is_deleted, created_at, updated_at)
VALUES ('9', N'莫西卡', '17', '1', '8', 0, DEFAULT, DEFAULT);
set IDENTITY_INSERT people ON
INSERT INTO people (id, name, age, sex, teacher_id, is_deleted, created_at, updated_at)
VALUES ('10', N'象帕', '15', '1', '0', 0, DEFAULT, DEFAULT);
set IDENTITY_INSERT people OFF;
