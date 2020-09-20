package gaarason.database.contract.record.bind;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;

import java.util.Collection;
import java.util.Map;

/**
 * 切换关系, 如果指定关系已存在，则解除，如果指定关系不存在，则增加
 */
public interface Toggle {

    /**
     * 切换关系, 如果指定关系已存在，则解除，如果指定关系不存在，则增加
     * @param targetRecord 目标record
     * @return 受影响的行数
     */
    int toggle(Record<?, ?> targetRecord);

    /**
     * 切换关系, 如果指定关系已存在，则解除，如果指定关系不存在，则增加
     * @param targetRecords 目标records
     * @return 受影响的行数
     */
    int toggle(RecordList<?, ?> targetRecords);

    /**
     * 切换关系, 如果指定关系已存在，则解除，如果指定关系不存在，则增加
     * @param targetRecord 目标record
     * @param stringStringMap 当BelongsToMany时生效, 将数据插入中间表
     * @return 受影响的行数
     */
    int toggle(Record<?, ?> targetRecord, Map<String, String> stringStringMap);

    /**
     * 切换关系, 如果指定关系已存在，则解除，如果指定关系不存在，则增加
     * @param targetRecords 目标records
     * @param stringStringMap 当BelongsToMany时生效, 将数据插入中间表
     * @return 受影响的行数
     */
    int toggle(RecordList<?, ?> targetRecords, Map<String, String> stringStringMap);

    /**
     * 切换关系, 如果指定关系已存在，则解除，如果指定关系不存在，则增加
     * @param id 目标record的主键
     * @return 受影响的行数
     */
    int toggle(String id);

    /**
     * 切换关系, 如果指定关系已存在，则解除，如果指定关系不存在，则增加
     * @param ids 目标records的主键集合
     * @return 受影响的行数
     */
    int toggle(Collection<String> ids);

    /**
     * 切换关系, 如果指定关系已存在，则解除，如果指定关系不存在，则增加
     * @param id 目标record的主键
     * @param stringStringMap 当BelongsToMany时生效, 将数据插入中间表
     * @return 受影响的行数
     */
    int toggle(String id, Map<String, String> stringStringMap);

    /**
     * 切换关系, 如果指定关系已存在，则解除，如果指定关系不存在，则增加
     * @param ids 目标records的主键集合
     * @param stringStringMap 当BelongsToMany时生效, 将数据插入中间表
     * @return 受影响的行数
     */
    int toggle(Collection<String> ids, Map<String, String> stringStringMap);
}
