package gaarason.database.contracts.builder;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.core.lang.Nullable;
import gaarason.database.query.Builder;

import java.util.List;

/**
 * 需求字段
 * @param <T>
 * @param <K>
 */
public interface Select<T, K> {

    /**
     * 查询字段
     * @param column 列名
     * @return 查询构造器
     */
    Builder<T, K> select(String column);

    /**
     * 查询字段
     * @param column 列名数组
     * @return 查询构造器
     */
    Builder<T, K> select(String... column);

    /**
     * 查询字段
     * @param columnList 列名列表
     * @return 查询构造器
     */
    Builder<T, K> select(List<String> columnList);

    /**
     * 查询字段
     * @param function 数据库方法名
     * @param parameter 数据库方法参数
     * @param alias 字段别名
     * @return 查询构造器
     */
    Builder<T, K> selectFunction(String function, String parameter, @Nullable String alias);

    /**
     *
     * @param function 数据库方法名
     * @param closure 返回代码片段
     * @param alias 字段别名
     * @return 查询构造器
     */
    Builder<T, K> selectFunction(String function, GenerateSqlPart<T, K> closure, @Nullable String alias);

}
