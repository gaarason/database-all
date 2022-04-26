# database
[![](https://jitpack.io/v/gaarason/database-all.svg)](https://jitpack.io/#gaarason/database-all)  

Eloquent ORM for Java

## 简介

- 让连接数据库以及对数据库进行增删改查操作变得非常简单，不论希望使用原生 SQL、还是查询构建器，还是 Eloquent ORM。  
      
- Eloquent ORM 提供一个美观、简单的与数据库打交道的 ActiveRecord 实现，每张数据表都对应一个与该表数据结构对应的实体（Entity），以及的进行交互的模型（Model），通过模型类，你可以对数据表进行查询、插入、更新、删除等操作，并将结果反映到实体实例化的 java 对象中。  
 
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
* [版本信息](/document/version.md)


- 以如下的方式在程序中查询数据
- 查询 model.newQuery().select().where().get().toObject();
- 更新 model.newQuery().data().where().update();
- 删除 model.newQuery().where().delete();
- 新增 model.newQuery().select().value().insert();

```java
// 查询id为4的一条数据
Student student = studentModel.find(4).toObject();


// 稍复杂嵌套的语句 select id,name from student where id=3 or(age>11 and id=7 and(id between 4 and 10 and age>11))
List<Student> Students = studentModel.newQuery().where("id", "3").orWhere(
    builder -> builder.where("age", ">", "11").where("id", "7").andWhere(
        builder2 -> builder2.whereBetween("id", "4", "10").where("age", ">", "11")
    )
).select("id", "name").get().toObjectList();


// 关联查询 找出学生们的老师们的父亲们的那些房子
List<Student> Students = studentModel.newQuery().whereIn("id", "1","2","3").get().with("teacher.father.house").toObjectList();


// 增加关联 给id为8的学生增加3名老师(id分别为1,2,3)
studentModel.findOrFail(8).bind("teachers").attach( teacherModel.findMany(1,2,3) );
```
## spring boot 快速开始

1.引入仓库 pom.xml  
```$xslt
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
2.引入依赖 pom.xml  
```$xslt
<dependency>
    <groupId>com.github.gaarason.database-all</groupId>
    <artifactId>database-spring-boot-starter</artifactId>
    <version>RELEASE</version>
</dependency>
```
3.配置连接 application.properties  
```$xslt
spring.datasource.url=jdbc:mysql://mysql.local/test_master_0?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
# choose the type as u like
spring.datasource.type=com.alibaba.druid.pool.DruidDataSource

gaarason.database.snow-flake.worker-id=2
```
4.快速开始  
```java
@Resource
GeneralModel generalModel;

@Test
public void 简单查询() {
    // select * from student where id=3 limit 1
    Record<GeneralModel.Table, Object> record = generalModel.newQuery().from("student").where("id", "3").firstOrFail();
    
    Map<String, Object> stringObjectMap = record.toMap();
    
    System.out.println(stringObjectMap);
}

```
