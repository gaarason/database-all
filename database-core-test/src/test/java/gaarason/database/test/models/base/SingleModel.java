package gaarason.database.test.models.base;

import gaarason.database.connections.ProxyDataSource;
import gaarason.database.eloquent.Model;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Collection;

@Slf4j
public class SingleModel<T> extends Model<T> {

    @Resource(name = "proxyDataSourceSingle")
    protected ProxyDataSource dataSource;

    public ProxyDataSource getProxyDataSource(){
        return dataSource;
    }

    @Override
    public void log(String sql, Collection<String> parameterList) {
        String format = String.format(sql.replace(" ? ", "\"%s\""), parameterList.toArray());
        log.info("SQL complete         : {}", format);
    }

}
