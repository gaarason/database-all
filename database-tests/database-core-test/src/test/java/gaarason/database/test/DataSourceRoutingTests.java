package gaarason.database.test;

import gaarason.database.bootstrap.ContainerBootstrap;
import gaarason.database.connection.DataSourceGroup;
import gaarason.database.connection.GaarasonDataSourceContext;
import gaarason.database.connection.GaarasonRoutingDataSourceBuilder;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.core.Container;
import gaarason.database.test.utils.MultiThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Savepoint;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 多数据源分组路由相关单元测试
 * <p>
 * 覆盖: {@link GaarasonDataSourceContext}(组/库/表栈、解析扩展)、{@link DataSourceGroup}、{@link GaarasonRoutingDataSourceBuilder}
 * 及事务内组键锁定、代理连接上的 catalog 切换等.
 *
 * @author xt
 */
@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class DataSourceRoutingTests {

    @After
    public void tearDown() {
        GaarasonDataSourceContext.setDynamicDatabaseRouting(databaseKey -> databaseKey);
        GaarasonDataSourceContext.setDynamicTableRouting((logicalTableName, routeExpression) -> {
            if (routeExpression == null || routeExpression.isEmpty()) {
                return logicalTableName;
            }
            return logicalTableName + "_" + routeExpression;
        });
        GaarasonDataSourceContext.setDynamicExplicitTableRouting(() -> true);
    }

    // ============================= GaarasonDataSourceContext =============================

    @Test
    public void 路由上下文_execute带返回值_自动恢复() {
        String result = GaarasonDataSourceContext.executeDataSourceGroup("order", () -> {
            return "done";
        });
        Assert.assertEquals("done", result);
    }

    @Test
    public void 路由上下文_execute异常后仍恢复() {
        try {
            GaarasonDataSourceContext.executeDataSourceGroup("order", () -> {
                throw new RuntimeException("模拟业务异常");
            });
            Assert.fail("应该抛出异常");
        } catch (RuntimeException e) {
            Assert.assertEquals("模拟业务异常", e.getMessage());
        }
    }

    @Test
    public void 库上下文_execute嵌套_自动恢复() {
        String result = GaarasonDataSourceContext.executeDatabase("db1", () -> {
            return GaarasonDataSourceContext.executeDatabase("db2", () -> {
                return "ok";
            });
        });
        Assert.assertEquals("ok", result);
    }

    @Test
    public void 动态表上下文_execute嵌套_自动恢复() {
        GaarasonDataSourceContext.executeTable("001", () -> {
            Assert.assertEquals("student_001", GaarasonDataSourceContext.resolvePhysicalTableName("student"));
            GaarasonDataSourceContext.executeTable("002", () -> {
                Assert.assertEquals("student_002", GaarasonDataSourceContext.resolvePhysicalTableName("student"));
            });
        });
    }

    @Test
    public void 动态表上下文_可自定义解析器() {
        GaarasonDataSourceContext.setDynamicTableRouting((logical, expr) -> logical + "__" + expr);
        GaarasonDataSourceContext.executeTable("shardA", () -> {
            Assert.assertEquals("student__shardA", GaarasonDataSourceContext.resolvePhysicalTableName("student"));
        });
    }

    @Test
    public void 动态表上下文_覆盖策略可配置() {
        Assert.assertTrue(GaarasonDataSourceContext.shouldDynamicTableOverrideExplicit());
        GaarasonDataSourceContext.setDynamicExplicitTableRouting(() -> false);
        Assert.assertFalse(GaarasonDataSourceContext.shouldDynamicTableOverrideExplicit());
    }

    @Test
    public void 路由上下文_多线程并发安全() {
        MultiThreadUtil.run(50, 100, () -> GaarasonDataSourceContext.executeDataSourceGroup(
            Thread.currentThread().getName(), () -> {
                // 执行成功即证明上下文在并发场景可独立入栈出栈
            }));
    }

    // ============================= DataSourceGroup =============================

    @Test(expected = IllegalArgumentException.class)
    public void 数据源组_空主库列表抛异常() {
        new DataSourceGroup(Collections.emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void 数据源组_空主库列表_含从库_抛异常() {
        new DataSourceGroup(Collections.emptyList(), Collections.singletonList(dummyDs("slave1")));
    }

    @Test
    public void 数据源组_仅主库时_hasSlave为false() {
        DataSourceGroup group = new DataSourceGroup(Collections.singletonList(dummyDs("master1")));
        Assert.assertFalse(group.hasSlave());
        Assert.assertTrue(group.getSlaveDataSourceList().isEmpty());
        Assert.assertEquals(1, group.getMasterDataSourceList().size());
    }

    @Test
    public void 数据源组_有从库时_hasSlave为true() {
        DataSourceGroup group = new DataSourceGroup(
            Collections.singletonList(dummyDs("master1")),
            Collections.singletonList(dummyDs("slave1"))
        );
        Assert.assertTrue(group.hasSlave());
        Assert.assertEquals(1, group.getMasterDataSourceList().size());
        Assert.assertEquals(1, group.getSlaveDataSourceList().size());
    }

    @Test
    public void 数据源组_仅主库时_读写都返回主库() {
        DataSource master = dummyDs("master1");
        DataSourceGroup group = new DataSourceGroup(Collections.singletonList(master));

        Assert.assertSame(master, group.select(true));
        Assert.assertSame(master, group.select(false));
    }

    @Test
    public void 数据源组_有从库时_写返回主库_读返回从库() {
        DataSource master = dummyDs("master1");
        DataSource slave = dummyDs("slave1");
        DataSourceGroup group = new DataSourceGroup(
            Collections.singletonList(master),
            Collections.singletonList(slave)
        );

        Assert.assertSame(master, group.select(true));
        Assert.assertSame(slave, group.select(false));
    }

    @Test
    public void 数据源组_多主库随机选择() {
        DataSource master1 = dummyDs("master1");
        DataSource master2 = dummyDs("master2");
        DataSourceGroup group = new DataSourceGroup(Arrays.asList(master1, master2));

        boolean hitMaster1 = false;
        boolean hitMaster2 = false;
        for (int i = 0; i < 100; i++) {
            DataSource selected = group.select(true);
            if (selected == master1) hitMaster1 = true;
            if (selected == master2) hitMaster2 = true;
        }
        Assert.assertTrue("100次中应该至少命中一次master1", hitMaster1);
        Assert.assertTrue("100次中应该至少命中一次master2", hitMaster2);
    }

    @Test
    public void 数据源组_空从库列表视为无从库() {
        DataSource master = dummyDs("master1");
        DataSourceGroup group = new DataSourceGroup(
            Collections.singletonList(master),
            Collections.emptyList()
        );
        Assert.assertFalse(group.hasSlave());
        Assert.assertSame(master, group.select(false));
    }

    @Test
    public void 数据源组_列表不可变() {
        DataSource master = dummyDs("master1");
        DataSource slave = dummyDs("slave1");
        DataSourceGroup group = new DataSourceGroup(
            Collections.singletonList(master),
            Collections.singletonList(slave)
        );

        try {
            group.getMasterDataSourceList().add(dummyDs("master2"));
            Assert.fail("master列表应该不可变");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            group.getSlaveDataSourceList().add(dummyDs("slave2"));
            Assert.fail("slave列表应该不可变");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ============================= GaarasonRoutingDataSourceBuilder =============================

    @Test(expected = IllegalArgumentException.class)
    public void 路由构建器_空组抛异常() {
        GaarasonRoutingDataSourceBuilder.create()
            .defaultGroup("master")
            .build(buildContainer());
    }

    @Test(expected = IllegalArgumentException.class)
    public void 路由构建器_默认组不存在时抛异常() {
        GaarasonRoutingDataSourceBuilder.create()
            .defaultGroup("nonexistent")
            .group("master", Collections.singletonList(dummyDs("master1")))
            .build(buildContainer());
    }

    @Test
    public void 路由构建器_正常构建_返回GaarasonDataSource() {
        GaarasonDataSource ds = GaarasonRoutingDataSourceBuilder.create()
            .defaultGroup("master")
            .group("master", Collections.singletonList(dummyDs("master1")))
            .build(buildContainer());

        Assert.assertNotNull(ds);
        Assert.assertEquals(1, ds.getMasterDataSourceList().size());
    }

    @Test
    public void 路由构建器_多组构建_默认路由到默认组() {
        DataSource masterDs = dummyDs("master1");
        DataSource orderDs = dummyDs("order1");

        GaarasonDataSource ds = GaarasonRoutingDataSourceBuilder.create()
            .defaultGroup("master")
            .group("master", Collections.singletonList(masterDs))
            .group("order", Collections.singletonList(orderDs))
            .build(buildContainer());

        Assert.assertTrue(ds.getMasterDataSourceList().contains(masterDs));
    }

    @Test
    public void 路由构建器_切换上下文后路由到指定组() {
        DataSource masterDs = dummyDs("master1");
        DataSource orderDs = dummyDs("order1");

        GaarasonDataSource ds = GaarasonRoutingDataSourceBuilder.create()
            .defaultGroup("master")
            .group("master", Collections.singletonList(masterDs))
            .group("order", Collections.singletonList(orderDs))
            .build(buildContainer());

        Assert.assertTrue("默认应路由到master组",
            ds.getMasterDataSourceList().contains(masterDs));

        GaarasonDataSourceContext.executeDataSourceGroup("order", () -> {
            Assert.assertTrue("切换后应路由到order组",
                ds.getMasterDataSourceList().contains(orderDs));
        });

        Assert.assertTrue("清除后应恢复到master组",
            ds.getMasterDataSourceList().contains(masterDs));
    }

    @Test
    public void 路由构建器_使用DataSourceGroup对象构建() {
        DataSource masterDs = dummyDs("master1");
        DataSource slaveDs = dummyDs("slave1");

        DataSourceGroup group = new DataSourceGroup(
            Collections.singletonList(masterDs),
            Collections.singletonList(slaveDs)
        );

        GaarasonDataSource ds = GaarasonRoutingDataSourceBuilder.create()
            .defaultGroup("main")
            .group("main", group)
            .build(buildContainer());

        Assert.assertNotNull(ds);
        Assert.assertTrue(ds.getMasterDataSourceList().contains(masterDs));
        Assert.assertTrue(ds.getSlaveDataSourceList().contains(slaveDs));
    }

    @Test
    public void 路由构建器_含主从的组构建() {
        DataSource masterDs = dummyDs("master1");
        DataSource slaveDs = dummyDs("slave1");

        GaarasonDataSource ds = GaarasonRoutingDataSourceBuilder.create()
            .defaultGroup("master")
            .group("master",
                Collections.singletonList(masterDs),
                Collections.singletonList(slaveDs))
            .build(buildContainer());

        Assert.assertNotNull(ds);
        Assert.assertTrue(ds.getMasterDataSourceList().contains(masterDs));
        Assert.assertTrue(ds.getSlaveDataSourceList().contains(slaveDs));
    }

    @Test
    public void 路由构建器_多线程路由隔离() {
        DataSource masterDs = dummyDs("master1");
        DataSource orderDs = dummyDs("order1");
        DataSource analyticsDs = dummyDs("analytics1");

        GaarasonDataSource ds = GaarasonRoutingDataSourceBuilder.create()
            .defaultGroup("master")
            .group("master", Collections.singletonList(masterDs))
            .group("order", Collections.singletonList(orderDs))
            .group("analytics", Collections.singletonList(analyticsDs))
            .build(buildContainer());

        MultiThreadUtil.run(30, 50, () -> {
            Assert.assertTrue("默认路由到master", ds.getMasterDataSourceList().contains(masterDs));

            GaarasonDataSourceContext.executeDataSourceGroup("order", () -> {
                Assert.assertTrue("应路由到order", ds.getMasterDataSourceList().contains(orderDs));

                GaarasonDataSourceContext.executeDataSourceGroup("analytics", () -> {
                    Assert.assertTrue("应路由到analytics",
                        ds.getMasterDataSourceList().contains(analyticsDs));
                });

                Assert.assertTrue("应恢复到order", ds.getMasterDataSourceList().contains(orderDs));
            });

            Assert.assertTrue("应恢复到master", ds.getMasterDataSourceList().contains(masterDs));
        });
    }

    @Test
    public void 路由构建器_事务内锁定数据库键() {
        AtomicReference<String> lastDatabaseKey = new AtomicReference<>();
        DataSource masterDs = switchableDs("master1", lastDatabaseKey);

        GaarasonDataSource ds = GaarasonRoutingDataSourceBuilder.create()
            .defaultGroup("master")
            .group("master", Collections.singletonList(masterDs))
            .build(buildContainer());

        GaarasonDataSourceContext.executeDatabase("db001", () -> {
            ds.begin();
            try {
                Connection txConnection = ds.getLocalConnection(false);
                Assert.assertEquals("db001", lastDatabaseKey.get());

                GaarasonDataSourceContext.executeDatabase("db002", () -> {
                    Connection txConnectionAgain = ds.getLocalConnection(false);
                    Assert.assertSame(txConnection, txConnectionAgain);
                    Assert.assertEquals("事务内应锁定首次数据库键", "db001", lastDatabaseKey.get());
                });
            } finally {
                ds.commit();
            }
        });
    }

    @Test
    public void 路由构建器_可注入动态表解析器() {
        GaarasonRoutingDataSourceBuilder.create()
            .defaultGroup("master")
            .group("master", Collections.singletonList(dummyDs("master1")))
            .dynamicTableRouting((logical, expr) -> logical + "_custom_" + expr)
            .build(buildContainer());

        GaarasonDataSourceContext.executeTable("007", () -> {
            Assert.assertEquals("student_custom_007", GaarasonDataSourceContext.resolvePhysicalTableName("student"));
        });
    }

    // ============================= 辅助方法 =============================

    /**
     * 创建最小化 Container, 仅用于构建路由数据源, 不启动扫描
     */
    private static Container buildContainer() {
        return ContainerBootstrap.build();
    }

    /**
     * 创建不执行任何操作的 DataSource 桩对象
     * @param name 标识名, 用于 toString 区分
     * @return DataSource 代理
     */
    private static DataSource dummyDs(String name) {
        return (DataSource) Proxy.newProxyInstance(
            DataSource.class.getClassLoader(),
            new Class<?>[]{DataSource.class},
            new DummyDataSourceHandler(name)
        );
    }

    private static DataSource switchableDs(String name, AtomicReference<String> lastDatabaseKey) {
        return (DataSource) Proxy.newProxyInstance(
            DataSource.class.getClassLoader(),
            new Class<?>[]{DataSource.class},
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getConnection":
                        return switchableConnection(name, lastDatabaseKey);
                    case "toString":
                        return "SwitchableDataSource[" + name + "]";
                    case "hashCode":
                        return name.hashCode();
                    case "equals":
                        return proxy == args[0];
                    default:
                        throw new UnsupportedOperationException(
                            "SwitchableDataSource[" + name + "] does not support: " + method.getName());
                }
            }
        );
    }

    private static Connection switchableConnection(String name, AtomicReference<String> lastDatabaseKey) {
        AtomicReference<Boolean> closed = new AtomicReference<>(false);
        return (Connection) Proxy.newProxyInstance(
            Connection.class.getClassLoader(),
            new Class<?>[]{Connection.class},
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "setCatalog":
                    case "setSchema":
                        lastDatabaseKey.set(String.valueOf(args[0]));
                        return null;
                    case "setAutoCommit":
                    case "commit":
                    case "rollback":
                    case "releaseSavepoint":
                        return null;
                    case "setSavepoint":
                        return Proxy.newProxyInstance(
                            Savepoint.class.getClassLoader(),
                            new Class<?>[]{Savepoint.class},
                            (p, m, a) -> 1
                        );
                    case "isClosed":
                        return closed.get();
                    case "close":
                        closed.set(true);
                        return null;
                    case "toString":
                        return "SwitchableConnection[" + name + "]";
                    case "hashCode":
                        return System.identityHashCode(proxy);
                    case "equals":
                        return proxy == args[0];
                    default:
                        throw new UnsupportedOperationException(
                            "SwitchableConnection[" + name + "] does not support: " + method.getName());
                }
            }
        );
    }

    /**
     * DataSource 代理处理器
     */
    private static class DummyDataSourceHandler implements InvocationHandler {
        private final String name;

        DummyDataSourceHandler(String name) {
            this.name = name;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            switch (method.getName()) {
                case "toString":
                    return "DummyDataSource[" + name + "]";
                case "hashCode":
                    return name.hashCode();
                case "equals":
                    return proxy == args[0];
                default:
                    throw new UnsupportedOperationException(
                        "DummyDataSource[" + name + "] does not support: " + method.getName());
            }
        }
    }
}
