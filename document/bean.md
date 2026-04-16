# database

Eloquent ORM for Java

## 目录

* [注册配置 Configuration](/document/bean.md)
    * [SpringBoot](#SpringBoot)
        * [单连接](#单连接)
            * [单库连接](#单库连接)
            * [读写分离](#读写分离)
        * [多数据源分组](#多数据源分组)
            * [YAML 配置方式](#YAML-配置方式)
            * [数据源组切换](#数据源组切换)
        * [使用GaarasonDataSource](#使用GaarasonDataSource)
    * [非spring boot](#非spring)
        * [编程式多数据源分组](#编程式多数据源分组)
    * [拓展配置](#拓展配置)
        * [包扫描](#包扫描)
        * [自定义查询构造器](#自定义查询构造器)
        * [新增支持的数据库](#新增支持的数据库)
* [数据映射 Mapping](/document/mapping.md)
* [数据模型 Model](/document/model.md)
* [查询结果集 Record](/document/record.md)
* [查询构造器 Query Builder](/document/query.md)
* [关联关系 Relationship](/document/relationship.md)
* [生成代码 Generate](/document/generate.md)
* [GraalVM](/document/graalvm.md)
* [版本信息 Version](/document/version.md)

## SpringBoot

- 使用 spring boot 的自动配置能力完成 `配置`->`DataSource`->`GaarasonDataSource`, 得到可用的`GaarasonDataSource`
- 使用 spring 的依赖注入能力完成 `Model`中的`GaarasonDataSource`注入

### 单连接

单个数据库连接 ( GaarasonDataSource )

#### 单库连接

- 读写都在同一数据库的典型场景
- 使用提供的`database-spring-boot-starter`, 即可以零配置使用
- 底层实现上等价于`单组路由模式`（默认组仅一个 master 数据源）
- 详见[GaarasonDatabaseAutoConfiguration.java](/database-spring-boot-starter/src/main/java/gaarason/database/spring/boot/starter/configurations/GaarasonDatabaseAutoConfiguration.java)


```properties
spring.datasource.url=jdbc:mysql://mysql.local/test_master_0?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

#### 读写分离

- 读, 写操作使用不同的数据库链接(DataSource), 程序会自动选择合适的(DataSource)
- 在当前实现中, 单链接读写分离与多组读写分离已统一到同一套路由器实现, 区别仅在于是否存在多个数据源组
- 推荐优先使用下文的`gaarason.database.datasource.groups`配置（单链接场景可仅配置一个组）


### 多数据源分组

- 支持多个数据源组, 每组包含多个写库(一般为一个主库)和多个读库, 自动进行读写分离
- 提供 YAML 配置和编程式两种构建方式
- 提供 `@GaarasonDS` 注解和 [`GaarasonDataSourceContext`](/database-core/src/main/java/gaarason/database/connection/GaarasonDataSourceContext.java) 编码两种切换方式
- 事务开始后自动锁定数据源组, 保证事务期间不会因上下文切换而路由到其他组
- 未配置从库时, 读请求自动回退到主库

#### YAML 配置方式

通过 `gaarason.database.datasource` 前缀进行配置, 无需手动创建 DataSource Bean:

application.yml 示例:
```yaml
gaarason:
  database:
    datasource:
      default-group: master
      groups:
        master:
          type: com.alibaba.druid.pool.DruidDataSource
          master:
            - url: jdbc:mysql://master1:3306/db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
              username: root
              password: root
              driver-class-name: com.mysql.cj.jdbc.Driver
          slave:
            - url: jdbc:mysql://slave1:3306/db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
              username: root
              password: root
              driver-class-name: com.mysql.cj.jdbc.Driver
            - url: jdbc:mysql://slave2:3306/db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
              username: root
              password: root
              driver-class-name: com.mysql.cj.jdbc.Driver
        order:
          type: com.alibaba.druid.pool.DruidDataSource
          master:
            - url: jdbc:mysql://order-master:3306/order_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
              username: root
              password: root
              driver-class-name: com.mysql.cj.jdbc.Driver
```

配置属性说明:
- 详见 [`GaarasonDataSourceProperties`](/database-spring-boot-starter/src/main/java/gaarason/database/spring/boot/starter/properties/GaarasonDataSourceProperties.java)

| 属性 | 说明 | 默认值 |
|---|---|---|
| `default-group` | 默认使用的数据源组名 | `master` |
| `groups` | 数据源组映射 (组名 -> 组配置) | 空 |
| `groups.{name}.type` | DataSource 类型全限定类名 | 自动检测 |
| `groups.{name}.master` | 主数据源列表(写) | 必填 |
| `groups.{name}.slave` | 从数据源列表(读), 不配置时读回退到主库 | 空 |

> **向后兼容**: 未配置 `gaarason.database.datasource.groups` 时, 框架会自动将单 Spring `DataSource` 映射为`单默认组`并使用同一套路由能力, 无需修改已有配置

> **迁移建议**: 历史项目可继续沿用单 `DataSource` Bean；新项目建议统一使用 `gaarason.database.datasource.groups`，即便只有一个组，也便于后续平滑扩展到多组。

#### 数据源组切换

提供两种切换方式, 可混合使用:

**方式一: `@GaarasonDS` 注解 (依赖 Spring AOP)**

可标注在方法或类上, 方法级别优先于类级别:

```java
@Service
public class OrderService {

    @Resource
    private OrderModel orderModel;

    @GaarasonDS("order")
    public void processOrder(Long orderId) {
        // 此方法内所有数据库操作将路由到 "order" 数据源组
        orderModel.newQuery().where("id", orderId).first();
    }
}
```

类级别注解, 该类所有方法默认使用指定组:
```java
@GaarasonDS("order")
@Service
public class OrderService {

    @Resource
    private OrderModel orderModel;

    public void processOrder(Long orderId) {
        orderModel.newQuery().where("id", orderId).first();
    }

    @GaarasonDS("master")
    public void syncToMaster() {
        // 方法级别覆盖类级别, 使用 "master" 组
    }
}
```

- 详见 [`@GaarasonDS`](/database-spring-boot-starter/src/main/java/gaarason/database/spring/boot/starter/annotation/GaarasonDS.java)
- 详见 [`GaarasonDataSourceAspect`](/database-spring-boot-starter/src/main/java/gaarason/database/spring/boot/starter/aop/GaarasonDataSourceAspect.java)

**方式二: `GaarasonDataSourceContext` 编码 (不依赖 Spring)**

适用于非 Spring 环境或需要细粒度控制的场景:

```java
// 方式 A: 手动 set / clear
GaarasonDataSourceContext.set("order");
try {
    orderModel.newQuery().where("id", 1).first();
} finally {
    GaarasonDataSourceContext.clear();
}

// 方式 B: 自动管理 (推荐)
GaarasonDataSourceContext.execute("order", () -> {
    orderModel.newQuery().where("id", 1).first();
});

// 方式 C: 带返回值
Order order = GaarasonDataSourceContext.execute("order", () -> {
    return orderModel.newQuery().where("id", 1).first();
});
```

支持嵌套调用, 基于栈式实现自动恢复前值:
```java
GaarasonDataSourceContext.execute("order", () -> {
    // 此处使用 "order" 组
    orderModel.newQuery().first();

    GaarasonDataSourceContext.execute("master", () -> {
        // 此处使用 "master" 组
        userModel.newQuery().first();
    });

    // 自动恢复到 "order" 组
    orderModel.newQuery().get();
});
```

- 详见 [`GaarasonDataSourceContext`](/database-core/src/main/java/gaarason/database/connection/GaarasonDataSourceContext.java)



## 非spring

- 在不使用 spring 的场景下, 本质上是一致的, `配置`->`DataSource`->`GaarasonDataSource`, 并将`GaarasonDataSource`与`Model`连接起来

```java
/**
 * 定义model
 */
public class TestModel extends Model<QueryBuilder<TestModel.Inner, Integer>, TestModel.Inner, Integer> {

    protected final static GaarasonDataSource gaarasonDataSource;

    /*
     * `配置`->`DataSource`->`GaarasonDataSource`
     * 一般定义到父类 或者 一个统一的外部即可
     */
    static {
        // 设置包扫描配置
        System.setProperty("gaarason.database.scan.packages", "com.temp.model,com.temp.dao");
        
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

        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(druidDataSource);

        // 在此初始化 GaarasonDataSource
        // 结果保存在内静态属性上, 以保证仅初始化一次
        gaarasonDataSource = GaarasonDataSourceBuilder.build(dataSources);
    }

    /**
     * 将`GaarasonDataSource`与`Model`连接起来
     * @return GaarasonDataSource
     */
    @Override
    public GaarasonDataSource getGaarasonDataSource() {
        // 简单的获取已经完成初始化的 GaarasonDataSource
        return gaarasonDataSource;
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

    /**
     * 普通业务调用
     */
    public void doSomeThing(){
        newQuery().where("name", "alice").first();
    }
}

```

### 编程式多数据源分组

不依赖 Spring, 使用 [`GaarasonRoutingDataSourceBuilder`](/database-core/src/main/java/gaarason/database/connection/GaarasonRoutingDataSourceBuilder.java) 构建多组路由数据源:

```java
// 创建各组的 DataSource
DruidDataSource masterDs = new DruidDataSource();
masterDs.setUrl("jdbc:mysql://master1:3306/db");
masterDs.setUsername("root");
masterDs.setPassword("root");

DruidDataSource slaveDs = new DruidDataSource();
slaveDs.setUrl("jdbc:mysql://slave1:3306/db");
slaveDs.setUsername("root");
slaveDs.setPassword("root");

DruidDataSource orderDs = new DruidDataSource();
orderDs.setUrl("jdbc:mysql://order1:3306/order_db");
orderDs.setUsername("root");
orderDs.setPassword("root");

// 使用构建器创建路由数据源
GaarasonDataSource gaarasonDataSource = GaarasonRoutingDataSourceBuilder.create()
    .defaultGroup("master")
    .group("master", Collections.singletonList(masterDs), Collections.singletonList(slaveDs))
    .group("order", Collections.singletonList(orderDs))
    .build(container);
```

切换数据源组使用 `GaarasonDataSourceContext`:

```java
// 在指定组内执行
GaarasonDataSourceContext.execute("order", () -> {
    orderModel.newQuery().where("id", 1).first();
});
```

- 详见 [`GaarasonDataSourceContext`](/database-core/src/main/java/gaarason/database/connection/GaarasonDataSourceContext.java)
- 详见 [`DataSourceGroup`](/database-core/src/main/java/gaarason/database/connection/DataSourceGroup.java)

## 拓展配置

- 框架内置了 40+ 种数据库方言支持(MySQL、PostgreSQL、Oracle、SQL Server、DB2、达梦、人大金仓等), 通过 JDBC `DatabaseMetaData.getDatabaseProductName()` 自动检测并选择对应的 SQL 语法
- 各个数据库的功能本质和逻辑比较类似, 但分页、UPSERT、标识符引号等存在差异, 已通过 `DbType` 枚举和方言 `Grammar` 类进行适配

### 包扫描

程序中, 会扫描`Model`类型, 并解析其信息; 会扫描`GaarasonAutoconfiguration`类型, 完成自动自定义配置

由于`java8` 与`其他8以上java版本`的`ClassLoader`在实现上的差别, 使得当`packages`不指定时, `java8`会扫描所有包, 而`其他8以上java版本`则完全不扫描  
因此, `java8`以上的版本, 必须配置本项; `java8`为了更快的启动 (更高效/准确的扫描), 也建议配置本项   

以下的配置方式, 选择其一即可

- Java 代码中指定`System.setProperty("gaarason.database.scan.packages", "you.package1,you.package2")`
- Jvm 启动时指定`-Dgaarason.database.scan.packages=you.package1,you.package2`
- SpringBoot 下, 可使用 `@GaarasonDatabaseScan({"you.package1","you.package2"})`
- SpringBoot 下, 可通过`application.properties` 中配置 `gaarason.database.scan.packages=you.package1,you.package2`


### 自定义查询构造器


- 对于`model`中使用`newQuery()`返回的`Builder`对象,进行修改.
- 举例 修改默认的`limit(int)` 方法. 并添加自定义方法`add(Object)`

1. 实现 `Builder` 接口, 建议直接继承 `AbstractBuilder`, 并正确赋值泛型;

```java
public class MySqlBuilderV2 extends AbstractBuilder<MySqlBuilderV2<T, K>, T, K> {
    
    // 必须实现
    @Override
    public MySqlBuilderV2<T, K> getSelf() {
        return this;
    }
   
    // 对任意方法进行修改
    @Override
    public MySqlBuilderV2<T , K> limit(Object take) {
        Collection<Object> parameters = new ArrayList<>(1);
        String sqlPart = grammar.replaceValueAndFillParameters(take, parameters);
        grammar.set(Grammar.SQLPartType.LIMIT, sqlPart, parameters);
        return getSelf();
    }
    
    // 添加任意方法
    public MySqlBuilderV2<T, K> add(Object something) {
        //....

        return getSelf();
    }
}

```

2. 实现 `QueryBuilderConfig` 接口;

```java
public class MysqlQueryBuilderConfigV2 implements QueryBuilderConfig, Serializable {

    @Override
    public String getValueSymbol() {
        return "'";
    }

    @Override
    public boolean support(String databaseProductName) {
        return "mysql".equals(databaseProductName);
    }

    @Override
    public Grammar newGrammar(String tableName) {
        return new BaseGrammar(tableName, "`") {
            private static final long serialVersionUID = 1L;
        };
    }

    @Override
    public <T, K> Builder<?, T, K> newBuilder(GaarasonDataSource gaarasonDataSource, Model<?, T, K> model) {
        return new MySqlBuilderV2<T, K>().initBuilder(gaarasonDataSource, ObjectUtils.typeCast(model), newGrammar(model.getTableName()));
    }
}

```

3. 实现`GaarasonAutoconfiguration`接口, 程序会自动通过包扫描, 完成加载, 任何的数据库操作的产生, 都会触发有且仅有的一次扫描.

```java

public class MysqlV2Autoconfiguration implements GaarasonAutoconfiguration {
    @Override
    public void init(Container container) {
        // 执行注册 MysqlQueryBuilderConfigV2
        container.register(QueryBuilderConfig.class,
                new InstanceCreatorFunctionalInterface<QueryBuilderConfig>() {
                    @Override
                    public QueryBuilderConfig execute(Class<QueryBuilderConfig> clazz) throws Throwable {
                        return new MysqlQueryBuilderConfigV2();
                    }

                    // 更高的优先级, 很关键
                    @Override
                    public Integer getOrder() {
                        return InstanceCreatorFunctionalInterface.super.getOrder() - 1;
                    }
                });
        // ....
    }
}
```

4. 业务`model`声明新的 `builder`
- 需要将业务上的`model`基类的泛型, 更改为新的`builder`类
```java
public abstract static class BaseModel<T extends BaseEntity, K> extends Model<MySqlBuilderV2<T, K>, T, K> {
    
    // ....
   
}
```
5. 业务使用  
- 如同原生方法一样直接调用即可
```java
testModel.newQuery().add("ss").get();
```

### 预置支持的数据库

框架通过 [`DbType`](/database-api/src/main/java/gaarason/database/appointment/DbType.java) 枚举内置了以下数据库的方言支持, 通过 JDBC 自动检测, **无需任何额外配置**:

| 数据库 | 描述 | 方言分组 |
|---|---|---|
| MySQL | MySQL 数据库 | MYSQL |
| MariaDB | MariaDB 数据库 | MYSQL |
| Oracle | Oracle11g 及以下数据库 | ORACLE |
| Oracle 12c | Oracle12c 及以上数据库 | ORACLE_12C |
| PostgreSQL | PostgreSQL 数据库 | POSTGRESQL |
| SQL Server | SQLServer 数据库 | SQL_SERVER |
| DB2 | DB2 数据库 | DB2 |
| H2 | H2 数据库 | POSTGRESQL |
| HSQL | HSQL 数据库 | POSTGRESQL |
| SQLite | SQLite 数据库 | POSTGRESQL |
| 达梦(DM) | 达梦数据库 | ORACLE_12C |
| 人大金仓 | 人大金仓数据库 | POSTGRESQL |
| OceanBase | OceanBase 数据库 | MYSQL |
| ClickHouse | ClickHouse 数据库 | POSTGRESQL |
| openGauss | 华为 openGauss 数据库 | POSTGRESQL |
| Greenplum | Greenplum 数据库 | POSTGRESQL |
| Informix | Informix 数据库 | INFORMIX |
| Firebird | Firebird 数据库 | FIREBIRD |
| Derby | Derby 数据库 | ORACLE_12C |
| Doris | Doris 数据库 | MYSQL |
| Hive | Hive 数据库 | MYSQL |
| ... | 更多 40+ 种数据库 | 见 [`DbType`](/database-api/src/main/java/gaarason/database/appointment/DbType.java) 枚举 |

各方言分组对应的 Grammar 实现:

| 方言分组 | Grammar 类 | 分页语法 |
|---|---|---|
| MYSQL | [`BaseGrammar`](/database-query/src/main/java/gaarason/database/query/grammars/BaseGrammar.java) | `LIMIT offset, count` |
| POSTGRESQL | [`PostgreSqlGrammar`](/database-query/src/main/java/gaarason/database/query/grammars/PostgreSqlGrammar.java) | `LIMIT count OFFSET offset` |
| ORACLE | [`OracleGrammar`](/database-query/src/main/java/gaarason/database/query/grammars/OracleGrammar.java) | ROWNUM |
| ORACLE_12C | [`Oracle12cGrammar`](/database-query/src/main/java/gaarason/database/query/grammars/Oracle12cGrammar.java) | `OFFSET n ROWS FETCH NEXT m ROWS ONLY` |
| SQL_SERVER | [`MsSqlGrammar`](/database-query/src/main/java/gaarason/database/query/grammars/MsSqlGrammar.java) | `OFFSET n ROWS FETCH NEXT m ROWS ONLY` |
| DB2 | [`Db2Grammar`](/database-query/src/main/java/gaarason/database/query/grammars/Db2Grammar.java) | `FETCH FIRST n ROWS ONLY` |
| INFORMIX | [`InformixGrammar`](/database-query/src/main/java/gaarason/database/query/grammars/InformixGrammar.java) | `SKIP m FIRST n` |
| FIREBIRD | [`FirebirdGrammar`](/database-query/src/main/java/gaarason/database/query/grammars/FirebirdGrammar.java) | `ROWS m TO n` |

### 新增支持的数据库

如果预置的 `DbType` 未覆盖你的数据库, 可以通过自定义 `QueryBuilderConfig` 来支持:
- 根据实际情况, 实现 `Grammar` 子类处理分页、UPSERT 等语法差异
- 根据实际情况, 自定义 `Builder` 查询构造器

```java
public class CustomQueryBuilderConfig implements QueryBuilderConfig, Serializable {

    @Override
    public String getValueSymbol() {
        return "'";
    }

    // 根据 JDBC 返回的数据库产品名称, 启用当前配置
    @Override
    public boolean support(String databaseProductName) {
        return "your_database".equals(databaseProductName);
    }

    // 创建方言对应的 Grammar, 可继承 BaseGrammar 并重写分页等方法
    @Override
    public Grammar newGrammar(String tableName) {
        return new PostgreSqlGrammar(tableName);
    }

    @Override
    public <T, K> Builder<?, T, K> newBuilder(GaarasonDataSource gaarasonDataSource, Model<?, T, K> model) {
        return new QueryBuilder<T, K>().initBuilder(gaarasonDataSource, ObjectUtils.typeCast(model), newGrammar(model.getTableName()));
    }
}
```
