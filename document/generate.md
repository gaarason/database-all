# database
Eloquent ORM for Java
## 目录
* [注册bean](/document/bean.md)
* [数据映射](/document/mapping.md)
* [数据模型](/document/model.md)
* [查询结果集](/document/record.md)
* [查询构造器](/document/query.md)
* [生成代码](/document/generate.md)
    * [总览](#总览)
    * [非spring](#非spring)
    * [spring](#spring)
## 总览

通过数据库连接信息, 自动生成代码(`entity`,`model`) 

## 非spring

1.引入仓库 pom.xml  
```$xslt
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
2.引入依赖 pom.xml  
```$xslt
<dependency>
    <groupId>com.github.gaarason.database-all</groupId>
    <artifactId>database-generator</artifactId>
    <version>1.0.5</version>
</dependency>
```
3.编写单元测试  
```java
package gaarason.database.generator.test;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.connections.ProxyDataSource;
import gaarason.database.eloquent.Model;
import gaarason.database.generator.Generator;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class GeneratorTests {

    @Test
    public void run() {
        ProxyDataSource proxyDataSource = proxyDataSource();
        ToolModel toolModel = new ToolModel(proxyDataSource);
        AutoGenerator autoGenerator = new AutoGenerator(toolModel);

        // set
        autoGenerator.setStaticField(true);
        autoGenerator.setIsSpringBoot(true);
        autoGenerator.setCorePoolSize(20);
        autoGenerator.setOutputDir("./");
        autoGenerator.setNamespace("gaarason.database.spring.boot.starter.test.data");
        String[] disableCreate = {"created_at", "updated_at"};
        autoGenerator.setDisInsertable(disableCreate);
        String[] disableUpdate = {"created_at", "updated_at"};
        autoGenerator.setDisUpdatable(disableUpdate);

        autoGenerator.run();
    }

    private DataSource dataSourceMaster0() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(
            "jdbc:mysql://sakya.local/test_master_0?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("root");
        druidDataSource.setDbType("com.alibaba.druid.pool.DruidDataSource");
        druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        druidDataSource.setInitialSize(20);
        druidDataSource.setMaxActive(20);
        return druidDataSource;
    }

    private List<DataSource> dataSourceMasterList() {
        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(dataSourceMaster0());
        return dataSources;
    }

    private ProxyDataSource proxyDataSource() {
        List<DataSource> dataSources = dataSourceMasterList();
        return new ProxyDataSource(dataSources);
    }

    public static class ToolModel extends Model<ToolModel.Inner> {
        private ProxyDataSource proxyDataSource;
        public ToolModel(ProxyDataSource dataSource) {
            proxyDataSource = dataSource;
        }
        public ProxyDataSource getProxyDataSource() {
            return proxyDataSource;
        }
        public static class Inner {
        }
    }

    public class AutoGenerator extends Manager {
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
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
2.引入依赖 pom.xml  
```$xslt
<dependency>
    <groupId>com.github.gaarason.database-all</groupId>
    <artifactId>database-spring-boot-starter</artifactId>
    <version>1.0.5</version>
</dependency>
```
3.配置连接 application.properties  
```$xslt
spring.datasource.druid.url=jdbc:mysql://sakya.local/test_master_0?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai
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
    GeneralGenerator generalGenerator;

    // 执行此方法即可生成    
    @Test
    public void 生成代码() {
        // 设置
        generalGenerator.setStaticField(true);
        generalGenerator.setIsSpringBoot(true);
        generalGenerator.setOutputDir("./src/main/java/");
        generalGenerator.setNamespace("gaarason.database.spring.boot.starter.test.data");
        String[] disableCreate = {"created_at", "updated_at"};
        generalGenerator.setDisInsertable(disableCreate);
        String[] disableUpdate = {"created_at", "updated_at"};
        generalGenerator.setDisUpdatable(disableUpdate);

        generalGenerator.run();
    }
}
```

