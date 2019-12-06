package gaarason.database.test.models.base;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.connections.ProxyDataSource;
import gaarason.database.eloquent.Model;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class MasterSlaveModel<T> extends Model<T> {

    @Override
    public ProxyDataSource getProxyDataSource(){
        return new ProxyDataSource(dataSourceMasterList());
    }

    private DataSource dataSourceMaster0() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(
            "jdbc:mysql://sakya.local/test_master_0?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai");
        druidDataSource.setDbType("com.alibaba.druid.pool.DruidDataSource");
        druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("root");
        return druidDataSource;
    }

    private List<DataSource> dataSourceMasterList() {
        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(dataSourceMaster0());
        return dataSources;
    }

    private ProxyDataSource proxyDataSource() {
        List<DataSource> dataSources = dataSourceMasterList();
        return new ProxyDataSource(dataSources);
    }
}
