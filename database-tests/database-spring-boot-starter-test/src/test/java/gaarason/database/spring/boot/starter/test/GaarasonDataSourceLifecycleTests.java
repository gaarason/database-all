package gaarason.database.spring.boot.starter.test;

import gaarason.database.bootstrap.ContainerBootstrap;
import gaarason.database.connection.DataSourceGroup;
import gaarason.database.connection.GaarasonRoutingDataSourceWrapper;
import gaarason.database.core.Container;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 路由数据源的资源生命周期验证(纯单测, 不依赖数据库).
 * <p>
 * 覆盖: 自动配置自建的连接池随包装器关闭、Spring 托管的连接池不被越权关闭、重复关闭保持幂等.
 *
 * @author xt
 */
public class GaarasonDataSourceLifecycleTests {

    /**
     * 由自动配置自建的连接池, 上下文关闭时必须被释放, 否则连接与池线程一直残留
     */
    @Test
    public void 关闭路由数据源应释放自建的连接池() {
        RecordingDataSource created = new RecordingDataSource();
        GaarasonRoutingDataSourceWrapper wrapper = newWrapper(Collections.singletonList(created),
            Collections.singletonList(created));

        wrapper.close();

        Assert.assertTrue("自建的连接池应被关闭", created.closed);
    }

    /**
     * Spring 容器托管的 DataSource 由其自身生命周期关闭, 包装器不能越权关闭
     */
    @Test
    public void 关闭路由数据源不应关闭Spring托管的连接池() {
        RecordingDataSource springManaged = new RecordingDataSource();
        GaarasonRoutingDataSourceWrapper wrapper = newWrapper(Collections.singletonList(springManaged),
            Collections.emptyList());

        wrapper.close();

        Assert.assertFalse("Spring 托管的连接池不应被包装器关闭", springManaged.closed);
    }

    /**
     * 重复关闭必须幂等, 不能抛出异常影响其他 Bean 的销毁
     */
    @Test
    public void 重复关闭路由数据源应保持幂等() {
        RecordingDataSource created = new RecordingDataSource();
        GaarasonRoutingDataSourceWrapper wrapper = newWrapper(Collections.singletonList(created),
            Collections.singletonList(created));

        wrapper.close();
        wrapper.close();

        Assert.assertTrue(created.closed);
        Assert.assertEquals("关闭动作应只发生一次", 1, created.closeCount);
    }

    /**
     * 关闭时单个连接池抛异常不应中断其余连接池的释放
     */
    @Test
    public void 连接池关闭异常不应中断其余连接池释放() {
        RecordingDataSource broken = new RecordingDataSource();
        broken.throwOnClose = true;
        RecordingDataSource another = new RecordingDataSource();
        GaarasonRoutingDataSourceWrapper wrapper = newWrapper(Arrays.asList(broken, another),
            Arrays.asList(broken, another));

        wrapper.close();

        Assert.assertTrue("后续连接池仍应被关闭", another.closed);
    }

    /**
     * 构造 master/order 两组的路由数据源
     *
     * @param groupDataSources 组内可见的数据源(视为 master 组的成员)
     * @param createdDataSources 由自动配置自建、需要包装器负责关闭的数据源
     */
    private static GaarasonRoutingDataSourceWrapper newWrapper(List<DataSource> groupDataSources,
        List<DataSource> createdDataSources) {
        Map<String, DataSourceGroup> groupMap = new LinkedHashMap<>();
        groupMap.put("master", new DataSourceGroup(groupDataSources));
        groupMap.put("order", new DataSourceGroup(Collections.singletonList(new RecordingDataSource())));
        return new GaarasonRoutingDataSourceWrapper(groupMap, "master", newContainer(), createdDataSources);
    }

    private static Container newContainer() {
        ContainerBootstrap container = ContainerBootstrap.build();
        container.signUpIdentification("starter-lifecycle-" + System.nanoTime());
        container.defaultRegister();
        return container;
    }

    /**
     * 记录关闭动作的数据源桩
     */
    private static class RecordingDataSource implements DataSource, AutoCloseable {

        private boolean closed;

        private int closeCount;

        private boolean throwOnClose;

        @Override
        public void close() {
            closeCount++;
            closed = true;
            if (throwOnClose) {
                throw new IllegalStateException("模拟连接池关闭失败");
            }
        }

        @Override
        public Connection getConnection() throws SQLException {
            throw new SQLException("not supported");
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            throw new SQLException("not supported");
        }

        @Override
        public PrintWriter getLogWriter() {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) {
        }

        @Override
        public void setLoginTimeout(int seconds) {
        }

        @Override
        public int getLoginTimeout() {
            return 0;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new SQLException("not a wrapper");
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }
    }
}
