package gaarason.database.contract.record.bind;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;

import java.util.Collection;
import java.util.Map;

/**
 * 同步到关联关系, 任何不在指定范围的对应记录将会移除
 * @author xt
 */
public interface Sync {

    /**
     * 同步到关联关系, 任何不在指定范围的对应记录将会移除
     * @param targetRecord 目标record
     * @return 受影响的行数
     */
    int sync(Record<?, ?> targetRecord);

    /**
     * 同步到关联关系, 任何不在指定范围的对应记录将会移除
     * @param targetRecords 目标records
     * @return 受影响的行数
     */
    int sync(RecordList<?, ?> targetRecords);

    /**
     * 同步到关联关系, 任何不在指定范围的对应记录将会移除
     * @param targetRecord 目标record
     * @param relationDataMap 当BelongsToMany时生效, 将数据插入中间表
     * @return 受影响的行数
     */
    int sync(Record<?, ?> targetRecord, Map<String, Object> relationDataMap);

    /**
     * 同步到关联关系, 任何不在指定范围的对应记录将会移除
     * @param targetRecords 目标records
     * @param relationDataMap 当BelongsToMany时生效, 将数据插入中间表
     * @return 受影响的行数
     */
    int sync(RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap);

    /**
     * 同步到关联关系, 任何不在指定范围的对应记录将会移除
     * @param id 目标record的主键
     * @return 受影响的行数
     */
    int sync(Object id);

    /**
     * 同步到关联关系, 任何不在指定范围的对应记录将会移除
     * @param ids 目标records的主键集合
     * @return 受影响的行数
     */
    int sync(Collection<Object> ids);

    /**
     * 同步到关联关系, 任何不在指定范围的对应记录将会移除
     * @param id 目标record的主键
     * @param relationDataMap 当BelongsToMany时生效, 将数据插入中间表
     * @return 受影响的行数
     */
    int sync(Object id, Map<String, Object> relationDataMap);

    /**
     * 同步到关联关系, 任何不在指定范围的对应记录将会移除
     * @param ids 目标records的主键集合
     * @param relationDataMap 当BelongsToMany时生效, 将数据插入中间表
     * @return 受影响的行数
     */
    int sync(Collection<Object> ids, Map<String, Object> relationDataMap);
}
