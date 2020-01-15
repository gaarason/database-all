package gaarason.database.eloquent;

import gaarason.database.connections.ProxyDataSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class GeneralModel extends Model<GeneralModel.Table> {

    @Resource
    private ProxyDataSource proxyDataSource;

    @Override
    public ProxyDataSource getProxyDataSource() {
        return proxyDataSource;
    }

    public static class Table {

    }

}
