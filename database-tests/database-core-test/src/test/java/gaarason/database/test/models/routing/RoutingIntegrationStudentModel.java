package gaarason.database.test.models.routing;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import gaarason.database.contract.support.FieldStrategy;
import gaarason.database.bootstrap.ContainerBootstrap;
import gaarason.database.connection.GaarasonRoutingDataSourceBuilder;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.eloquent.Model;
import gaarason.database.test.config.MySqlBuilderV2;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * 路由集成测试专用 Model：使用多组 {@link gaarason.database.connection.GaarasonRoutingDataSourceBuilder} 数据源.
 *
 * @author xt
 */
public class RoutingIntegrationStudentModel extends
    Model<MySqlBuilderV2<RoutingIntegrationStudentModel.Entity, Integer>, RoutingIntegrationStudentModel.Entity, Integer> {

    private static final GaarasonDataSource ROUTING_DATA_SOURCE = buildRoutingDataSource();

    private static GaarasonDataSource buildRoutingDataSource() {
        DruidDataSource masterDs = newMysqlDataSource("test_master_0");
        DruidDataSource orderDs = newMysqlDataSource("gaarason_routing_b");
        ContainerBootstrap container = ContainerBootstrap.build().autoBootstrap();
        container.signUpIdentification("routing-integration-mysql");
        return GaarasonRoutingDataSourceBuilder.create()
            .defaultGroup("master")
            .group("master", Collections.singletonList(masterDs))
            .group("order", Collections.singletonList(orderDs))
            .build(container);
    }

    private static DruidDataSource newMysqlDataSource(String database) {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(
            "jdbc:mysql://mysql.local/" + database
                + "?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false"
                + "&autoReconnect=true&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true");
        druidDataSource.setDbType("com.alibaba.druid.pool.DruidDataSource");
        druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("root");
        druidDataSource.setInitialSize(2);
        druidDataSource.setMinIdle(2);
        druidDataSource.setMaxActive(8);
        druidDataSource.setMaxWait(60000);
        druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
        druidDataSource.setMinEvictableIdleTimeMillis(300000);
        druidDataSource.setValidationQuery("SELECT 1");
        List<String> iniSql = Collections.singletonList(
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
        return druidDataSource;
    }

    @Override
    protected boolean softDeleting() {
        return false;
    }

    @Override
    public GaarasonDataSource getGaarasonDataSource() {
        return ROUTING_DATA_SOURCE;
    }

    @Data
    @Table(name = "student")
    public static class Entity implements Serializable {

        private static final long serialVersionUID = 1L;

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

        private Boolean isDeleted;
    }
}
