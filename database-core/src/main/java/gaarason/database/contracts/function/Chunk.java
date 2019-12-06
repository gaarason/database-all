package gaarason.database.contracts.function;

import gaarason.database.eloquent.RecordList;

@FunctionalInterface
public interface Chunk<V> {

    /**
     * 分块处理
     * @param records 结果集
     */
    boolean deal(RecordList<V> records);
}
