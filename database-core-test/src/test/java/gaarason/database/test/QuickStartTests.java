package gaarason.database.test;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.connections.ProxyDataSource;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Table;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class QuickStartTests {

    /**
     * step 1
     * 定义model
     */
    public static class TestModel extends Model<TestModel.Inner>{

        /**
         * step 2
         * 定义 DataSource
         * @return DataSource
         */
        private static DataSource dataSourceMaster0() {
            DruidDataSource druidDataSource = new DruidDataSource();
            druidDataSource.setUrl(
                "jdbc:mysql://sakya.local/test_master_0?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai");
            druidDataSource.setDbType("com.alibaba.druid.pool.DruidDataSource");
            druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            druidDataSource.setUsername("root");
            druidDataSource.setPassword("root");
            return druidDataSource;
        }

        /**
         * step 3
         * 定义 List<DataSource>
         * @return List<DataSource>
         */
        private static List<DataSource> dataSourceMasterList() {
            List<DataSource> dataSources = new ArrayList<>();
            dataSources.add(dataSourceMaster0());
            return dataSources;
        }

        /**
         * step 4
         * 定义 ProxyDataSource
         * @return ProxyDataSource
         */
        private static ProxyDataSource proxyDataSource() {
            List<DataSource> dataSources = dataSourceMasterList();
            return new ProxyDataSource(dataSources);
        }

        /**
         * step 5
         * 使用 ProxyDataSource
         * @return ProxyDataSource
         */
        @Override
        public ProxyDataSource getProxyDataSource() {
            return proxyDataSource();
        }

        /**
         * step 6
         * 定义 entity
         */
        @Data
        @Table(name = "student")
        public static class Inner {

        }
    }

    @Test
    public void testSelect() {
        TestModel testModel = new TestModel();
        List<Map<String, Object>> maps = testModel.newQuery().limit(3).get().toMapList();
        System.out.println(maps);
        Assert.assertEquals(3, maps.size());
    }

}
