package gaarason.database.eloquent;

import gaarason.database.connections.ProxyDataSource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class GeneralModel extends Model<GeneralModel.Table> {

    @Resource
    private ProxyDataSource proxyDataSource;

    @Override
    public ProxyDataSource getProxyDataSource() {
        return proxyDataSource;
    }

    public static class Table{

    }

}
