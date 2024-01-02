# database

[![](https://jitpack.io/v/gaarason/database-all.svg)](https://jitpack.io/#gaarason/database-all)
[![](https://img.shields.io/github/stars/gaarason/database-all)](https://github.com/gaarason/database-all)

Eloquent ORM for Java

## 简介 Introduction

- 让连接数据库以及对数据库进行增删改查操作变得非常简单，不论希望使用原生 SQL、还是查询构造器，还是 Eloquent ORM。
- It makes connecting to the database and adding, deleting, modifying and querying the database very simple, whether you want to use native SQL, query builder, or Eloquent ORM.

- Eloquent ORM 提供一个美观、简单的与数据库打交道的 ActiveRecord
  实现，每个数据表都对应一个与该表数据结构对应的实体（Entity），以及的进行交互的模型（Model），通过模型类，你可以对数据表进行查询、插入、更新、删除等操作，并将结果反映到实体实例化的 java 对象中。
- Eloquent ORM provides a beautiful and simple ActiveRecord for working with databases
  Implementation, each data table corresponds to an entity (Entity) corresponding to the data structure of the table, and a model (Model) for interaction. Through the model class, you can query, insert, update, delete and other operations on the data table. , and reflect the results into the java object instantiated by the entity.

- 对于关联关系 Eloquent ORM 提供了富有表现力的声明方式，与简洁的使用方法，并专注在内部进行查询与内存优化，在复杂的关系中有仍然有着良好的体验。
- For relationships, Eloquent ORM provides expressive declaration methods and concise usage methods. It focuses on internal query and memory optimization, and still has a good experience in complex relationships.

- 支持原生Java8,Java11,Java17应用, 支持SpringBoot 2x 以及 3x ,兼容于其他常见的 ORM 框架, 以及常见的数据源 (DataSource)。
- Supports native Java8, Java11, Java17 applications, supports SpringBoot 2x and 3x, and is compatible with other common ORM frameworks and common data sources (DataSource).

## 目录

* [注册配置 Configuration](/document/bean.md)
* [数据映射 Mapping](/document/mapping.md)
* [数据模型 Model](/document/model.md)
* [查询结果集 Record](/document/record.md)
* [查询构造器 Query Builder](/document/query.md)
* [关联关系 Relationship](/document/relationship.md)
* [生成代码 Generate](/document/generate.md)
* [GraalVM](/document/graalvm.md)
* [版本信息 Version](/document/version.md)

- 以如下的方式在程序中查询数据 Query data in the program in the following way
- 查询 Select : model.newQuery().select().where().get().toObject();
- 更新 Update : model.newQuery().data().where().update();
- 删除 Delete : model.newQuery().where().delete();
- 插入 Insert : model.newQuery().column().value().insert();

```java
// select * from student where id = 4 limit 1
Student student = studentModel.find(4).toObject();

// select * from student where id = 4 limit 1
Student student = studentModel.newQuery().query("select * from student where id= ? limit ? ", 4, 1).toObject();

// select name,age from student where id in (1, 2, 3)
List<Student> students = studentModel.newQuery()
        .select(Student::getName).select(Student::getAge)
        .whereIn(Student::getId, 1, 2, 3)
        .get().toObjectList();

// select id,name from student where id=3 or(age>11 and id=7 and(id between 4 and 10 and age>11))
List<Student> students = studentModel.newQuery().where("id", "3").orWhere(
                builder->builder.where("age", ">", "11").where("id", "7").andWhere(
            builder2->builder2.whereBetween("id", "4", "10").where("age", ">", "11")
        )
).select("id", "name").get().toObjectList();

// select * from student where id in (1, 2, 3)
// select * from teacher where id in (?, ?, ?)
// select * from father where id in (?, ?, ?)
// select * from house where owner_id in (?, ?, ?)
List<Student> students = studentModel.newQuery().whereIn("id", 1, 2, 3).get().with("teacher.father.house").toObjectList();

// select * from student where id = 8 limit 1
// select * from relation_student_teacher where student_id = 8 and teacher_id in (1, 2, 3)
// insert into relation_student_teacher set student_id = 8 and teacher_id = 3
studentModel.findOrFail(8).bind("teachers").attach( 1, 2, 3 );
```

## Spring boot Quick start

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
# 非 SpringBoot, 建议手动指定包扫描路径
# gaarason.database.scan.packages=you.package1,you.package2
```

4.快速开始 quick start

使用预置的 `GeneralModel` ，无需其他定义，即可进行查询。   
Using the pre-built `GeneralModel`, no additional definitions are required to query.

```java
@Resource
GeneralModel generalModel;

@Test
public void simpleQuery() {

    // select * from student where id = 3 limit 1
    Record<?, ?> record = generalModel.newQuery().from("student").where("id", 3).firstOrFail();

    // to map
    Map<String, Object> stringObjectMap = record.toMap();

    System.out.println(stringObjectMap);
}

```

## 完整体验 Complete experience

- 借助 [生成代码 Generate](/document/generate.md), 自动化地为每个数据表都定义一个与该表数据结构对应的实体（Entity）， 以及的进行交互的模型（Model）
- With the help of [生成代码 Generate](/document/generate.md), each data table is automatically defined with an entity (Entity) corresponding to the data structure of the table, and a model (Model) for interaction.
- 在模型（Model）中, 通过 [查询构造器 Query Builder](/document/query.md) 你可以对数据表进行查询、插入、更新、删除等操作，并将结果反映到 [查询结果集 Record](/document/record.md) 中
- In the model, through the [查询构造器 Query Builder](/document/query.md) you can query, insert, update, delete and other operations on the data table, and reflect the results to the [查询结果集 Record](/document/record.md) in
- 在 [查询结果集 Record](/document/record.md) 中可以快速的将结果转化为的 java 实体（Entity）对象， 以及其他数据结构以及处理操作
- In [查询结果集 Record](/document/record.md), the results can be quickly converted into java entity objects, as well as other data structures and processing operations.
- 通过在实体（Entity）中应用各种的声明式注解进行 [数据映射 Mapping](/document/mapping.md)， 便可以方便的在模型（Model）中应用诸如 [关联关系 Relationship](/document/relationship.md)、ORM以及各种自定义操作
- By applying various declarative annotations to [数据映射 Mapping](/document/mapping.md) in the Entity, you can easily apply [关联关系 Relationship](/document/relationship.md), ORM and various custom operations