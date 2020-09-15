package gaarason.database.contract.function;

import gaarason.database.contract.eloquent.Record;

@FunctionalInterface
public interface FilterRecordAttributeFunctionalInterface<T, K, V> {

    V execute(Record<T, K> record);
}
