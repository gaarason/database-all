package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.core.lang.Nullable;

import java.io.Serializable;
import java.util.Collection;

/**
 * 需求字段
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Select<T extends Serializable, K extends Serializable> {

    /**
     * 查询字段
     * @param column 列名
     * @return 查询构造器
     */
    Builder<T, K> select(String column);

    /**
     * 查询字段
     * @param column 列名
     * @return 查询构造器
     */
    Builder<T, K> selectRaw(String column);

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
    Builder<T, K> select(Collection<String> columnList);

    /**
     * 查询字段
     * @param function  数据库方法名
     * @param parameter 数据库方法参数
     * @param alias     字段别名
     * @return 查询构造器
     */
    Builder<T, K> selectFunction(String function, String parameter, @Nullable String alias);

    /**
     * @param function 数据库方法名
     * @param closure  返回代码片段
     * @param alias    字段别名
     * @return 查询构造器
     */
    Builder<T, K> selectFunction(String function, GenerateSqlPartFunctionalInterface closure, @Nullable String alias);

}
