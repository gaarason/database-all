package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.ChunkFunctionalInterface;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.exception.SQLRuntimeException;

/**
 * 执行
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface ExecuteLambda<B extends Builder<B, T, K>, T, K> extends Execute<B, T, K>, Support<B, T, K> {

    /**
     * 分块获取所有数据(数据库性能好), 并处理
     * @param num 单次获取的数据量
     * @param column 分页字段表达式 (字段要求: 数据库唯一约束(索引), 排序稳定 . eg: 单调递增主键)
     * @param chunkFunctionalInterface 对单次获取的数据量的处理
     * @param <F> 属性类型
     * @throws SQLRuntimeException 数据库异常
     */
    default <F> void dealChunk(int num, ColumnFunctionalInterface<T, F> column,
        ChunkFunctionalInterface<T, K> chunkFunctionalInterface)
        throws SQLRuntimeException {
        dealChunk(num, lambda2ColumnName(column), chunkFunctionalInterface);
    }

}
