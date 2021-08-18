package gaarason.database.contract.function;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.core.lang.Nullable;

/**
 * 结果集过滤
 * @author xt
 */
@FunctionalInterface
public interface FilterRecordAttributeFunctionalInterface<T, K, V> {

    /**
     * 结果集过滤
     * @param tkRecord 结果集
     * @return 过滤后的结果
     */
    @Nullable
    V execute(Record<T, K> tkRecord);
}
