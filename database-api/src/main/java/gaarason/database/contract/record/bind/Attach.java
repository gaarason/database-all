package gaarason.database.contract.record.bind;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;

import java.util.Collection;
import java.util.Map;

/**
 * 新增关系
 * @author xt
 */
public interface Attach {

    /**
     * 新增单个关系
     * @param targetRecord 目标record
     * @return 受影响的行数
     */
    int attach(Record<?, ?> targetRecord);

    /**
     * 新增多个关系
     * @param targetRecords 目标records
     * @return 受影响的行数
     */
    int attach(RecordList<?, ?> targetRecords);

    /**
     * 新增单个关系
     * @param targetRecord 目标record
     * @param relationDataMap 当BelongsToMany时生效, 将数据插入中间表
     * @return 受影响的行数
     */
    int attach(Record<?, ?> targetRecord, Map<String, Object> relationDataMap);

    /**
     * 新增多个关系
     * @param targetRecords 目标records
     * @param relationDataMap 当BelongsToMany时生效, 将数据插入中间表
     * @return 受影响的行数
     */
    int attach(RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap);

    /**
     * 新增单个关系
     * @param id 目标record的主键
     * @return 受影响的行数
     */
    int attach(Object id);

    /**
     * 新增多个关系
     * @param ids 目标records的主键集合
     * @return 受影响的行数
     */
    int attach(Collection<Object> ids);

    /**
     * 新增单个关系
     * @param id 目标record的主键
     * @param relationDataMap 当BelongsToMany时生效, 将数据插入中间表
     * @return 受影响的行数
     */
    int attach(Object id, Map<String, Object> relationDataMap);

    /**
     * 新增多个关系
     * @param ids 目标records的主键集合
     * @param relationDataMap 当BelongsToMany时生效, 将数据插入中间表
     * @return 受影响的行数
     */
    int attach(Collection<Object> ids, Map<String, Object> relationDataMap);

}
