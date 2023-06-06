# database

Eloquent ORM for Java

## 目录

* [注册配置](/document/bean.md)
    * [spring boot](#spring)
        * [单连接](#单连接)
            * [单库连接](#单库连接)
            * [读写分离](#读写分离)
        * [多连接](#多连接)
        * [使用GaarasonDataSource](#使用GaarasonDataSource)
    * [非spring boot](#非spring)
    * [拓展配置](#拓展配置)
        * [包扫描](#包扫描)
        * [新增支持的数据库](#新增支持的数据库)
        * [自定义查询构造器方法](#自定义查询构造器方法)
* [数据映射](/document/mapping.md)
* [数据模型](/document/model.md)
* [查询结果集](/document/record.md)
* [查询构造器](/document/query.md)
* [关联关系](/document/relationship.md)
* [生成代码](/document/generate.md)
* [GraalVM](/document/graalvm.md)
* [版本信息](/document/version.md)

## spring

- 使用 spring boot 的自动配置能力完成 `配置`->`DataSource`->`GaarasonDataSource`, 得到可用的`GaarasonDataSource`
- 使用 spring 的依赖注入能力完成 `Model`中的`GaarasonDataSource`注入

### 单连接

单个数据库连接 ( GaarasonDataSource )

#### 单库连接

- 读写都在同一数据库
- 对于基础的单一数据库链接的场景, 使用提供的`database-spring-boot-starter`, 即可以零配置使用

参考配置 如下, 以`gaarason/database/spring/boot/starter/configurations/GaarasonDatabaseAutoConfiguration.java`实际为准

```java
@Configuration
@AutoConfigureAfter({DruidDataSourceAutoConfigure.class, DataSourceAutoConfiguration.class})
@Import({GeneralModel.class, GeneralGenerator.class})
public class GaarasonDatabaseAutoConfiguration {

    private static final Log LOGGER = LogFactory.getLog(GaarasonDatabaseAutoConfiguration.class);

    /**
     * Spring配置GaarasonDatabaseProperties
     */
    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = GaarasonDatabaseProperties.PREFIX)
    public GaarasonDatabaseProperties gaarasonDatabaseProperties() {
        return new GaarasonDatabaseProperties();
    }

    /**
     * 容器初始化
     * @param applicationContext 应用上下文
     * @param gaarasonDatabaseProperties 配置
     * @return 容器
     */
    @Bean
    @ConditionalOnMissingBean
    public Container container(ApplicationContext applicationContext, GaarasonDatabaseProperties gaarasonDatabaseProperties){
        // 简单获取 @SpringBootApplication 所在的包名
        final String springBootApplicationPackage = applicationContext.getBeansWithAnnotation(
                SpringBootApplication.class)
            .entrySet()
            .iterator()
            .next()
            .getValue()
            .getClass()
            .getPackage().getName();

        /*
         * 将配置合并
         * 认定 GaarasonDatabaseScan 的解析一定在此之前完成了.
         * 默认使用 @SpringBootApplication 所在的包路径
         */
        gaarasonDatabaseProperties.mergeScan(GaarasonDatabaseScanRegistrar.getScan())
            .fillPackageWhenIsEmpty(springBootApplicationPackage)
            .fillAndVerify();

        // 从配置创建全新容器
        ContainerBootstrap container = ContainerBootstrap.build(gaarasonDatabaseProperties);

        /*
         * 序列化的必要步骤
         */
        container.signUpIdentification("primary-container");

        container.defaultRegister();

        // 注册 model实例获取方式
        container.getBean(ModelInstanceProvider.class).register(modelClass -> {
            try {
                return ObjectUtils.typeCast(applicationContext.getBean(modelClass));
            } catch (BeansException e) {
                return ObjectUtils.typeCast(
                    applicationContext.getBean(StringUtils.lowerFirstChar(modelClass.getSimpleName())));
            }
        });
        LOGGER.info("Model instance provider has been registered success.");

        container.bootstrapGaarasonAutoconfiguration();

        container.initialization();

        LOGGER.info("Container has completed initialization.");
        return container;
    }

    @Configuration
    public static class GaarasonDataSourceAutoconfigure {

        @Resource
        private DataSource dataSource;

        @Resource
        private Container container;

        /**
         * 数据源配置
         * @return 数据源
         */
        @Primary
        @Bean(autowireCandidate = false)
        @ConditionalOnMissingBean(GaarasonDataSource.class)
        public GaarasonDataSource gaarasonDataSource() {
            LOGGER.info("GaarasonDataSource init with " + dataSource.getClass().getName());
            // 创建 GaarasonDataSource
            return GaarasonDataSourceBuilder.build(dataSource, container);
        }

        /**
         * Spring 事物管理器
         * @return 事物管理器
         */
        @Primary
        @Bean
        @ConditionalOnMissingBean(GaarasonTransactionManager.class)
        public GaarasonTransactionManager gaarasonTransactionManager() {
            LOGGER.info("GaarasonTransactionManager init");
            return new GaarasonTransactionManager(gaarasonDataSource());
        }
    }

}
```

#### 读写分离

- 读, 写操作使用不同的数据库链接(DataSource), 程序会自动选择合适的(DataSource)
- 因为各个3方库都一般没有多数据源的默认配置, 所以在产生多个(DataSource)的步骤需要手动进行,
- a. 在所有的 DataSourceAutoConfigure 执行前,产生我们自己的DataSource;
- b. 使用这些 DataSource 产生 GaarasonDataSource

```java
@Configuration
@AutoConfigureBefore({DruidDataSourceAutoConfigure.class, DataSourceAutoConfiguration.class})
@EnableConfigurationProperties({GaarasonDatabaseProperties.class})
@Import({GeneralModel.class, GeneralGenerator.class})
public class GaarasonDatabaseAutoConfiguration {

    private static final Log LOGGER = LogFactory.getLog(GaarasonDatabaseAutoConfiguration.class);
    
    // 省略其他配置项
    // ....
    

    @Configuration
    public static class GaarasonDataSourceAutoconfigure {

        @Resource
        private Container container;

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

        /**
         * 数据源配置
         * @return 数据源
         */
        @Primary
        @Bean(autowireCandidate = false)
        @ConditionalOnMissingBean(GaarasonDataSource.class)
        public GaarasonDataSource gaarasonDataSource() {
            List<DataSource> dataSourceList = new ArrayList<>();
            dataSourceList.add(dataSourceMaster0());
            dataSourceList.add(dataSourceMaster1());
            List<DataSource> readDataSourceList = new ArrayList<>();
            readDataSourceList.add(dataSourceSlave0());
            readDataSourceList.add(dataSourceSlave1());
            return GaarasonDataSourceBuilder.build(dataSourceList, readDataSourceList, container);
        }

        /**
         * Spring 事物管理器
         * @return 事物管理器
         */
        @Primary
        @Bean
        @ConditionalOnMissingBean(GaarasonTransactionManager.class)
        public GaarasonTransactionManager gaarasonTransactionManager() {
            LOGGER.info("-------------------- GaarasonTransactionManager init ------------------");
            return new GaarasonTransactionManager(gaarasonDataSource());
        }
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

- 多个数据库连接(GaarasonDataSource), 一般场景是根据业务的上下文, 来确定使用哪个( GaarasonDataSource ), 兼容于读写分离
- 建议自定义代理类, 继承`GaarasonDataSourceWrapper`(即实现`GaarasonDataSource`接口),
  并重写`protected DataSource getRealDataSource(boolean isWriteOrTransaction)`

### 使用GaarasonDataSource

```java
@Repository
public class StudentModel extends Model<Student, Integer> {

    /**
     * 依赖注入
     * 父类依赖即可
     */
    @Resource
    private GaarasonDataSource gaarasonDataSource;

    /**
     * 实现方法
     * 父类实现即可
     */
    @Override
    public GaarasonDataSource getGaarasonDataSource() {
        return gaarasonDataSource;
    }

    /**
     * 普通业务调用
     */
    public void doSomeThing(){
        newQuery().where("name", "alice").first();
    }
}
```

## 非spring

- 在不使用 spring 的场景下, 本质上是一致, `配置`->`DataSource`->`GaarasonDataSource`, 并将`GaarasonDataSource`与`Model`连接起来

```java
/**
 * 定义model
 */
public class TestModel extends Model<TestModel.Inner, Integer> {

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

## 拓展配置

- 目前的主要是在做了mysql的适配,
- 而各个数据库的功能的本质和逻辑比较类似, 但是api差异确比较大

### 包扫描

程序中, 会扫描`Model`类型, 并解析其信息; 会扫描`GaarasonAutoconfiguration`类型, 完成自动自定义配置

由于`java8` 与`其他8以上java版本`的`ClassLoader`在实现上的差别, 使得当`packages`不指定时, `java8`会扫描所有包, 而`其他8以上java版本`则完全不扫描  
因此, `java8`以上的版本, 必须配置本项; `java8`为了更快的启动 (更高效/准确的扫描), 也建议配置本项

- Java代码中指定`System.setProperty("gaarason.database.scan.packages", "you.package1,you.package2")`
- Jvm启动时指定`-Dgaarason.database.scan.packages=you.package1,you.package2`
- SpringBoot下使用 `@GaarasonDatabaseScan("you.package1,you.package2")`

### 新增支持的数据库

- 参考 `database-query-mysql`模块

1. 实现 `Grammar` 接口

```java
public class H2Grammar implements Grammar {
    // 实现接口中的全部方法
}
```

2. 实现 `Builder` 接口

```java
public class H2Builder<T extends Serializable, K extends Serializable> implements Builder<T, K> {
    // 实现接口中的全部方法
}
```

3. 实现 `QueryBuilderConfig` 接口, 从而完成注册

```java
public class H2QueryBuilderConfig implements QueryBuilderConfig {

    @Override
    public String getValueSymbol() {
        return "'";
    }

    @Override
    public boolean support(String databaseProductName) {
        return "h2".equals(databaseProductName);
    }

    @Override
    public <T extends Serializable, K extends Serializable> Builder<T, K> newBuilder(GaarasonDataSource gaarasonDataSource, Model<T, K> model) {
        return new H2Builder<>(gaarasonDataSource, model, newGrammar(model.getEntityClass()));
    }

    @Override
    public <T extends Serializable> Grammar newGrammar(Class<T> entityClass) {
        return new H2Grammar(ModelShadowProvider.getByEntityClass(entityClass).getTableName());
    }
}
```

4. 实现`GaarasonAutoconfiguration`接口, 程序会自动通过包扫描, 完成加载, 任何的数据库操作的产生, 都会触发有且仅有的一次扫描.

```java

public class H2Autoconfiguration implements GaarasonAutoconfiguration {
    @Override
    public void init() {
        // 执行注册 H2QueryBuilderConfig
        ContainerProvider.register(QueryBuilderConfig.class,
            new InstanceCreatorFunctionalInterface<QueryBuilderConfig>() {
                @Override
                public QueryBuilderConfig execute(Class<QueryBuilderConfig> clazz) throws Throwable {
                    return new H2QueryBuilderConfig();
                }

                @Override
                public Integer getOrder() {
                    return InstanceCreatorFunctionalInterface.super.getOrder() - 1;
                }
            });
        // ....
    }
}
```

### 自定义查询构造器方法

- 对于`model`中使用`newQuery()`返回的`Builder`对象,进行修改.
- 举例修改 `MySqlBuilder` 中的 `limit(int)` 方法.

1. 实现 `Builder` 接口, 因为是修改, 所以通过继承当前的 `MySqlBuilder` 后按需更改;

```java
public class MySqlBuilderV2 extends MySqlBuilder {
    // 对任意方法进行修改
    @Override
    public Builder<T, K> limit(int take) {
        // 进行修改
        String sqlPart = String.valueOf(take);
        grammar.pushLimit(sqlPart);
        return this;
    }
}

```

2. 实现 `QueryBuilderConfig` 接口, 因为是修改, 所以通过继承当前的 `MysqlQueryBuilderConfig` 后按需更改;

```java
public class MysqlQueryBuilderConfigV2 extends MysqlQueryBuilderConfig {

    @Override
    public <T extends Serializable, K extends Serializable> Builder<T, K> newBuilder(
        GaarasonDataSource gaarasonDataSource, Model<T, K> model) {
        return new MySqlBuilderV2<>(gaarasonDataSource, model, newGrammar(model.getEntityClass()));
    }
}

```

3. 实现`GaarasonAutoconfiguration`接口, 程序会自动通过包扫描, 完成加载, 任何的数据库操作的产生, 都会触发有且仅有的一次扫描.

```java

public class MysqlV2Autoconfiguration implements GaarasonAutoconfiguration {
    @Override
    public void init() {
        // 执行注册 MysqlQueryBuilderConfigV2
        ContainerProvider.register(QueryBuilderConfig.class,
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