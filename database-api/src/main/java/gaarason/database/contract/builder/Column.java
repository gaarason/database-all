package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.lang.Nullable;

import java.util.Collection;

/**
 * 列名
 * 仅用于 insert replace 两种语句
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Column<B extends Builder<B, T, K>, T, K> {

    /**
     * 新增字段
     * @param column 列名
     * @return 查询构造器
     */
    B column(String column);

    /**
     * 新增字段
     * @param sqlPart sql片段
     * @return 查询构造器
     */
    B columnRaw(@Nullable String sqlPart);

    /**
     * 新增字段
     * @param sqlPart sql片段
     * @param parameters 绑定的参数
     * @return 查询构造器
     */
    B columnRaw(@Nullable String sqlPart, @Nullable Collection<?> parameters);

    /**
     * 新增字段
     * @param column 列名数组
     * @return 查询构造器
     */
    B column(String... column);

    /**
     * 新增字段
     * @param columnList 列名列表
     * @return 查询构造器
     */
    B column(Collection<String> columnList);

}
