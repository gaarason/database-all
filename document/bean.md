# database
Eloquent ORM for Java
## 目录
* [注册bean](/document/bean.md)
    * [spring boot](#spring)
        * [单连接](#单连接)
            * [单库连接](#单库连接)
            * [读写分离](#读写分离)
        * [多连接](#多连接)
        * [使用ProxyDataSource](#使用ProxyDataSource)
        
    * [非spring boot](#非spring)
* [数据映射](/document/mapping.md)
* [数据模型](/document/model.md)
* [查询结果集](/document/record.md)
* [查询构造器](/document/query.md)
* [反向生成代码](/document/generate.md)
## spring

使用 spring boot 管理 bean

### 单连接

单个数据库连接

#### 单库连接

读写都在同一数据库

bean配置 如下

```java
package com.demo.common.data.spring;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import gaarason.database.connections.ProxyDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class BeanConfiguration {

    // 单个主要连接
    @Bean(name = "dataSourceMaster0")
    @Primary
    @ConfigurationProperties(prefix = "database.master0")
    public DataSource dataSourceMaster0() {
        return DruidDataSourceBuilder.create().build();
    }

    // 主要连接的集合, 一主多从以及单机配置一个
    @Bean
    public List<DataSource> dataSourceMasterList() {
        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(dataSourceMaster0());
        return dataSources;
    }

    // 将被使用的bean
    @Bean
    public ProxyDataSource proxyDataSource(@Qualifier("dataSourceMasterList") List<DataSource> dataSourceMasterList) {
        return new ProxyDataSource(dataSourceMasterList);
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
connectionInitSqls[0]=SET SESSION SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION'
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
import gaarason.database.connections.ProxyDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

import javax.sql.DataSource;
import java.util.*;

@Configuration
@PropertySource( value = {"database.properties"})
public class BeanConfiguration {

    // 主要写库1
    @Bean(name = "dataSourceMaster0")
    @Primary
    @ConfigurationProperties(prefix = "database.master0")
    public DataSource dataSourceMaster0() {
        return DruidDataSourceBuilder.create().build();
    }

    // 写库2
    @Bean(name = "dataSourceMaster1")
    @ConfigurationProperties(prefix = "database.master1")
    public DataSource dataSourceMaster1() {
        return DruidDataSourceBuilder.create().build();
    }

    // 读库1
    @Bean(name = "dataSourceSlave0")
    @ConfigurationProperties(prefix = "database.slave0")
    public DataSource dataSourceSlave0() {
        return DruidDataSourceBuilder.create().build();
    }

    // 读库2
    @Bean(name = "dataSourceSlave1")
    @ConfigurationProperties(prefix = "database.slave1")
    public DataSource dataSourceSlave1() {
        log.info("-------------------- database.slave1 init ---------------------");
        return DruidDataSourceBuilder.create().build();
    }

    // 写连接集合
    @Bean("dataSourceMasterList")
    public List<DataSource> dataSourceMasterList() {
        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(dataSourceMaster0());
        dataSources.add(dataSourceMaster1());
        return dataSources;
    }

    // 读连接集合
    @Bean("dataSourceSlaveList")
    public List<DataSource> dataSourceSlaveList() {
        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(dataSourceSlave0());
        dataSources.add(dataSourceSlave1());
        return dataSources;
    }

    // 将被使用的bean
    @Bean
    public ProxyDataSource proxyDataSource(@Qualifier("dataSourceMasterList") List<DataSource> dataSourceMasterList, @Qualifier(
        "dataSourceSlaveList") List<DataSource> readDataSourceList) {
        return readDataSourceList.isEmpty() ? new ProxyDataSource(dataSourceMasterList) :
            new ProxyDataSource(dataSourceMasterList, readDataSourceList);
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
connectionInitSqls[0]=SET SESSION SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION'
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

多个数据库连接, 即声明多个可用bean, 兼容读写分离bean声明, 可以动态切换数据库使用的连接


```java
package com.demo.common.data.spring;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import gaarason.database.connections.ProxyDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class BeanConfiguration {

    // 单个主要连接, 华东区域
    @Bean(name = "dataSourceMasterHUADONG")
    @Primary
    @ConfigurationProperties(prefix = "database.huadong.master")
    public DataSource dataSourceMasterHUADONG() {
        return DruidDataSourceBuilder.create().build();
    }

    // 主要连接的集合, 一主多从以及单机配置一个
    @Bean
    public List<DataSource> dataSourceMasterHUADONGList() {
        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(dataSourceMasterHUADONG());
        return dataSources;
    }

    // 将被使用的bean HUADONG
    @Bean
    public ProxyDataSource proxyDataSourceHUADONG(@Qualifier("dataSourceMasterHUADONGList") List<DataSource> dataSourceMasterList) {
        return new ProxyDataSource(dataSourceMasterList);
    }
   
    //////////////// 另一个连接 ///////////////
    
    // 单个主要连接, 华南区域
    @Bean(name = "dataSourceMasterHUANAN")
    @Primary
    @ConfigurationProperties(prefix = "database.huanan.master")
    public DataSource dataSourceMasterHUANAN() {
        return DruidDataSourceBuilder.create().build();
    }

    // 主要连接的集合, 一主多从以及单机配置一个
    @Bean
    public List<DataSource> dataSourceMasterHUANANList() {
        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(dataSourceMaster1());
        return dataSources;
    }

    // 将被使用的bean HUANAN
    @Bean
    public ProxyDataSource proxyDataSourceHUANAN(@Qualifier("dataSourceMasterHUANANList") List<DataSource> dataSourceMasterList) {
        return new ProxyDataSource(dataSourceMasterList);
    }
    

}
```
application.properties 省略


## 使用ProxyDataSource

上面生成的`ProxyDataSource`进行使用

```java
package gaarason.database.models;

import gaarason.database.connections.ProxyDataSource;
import gaarason.database.eloquent.Column;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Primary;
import gaarason.database.eloquent.Table;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Component
public class StudentSingle2Model extends Model<StudentSingle2Model.Entity> {

    @Data
    @Table(name = "student")
    public static class Entity {
        @Primary
        private Integer id;

        @Column(length = 20)
        private String name;

        private Byte age;

        private Byte sex;

        @Column(name = "teacher_id")
        private Integer teacherId;

        @Column(name = "created_at")
        private Date createdAt;

        @Column(name = "updated_at")
        private Date updatedAt;
    }

    // 声明依赖的 ProxyDataSource 不需要则不用声明依赖
    @Resource(name = "proxyDataSourceHUADONG")
    protected ProxyDataSource proxyDataSourceHUADONG;
    
    // 声明依赖的 ProxyDataSource 不需要则不用声明依赖
    @Resource(name = "proxyDataSourceHUANAN")
    protected ProxyDataSource proxyDataSourceHUANAN;

    public ProxyDataSource getProxyDataSource(){
        // 返回指定连接
        return proxyDataSourceHUADONG;
    }
}

```
## 非spring
```java

    // 主要连接
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

    // 主要连接列表
    private List<DataSource> dataSourceMasterList() {
        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(dataSourceMaster0());
        return dataSources;
    }

    // DataSource代理
    private ProxyDataSource proxyDataSource() {
        List<DataSource> dataSources = dataSourceMasterList();
        return new ProxyDataSource(dataSources);
    }

    // 声明模型
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
```