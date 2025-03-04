package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.BuilderWrapper;
import gaarason.database.lang.Nullable;

/**
 * 排序
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Order<B extends Builder<B, T, K>, T, K> {

    /**
     * 排序
     * @param column 列名, 为null则忽略
     * @param orderByType 正序|倒序
     * @return 查询构造器
     */
    B orderBy(@Nullable String column, gaarason.database.appointment.OrderBy orderByType);

    /**
     * 排序
     * @param column 列名, 为null则忽略
     * @return 查询构造器
     */
    B orderBy(@Nullable String column);

    /**
     * 排序
     * @param sqlPart sql片段, 为null则忽略
     * @return 查询构造器
     */
    B orderByRaw(@Nullable String sqlPart);

    /**
     * 将排序片段增加到首位
     * @param closure 闭包
     * @return 查询构造器
     */
    B firstOrderBy(BuilderWrapper<B, T, K> closure);

}
