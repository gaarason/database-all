package gaarason.database.spring.boot.starter.test;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.annotation.BelongsToMany;
import gaarason.database.annotation.Column;
import gaarason.database.annotation.HasOneOrMany;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import gaarason.database.appointment.JoinType;
import gaarason.database.appointment.SqlType;
import gaarason.database.connection.GaarasonDataSourceContext;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.routing.DynamicDatabaseRouting;
import gaarason.database.contract.routing.DynamicDataSourceGroupRouting;
import gaarason.database.contract.routing.DynamicJdbcCatalogRouting;
import gaarason.database.contract.routing.DynamicTableRouting;
import gaarason.database.spring.boot.starter.annotation.GaarasonDatabase;
import gaarason.database.spring.boot.starter.annotation.GaarasonDataSourceGroup;
import gaarason.database.spring.boot.starter.annotation.GaarasonTable;
import gaarason.database.spring.boot.starter.test.data.model.base.BaseModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 业务场景集成测试:
 * 1. 组路由: 国家 hashCode 取余
 * 2. 切库: userId 取余
 * 3. 切表: 订单分片参数映射到 订单_1..12
 * 4. 查询: 注解关系查询 + join 构造器，均校验 SQL 拼接与结果
 *
 * @author xt
 */
@RunWith(SpringRunner.class)
@org.springframework.boot.test.context.SpringBootTest(classes = TestApplication.class)
@TestPropertySource(locations = "classpath:application-biz-routing.properties")
@Import(MysqlBizEcommerceRoutingSpringTests.BizRoutingTestConfiguration.class)
public class MysqlBizEcommerceRoutingSpringTests {

    private static final String SQL_SCRIPT = "sql/mysql-routing_biz_ecommerce.sql";

    @Resource
    private BizRoutingQueryService bizRoutingQueryService;

    @Resource
    private RoutingTrace routingTrace;

    @BeforeClass
    public static void initDatabase() throws Exception {
        executeSqlScript(SQL_SCRIPT);
    }

    @Before
    public void beforeEach() {
        clearAllRoutingContext();
        routingTrace.clear();
    }

    @Test
    public void 关联查询注解_当前用户指定月份订单与商品_路由与sql均正确() {
        String country = pickCountryByParity(0);
        RelationQueryResult result = bizRoutingQueryService.queryByRelation(country, 101L, 3, "2026-03");

        Assert.assertEquals(1, result.getOrderCount());
        Assert.assertEquals(2, result.getProductCount());
        Assert.assertEquals("m1", result.getCatalog());
        Assert.assertTrue(result.getDataSourceUrl().contains(expectedGroupUrlToken(country)));
        Assert.assertTrue(result.getOrderRelationSql().contains("订单_3"));
        Assert.assertTrue(result.getOrderRelationSql().contains("order_month"));
        Assert.assertTrue(result.getProductRelationSql().length() > 0);
    }

    @Test
    public void join查询构造器_当前用户指定月份订单与商品_sql拼接与路由均正确() {
        String country = pickCountryByParity(1);
        JoinQueryResult result = bizRoutingQueryService.queryByJoin(country, 102L, 3, "2026-03");

        Assert.assertEquals(Long.valueOf(2L), result.getJoinRowCount());
        Assert.assertEquals("m2", result.getCatalog());
        Assert.assertTrue(result.getDataSourceUrl().contains(expectedGroupUrlToken(country)));
        Assert.assertTrue(result.getJoinSql().contains("订单_3"));
        Assert.assertTrue(result.getJoinSql().contains("订单商品"));
        Assert.assertTrue(result.getJoinSql().contains("商品"));
        Assert.assertTrue(result.getJoinSql().contains("order_month"));
    }

    @Test
    public void 注解切换_关联查询与编码方式等价() {
        String country = pickCountryByParity(0);
        RelationQueryResult byAnnotation = bizRoutingQueryService.queryByRelationWithAnnotation(country, 101L, 3, "2026-03");
        RelationQueryResult byContext = bizRoutingQueryService.queryByRelation(country, 101L, 3, "2026-03");

        Assert.assertEquals(byContext.getOrderCount(), byAnnotation.getOrderCount());
        Assert.assertEquals(byContext.getProductCount(), byAnnotation.getProductCount());
        Assert.assertEquals(byContext.getCatalog(), byAnnotation.getCatalog());
        Assert.assertTrue(byAnnotation.getDataSourceUrl().contains(expectedGroupUrlToken(country)));
        Assert.assertTrue(byAnnotation.getOrderRelationSql().contains("订单_3"));
    }

    @Test
    public void 注解切换_join查询与编码方式等价() {
        String country = pickCountryByParity(1);
        JoinQueryResult byAnnotation = bizRoutingQueryService.queryByJoinWithAnnotation(country, 102L, 3, "2026-03");
        JoinQueryResult byContext = bizRoutingQueryService.queryByJoin(country, 102L, 3, "2026-03");

        Assert.assertEquals(byContext.getJoinRowCount(), byAnnotation.getJoinRowCount());
        Assert.assertEquals(byContext.getCatalog(), byAnnotation.getCatalog());
        Assert.assertTrue(byAnnotation.getDataSourceUrl().contains(expectedGroupUrlToken(country)));
        Assert.assertTrue(byAnnotation.getJoinSql().contains("订单_3"));
        Assert.assertTrue(byAnnotation.getJoinSql().contains("订单商品"));
        Assert.assertTrue(byAnnotation.getJoinSql().contains("商品"));
    }

    private static String expectedGroupUrlToken(String country) {
        int parity = Math.floorMod(country.hashCode(), 2);
        return parity == 0 ? "routingGroup=region_a" : "routingGroup=region_b";
    }

    private static String pickCountryByParity(int parity) {
        List<String> candidates = Arrays.asList(
            "China", "Japan", "Korea", "France", "Brazil", "Canada", "Germany", "Spain", "Mexico");
        for (String candidate : candidates) {
            if (Math.floorMod(candidate.hashCode(), 2) == parity) {
                return candidate;
            }
        }
        throw new IllegalStateException("No country candidate for parity=" + parity);
    }

    private static void clearAllRoutingContext() {
        // execute* 与注解切面均采用 try/finally 自动恢复上下文，无需显式清理
    }

    private static void executeSqlScript(String classpathLocation) throws Exception {
        List<String> statements = readSqlStatements(classpathLocation);
        try (Connection connection = DriverManager.getConnection(
            "jdbc:mysql://mysql.local/mysql?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull"
                + "&useSSL=false&autoReconnect=true&serverTimezone=Asia/Shanghai",
            "root",
            "root");
             Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.execute(sql);
            }
        }
    }

    private static List<String> readSqlStatements(String classpathLocation) throws IOException {
        InputStream inputStream = MysqlBizEcommerceRoutingSpringTests.class.getClassLoader()
            .getResourceAsStream(classpathLocation);
        if (inputStream == null) {
            throw new IllegalArgumentException("SQL script not found: " + classpathLocation);
        }
        byte[] bytes;
        try (InputStream in = inputStream) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            bytes = outputStream.toByteArray();
        }
        String content = new String(bytes, StandardCharsets.UTF_8);
        String[] segments = content.split(";\\r?\\n");
        List<String> statements = new ArrayList<>(segments.length);
        for (String segment : segments) {
            String sql = segment.trim();
            if (!sql.isEmpty()) {
                statements.add(sql);
            }
        }
        return statements;
    }

    @Configuration
    static class BizRoutingTestConfiguration {

        @Bean
        public RoutingTrace routingTrace() {
            return new RoutingTrace();
        }

        @Bean
        public DynamicDataSourceGroupRouting dynamicDataSourceGroupRouting() {
            return groupKey -> groupKey;
        }

        @Bean
        public DynamicDatabaseRouting dynamicDatabaseRouting() {
            return databaseKey -> databaseKey;
        }

        @Bean
        public DynamicTableRouting dynamicTableRouting() {
            return (logicalTableName, routeExpression) -> {
                if ("订单".equals(logicalTableName) && routeExpression != null && !routeExpression.isEmpty()) {
                    return "订单_" + routeExpression;
                }
                return logicalTableName;
            };
        }

        @Bean
        public DynamicJdbcCatalogRouting dynamicJdbcCatalogRouting(RoutingTrace routingTrace) {
            return (dataSource, connection, catalogKey) -> {
                routingTrace.mark(dataSource, catalogKey);
                try {
                    connection.setCatalog(catalogKey);
                } catch (Throwable ignored) {
                    connection.setSchema(catalogKey);
                }
            };
        }

        @Bean
        public BizUserModel bizUserModel() {
            return new BizUserModel();
        }

        @Bean
        public BizOrderModel bizOrderModel() {
            return new BizOrderModel();
        }

        @Bean
        public BizProductModel bizProductModel() {
            return new BizProductModel();
        }

        @Bean
        public BizOrderProductModel bizOrderProductModel() {
            return new BizOrderProductModel();
        }

        @Bean
        public BizRoutingQueryService bizRoutingQueryService(
            BizUserModel userModel,
            BizOrderModel orderModel,
            RoutingTrace routingTrace
        ) {
            return new BizRoutingQueryService(userModel, orderModel, routingTrace);
        }

        @Bean("bizRoutingAnnotationHelper")
        public BizRoutingAnnotationHelper bizRoutingAnnotationHelper() {
            return new BizRoutingAnnotationHelper();
        }
    }

    static class BizRoutingQueryService {

        private final BizUserModel userModel;
        private final BizOrderModel orderModel;
        private final RoutingTrace routingTrace;

        BizRoutingQueryService(BizUserModel userModel, BizOrderModel orderModel, RoutingTrace routingTrace) {
            this.userModel = userModel;
            this.orderModel = orderModel;
            this.routingTrace = routingTrace;
        }

        RelationQueryResult queryByRelation(String country, Long userId, int orderShard, String yearMonth) {
            return GaarasonDataSourceContext.executeDataSourceGroup(resolveGroupByCountry(country), () ->
                GaarasonDataSourceContext.executeDatabase(resolveDatabaseByUserId(userId), () ->
                    GaarasonDataSourceContext.executeTable(String.valueOf(orderShard), () -> {
                        AtomicReference<String> orderRelationSql = new AtomicReference<>("");
                        AtomicReference<String> productRelationSql = new AtomicReference<>("");

                        Record<BizUserEntity, Long> userRecord = userModel.findOrFail(userId);
                        BizUserEntity user = userRecord.with("orders", builder -> {
                            Builder<?, ?, ?> relationBuilder = builder.where("order_month", yearMonth);
                            orderRelationSql.set(relationBuilder.toSql(SqlType.SELECT));
                            return relationBuilder;
                        }).toObject();

                        int orderCount = user.getOrders() == null ? 0 : user.getOrders().size();
                        int productCount = 0;
                        if (orderCount > 0) {
                            Long orderId = user.getOrders().get(0).getId();
                            BizOrderEntity order = orderModel.findOrFail(orderId).with("products", builder -> {
                                productRelationSql.set(builder.toSql(SqlType.SELECT));
                                return builder;
                            }).toObject();
                            productCount = order.getProducts() == null ? 0 : order.getProducts().size();
                        }

                        return new RelationQueryResult(
                            orderCount,
                            productCount,
                            routingTrace.getDataSourceUrl(),
                            routingTrace.getCatalog(),
                            orderRelationSql.get(),
                            productRelationSql.get()
                        );
                    })
                )
            );
        }

        @GaarasonDataSourceGroup(spel = true, value = "@bizRoutingAnnotationHelper.groupByCountry(#p0)")
        @GaarasonDatabase(spel = true, value = "@bizRoutingAnnotationHelper.databaseByUserId(#p1)")
        @GaarasonTable(spel = true, value = "#p2")
        RelationQueryResult queryByRelationWithAnnotation(String country, Long userId, int orderShard, String yearMonth) {
            return queryByRelationCore(userId, yearMonth);
        }

        JoinQueryResult queryByJoin(String country, Long userId, int orderShard, String yearMonth) {
            return GaarasonDataSourceContext.executeDataSourceGroup(resolveGroupByCountry(country), () ->
                GaarasonDataSourceContext.executeDatabase(resolveDatabaseByUserId(userId), () ->
                    GaarasonDataSourceContext.executeTable(String.valueOf(orderShard), () -> {
                        return queryByJoinCore(userId, yearMonth);
                    })
                )
            );
        }

        private String resolveGroupByCountry(String country) {
            return Math.floorMod(country.hashCode(), 2) == 0 ? "region_a" : "region_b";
        }

        private String resolveDatabaseByUserId(Long userId) {
            return Math.floorMod(userId.intValue(), 2) == 0 ? "m2" : "m1";
        }

        @GaarasonDataSourceGroup(spel = true, value = "@bizRoutingAnnotationHelper.groupByCountry(#p0)")
        @GaarasonDatabase(spel = true, value = "@bizRoutingAnnotationHelper.databaseByUserId(#p1)")
        @GaarasonTable(spel = true, value = "#p2")
        JoinQueryResult queryByJoinWithAnnotation(String country, Long userId, int orderShard, String yearMonth) {
            return queryByJoinCore(userId, yearMonth);
        }

        private RelationQueryResult queryByRelationCore(Long userId, String yearMonth) {
            AtomicReference<String> orderRelationSql = new AtomicReference<>("");
            AtomicReference<String> productRelationSql = new AtomicReference<>("");

            Record<BizUserEntity, Long> userRecord = userModel.findOrFail(userId);
            BizUserEntity user = userRecord.with("orders", builder -> {
                Builder<?, ?, ?> relationBuilder = builder.where("order_month", yearMonth);
                orderRelationSql.set(relationBuilder.toSql(SqlType.SELECT));
                return relationBuilder;
            }).toObject();

            int orderCount = user.getOrders() == null ? 0 : user.getOrders().size();
            int productCount = 0;
            if (orderCount > 0) {
                Long orderId = user.getOrders().get(0).getId();
                BizOrderEntity order = orderModel.findOrFail(orderId).with("products", builder -> {
                    productRelationSql.set(builder.toSql(SqlType.SELECT));
                    return builder;
                }).toObject();
                productCount = order.getProducts() == null ? 0 : order.getProducts().size();
            }

            return new RelationQueryResult(
                orderCount,
                productCount,
                routingTrace.getDataSourceUrl(),
                routingTrace.getCatalog(),
                orderRelationSql.get(),
                productRelationSql.get()
            );
        }

        private JoinQueryResult queryByJoinCore(Long userId, String yearMonth) {
            String joinSql = orderModel.newQuery()
                .from("订单")
                .join(JoinType.INNER, "订单商品 as op inner join 商品 as p on op.product_id = p.id",
                    "id", "=", "`op`.`order_id`")
                .where("user_id", userId)
                .where("order_month", yearMonth)
                .toSql(SqlType.SELECT);

            Long joinRowCount = orderModel.newQuery()
                .from("订单")
                .join(JoinType.INNER, "订单商品 as op inner join 商品 as p on op.product_id = p.id",
                    "id", "=", "`op`.`order_id`")
                .where("user_id", userId)
                .where("order_month", yearMonth)
                .count("*");

            return new JoinQueryResult(
                joinSql,
                joinRowCount,
                routingTrace.getDataSourceUrl(),
                routingTrace.getCatalog()
            );
        }
    }

    static class BizRoutingAnnotationHelper {

        public String groupByCountry(String country) {
            return Math.floorMod(country.hashCode(), 2) == 0 ? "region_a" : "region_b";
        }

        public String databaseByUserId(Long userId) {
            return Math.floorMod(userId.intValue(), 2) == 0 ? "m2" : "m1";
        }
    }

    static class RoutingTrace {

        private final ThreadLocal<String> dataSourceUrl = new ThreadLocal<>();
        private final ThreadLocal<String> catalog = new ThreadLocal<>();

        void mark(DataSource dataSource, String catalogKey) {
            if (dataSource instanceof DruidDataSource) {
                dataSourceUrl.set(((DruidDataSource) dataSource).getUrl());
            } else {
                // 兼容 Hikari / 其他数据源实现，尽量提取 JDBC URL 以断言组切换
                String url = extractJdbcUrl(dataSource);
                if (url != null) {
                    dataSourceUrl.set(url);
                }
            }
            catalog.set(catalogKey);
        }

        private String extractJdbcUrl(DataSource dataSource) {
            try {
                java.lang.reflect.Method getUrl = dataSource.getClass().getMethod("getUrl");
                Object value = getUrl.invoke(dataSource);
                if (value != null) {
                    return value.toString();
                }
            } catch (Throwable ignored) {
                // ignored
            }
            try {
                java.lang.reflect.Method getJdbcUrl = dataSource.getClass().getMethod("getJdbcUrl");
                Object value = getJdbcUrl.invoke(dataSource);
                if (value != null) {
                    return value.toString();
                }
            } catch (Throwable ignored) {
                // ignored
            }
            return null;
        }

        String getDataSourceUrl() {
            return dataSourceUrl.get() == null ? "" : dataSourceUrl.get();
        }

        String getCatalog() {
            return catalog.get() == null ? "" : catalog.get();
        }

        void clear() {
            dataSourceUrl.remove();
            catalog.remove();
        }
    }

    static class RelationQueryResult {

        private final int orderCount;
        private final int productCount;
        private final String dataSourceUrl;
        private final String catalog;
        private final String orderRelationSql;
        private final String productRelationSql;

        RelationQueryResult(int orderCount, int productCount, String dataSourceUrl, String catalog,
            String orderRelationSql, String productRelationSql) {
            this.orderCount = orderCount;
            this.productCount = productCount;
            this.dataSourceUrl = dataSourceUrl;
            this.catalog = catalog;
            this.orderRelationSql = orderRelationSql;
            this.productRelationSql = productRelationSql;
        }

        int getOrderCount() {
            return orderCount;
        }

        int getProductCount() {
            return productCount;
        }

        String getDataSourceUrl() {
            return dataSourceUrl;
        }

        String getCatalog() {
            return catalog;
        }

        String getOrderRelationSql() {
            return orderRelationSql;
        }

        String getProductRelationSql() {
            return productRelationSql;
        }
    }

    static class JoinQueryResult {

        private final String joinSql;
        private final Long joinRowCount;
        private final String dataSourceUrl;
        private final String catalog;

        JoinQueryResult(String joinSql, Long joinRowCount, String dataSourceUrl, String catalog) {
            this.joinSql = joinSql;
            this.joinRowCount = joinRowCount;
            this.dataSourceUrl = dataSourceUrl;
            this.catalog = catalog;
        }

        String getJoinSql() {
            return joinSql;
        }

        Long getJoinRowCount() {
            return joinRowCount;
        }

        String getDataSourceUrl() {
            return dataSourceUrl;
        }

        String getCatalog() {
            return catalog;
        }
    }

    @Table(name = "用户")
    static class BizUserEntity implements Serializable {
        private static final long serialVersionUID = 1L;
        @Primary
        @Column(name = "id")
        private Long id;
        @Column(name = "country")
        private String country;
        @Column(name = "name")
        private String name;
        @HasOneOrMany(sonModelForeignKey = "user_id")
        private List<BizOrderEntity> orders;

        public Long getId() {
            return id;
        }

        public List<BizOrderEntity> getOrders() {
            return orders;
        }
    }

    @Table(name = "订单")
    static class BizOrderEntity implements Serializable {
        private static final long serialVersionUID = 1L;
        @Primary
        @Column(name = "id")
        private Long id;
        @Column(name = "user_id")
        private Long userId;
        @Column(name = "order_no")
        private String orderNo;
        @Column(name = "order_month")
        private String orderMonth;
        @BelongsToMany(
            relationModel = BizOrderProductModel.class,
            foreignKeyForLocalModel = "order_id",
            foreignKeyForTargetModel = "product_id",
            localModelLocalKey = "id",
            targetModelLocalKey = "id"
        )
        private List<BizProductEntity> products;

        public Long getId() {
            return id;
        }

        public List<BizProductEntity> getProducts() {
            return products;
        }
    }

    @Table(name = "商品")
    static class BizProductEntity implements Serializable {
        private static final long serialVersionUID = 1L;
        @Primary
        @Column(name = "id")
        private Long id;
        @Column(name = "name")
        private String name;
    }

    @Table(name = "订单商品")
    static class BizOrderProductEntity implements Serializable {
        private static final long serialVersionUID = 1L;
        @Primary
        @Column(name = "id")
        private Long id;
        @Column(name = "order_id")
        private Long orderId;
        @Column(name = "product_id")
        private Long productId;
    }

    static class BizUserModel extends BaseModel<BizUserEntity, Long> {
    }

    static class BizOrderModel extends BaseModel<BizOrderEntity, Long> {
    }

    static class BizProductModel extends BaseModel<BizProductEntity, Long> {
    }

    static class BizOrderProductModel extends BaseModel<BizOrderProductEntity, Long> {
    }
}
