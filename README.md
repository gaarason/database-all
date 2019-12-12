# database
Eloquent ORM for Java
## 目录
* [注册bean](/document/bean.md)
* [数据映射](/document/mapping.md)
* [数据模型](/document/model.md)
* [查询结果集](/document/record.md)
* [查询构造器](/document/query.md)
* [反向生成代码](/document/generate.md)


- 如果希望以如下的方式在程序中查询数据, 那么这个项目不会让你失望 !
```java
// 查询id为4的一条数据
Student student = studentModel.find("4").toObject();

// select id,name from student where id=3 or(age>11 and id=7 and(id between 4 and 10 and age>11))
List<Student> Students = studentModel.newQuery().where("id", "3").orWhere(
    builder -> builder.where("age", ">", "11").where("id", "7").andWhere(
        builder2 -> builder2.whereBetween("id", "4", "10").where("age", ">", "11")
    )
).select("id", "name").get().toObjectList();
```
## spring boot 快速开始

1. 引入依赖 pom.xml
```$xslt
<dependency>
    <groupId>gaarason</groupId>
    <artifactId>database-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
2. 配置连接 application.properties
```$xslt
spring.datasource.druid.url=jdbc:mysql://sakya.local/test_master_0?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai
spring.datasource.druid.username=root
spring.datasource.druid.password=root
spring.datasource.druid.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.druid.db-type=com.alibaba.druid.pool.DruidDataSource
```
3. 快速开始 
```java
package gaarason.database.spring.boot.starter.test;

import gaarason.database.eloquent.GeneralModel;
import gaarason.database.eloquent.Record;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.JVM)
public class TestApplicationTests {

    @Resource
    GeneralModel generalModel;

    @Test
    public void 简单查询() {
        Record<GeneralModel.Table> first = generalModel.newQuery().from("student").where("id", "3").first();
        Assert.assertNotNull(first);
        Map<String, Object> stringObjectMap = first.toMap();
        Assert.assertEquals((long) stringObjectMap.get("id"), 3);
        System.out.println(stringObjectMap);
    }

}
```
