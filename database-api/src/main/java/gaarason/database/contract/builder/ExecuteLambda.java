package gaarason.database.contract.builder;

import gaarason.database.contract.function.ChunkFunctionalInterface;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.exception.SQLRuntimeException;

import java.io.Serializable;

/**
 * 执行
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface ExecuteLambda<T extends Serializable, K extends Serializable> extends Execute<T, K>, Support<T, K> {

    /**
     * 分块获取所有数据(数据库性能好), 并处理
     * @param num 单次获取的数据量
     * @param column 分页字段表达式 (字段要求: 数据库唯一约束(索引), 排序稳定 . eg: 单调递增主键)
     * @param chunkFunctionalInterface 对单次获取的数据量的处理
     * @throws SQLRuntimeException 数据库异常
     */
    default void dealChunk(int num, ColumnFunctionalInterface<T> column,
                           ChunkFunctionalInterface<T, K> chunkFunctionalInterface)
        throws SQLRuntimeException {
        dealChunk(num, lambda2ColumnName(column), chunkFunctionalInterface);
    }

}
