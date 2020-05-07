package gaarason.database.contracts.function;

import gaarason.database.eloquent.Record;

@FunctionalInterface
public interface FilterRecordAttribute<T, K> {

    Object filter(Record<T, K> record);
}
