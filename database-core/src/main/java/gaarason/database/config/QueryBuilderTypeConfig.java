package gaarason.database.config;

import java.util.List;

/**
 * 查询构造
 * @author xt
 * @since 2021/11/30 5:35 下午
 */
public interface QueryBuilderTypeConfig {

    /**
     * 获取支持的数据库类型列表
     * @return 支持的数据库类型列表
     */
    List<Class<? extends QueryBuilderConfig>> getAllDatabaseTypes();
}
