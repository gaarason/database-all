package gaarason.database.contracts.function;

import gaarason.database.eloquent.RecordList;

@FunctionalInterface
public interface Chunk<T, K> {

    /**
     * 分块处理
     * @param records 结果集
     */
    boolean deal(RecordList<T, K> records);
}
