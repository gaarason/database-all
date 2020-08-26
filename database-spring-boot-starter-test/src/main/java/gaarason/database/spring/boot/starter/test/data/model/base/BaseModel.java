package gaarason.database.spring.boot.starter.test.data.model.base;

import gaarason.database.contracts.GaarasonDataSource;
import gaarason.database.eloquent.Model;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Collection;

@Slf4j
abstract public class BaseModel<T, K> extends Model<T, K> {

    @Resource
    protected GaarasonDataSource gaarasonDataSource;

    @Override
    public GaarasonDataSource getDataSource() {
        return gaarasonDataSource;
    }

    /**
     * sql日志记录
     * @param sql           带占位符的sql
     * @param parameterList 参数
     */
    public void log(String sql, Collection<String> parameterList) {
        String format = String.format(sql.replace(" ? ", "\"%s\""), parameterList.toArray());
        log.debug("SQL complete : {}", format);
    }

}