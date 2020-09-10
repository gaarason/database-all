package gaarason.database.test.models.base;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.connections.GaarasonDataSourceProvider;
import gaarason.database.eloquent.Model;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

@Slf4j
public class Single2Model<T, K> extends Model<T, K> {

    private static GaarasonDataSourceProvider gaarasonDataSourceProvider = proxyDataSource();

    @Override
    public GaarasonDataSourceProvider getGaarasonDataSource(){
        return gaarasonDataSourceProvider;
    }

    @Override
    public void log(String sql, Collection<String> parameterList) {
        String format = String.format(sql.replace(" ? ", "\"%s\""), parameterList.toArray());
        log.info("SQL complete         : {}", format);
    }

    private static DataSource dataSourceMaster0() {
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
        iniSql.add("SET SESSION SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION'");
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

    private static List<DataSource> dataSourceMasterList() {
        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(dataSourceMaster0());
        return dataSources;
    }

    private static GaarasonDataSourceProvider proxyDataSource() {
        List<DataSource> dataSources = dataSourceMasterList();
        return new GaarasonDataSourceProvider(dataSources);
    }
}
