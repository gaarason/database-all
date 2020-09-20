package gaarason.database.contract.eloquent.relation;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.support.Column;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 关联关系字段处理, 目前是 @HasOnePrMany @BelongsTo @BelongsToMany 所属范围
 */
public interface RelationSubQuery {

    /**
     * 批量关联查询的sql预生成, 可以作为缓存key
     * @param stringColumnMapList 当前recordList的元数据
     * @param generateSqlPart     Builder(目标表可用)
     * @return sql数组 [0 -> 目标表操作, 1 -> 中间表操作]
     */
    String[] prepareSqlArr(List<Map<String, Column>> stringColumnMapList,
                           GenerateSqlPartFunctionalInterface generateSqlPart);

    /**
     * 批量关联查询
     * @param sql1 sql
     * @return 查询结果集
     */
    RecordList<?, ?> dealBatchPrepare(String sql1);

    /**
     * 批量关联查询
     * @param sql0               sql
     * @param relationRecordList @BelongsToMany 中间表数据
     * @return 查询结果集
     */
    RecordList<?, ?> dealBatch(String sql0, RecordList<?, ?> relationRecordList);

    /**
     * 筛选批量关联查询结果对象
     * @param record                  当前record
     * @param TargetRecordList        目标的recordList
     * @param cacheRelationRecordList 结果缓存
     * @return 筛选后的查询结果集
     */
    List<?> filterBatchRecord(Record<?, ?> record, RecordList<?, ?> TargetRecordList,
                              Map<String, RecordList<?, ?>> cacheRelationRecordList);

    /**
     * 增加关联关系
     * @param record          当前record
     * @param targetRecords   目标的recordList
     * @param stringStringMap 仅 @BelongsToMany 时有效, 增加额外信息到中间表
     * @return 受影响的行数
     */
    int attach(Record<?, ?> record, RecordList<?, ?> targetRecords, Map<String, String> stringStringMap);

    /**
     * 增加关联关系
     * @param record                 当前record
     * @param targetPrimaryKeyValues 目标的recordList的主键集合
     * @param stringStringMap        仅 @BelongsToMany 时有效, 增加额外信息到中间表
     * @return 受影响的行数
     */
    int attach(Record<?, ?> record, Collection<String> targetPrimaryKeyValues, Map<String, String> stringStringMap);

    /**
     * 解除所有关联关系
     * @param record 当前record
     * @return 受影响的行数
     */
    int detach(Record<?, ?> record);

    /**
     * 解除目标关联关系
     * @param record        当前record
     * @param targetRecords 目标的recordList
     * @return 受影响的行数
     */
    int detach(Record<?, ?> record, RecordList<?, ?> targetRecords);

    /**
     * 解除目标关联关系
     * @param record                 当前record
     * @param targetPrimaryKeyValues 目标的recordList的主键集合
     * @return 受影响的行数
     */
    int detach(Record<?, ?> record, Collection<String> targetPrimaryKeyValues);

    /**
     * 同步到关联关系, 任何不在指定范围的对应记录将会移除
     * @param record          当前record
     * @param targetRecords   目标的recordList
     * @param stringStringMap 仅 @BelongsToMany 时有效, 增加额外信息到中间表
     * @return 受影响的行数
     */
    int sync(Record<?, ?> record, RecordList<?, ?> targetRecords, Map<String, String> stringStringMap);

    /**
     * 同步到关联关系, 任何不在指定范围的对应记录将会移除
     * @param record                 当前record
     * @param targetPrimaryKeyValues 目标的recordList的主键集合
     * @param stringStringMap        仅 @BelongsToMany 时有效, 增加额外信息到中间表
     * @return 受影响的行数
     */
    int sync(Record<?, ?> record, Collection<String> targetPrimaryKeyValues, Map<String, String> stringStringMap);

    /**
     * 切换关系, 如果指定关系已存在，则解除，如果指定关系不存在，则增加
     * @param record          当前record
     * @param targetRecords   目标的recordList
     * @param stringStringMap 仅 @BelongsToMany 时有效, 增加额外信息到中间表
     * @return 受影响的行数
     */
    int toggle(Record<?, ?> record, RecordList<?, ?> targetRecords, Map<String, String> stringStringMap);

    /**
     * 切换关系, 如果指定关系已存在，则解除，如果指定关系不存在，则增加
     * @param record                 当前record
     * @param targetPrimaryKeyValues 目标的recordList的主键集合
     * @param stringStringMap        仅 @BelongsToMany 时有效, 增加额外信息到中间表
     * @return 受影响的行数
     */
    int toggle(Record<?, ?> record, Collection<String> targetPrimaryKeyValues, Map<String, String> stringStringMap);

}
