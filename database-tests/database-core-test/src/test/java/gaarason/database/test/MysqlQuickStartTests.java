package gaarason.database.test;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import gaarason.database.connection.GaarasonDataSourceBuilder;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.support.FieldStrategy;
import gaarason.database.eloquent.Model;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.Serializable;
import java.util.*;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class MysqlQuickStartTests {

    @Test
    public void testSelect() {
        TestModel testModel = new TestModel();
        List<Map<String, Object>> maps = testModel.newQuery().limit(3).get().toMapList();
        System.out.println(maps);
        Assert.assertEquals(3, maps.size());
    }

    @Test
    public void 临时对象赋值() {
        TestModel testModel = new TestModel();
        List<TestModel.Inner> inners = testModel.newQuery().limit(3).get().toObjectList(TestModel.Inner.class);
        System.out.println(inners);
        Assert.assertEquals(3, inners.size());
    }

    @Test
    public void findMany() {
        TestModel testModel = new TestModel();
        RecordList<TestModel.Inner, Integer> many = testModel.findMany(Arrays.asList(1, 2, 3));
        RecordList<TestModel.Inner, Integer> many1 = testModel.findMany(1, 2, 3);
        Assert.assertEquals(3, many.size());
        Assert.assertEquals(many1.size(), 3);
        RecordList<TestModel.Inner, Integer> many2 = testModel.findMany(1, "2", 3L);
        Assert.assertEquals(many2.size(), 3);
    }

    /**
     * 定义model
     */
    public static class TestModel extends Model<TestModel.Inner, Integer> {

        private final static GaarasonDataSource gaarasonDataSourceWrapper;

        static {
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
            druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
            druidDataSource.setMinEvictableIdleTimeMillis(300000);
            druidDataSource.setValidationQuery("SELECT 1");
            List<String> iniSql = new ArrayList<>();
            iniSql.add(
                "SET SESSION SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION'");
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

            gaarasonDataSourceWrapper = GaarasonDataSourceBuilder.build(druidDataSource);
        }

        /**
         * 使用 ProxyDataSource
         * @return ProxyDataSource
         */
        @Override
        public GaarasonDataSource getGaarasonDataSource() {
            return gaarasonDataSourceWrapper;
        }

        /**
         * 定义 entity
         */
        @Data
        @Table(name = "student")
        public static class Inner implements Serializable {
            @Primary
            private Integer id;

            @Column(length = 20)
            private String name;

            private Byte age;

            private Byte sex;

            @Column(name = "teacher_id")
            private Integer teacherId;

            @Column(name = "created_at", insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class)
            private Date createdAt;

            @Column(name = "updated_at", insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class)
            private Date updatedAt;
        }
    }
}
