package gaarason.database.contract.function;

import gaarason.database.contract.eloquent.RecordList;

import java.io.Serializable;

/**
 * 分块处理
 * @author xt
 */
@FunctionalInterface
public interface ChunkFunctionalInterface<T extends Serializable, K extends Serializable> {

    /**
     * 分块处理
     * @param records 结果集
     * @return 是否成功
     */
    boolean execute(RecordList<T, K> records);
}
