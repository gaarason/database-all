package gaarason.database.contract.function;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.core.lang.Nullable;

@FunctionalInterface
public interface FilterRecordAttributeFunctionalInterface<T, K, V> {

    @Nullable
    V execute(Record<T, K> record);
}
