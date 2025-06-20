package gaarason.database.contract.function;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.lang.Nullable;

/**
 * 结果集转化
 * @author xt
 */
@FunctionalInterface
public interface RecordConversionFunctionalInterface<T, K, V> {

    /**
     * 结果集转化
     * @param tkRecord 结果集
     * @return 转化后的结果
     */
    @Nullable
    V execute(Record<T, K> tkRecord);
}
