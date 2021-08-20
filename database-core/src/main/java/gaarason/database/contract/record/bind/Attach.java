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
     * @param stringStringMap 当BelongsToMany时生效, 将数据插入中间表
     * @return 受影响的行数
     */
    int attach(Record<?, ?> targetRecord, Map<String, String> stringStringMap);

    /**
     * 新增多个关系
     * @param targetRecords 目标records
     * @param stringStringMap 当BelongsToMany时生效, 将数据插入中间表
     * @return 受影响的行数
     */
    int attach(RecordList<?, ?> targetRecords, Map<String, String> stringStringMap);

    /**
     * 新增单个关系
     * @param id 目标record的主键
     * @return 受影响的行数
     */
    int attach(String id);

    /**
     * 新增多个关系
     * @param ids 目标records的主键集合
     * @return 受影响的行数
     */
    int attach(Collection<String> ids);

    /**
     * 新增单个关系
     * @param id 目标record的主键
     * @param stringStringMap 当BelongsToMany时生效, 将数据插入中间表
     * @return 受影响的行数
     */
    int attach(String id, Map<String, String> stringStringMap);

    /**
     * 新增多个关系
     * @param ids 目标records的主键集合
     * @param stringStringMap 当BelongsToMany时生效, 将数据插入中间表
     * @return 受影响的行数
     */
    int attach(Collection<String> ids, Map<String, String> stringStringMap);

}
