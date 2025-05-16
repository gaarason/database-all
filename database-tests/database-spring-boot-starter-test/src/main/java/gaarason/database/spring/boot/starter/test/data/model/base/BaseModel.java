package gaarason.database.spring.boot.starter.test.data.model.base;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.eloquent.Model;
import gaarason.database.query.MySqlBuilder;
import gaarason.database.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Collection;

/**
 * 数据模型
 * @param <T>
 * @param <K>
 * @author xt
 */
@Slf4j
public abstract class BaseModel<T extends Serializable, K extends Serializable> extends Model<MySqlBuilder<T, K>, T, K> {

    @Lazy
    @Resource
    protected GaarasonDataSource gaarasonDataSource;

    @Override
    public GaarasonDataSource getGaarasonDataSource() {
        return gaarasonDataSource;
    }

    /**
     * sql日志记录
     * @param sql 带占位符的sql
     * @param parameterList 参数
     */
    @Override
    public void log(String sql, Collection<?> parameterList) {
        String format = String.format(StringUtils.replace(sql, " ? ", "\"%s\""), parameterList.toArray());
        log.info("SQL complete : {}", format);
    }

}