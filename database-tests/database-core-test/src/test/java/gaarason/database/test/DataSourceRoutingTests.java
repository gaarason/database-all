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
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 多数据源分组路由相关单元测试
 * <p>
 * 覆盖: {@link GaarasonDataSourceContext}, {@link DataSourceGroup}, {@link GaarasonRoutingDataSourceBuilder}
 * @author xt
 */
@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class DataSourceRoutingTests {

    @After
    public void tearDown() {
        while (GaarasonDataSourceContext.get() != null) {
            GaarasonDataSourceContext.clear();
        }
    }

    // ============================= GaarasonDataSourceContext =============================

    @Test
    public void 路由上下文_设置并获取() {
        Assert.assertNull(GaarasonDataSourceContext.get());
        GaarasonDataSourceContext.set("order");
        Assert.assertEquals("order", GaarasonDataSourceContext.get());
        GaarasonDataSourceContext.clear();
        Assert.assertNull(GaarasonDataSourceContext.get());
    }

    @Test
    public void 路由上下文_清除空栈不抛异常() {
        Assert.assertNull(GaarasonDataSourceContext.get());
        GaarasonDataSourceContext.clear();
        Assert.assertNull(GaarasonDataSourceContext.get());
    }

    @Test
    public void 路由上下文_嵌套设置_栈式恢复() {
        GaarasonDataSourceContext.set("master");
        Assert.assertEquals("master", GaarasonDataSourceContext.get());

        GaarasonDataSourceContext.set("order");
        Assert.assertEquals("order", GaarasonDataSourceContext.get());

        GaarasonDataSourceContext.set("analytics");
        Assert.assertEquals("analytics", GaarasonDataSourceContext.get());

        GaarasonDataSourceContext.clear();
        Assert.assertEquals("order", GaarasonDataSourceContext.get());

        GaarasonDataSourceContext.clear();
        Assert.assertEquals("master", GaarasonDataSourceContext.get());

        GaarasonDataSourceContext.clear();
        Assert.assertNull(GaarasonDataSourceContext.get());
    }

    @Test
    public void 路由上下文_execute带返回值_自动恢复() {
        Assert.assertNull(GaarasonDataSourceContext.get());

        String result = GaarasonDataSourceContext.execute("order", () -> {
            Assert.assertEquals("order", GaarasonDataSourceContext.get());
            return "done";
        });

        Assert.assertEquals("done", result);
        Assert.assertNull(GaarasonDataSourceContext.get());
    }

    @Test
    public void 路由上下文_execute无返回值_自动恢复() {
        Assert.assertNull(GaarasonDataSourceContext.get());

        GaarasonDataSourceContext.execute("order", () -> {
            Assert.assertEquals("order", GaarasonDataSourceContext.get());
        });

        Assert.assertNull(GaarasonDataSourceContext.get());
    }

    @Test
    public void 路由上下文_execute嵌套_外层自动恢复() {
        GaarasonDataSourceContext.set("master");

        GaarasonDataSourceContext.execute("order", () -> {
            Assert.assertEquals("order", GaarasonDataSourceContext.get());

            String inner = GaarasonDataSourceContext.execute("analytics", () -> {
                Assert.assertEquals("analytics", GaarasonDataSourceContext.get());
                return "inner-done";
            });
            Assert.assertEquals("inner-done", inner);

            Assert.assertEquals("order", GaarasonDataSourceContext.get());
        });

        Assert.assertEquals("master", GaarasonDataSourceContext.get());
    }

    @Test
    public void 路由上下文_execute异常后仍恢复() {
        Assert.assertNull(GaarasonDataSourceContext.get());

        try {
            GaarasonDataSourceContext.execute("order", () -> {
                Assert.assertEquals("order", GaarasonDataSourceContext.get());
                throw new RuntimeException("模拟业务异常");
            });
            Assert.fail("应该抛出异常");
        } catch (RuntimeException e) {
            Assert.assertEquals("模拟业务异常", e.getMessage());
        }

        Assert.assertNull(GaarasonDataSourceContext.get());
    }

    @Test
    public void 路由上下文_线程隔离() throws InterruptedException {
        AtomicReference<String> threadResult = new AtomicReference<>();
        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(1);

        GaarasonDataSourceContext.set("main-thread-group");

        new Thread(() -> {
            try {
                ready.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            threadResult.set(GaarasonDataSourceContext.get());
            done.countDown();
        }).start();

        ready.countDown();
        done.await();

        Assert.assertNull("子线程不应继承父线程的上下文", threadResult.get());
        Assert.assertEquals("main-thread-group", GaarasonDataSourceContext.get());
    }

    @Test
    public void 路由上下文_多线程并发安全() {
        MultiThreadUtil.run(50, 100, () -> {
            String threadName = Thread.currentThread().getName();
            GaarasonDataSourceContext.set(threadName);
            Assert.assertEquals(threadName, GaarasonDataSourceContext.get());

            GaarasonDataSourceContext.set(threadName + "-inner");
            Assert.assertEquals(threadName + "-inner", GaarasonDataSourceContext.get());

            GaarasonDataSourceContext.clear();
            Assert.assertEquals(threadName, GaarasonDataSourceContext.get());

            GaarasonDataSourceContext.clear();
            Assert.assertNull(GaarasonDataSourceContext.get());
        });
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

        GaarasonDataSourceContext.set("order");
        try {
            Assert.assertTrue("切换后应路由到order组",
                ds.getMasterDataSourceList().contains(orderDs));
        } finally {
            GaarasonDataSourceContext.clear();
        }

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

            GaarasonDataSourceContext.execute("order", () -> {
                Assert.assertTrue("应路由到order", ds.getMasterDataSourceList().contains(orderDs));

                GaarasonDataSourceContext.execute("analytics", () -> {
                    Assert.assertTrue("应路由到analytics",
                        ds.getMasterDataSourceList().contains(analyticsDs));
                });

                Assert.assertTrue("应恢复到order", ds.getMasterDataSourceList().contains(orderDs));
            });

            Assert.assertTrue("应恢复到master", ds.getMasterDataSourceList().contains(masterDs));
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
