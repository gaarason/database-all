# database

[![](https://jitpack.io/v/gaarason/database-all.svg)](https://jitpack.io/#gaarason/database-all)
[![](https://img.shields.io/github/stars/gaarason/database-all)](https://github.com/gaarason/database-all)

Eloquent ORM for Java

## 简介

- 让连接数据库以及对数据库进行增删改查操作变得非常简单，不论希望使用原生 SQL、还是查询构造器，还是 Eloquent ORM。

- Eloquent ORM 提供一个美观、简单的与数据库打交道的 ActiveRecord
  实现，每个数据表都对应一个与该表数据结构对应的实体（Entity），以及的进行交互的模型（Model），通过模型类，你可以对数据表进行查询、插入、更新、删除等操作，并将结果反映到实体实例化的 java 对象中。

- 对于关联关系 Eloquent ORM 提供了富有表现力的声明方式，与简洁的使用方法，并专注在内部进行查询与内存优化，在复杂的关系中有仍然有着良好的体验。

- 兼容于其他常见的 ORM 框架, 以及常见的数据源 (DataSource)

## 目录

* [注册配置](/document/bean.md)
* [数据映射](/document/mapping.md)
* [数据模型](/document/model.md)
* [查询结果集](/document/record.md)
* [查询构造器](/document/query.md)
* [关联关系](/document/relationship.md)
* [生成代码](/document/generate.md)
* [GraalVM](/document/graalvm.md)
* [版本信息](/document/version.md)

- 以如下的方式在程序中查询数据
- 查询 model.newQuery().select().where().get().toObject();
- 更新 model.newQuery().data().where().update();
- 删除 model.newQuery().where().delete();
- 新增 model.newQuery().column().value().insert();

```java
// 查询id为4的一条数据 
// select * from student where id = 4 limit 1
Student student = studentModel.find(4).toObject();

// 查询id为4的一条数据 
// select * from student where id = 4 limit 1
Student student = studentModel.newQuery().query("select * from student where id= ? limit ? ", 4, 1).toObject();

// 表达式列名风格 
// select name,age from student where id in (1, 2, 3)
List<Student> students = studentModel.newQuery()
        .select(Student::getName).select(Student::getAge)
        .whereIn(Student::getId, 1, 2, 3)
        .get().toObjectList();

// 稍复杂嵌套的语句 
// select id,name from student where id=3 or(age>11 and id=7 and(id between 4 and 10 and age>11))
List<Student> students = studentModel.newQuery().where("id", "3").orWhere(
                builder->builder.where("age", ">", "11").where("id", "7").andWhere(
            builder2->builder2.whereBetween("id", "4", "10").where("age", ">", "11")
        )
).select("id", "name").get().toObjectList();

// 关联查询 找出学生们的老师们的父亲们的那些房子
// select * from student where id in (1, 2, 3)
// select * from teacher where id in (?, ?, ?)
// select * from father where id in (?, ?, ?)
// select * from house where owner_id in (?, ?, ?)
List<Student> students = studentModel.newQuery().whereIn("id", 1, 2, 3).get().with("teacher.father.house").toObjectList();

// 增加关联 给id为8的学生增加3名老师(id分别为1,2,3), 已存在的不重复添加
// select * from student where id = 8 limit 1
// select * from relation_student_teacher where student_id = 8 and teacher_id in (1, 2, 3)
// insert into relation_student_teacher set student_id = 8 and teacher_id = 3
studentModel.findOrFail(8).bind("teachers").attach( 1, 2, 3 );
```

## spring boot 快速开始

1.引入仓库 pom.xml

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

2.引入依赖 pom.xml

**latest-version**：![](https://jitpack.io/v/gaarason/database-all.svg)

```xml
<dependency>
    <groupId>com.github.gaarason.database-all</groupId>
    <artifactId>database-spring-boot-starter</artifactId>
    <version>{latest-version}</version>
</dependency>
```

3.配置连接 application.properties

```properties
spring.datasource.url=jdbc:mysql://mysql.local/test_master_0?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# 使用你喜欢的 datasource, 这边增加了 DruidDataSource 的支持, 使其符合 Spring 的指定风格
spring.datasource.type=com.alibaba.druid.pool.DruidDataSource

# 雪花算法工作id, 默认是0
# gaarason.database.snow-flake.worker-id=1

# 包扫描路径, 默认是`@SpringBootApplication`所在的包
# gaarason.database.scan.packages=you.package1,you.package2
```

4.快速开始

使用预置的 `GeneralModel` ，无需其他定义，即可进行查询。

```java
@Resource
GeneralModel generalModel;

@Test
public void 简单查询() {

    // select * from student where id= 3 limit 1
    Record<GeneralModel.Table,Object> record = generalModel.newQuery().from("student").where("id", 3).firstOrFail();

    // 结果转化到map
    Map<String, Object> stringObjectMap = record.toMap();

    System.out.println(stringObjectMap);
}

```
