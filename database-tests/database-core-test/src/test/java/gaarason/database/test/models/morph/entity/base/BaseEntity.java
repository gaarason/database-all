package gaarason.database.test.models.morph.entity.base;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.connection.GaarasonDataSourceBuilder;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.eloquent.Model;
import gaarason.database.exception.base.BaseException;
import gaarason.database.test.config.MySqlBuilderV2;
import gaarason.database.test.utils.DatabaseTypeUtil;
import gaarason.database.util.StringUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

@Data
@Accessors(chain = true)
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** auto generator start **/


    @Primary()
    @Column(name = "id", unsigned = true)
    private Long id;


    /** auto generator end **/

    @Slf4j
    public abstract static class BaseModel<T extends BaseEntity, K> extends Model<MySqlBuilderV2<T, K>, T, K> {

        private static final GaarasonDataSource mysql = mysqlDataSource();

        private static final GaarasonDataSource mssql = mssqlDataSource();

        private static GaarasonDataSource mysqlDataSource() {
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
            GaarasonDataSource dataSource = GaarasonDataSourceBuilder.build(druidDataSource);
            dataSource.getContainer().signUpIdentification("mysql3");
            return dataSource;
        }

        private static GaarasonDataSource mssqlDataSource() {
            DruidDataSource druidDataSource = new DruidDataSource();
//        druidDataSource.setUrl(
//            "jdbc:mysql://mysql.local/test_master_0?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai");
//        druidDataSource.setDbType("com.alibaba.druid.pool.DruidDataSource");
//        druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
//        druidDataSource.setUsername("root");
//        druidDataSource.setPassword("root");
//        druidDataSource.setInitialSize(5);
//        druidDataSource.setMinIdle(5);
//        druidDataSource.setMaxActive(10);
//        druidDataSource.setMaxWait(60000);
//        druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
//        druidDataSource.setMinEvictableIdleTimeMillis(300000);
//        druidDataSource.setValidationQuery("SELECT 1");
//        List<String> iniSql = new ArrayList<>();
//        iniSql.add(
//            "SET SESSION SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION'");
//        druidDataSource.setConnectionInitSqls(iniSql);
//        druidDataSource.setTestOnBorrow(false);
//        druidDataSource.setTestOnReturn(false);
//        druidDataSource.setPoolPreparedStatements(false);
//        druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(-1);
//        Properties properties = new Properties();
//        properties.setProperty("druid.stat.mergeSql", "true");
//        properties.setProperty("druid.stat.slowSqlMillis", "5000");
//        druidDataSource.setConnectProperties(properties);
//        druidDataSource.setUseGlobalDataSourceStat(true);
            return GaarasonDataSourceBuilder.build(druidDataSource);
        }

        @Override
        public GaarasonDataSource getGaarasonDataSource() {
            switch (DatabaseTypeUtil.getDatabaseType()) {
                case MYSQL:
                    return mysql;
                case MSSQL:
                    return mssql;
                default:
                    throw new BaseException();
            }
        }

        @Override
        public void log(String sql, Collection<?> parameterList) {
//        String format = String.format(sql.replace(" ? ", "\"%s\""), parameterList.toArray());
            String format = StringUtils.toSql(sql, parameterList);
            log.info("SQL complete         : {}", format);
        }
    }
}