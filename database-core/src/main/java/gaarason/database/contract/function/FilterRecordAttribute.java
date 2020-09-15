package gaarason.database.contract.function;

import gaarason.database.eloquent.Record;

@FunctionalInterface
public interface FilterRecordAttribute<T, K, V> {

     V filter(Record<T, K> record);
}
