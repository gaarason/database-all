package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.lang.Nullable;

import java.util.Collection;

/**
 * 值
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Value<B extends Builder<B, T, K>, T, K> {

    /**
     * 插入数据使用
     * @param values 值列表
     * @return 查询构造器
     */
    B value(@Nullable Collection<?> values);

    /**
     * 批量插入数据使用
     * @param valuesList 值列表的列表
     * @return 查询构造器
     */
    B valueList(Collection<? extends Collection<?>> valuesList);

}
