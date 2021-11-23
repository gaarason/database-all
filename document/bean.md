# database
Eloquent ORM for Java
## 目录
* [注册bean](/document/bean.md)
    * [spring boot](#spring)
        * [单连接](#单连接)
            * [单库连接](#单库连接)
            * [读写分离](#读写分离)
        * [多连接](#多连接)
        * [使用GaarasonDataSource](#使用GaarasonDataSource)
    * [非spring boot](#非spring)
* [数据映射](/document/mapping.md)
* [数据模型](/document/model.md)
* [查询结果集](/document/record.md)
* [查询构造器](/document/query.md)
* [关联关系](/document/relationship.md)
* [生成代码](/document/generate.md)
* [版本信息](/document/version.md)
## spring

使用 spring boot 管理 bean

### 单连接

单个数据库连接

#### 单库连接

读写都在同一数据库

bean配置 如下

```java
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import gaarason.database.connection.GaarasonDataSourceBuilder;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.eloquent.GeneralModel;
import gaarason.database.generator.GeneralGenerator;
import gaarason.database.spring.boot.starter.properties.GaarasonDatabaseProperties;
import gaarason.database.spring.boot.starter.provider.GaarasonTransactionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.util.Collections;

@Slf4j
@Configuration
@AutoConfigureBefore(DruidDataSourceAutoConfigure.class)
public class GaarasonDataSourceConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "database.master0")
    @ConditionalOnMissingBean
    public DataSource dataSourceDruidConfig() {
        log.info("-------------------- dataSource druid config init ---------------------");
        return DruidDataSourceBuilder.create().build();
    }

    @Bean
    @ConditionalOnMissingBean
    public GaarasonDataSource gaarasonDataSource() {
        log.info("-------------------- gaarasonDataSource init --------------------------");
        return GaarasonDataSourceBuilder.create().build(Collections.singletonList(dataSourceDruidConfig()));
    }

    @Bean
    @ConditionalOnMissingBean
    public GaarasonTransactionManager gaarasonTransactionManager() {
        log.info("-------------------- gaarasonTransactionManager init ------------------");
        return new GaarasonTransactionManager(gaarasonDataSource());
    }
}
```

application.properties 如下

```
driverClassName=com.mysql.cj.jdbc.Driver
type=com.alibaba.druid.pool.DruidDataSource
initialSize=5
minIdle=5
maxActive=20
maxWait=60000
timeBetweenEvictionRunsMillis=60000
minEvictableIdleTimeMillis=300000
validationQuery=SELECT 1
connectionInitSqls[0]=SET SESSION SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION'
testWhileIdle=true
testOnBorrow=false
testOnReturn=false
poolPreparedStatements=false
maxPoolPreparedStatementPerConnectionSize=-1
# 配置监控统计拦截的filters，去掉后监控界面sql无法统计，'wall'用于防火墙
# filters=stat,wall,logback
connectionProperties=druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000
useGlobalDataSourceStat=true

# 主数据源
database.master0.url=jdbc:mysql://127.0.0.1/test?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai
database.master0.username=root
database.master0.password=root
database.master0.driverClassName=${driverClassName}
database.master0.type=${type}
database.master0.initialSize=${initialSize}
database.master0.minIdle=${minIdle}
database.master0.maxActive=${maxActive}
database.master0.maxWait=${maxWait}
database.master0.timeBetweenEvictionRunsMillis=${timeBetweenEvictionRunsMillis}
database.master0.minEvictableIdleTimeMillis=${minEvictableIdleTimeMillis}
database.master0.validationQuery=${validationQuery}
database.master0.connectionInitSqls[0]=${connectionInitSqls[0]}
database.master0.testOnBorrow=${testOnBorrow}
database.master0.testOnReturn=${testOnReturn}
database.master0.poolPreparedStatements=${poolPreparedStatements}
database.master0.maxPoolPreparedStatementPerConnectionSize=${maxPoolPreparedStatementPerConnectionSize}
# database.master0.filters=stat,wall,logback
database.master0.connectionProperties=${connectionProperties}
database.master0.useGlobalDataSourceStat=${useGlobalDataSourceStat}
```

#### 读写分离

```java
package gaarason.database.spring;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.connection.GaarasonDataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

import javax.sql.DataSource;
import java.util.*;

@Configuration
@PropertySource( value = {"database.properties"})
public class BeanConfiguration {

    // 主要写库1
    @Bean
    @ConfigurationProperties(prefix = "database.master0")
    public DataSource dataSourceMaster0() {
        return DruidDataSourceBuilder.create().build();
    }

    // 写库2
    @Bean
    @ConfigurationProperties(prefix = "database.master1")
    public DataSource dataSourceMaster1() {
        return DruidDataSourceBuilder.create().build();
    }

    // 读库1
    @Bean
    @ConfigurationProperties(prefix = "database.slave0")
    public DataSource dataSourceSlave0() {
        return DruidDataSourceBuilder.create().build();
    }

    // 读库2
    @Bean
    @ConfigurationProperties(prefix = "database.slave1")
    public DataSource dataSourceSlave1() {
        return DruidDataSourceBuilder.create().build();
    }


    @Bean
    @ConditionalOnMissingBean
    public GaarasonDataSource gaarasonDataSource() {
        List<DataSource> dataSourceList = new ArrayList<>();
        dataSourceList.add(dataSourceMaster0());
        dataSourceList.add(dataSourceMaster1());
        List<DataSource> readDataSourceList = new ArrayList<>();
        readDataSourceList.add(dataSourceSlave0());
        readDataSourceList.add(dataSourceSlave1());
        return GaarasonDataSourceBuilder.create().build(dataSourceList, readDataSourceList);
    }

    @Bean
    @ConditionalOnMissingBean
    public GaarasonTransactionManager gaarasonTransactionManager() {
        log.info("-------------------- gaarasonTransactionManager init ------------------");
        return new GaarasonTransactionManager(gaarasonDataSource());
    }

}
```

application.properties 如下

```
driverClassName=com.mysql.cj.jdbc.Driver
type=com.alibaba.druid.pool.DruidDataSource
initialSize=5
minIdle=5
maxActive=20
maxWait=60000
timeBetweenEvictionRunsMillis=60000
minEvictableIdleTimeMillis=300000
validationQuery=SELECT 1
connectionInitSqls[0]=SET SESSION SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION'
testWhileIdle=true
testOnBorrow=false
testOnReturn=false
poolPreparedStatements=false
maxPoolPreparedStatementPerConnectionSize=-1
# 配置监控统计拦截的filters，去掉后监控界面sql无法统计，'wall'用于防火墙
# filters=stat,wall,logback
connectionProperties=druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000
useGlobalDataSourceStat=true


# 主数据源
database.master0.url=jdbc:mysql://localhost/test?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai
database.master0.username=root
database.master0.password=root
database.master0.driverClassName=${driverClassName}
database.master0.type=${type}
database.master0.initialSize=${initialSize}
database.master0.minIdle=${minIdle}
database.master0.maxActive=${maxActive}
database.master0.maxWait=${maxWait}
database.master0.timeBetweenEvictionRunsMillis=${timeBetweenEvictionRunsMillis}
database.master0.minEvictableIdleTimeMillis=${minEvictableIdleTimeMillis}
database.master0.validationQuery=${validationQuery}
database.master0.connectionInitSqls[0]=${connectionInitSqls[0]}
database.master0.testOnBorrow=${testOnBorrow}
database.master0.testOnReturn=${testOnReturn}
database.master0.poolPreparedStatements=${poolPreparedStatements}
database.master0.maxPoolPreparedStatementPerConnectionSize=${maxPoolPreparedStatementPerConnectionSize}
# database.master0.filters=stat,wall,logback
database.master0.connectionProperties=${connectionProperties}
database.master0.useGlobalDataSourceStat=${useGlobalDataSourceStat}


database.master1.url=jdbc:mysql://localhost/test?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai
database.master1.username=root
database.master1.password=root
database.master1.driverClassName=${driverClassName}
database.master1.type=${type}
database.master1.initialSize=${initialSize}
database.master1.minIdle=${minIdle}
database.master1.maxActive=${maxActive}
database.master1.maxWait=${maxWait}
database.master1.timeBetweenEvictionRunsMillis=${timeBetweenEvictionRunsMillis}
database.master1.minEvictableIdleTimeMillis=${minEvictableIdleTimeMillis}
database.master1.validationQuery=${validationQuery}
database.master1.connectionInitSqls[0]=${connectionInitSqls[0]}
database.master1.testWhileIdle=${testWhileIdle}
database.master1.testOnBorrow=${testOnBorrow}
database.master1.testOnReturn=${testOnReturn}
database.master1.poolPreparedStatements=${poolPreparedStatements}
database.master1.maxPoolPreparedStatementPerConnectionSize=${maxPoolPreparedStatementPerConnectionSize}
#database.master1.filters=stat,wall,logback
database.master1.connectionProperties=${connectionProperties}
database.master1.useGlobalDataSourceStat=${useGlobalDataSourceStat}

# 从数据源
database.slave0.type=${type}
database.slave0.driverClassName=${driverClassName}
database.slave0.url=jdbc:mysql://localhost/test?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai
database.slave0.username=root
database.slave0.password=root
database.slave0.initialSize=${initialSize}
database.slave0.minIdle=${minIdle}
database.slave0.maxActive=${maxActive}
database.slave0.maxWait=${maxWait}
database.slave0.timeBetweenEvictionRunsMillis=${timeBetweenEvictionRunsMillis}
database.slave0.minEvictableIdleTimeMillis=${minEvictableIdleTimeMillis}
database.slave0.validationQuery=${validationQuery}
database.slave0.connectionInitSqls[0]=${connectionInitSqls[0]}
database.slave0.testWhileIdle=${testWhileIdle}
database.slave0.testOnBorrow=${testOnBorrow}
database.slave0.testOnReturn=${testOnReturn}
database.slave0.poolPreparedStatements=${poolPreparedStatements}
database.slave0.maxPoolPreparedStatementPerConnectionSize=${maxPoolPreparedStatementPerConnectionSize}
#database.slave0.filters=stat,wall,logback
database.slave0.connectionProperties=${connectionProperties}
database.slave0.useGlobalDataSourceStat=${useGlobalDataSourceStat}

database.slave1.type=${type}
database.slave1.driverClassName=${driverClassName}
database.slave1.url=jdbc:mysql://localhost/test?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai
database.slave1.username=root
database.slave1.password=root
database.slave1.initialSize=${initialSize}
database.slave1.minIdle=${minIdle}
database.slave1.maxActive=${maxActive}
database.slave1.maxWait=${maxWait}
database.slave1.timeBetweenEvictionRunsMillis=${timeBetweenEvictionRunsMillis}
database.slave1.minEvictableIdleTimeMillis=${minEvictableIdleTimeMillis}
database.slave1.validationQuery=${validationQuery}
database.slave1.connectionInitSqls[0]=${connectionInitSqls[0]}
database.slave1.testWhileIdle=${testWhileIdle}
database.slave1.testOnBorrow=${testOnBorrow}
database.slave1.testOnReturn=${testOnReturn}
database.slave1.poolPreparedStatements=${poolPreparedStatements}
database.slave1.maxPoolPreparedStatementPerConnectionSize=${maxPoolPreparedStatementPerConnectionSize}
#database.slave1.filters=stat,wall,logback
database.slave1.connectionProperties=${connectionProperties}
database.slave1.useGlobalDataSourceStat=${useGlobalDataSourceStat}

```

### 多连接

- 多个数据库连接, 即声明多个可用bean, 兼容读写分离bean声明, 用于动态切换数据库使用的连接
- 建议自定义类, 继承`GaarasonDataSourceWrapper`(即实现`GaarasonDataSource`接口), 并重写`protected DataSource getRealDataSource(boolean isWriteOrTransaction)`


## 非spring
```java

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.connection.GaarasonDataSourceWrapper;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.annotation.Column;
import gaarason.database.eloquent.annotation.Primary;
import gaarason.database.eloquent.annotation.Table;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.util.*;

/**
 * 定义model
 */
public class TestModel extends Model<TestModel.Inner, Integer> {

    private final static GaarasonDataSourceWrapper gaarasonDataSourceWrapper;

    static {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(
            "jdbc:mysql://mysql.local/test_master_0?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai");
        druidDataSource.setDbType("com.alibaba.druid.pool.DruidDataSource");
        druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("root");
        druidDataSource.setInitialSize(5);
        druidDataSource.setMinIdle(5);
        druidDataSource.setMaxActive(10);
        druidDataSource.setMaxWait(60000);
        druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
        druidDataSource.setMinEvictableIdleTimeMillis(300000);
        druidDataSource.setValidationQuery("SELECT 1");
        List<String> iniSql = new ArrayList<>();
        iniSql.add(
            "SET SESSION SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION'");
        druidDataSource.setConnectionInitSqls(iniSql);
        druidDataSource.setTestOnBorrow(false);
        druidDataSource.setTestOnReturn(false);
        druidDataSource.setPoolPreparedStatements(false);
        druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(-1);
        Properties properties = new Properties();
        properties.setProperty("druid.stat.mergeSql", "true");
        properties.setProperty("druid.stat.slowSqlMillis", "5000");
        druidDataSource.setConnectProperties(properties);
        druidDataSource.setUseGlobalDataSourceStat(true);

        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(druidDataSource);

        gaarasonDataSourceWrapper = new GaarasonDataSourceWrapper(dataSources);
    }

    /**
     * 使用 ProxyDataSource
     * @return ProxyDataSource
     */
    @Override
    public GaarasonDataSourceWrapper getGaarasonDataSource() {
        return gaarasonDataSourceWrapper;
    }

    /**
     * 定义 entity
     */
    @Data
    @Table(name = "student")
    public static class Inner {
        @Primary
        private Integer id;

        @Column(length = 20)
        private String name;

        private Byte age;

        private Byte sex;

        @Column(name = "teacher_id")
        private Integer teacherId;

        @Column(name = "created_at", insertable = false, updatable = false)
        private Date createdAt;

        @Column(name = "updated_at", insertable = false, updatable = false)
        private Date updatedAt;
    }
}

```