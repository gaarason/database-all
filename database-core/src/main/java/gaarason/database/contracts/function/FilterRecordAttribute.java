package gaarason.database.contracts.function;

import gaarason.database.eloquent.Record;

@FunctionalInterface
public interface FilterRecordAttribute<V> {

    Object filter(Record<V> record);
}
