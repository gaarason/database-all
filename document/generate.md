# database
Eloquent ORM for Java
## 目录
* [注册bean](/document/bean.md)
* [数据映射](/document/mapping.md)
* [数据模型](/document/model.md)
* [查询结果集](/document/record.md)
* [查询构造器](/document/query.md)
* [反向生成代码](/document/generate.md)
    * [总览](#总览)
    * [非spring](#非spring)
    * [spring](#spring)
        * [定义ProxyDataSource](#定义ProxyDataSource)
        * [定义ToolModel](#定义ToolModel)
        * [定义Manager](#定义Manager)
        * [使用](#使用)
## 总览

通过数据库连接,生成模板代码

## 非spring

```java
package gaarason.database;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.connections.ProxyDataSource;
import gaarason.database.eloquent.Model;
import gaarason.database.generator.Manager;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.JVM)
@Slf4j
public class GeneratorTests {
    @Test
    public void run() {
        ProxyDataSource proxyDataSource = proxyDataSource();

        ToolModel toolModel = new ToolModel(proxyDataSource);

        AutoGenerator autoGenerator = new AutoGenerator(toolModel);
        
        // 详细设置
        autoGenerator.setNamespace("temp");
        autoGenerator.setDisInsertable("created_at", "updated_at", "created_on", "updated_on");
        autoGenerator.setDisUpdatable("created_at", "updated_at", "created_on", "updated_on");

        autoGenerator.run();
    }

    private DataSource dataSourceMaster0() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(
            "jdbc:mysql://sakya.local/test_master_0?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai");
        druidDataSource.setDbType("com.alibaba.druid.pool.DruidDataSource");
        druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("root");
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

### 定义ProxyDataSource

参考[注册bean](/document/bean.md)中,声明`ProxyDataSource`的实现

### 定义ToolModel

```java
package gaarason.database.tool;

import gaarason.database.connections.ProxyDataSource;
import gaarason.database.eloquent.Model;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ToolModel extends Model<ToolModel.Inner> {
    // 定义ProxyDataSource
    @Resource(name = "proxyDataSourceSingle")
    protected ProxyDataSource dataSource;

    public ProxyDataSource getProxyDataSource() {
        return dataSource;
    }


    public static class Inner {


    }
}
```
### 定义Manager

继承`gaarason.database.tool.Manager`

```java
package gaarason.database.tool;

import gaarason.database.eloquent.Model;
import gaarason.database.generator.Manager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class Generator extends Manager {

    // 定义ToolModel
    @Resource
    ToolModel toolModel;

    @Override
    public Model getModel() {
        return toolModel;
    }
}

```
### 使用
```java
package gaarason.database;

import gaarason.database.tool.Generator;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.JVM)
public class GeneratorTests {
    @Resource
    Generator generator;

    @Test
    public void run() {
        // 设置属性
        generator.setNamespace("temp");
        generator.setDisInsertable("created_at", "updated_at", "created_on", "updated_on");
        generator.setDisUpdatable("created_at", "updated_at", "created_on", "updated_on");
        generator.run();
    }
}
```

