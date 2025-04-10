package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.BuilderAnyWrapper;
import gaarason.database.lang.Nullable;

import java.util.Collection;

/**
 * 数据表
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface From<B extends Builder<B, T, K>, T, K> {

    /**
     * 更改查询的表名
     * 用于非查询语句
     * @param sqlPart sql片段
     * @return 查询构造器
     */
    B tableRaw(@Nullable String sqlPart);

    /**
     * 更改查询的表名
     * 用于非查询语句
     * @param table 表名
     * @return 查询构造器
     */
    B table(String table);

    /**
     * 更改查询的表名
     * 仅查询语句, 非查询语句请使用 table()
     * @param sqlPart sql片段
     * @return 查询构造器
     */
    B fromRaw(@Nullable String sqlPart);

    /**
     * 更改查询的表名
     * 仅查询语句, 非查询语句请使用 table()
     * @param sqlPart sql片段
     * @param parameters 绑定的参数
     * @return 查询构造器
     */
    B fromRaw(@Nullable String sqlPart, Collection<?> parameters);

    /**
     * 更改查询的表名
     * @param table 表名
     * @return 查询构造器
     */
    B from(String table);

    /**
     * 更改查询的表名
     * @param anyEntity 任意实体对象
     * @return 查询构造器
     */
    B from(Object anyEntity);

    /**
     * 临时表查询
     * 仅查询语句, 非查询语句请使用 table()
     * @param alias 临时表别名
     * @param closure 闭包
     * @return 查询构造器
     */
    B from(String alias, BuilderAnyWrapper closure);

    /**
     * 临时表查询
     * 仅查询语句, 非查询语句请使用 table()
     * @param alias 临时表别名
     * @param sql 完整查询语句
     * @return 查询构造器
     */
    B from(String alias, String sql);
}
