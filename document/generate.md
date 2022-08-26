# database

Eloquent ORM for Java

## 目录

* [注册配置](/document/bean.md)
* [数据映射](/document/mapping.md)
* [数据模型](/document/model.md)
* [查询结果集](/document/record.md)
* [查询构造器](/document/query.md)
* [关联关系](/document/relationship.md)
* [生成代码](/document/generate.md)
    * [总览](#总览)
    * [非spring](#非spring)
    * [spring](#spring)
* [版本信息](/document/version.md)

## 总览

通过数据库连接信息, 自动生成代码(`entity`,`model`)

## 非spring

1.引入仓库 pom.xml

```$xslt
<query>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</query>
```

2.引入依赖 pom.xml

```$xslt
<dependency>
    <groupId>com.github.gaarason.database-all</groupId>
    <artifactId>database-generator</artifactId>
    <version>RELEASE</version>
</dependency>
```

3.编写单元测试

```java
package gaarason.database.generator.test;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.connection.GaarasonDataSourceWrapper;
import gaarason.database.eloquent.Model;
import gaarason.database.generator.Generator;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class GeneratorTests {

    // 推荐
    @Test
    public void run有参构造() {
        String jdbcUrl = "jdbc:mysql://mysql.local/test_master_0?useUnicode=true&characterEncoding=utf-8" +
                "&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "root";
        Generator generator = new Generator(jdbcUrl, username, password);
          // 风格切换
        generator.setStyle(Style.ENTITY);
        // set
        generator.setOutputDir("./src/test/java/");     // 所有生成文件的路径
  //        generator.setOutputDir("./src/test/java1/");     // 所有生成文件的路径
        generator.setNamespace("gaarason.database.test.models.relation.pojo");                 // 所有生成文件的所属命名空间
        generator.setCorePoolSize(20);                  // 所用的线程数
        generator.setSpringBoot(true);                // 是否生成spring boot相关注解
        generator.setSwagger(false);                   // 是否生成swagger相关注解
        generator.setValidator(false);                 // 是否生成validator相关注解
  
        generator.setEntityStaticField(false);          // 是否在实体中生成静态字段
        generator.setBaseEntityDir("base");             // 实体父类的相对路径
        generator.setBaseEntityFields("id");            // 实体父类存在的字段
        generator.setBaseEntityName("BaseEntity");      // 实体父类的类名
        generator.setEntityDir("entity");               // 实体的相对路径
        generator.setEntityPrefix("");                  // 实体的类名前缀
        generator.setEntitySuffix("");                  // 实体的类名后缀

//      generator.setColumnDisSelectable("created_at", "updated_at");             // 字段, 不可查询
//
//      generator.setColumnFill(FieldFill.NotFill.class, "created_at", "updated_at");  // 字段, 填充方式
//
//      generator.setColumnStrategy(FieldStrategy.Default.class, "created_at", "updated_at");   // 字段, 使用策略
//      generator.setColumnInsertStrategy(FieldStrategy.Never.class, "created_at", "updated_at");   // 字段, 新增使用策略
//      generator.setColumnUpdateStrategy(FieldStrategy.Never.class, "created_at", "updated_at");   // 字段, 更新使用策略
//      generator.setColumnConditionStrategy(FieldStrategy.Default.class, "created_at", "updated_at");   // 字段, 条件使用策略
//
//      generator.setColumnConversion(FieldConversion.Default.class, "created_at", "updated_at");   // 字段, 序列化与反序列化方式

        generator.setBaseModelDir("base");              // 模型父类的相对路径
        generator.setBaseModelName("BaseModel");        // 模型父类的类名
        generator.setModelDir("model");                 // 模型的相对路径
        generator.setModelPrefix("");                   // 模型的类名前缀
        generator.setModelSuffix("Model");              // 模型的类名后缀
  
        // 执行
        generator.run();
    }

    @Test
    public void run无参构造() {
        ProxyDataSource gaarasonDataSourceWrapper = gaarasonDataSourceWrapper();
        ToolModel toolModel = new ToolModel(gaarasonDataSourceWrapper);
        AutoGenerator autoGenerator = new AutoGenerator(toolModel);
        // set
        autoGenerator.setStaticField(true);
        autoGenerator.setIsSpringBoot(true);
        autoGenerator.setCorePoolSize(20);
        autoGenerator.setOutputDir("./src/test/java/");
        autoGenerator.setNamespace("test.data");
        autoGenerator.setDisInsertable("created_at", "updated_at");
        autoGenerator.setDisUpdatable("created_at", "updated_at");

        autoGenerator.run();
    }

    private DataSource dataSourceMaster0() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(
            "jdbc:mysql://mysql.local/test_master_0?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("root");

        druidDataSource.setDbType("com.alibaba.druid.pool.DruidDataSource");
        druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        druidDataSource.setInitialSize(20);
        druidDataSource.setMaxActive(20);
        druidDataSource.setLoginTimeout(3);
        druidDataSource.setQueryTimeout(3);
        return druidDataSource;
    }

    private List<DataSource> dataSourceMasterList() {
        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(dataSourceMaster0());
        return dataSources;
    }

    private ProxyDataSource gaarasonDataSourceWrapper() {
        List<DataSource> dataSources = dataSourceMasterList();
        return new ProxyDataSource(dataSources);
    }

    public static class ToolModel extends Model<ToolModel.Inner, Object> {
        private ProxyDataSource gaarasonDataSourceWrapper;
        public ToolModel(ProxyDataSource dataSource) {
            gaarasonDataSourceWrapper = dataSource;
        }
        public ProxyDataSource getProxyDataSource() {
            return gaarasonDataSourceWrapper;
        }
        public static class Inner {
        }
    }

    public class AutoGenerator extends Generator {
        private Model toolModel;
        public AutoGenerator(Model model) {
            toolModel = model;
        }
        public Model getModel() {
            return toolModel;
        }

    }
}

```

## spring

1.引入仓库 pom.xml

```$xslt
<query>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</query>
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
spring.datasource.druid.url=jdbc:mysql://mysql.local/test_master_0?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai
spring.datasource.druid.username=root
spring.datasource.druid.password=root
spring.datasource.druid.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.druid.db-type=com.alibaba.druid.pool.DruidDataSource
```

4.编写单元测试

```java
package gaarason.database.spring.boot.starter.test;

import gaarason.database.eloquent.GeneralModel;
import gaarason.database.eloquent.Record;
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
    GeneralGenerator generator;

    // 执行此方法即可生成    
    @Test
    public void 生成代码() {
        // set
        // 风格切换
        generator.setStyle(Style.ENTITY);
        // set
        generator.setOutputDir("./src/test/java/");     // 所有生成文件的路径
        //        generator.setOutputDir("./src/test/java1/");     // 所有生成文件的路径
        generator.setNamespace("gaarason.database.test.models.relation.pojo");                 // 所有生成文件的所属命名空间
        generator.setCorePoolSize(20);                  // 所用的线程数
        generator.setSpringBoot(true);                // 是否生成spring boot相关注解
        generator.setSwagger(false);                   // 是否生成swagger相关注解
        generator.setValidator(false);                 // 是否生成validator相关注解
  
        generator.setEntityStaticField(false);          // 是否在实体中生成静态字段
        generator.setBaseEntityDir("base");             // 实体父类的相对路径
        generator.setBaseEntityFields("id");            // 实体父类存在的字段
        generator.setBaseEntityName("BaseEntity");      // 实体父类的类名
        generator.setEntityDir("entity");               // 实体的相对路径
        generator.setEntityPrefix("");                  // 实体的类名前缀
        generator.setEntitySuffix("");                  // 实体的类名后缀

//      generator.setColumnDisSelectable("created_at", "updated_at");             // 字段, 不可查询
//
//      generator.setColumnFill(FieldFill.NotFill.class, "created_at", "updated_at");  // 字段, 填充方式
//
//      generator.setColumnStrategy(FieldStrategy.Default.class, "created_at", "updated_at");   // 字段, 使用策略
//      generator.setColumnInsertStrategy(FieldStrategy.Never.class, "created_at", "updated_at");   // 字段, 新增使用策略
//      generator.setColumnUpdateStrategy(FieldStrategy.Never.class, "created_at", "updated_at");   // 字段, 更新使用策略
//      generator.setColumnConditionStrategy(FieldStrategy.Default.class, "created_at", "updated_at");   // 字段, 条件使用策略
//
//      generator.setColumnConversion(FieldConversion.Default.class, "created_at", "updated_at");   // 字段, 序列化与反序列化方式

        generator.setBaseModelDir("base");              // 模型父类的相对路径
        generator.setBaseModelName("BaseModel");        // 模型父类的类名
        generator.setModelDir("model");                 // 模型的相对路径
        generator.setModelPrefix("");                   // 模型的类名前缀
        generator.setModelSuffix("Model");              // 模型的类名后缀

        // 执行
        generator.run();
    }
}
```

