package gaarason.database.test;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.connections.ProxyDataSource;
import gaarason.database.eloquent.annotations.Column;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.annotations.Primary;
import gaarason.database.eloquent.annotations.Table;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.util.*;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class QuickStartTests {

    /**
     * step 1
     * 定义model
     */
    public static class TestModel extends Model<TestModel.Inner, Integer> {

        private static ProxyDataSource proxyDataSource = proxyDataSource();

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
            druidDataSource.setInitialSize(5);
            druidDataSource.setMinIdle(5);
            druidDataSource.setMaxActive(10);
            druidDataSource.setMaxWait(60000);
            druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
            druidDataSource.setMinEvictableIdleTimeMillis(300000);
            druidDataSource.setValidationQuery("SELECT 1");
            List<String> iniSql = new ArrayList<>();
            iniSql.add(
                "SET SESSION SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION'");
            druidDataSource.setConnectionInitSqls(iniSql);
            druidDataSource.setTestOnBorrow(false);
            druidDataSource.setTestOnReturn(false);
            druidDataSource.setPoolPreparedStatements(false);
            druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(-1);
            Properties properties = new Properties();
            properties.setProperty("druid.stat.mergeSql", "true");
            properties.setProperty("druid.stat.slowSqlMillis", "5000");
            druidDataSource.setConnectProperties(properties);
            druidDataSource.setUseGlobalDataSourceStat(true);
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
            return proxyDataSource;
        }

        /**
         * step 6
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
    }

    @Test
    public void testSelect() {
        TestModel                 testModel = new TestModel();
        List<Map<String, Object>> maps      = testModel.newQuery().limit(3).get().toMapList();
        System.out.println(maps);
        Assert.assertEquals(3, maps.size());
    }

}
