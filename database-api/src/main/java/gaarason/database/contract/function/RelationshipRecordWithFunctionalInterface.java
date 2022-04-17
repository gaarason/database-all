package gaarason.database.contract.function;

import gaarason.database.contract.eloquent.Record;

import java.io.Serializable;

/**
 * Record关联关系
 * @author xt
 */
@FunctionalInterface
public interface RelationshipRecordWithFunctionalInterface extends Serializable {

    /**
     * Record关联关系
     * @param theRecord Record 查询结果集
     * @return Record 查询结果集
     */
    Record<?, ?> execute(Record<?, ?> theRecord);
}
