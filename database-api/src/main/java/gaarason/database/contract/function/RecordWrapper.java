package gaarason.database.contract.function;

import gaarason.database.contract.eloquent.Record;

import java.io.Serializable;

/**
 * 查询结果集包装
 * Record关联关系
 * @author xt
 */
@FunctionalInterface
public interface RecordWrapper extends Serializable {

    /**
     * 通用空实现
     */
    RecordWrapper EMPTY = theRecord -> theRecord;

    /**
     * Record关联关系
     * @param theRecord Record 查询结果集
     * @return Record 查询结果集
     */
    Record<?, ?> execute(Record<?, ?> theRecord);

    /**
     * 通用空实现
     * @return 查询结果集包装
     */
    static RecordWrapper empty() {
        return  EMPTY;
    }
}
