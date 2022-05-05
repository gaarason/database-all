package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.lang.Nullable;

import java.io.Serializable;
import java.util.Collection;

/**
 * 数据表
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface From<T extends Serializable, K extends Serializable> {

    /**
     * 更改查询的表名
     * @param sqlPart sql片段
     * @return 查询构造器
     */
    Builder<T, K> fromRaw(@Nullable String sqlPart);

    /**
     * 更改查询的表名
     * @param sqlPart sql片段
     * @param parameters 绑定的参数
     * @return 查询构造器
     */
    Builder<T, K> fromRaw(@Nullable String sqlPart, Collection<?> parameters);

    /**
     * 更改查询的表名
     * @param table 表名
     * @return 查询构造器
     */
    Builder<T, K> from(String table);

    /**
     * 临时表查询
     * @param alias   临时表别名
     * @param closure 闭包
     * @return 查询构造器
     */
    Builder<T, K> from(String alias, GenerateSqlPartFunctionalInterface<T, K> closure);

    /**
     * 临时表查询
     * @param alias 临时表别名
     * @param sql   完整查询语句
     * @return 查询构造器
     */
    Builder<T, K> from(String alias, String sql);
}
