package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.BuilderWrapper;
import gaarason.database.lang.Nullable;

import java.util.Collection;

/**
 * 需求字段
 * @param <T>
 * @param <K>
 * @author xt
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
     * @param sqlPart sql片段
     * @return 查询构造器
     */
    Builder<T, K> selectRaw(@Nullable String sqlPart);

    /**
     * 查询字段
     * @param sqlPart sql片段
     * @param parameters 绑定的参数
     * @return 查询构造器
     */
    Builder<T, K> selectRaw(@Nullable String sqlPart, @Nullable Collection<?> parameters);

    /**
     * 查询字段
     * @param column 列名数组
     * @return 查询构造器
     */
    Builder<T, K> select(String... column);

    /**
     * 查询字段
     * @param anyEntity 任意实体对象
     * @return 查询构造器
     */
    Builder<T, K> select(Object anyEntity);

    /**
     * 查询字段
     * @param anyEntityClass 任意实体类
     * @return 查询构造器
     */
    Builder<T, K> select(Class<?> anyEntityClass);

    /**
     * 查询字段
     * @param columnList 列名列表
     * @return 查询构造器
     */
    Builder<T, K> select(Collection<String> columnList);

    /**
     * 查询字段
     * @param function 数据库方法名
     * @param parameter 数据库方法参数
     * @param alias 字段别名
     * @return 查询构造器
     */
    Builder<T, K> selectFunction(String function, String parameter, @Nullable String alias);

    /**
     * 查询字段
     * @param function 数据库方法名
     * @param parameter 数据库方法参数
     * @return 查询构造器
     */
    Builder<T, K> selectFunction(String function, String parameter);

    /**
     * 调研数据库中的方法
     * @param function 数据库方法名
     * @param closure 返回代码片段
     * @param alias 字段别名
     * @return 查询构造器
     */
    Builder<T, K> selectFunction(String function, BuilderWrapper<T, K> closure, @Nullable String alias);

    /**
     * 调研数据库中的方法
     * @param function 数据库方法名
     * @param closure 返回代码片段
     * @return 查询构造器
     */
    Builder<T, K> selectFunction(String function, BuilderWrapper<T, K> closure);

    /**
     * 自定义查询字段
     * @param columnName 列名
     * @param value 值  // todo support Object type
     * @return 查询构造器
     */
    Builder<T, K> selectCustom(String columnName, String value);
}
