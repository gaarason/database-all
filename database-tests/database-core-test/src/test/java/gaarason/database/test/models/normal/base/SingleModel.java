package gaarason.database.test.models.normal.base;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.connection.GaarasonDataSourceBuilder;
import gaarason.database.connection.GaarasonDataSourceWrapper;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.eloquent.Model;
import gaarason.database.exception.base.BaseException;
import gaarason.database.test.utils.DatabaseTypeUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

@Slf4j
public class SingleModel<T extends Serializable, K extends Serializable> extends Model<T, K> {

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
        return GaarasonDataSourceBuilder.build(druidDataSource);
    }

    private static GaarasonDataSource mssqlDataSource() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl("jdbc:sqlserver://mssql.local:1433;DatabaseName=test_master_0");
        druidDataSource.setDbType("com.alibaba.druid.pool.DruidDataSource");

        druidDataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        druidDataSource.setUsername("sa");
        druidDataSource.setPassword("qwer@123");
        druidDataSource.setInitialSize(5);
        druidDataSource.setMinIdle(5);
        druidDataSource.setMaxActive(10);
        druidDataSource.setMaxWait(60000);
        druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
        druidDataSource.setMinEvictableIdleTimeMillis(300000);
        druidDataSource.setValidationQuery("SELECT 1");
        List<String> iniSql = new ArrayList<>();
//        iniSql.add(
//            "SET SESSION SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION'");
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
        String format = String.format(sql.replace("?", "\"%s\""), parameterList.toArray());
        log.info("SQL complete         : {}", format);
    }

    /**
     * 事件会当一个新模型被首次保存的时候触发
     * @return 继续操作
     */
    @Override
    public boolean creating(Record<T, K> record) {
        return true;
    }
}
