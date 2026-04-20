package gaarason.database.spring.boot.starter.test;

import gaarason.database.connection.GaarasonDataSourceContext;
import gaarason.database.spring.boot.starter.annotation.GaarasonDatabase;
import gaarason.database.spring.boot.starter.annotation.GaarasonDataSourceGroup;
import gaarason.database.spring.boot.starter.annotation.GaarasonTable;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * 数据源组 / Database / Table 三维度路由：注解字面量、注解 SpEL、与切面等效的 {@link GaarasonDataSourceContext} 编码.
 *
 * @author xt
 */
@RunWith(SpringRunner.class)
@org.springframework.boot.test.context.SpringBootTest(classes = TestApplication.class)
@Import(GaarasonThreeDimensionRoutingTests.ThreeDimensionRoutingBeans.class)
public class GaarasonThreeDimensionRoutingTests {

    private static final String G = "routing-group-x";
    private static final String D = "routing-db-y";
    private static final String T = "routing-table-z";

    @Resource
    private ThreeDimensionRoutingSubject subject;

    @Test
    public void 三维度组合_注解字面量() {
        RoutingSnapshot actual = subject.readCombinedLiteral();
        assertCombined(G, D, T, actual);
    }

    @Test
    public void 三维度组合_注解SpEL() {
        RoutingSnapshot actual = subject.readCombinedSpel(G, D, T);
        assertCombined(G, D, T, actual);
    }

    @Test
    public void 三维度组合_上下文嵌套execute与注解等效() {
        RoutingSnapshot actual = ThreeDimensionRoutingSubject.readViaNestedExecute(G, D, T);
        assertCombined(G, D, T, actual);
    }

    @Test
    public void 组维度_注解字面量() {
        RoutingSnapshot actual = subject.readGroupLiteral();
        Assert.assertEquals(G, actual.getGroup());
        Assert.assertNull(actual.getDatabase());
        Assert.assertNull(actual.getTable());
    }

    @Test
    public void 组维度_注解SpEL() {
        RoutingSnapshot actual = subject.readGroupSpel(G);
        Assert.assertEquals(G, actual.getGroup());
        Assert.assertNull(actual.getDatabase());
        Assert.assertNull(actual.getTable());
    }

    @Test
    public void 组维度_execute与注解等效() {
        RoutingSnapshot actual = ThreeDimensionRoutingSubject.readGroupViaExecute(G);
        Assert.assertEquals(G, actual.getGroup());
        Assert.assertNull(actual.getDatabase());
        Assert.assertNull(actual.getTable());
    }

    @Test
    public void 库维度_注解字面量() {
        RoutingSnapshot actual = subject.readDatabaseLiteral();
        Assert.assertNull(actual.getGroup());
        Assert.assertEquals(D, actual.getDatabase());
        Assert.assertNull(actual.getTable());
    }

    @Test
    public void 库维度_注解SpEL() {
        RoutingSnapshot actual = subject.readDatabaseSpel(D);
        Assert.assertNull(actual.getGroup());
        Assert.assertEquals(D, actual.getDatabase());
        Assert.assertNull(actual.getTable());
    }

    @Test
    public void 库维度_executeDatabase与注解等效() {
        RoutingSnapshot actual = ThreeDimensionRoutingSubject.readDatabaseViaExecute(D);
        Assert.assertNull(actual.getGroup());
        Assert.assertEquals(D, actual.getDatabase());
        Assert.assertNull(actual.getTable());
    }

    @Test
    public void 表维度_注解字面量() {
        RoutingSnapshot actual = subject.readTableLiteral();
        Assert.assertNull(actual.getGroup());
        Assert.assertNull(actual.getDatabase());
        Assert.assertEquals(T, actual.getTable());
    }

    @Test
    public void 表维度_注解SpEL() {
        RoutingSnapshot actual = subject.readTableSpel(T);
        Assert.assertNull(actual.getGroup());
        Assert.assertNull(actual.getDatabase());
        Assert.assertEquals(T, actual.getTable());
    }

    @Test
    public void 表维度_executeTable与注解等效() {
        RoutingSnapshot actual = ThreeDimensionRoutingSubject.readTableViaExecute(T);
        Assert.assertNull(actual.getGroup());
        Assert.assertNull(actual.getDatabase());
        Assert.assertEquals(T, actual.getTable());
    }

    private static void assertCombined(String g, String d, String t, RoutingSnapshot actual) {
        Assert.assertEquals(g, actual.getGroup());
        Assert.assertEquals(d, actual.getDatabase());
        Assert.assertEquals(t, actual.getTable());
    }

    @Configuration
    static class ThreeDimensionRoutingBeans {

        @Bean
        public ThreeDimensionRoutingSubject threeDimensionRoutingSubject() {
            return new ThreeDimensionRoutingSubject();
        }
    }

    /**
     * 与 {@link gaarason.database.spring.boot.starter.aop.GaarasonDataSourceAspect} 入栈顺序一致的三层嵌套 execute.
     */
    /**
     * 非 final，便于 AOP 使用 CGLIB 代理（与业务中可切面的 Service 一致）.
     */
    public static class ThreeDimensionRoutingSubject {

        @GaarasonDataSourceGroup(G)
        @GaarasonDatabase(D)
        @GaarasonTable(T)
        public RoutingSnapshot readCombinedLiteral() {
            return RoutingSnapshot.capture(G, D, T);
        }

        @GaarasonDataSourceGroup(spel = true, value = "#p0")
        @GaarasonDatabase(spel = true, value = "#p1")
        @GaarasonTable(spel = true, value = "#p2")
        public RoutingSnapshot readCombinedSpel(String groupKey, String databaseKey, String tableRoute) {
            return RoutingSnapshot.capture(groupKey, databaseKey, tableRoute);
        }

        @GaarasonDataSourceGroup(G)
        public RoutingSnapshot readGroupLiteral() {
            return RoutingSnapshot.capture(G, null, null);
        }

        @GaarasonDataSourceGroup(spel = true, value = "#p0")
        public RoutingSnapshot readGroupSpel(String groupKey) {
            return RoutingSnapshot.capture(groupKey, null, null);
        }

        @GaarasonDatabase(D)
        public RoutingSnapshot readDatabaseLiteral() {
            return RoutingSnapshot.capture(null, D, null);
        }

        @GaarasonDatabase(spel = true, value = "#p0")
        public RoutingSnapshot readDatabaseSpel(String databaseKey) {
            return RoutingSnapshot.capture(null, databaseKey, null);
        }

        @GaarasonTable(T)
        public RoutingSnapshot readTableLiteral() {
            return RoutingSnapshot.capture(null, null, T);
        }

        @GaarasonTable(spel = true, value = "#p0")
        public RoutingSnapshot readTableSpel(String tableRoute) {
            return RoutingSnapshot.capture(null, null, tableRoute);
        }

        public static RoutingSnapshot readViaNestedExecute(String groupKey, String databaseKey, String tableRoute) {
            return GaarasonDataSourceContext.executeDataSourceGroup(groupKey,
                () -> GaarasonDataSourceContext.executeDatabase(databaseKey,
                    () -> GaarasonDataSourceContext.executeTable(tableRoute,
                        () -> RoutingSnapshot.capture(groupKey, databaseKey, tableRoute))));
        }

        public static RoutingSnapshot readGroupViaExecute(String groupKey) {
            return GaarasonDataSourceContext.executeDataSourceGroup(groupKey,
                () -> RoutingSnapshot.capture(groupKey, null, null));
        }

        public static RoutingSnapshot readDatabaseViaExecute(String databaseKey) {
            return GaarasonDataSourceContext.executeDatabase(databaseKey,
                () -> RoutingSnapshot.capture(null, databaseKey, null));
        }

        public static RoutingSnapshot readTableViaExecute(String tableRoute) {
            return GaarasonDataSourceContext.executeTable(tableRoute,
                () -> RoutingSnapshot.capture(null, null, tableRoute));
        }
    }

    private static final class RoutingSnapshot {

        private final String group;
        private final String database;
        private final String table;

        private RoutingSnapshot(String group, String database, String table) {
            this.group = group;
            this.database = database;
            this.table = table;
        }

        static RoutingSnapshot capture(String group, String database, String table) {
            return new RoutingSnapshot(group, database, table);
        }

        String getGroup() {
            return group;
        }

        String getDatabase() {
            return database;
        }

        String getTable() {
            return table;
        }
    }
}
