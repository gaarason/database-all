package gaarason.database.contract.function;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.lang.Nullable;

import java.io.Serializable;

/**
 * 结果集过滤
 * @author xt
 */
@FunctionalInterface
public interface FilterRecordAttributeFunctionalInterface<T extends Serializable, K extends Serializable, V> {

    /**
     * 结果集过滤
     * @param tkRecord 结果集
     * @return 过滤后的结果
     */
    @Nullable
    V execute(Record<T, K> tkRecord);
}
