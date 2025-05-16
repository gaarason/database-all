# database

Eloquent ORM for Java

## 目录

* [注册配置 Configuration](/document/bean.md)
    * [SpringBoot](#SpringBoot)
        * [单连接](#单连接)
            * [单库连接](#单库连接)
            * [读写分离](#读写分离)
        * [多连接](#多连接)
        * [使用GaarasonDataSource](#使用GaarasonDataSource)
    * [非spring boot](#非spring)
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
- 详见[GaarasonDatabaseAutoConfiguration.java](/database-spring-boot-starter/src/main/java/gaarason/database/spring/boot/starter/configurations/GaarasonDatabaseAutoConfiguration.java)

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
            return new GaarasonSmartDataSourceWrapper(dataSourceList, readDataSourceList, container);
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
### 使用GaarasonDataSource

```java
@Repository
public class StudentModel extends Model<MysqlBuilder, Student, Integer> {

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

### 多连接

- 多个数据库连接(DataSource), 一般场景是根据业务的上下文, 来确定使用哪个( DataSource ), 兼容于读写分离
- 建议自定义代理类, 继承`GaarasonDataSourceWrapper`(即实现`GaarasonDataSource`接口),
  并重写`protected DataSource getRealDataSource(boolean isWriteOrTransaction)`

#### 示例使用
Web 场景下, 根据当前请求, 动态切换`DataSource`

1. 定义`GaarasonSmartDataSourceMultipleLinksWrapper`(即实现`GaarasonDataSource`接口),
   并重写`protected DataSource getRealDataSource(boolean isWriteOrTransaction)`
```java
public class GaarasonSmartDataSourceMultipleLinksWrapper extends GaarasonSmartDataSourceWrapper {

    /**
     * 每组链接 (写连接, 读链接)
     */
    protected final Map<String, List<List<DataSource>>> dataSourceMap;


    public GaarasonSmartDataSourceMultipleLinksWrapper(Map<String, List<List<DataSource>>> dataSourceMap, Container container) {
        // 不再使用原数据结构
        super(Collections.emptyList(), Collections.emptyList(), container);
        // 使用新的
        this.dataSourceMap = dataSourceMap;
    }

    @Override
    protected DataSource getRealDataSource(boolean isWriteOrTransaction) {
        Object databaseLink = "";
        // 选择合适的链接
        databaseLink = getHttpServletRequest().getAttribute("databaseLink");

        List<List<DataSource>> lists = dataSourceMap.get(String.valueOf(databaseLink));

        if(ObjectUtils.isEmpty(lists)) {
            throw new RuntimeException();
        }
        
        List<DataSource> masterDataSourceList = lists.get(0);
        List<DataSource> slaveDataSourceList = lists.size() > 1 ? lists.get(1) : Collections.emptyList();
        boolean hasSlave = !ObjectUtils.isEmpty(slaveDataSourceList);

        if (!hasSlave || isWriteOrTransaction) {
            return masterDataSourceList.get(ThreadLocalRandom.current().nextInt(masterDataSourceList.size()));
        } else {
            return slaveDataSourceList.get(ThreadLocalRandom.current().nextInt(slaveDataSourceList.size()));
        }
    }

    /**
     * 2个写链接, 通过名字区分
     */
    public static GaarasonSmartDataSourceMultipleLinksWrapper build(DataSource dataSource1, DataSource dataSource2, Container container) {
        Map<String, List<List<DataSource>>> dataSourceMap = new HashMap<>();
        List<List<DataSource>> listOfName1 = dataSourceMap.computeIfAbsent("name1", k -> new ArrayList<>());
        listOfName1.add(0, Collections.singletonList(dataSource1));

        List<List<DataSource>> listOfName2 = dataSourceMap.computeIfAbsent("name2", k -> new ArrayList<>());
        listOfName2.add(0, Collections.singletonList(dataSource2));

        return new GaarasonSmartDataSourceMultipleLinksWrapper(dataSourceMap, container);
    }


    /**
     * 示例, 获取当前 web 线程的 request
     * @return request
     */
    public static HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(requestAttributes == null) {
            throw new BusinessHPException("No HttpServletRequest");
        }
        return requestAttributes.getRequest();
    }
}

```
2. 配置`GaarasonSmartDataSourceMultipleLinksWrapper`

```java
import java.util.HashMap;

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

        // 库1
        @Bean
        @ConfigurationProperties(prefix = "database.master1")
        public DataSource dataSourceMaster1() {
            return DruidDataSourceBuilder.create().build();
        }

        // 库2
        @Bean
        @ConfigurationProperties(prefix = "database.master2")
        public DataSource dataSourceMaster2() {
            return DruidDataSourceBuilder.create().build();
        }

        @Primary
        @Bean
        public GaarasonDataSource gaarasonDataSource(DataSource dataSourceMaster1, DataSource dataSourceMaster2, Container container) {
            GaarasonSmartDataSourceMultipleLinksWrapper.build(dataSourceMaster1, dataSourceMaster2, container);
        }

        @Primary
        @Bean
        public GaarasonTransactionManager gaarasonTransactionManager(GaarasonDataSource gaarasonDataSource) {
            return new GaarasonTransactionManager(gaarasonDataSource);
        }
    }
}
```
3. 在业务入口处, 记录当前需要使用的链接 示例为 web 过滤器
```java
public class LoggingFilter implements Filter {


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            // 业务判断
            // ....
            // 记录标记
            request.setAttribute("databaseLink", "name1");
        }

        // 继续处理请求
        chain.doFilter(request, response);
    }
}
```

4. 业务使用

```java
@Repository
public class StudentModel extends Model<MysqlBuilder, Student, Integer> {
    
    @Resource
    private GaarasonDataSource gaarasonDataSource;

    /**
     * 普通业务调用
     */
    public void doSomeThing(){
        newQuery().where("name", "alice").first();
    }
}
```



## 非spring

- 在不使用 spring 的场景下, 本质上是一致的, `配置`->`DataSource`->`GaarasonDataSource`, 并将`GaarasonDataSource`与`Model`连接起来

```java
/**
 * 定义model
 */
public class TestModel extends Model<MysqlBuilder, TestModel.Inner, Integer> {

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
- 而各个数据库的功能的本质和逻辑比较类似, 但是部分api仍然存在差异

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

2. 实现 `QueryBuilderConfig` 接口, 因为是修改, 所以通过继承当前的 `MysqlQueryBuilderConfig` 后按需更改;

```java
public class MysqlQueryBuilderConfigV2 extends MysqlQueryBuilderConfig {

    @Override
    public <T, K> Builder<?, T, K> newBuilder(GaarasonDataSource gaarasonDataSource, Model<?, T, K> model) {
        return new MySqlBuilderV2<T, K>().initBuilder(gaarasonDataSource, ObjectUtils.typeCast(model), new MySqlGrammar(model.getTableName()));
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

### 新增支持的数据库
可以支持任意的 ( java 的 jdbc 支持的 )  数据库类型   
大致方式`同上`, 稍有区别的是  
在实现 `QueryBuilderConfig` 接口时, 多实现几个方法
- 根据实际情况, 重写 Builder 查询构造器
- 根据实际情况, 重写 Grammar 语法

```java
public class MysqlQueryBuilderConfigV2 extends MysqlQueryBuilderConfig {

    // 根据数据库名称, 启用当前配置
    @Override
    public boolean support(String databaseProductName) {
        return "mysql".equals(databaseProductName);
    }

    // 其他 QueryBuilderConfig 接口方法, 按需实现
    // 根据实际情况, 重写 Builder 查询构造器
    // 根据实际情况, 重写 Grammar 语法
    @Override
    public <T, K> Builder<?, T, K> newBuilder(GaarasonDataSource gaarasonDataSource, Model<?, T, K> model) {
        return new MySqlBuilderV2<T, K>().initBuilder(gaarasonDataSource, ObjectUtils.typeCast(model), new MySqlGrammar(model.getTableName()));
    }
}

```
