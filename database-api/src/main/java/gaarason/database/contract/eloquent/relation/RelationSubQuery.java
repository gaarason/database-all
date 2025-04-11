package gaarason.database.contract.eloquent.relation;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.BuilderAnyWrapper;
import gaarason.database.core.Container;
import gaarason.database.exception.OperationNotSupportedException;
import gaarason.database.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 关联关系字段处理, 目前是 @HasOnePrMany @BelongsTo @BelongsToMany 所属范围
 * @author xt
 */
public interface RelationSubQuery {

    /**
     * 容器
     * @return 容器
     */
    Container getContainer();

    /**
     * 空的查询结果集
     * @return 空的查询结果集
     */
    RecordList<?, ?> emptyRecordList();

    /**
     * 中间表 query builder
     * @param metadata 当前recordList的元数据
     * @return 中间表查询构造器
     */
    @Nullable
    default Builder<?, ?, ?> prepareRelationBuilder(List<Map<String, Object>> metadata) {
        return null;
    }

    /**
     * 批量关联查询 (中间表)
     * @param relationBuilder 中间表查询构造器
     * @return 查询结果集
     */
    RecordList<?, ?> dealBatchForRelation(@Nullable Builder<?, ?, ?> relationBuilder);

    /**
     * 目标表 query builder
     * @param metadata 当前recordList的元数据
     * @param relationRecordList 中间表数据
     * @param operationBuilder 操作构造器包装
     * @param customBuilder 查询构造器包装
     * @return 目标表查询构造器
     */
    @Nullable
    Builder<?, ?, ?> prepareTargetBuilder(List<Map<String, Object>> metadata,
        RecordList<?, ?> relationRecordList, BuilderAnyWrapper operationBuilder, BuilderAnyWrapper customBuilder);

    /**
     * 目标表 query builder
     * @param metadata 当前recordList的元数据
     * @param relationRecordList 中间表数据
     * @param operationBuilder 操作构造器包装
     * @param customBuilder 查询构造器包装
     * @return 目标表查询构造器
     */
    @Nullable
    default Builder<?, ?, ?> prepareTargetBuilderByRelationOperation(List<Map<String, Object>> metadata,
        RecordList<?, ?> relationRecordList, BuilderAnyWrapper operationBuilder,
        BuilderAnyWrapper customBuilder) {
        throw new OperationNotSupportedException();
    }

    /**
     * 批量关联查询 (目标表)
     * @param targetBuilder 目标表查询构造器
     * @param relationRecordList @BelongsToMany 中间表数据
     * @return 查询结果集
     */
    RecordList<?, ?> dealBatchForTarget(@Nullable Builder<?, ?, ?> targetBuilder,
        RecordList<?, ?> relationRecordList);

    /**
     * 批量关联查询 (目标表)
     * @param targetBuilder 目标表查询构造器
     * @param relationRecordList @BelongsToMany 中间表数据
     * @return 查询结果集
     */
    default RecordList<?, ?> dealBatchForTargetByRelationOperation(@Nullable Builder<?, ?, ?> targetBuilder,
        RecordList<?, ?> relationRecordList) {
        throw new OperationNotSupportedException();
    }

    /**
     * 筛选批量关联查询结果
     * 针对关联关系操作
     * @param theRecord 当前record
     * @param targetRecordList 目标的recordList
     * @return 关联查询操作的结果
     */
    default Map<String, Object> filterBatchRecordByRelationOperation(Record<?, ?> theRecord,
        RecordList<?, ?> targetRecordList) {
        throw new OperationNotSupportedException();
    }

    /**
     * 筛选批量关联查询结果对象
     * @param theRecord 当前record
     * @param targetRecordList 目标的recordList
     * @return 筛选后的查询结果集
     */
    List<Object> filterBatchRecord(Record<?, ?> theRecord, RecordList<?, ?> targetRecordList, List<?> targetObjectList);

    /**
     * 实现 whereHas
     * @param builder 当前查询构造器
     * @param customBuilder 查询构造器包装
     * @return 查询构造器
     */
    Builder<?, ?, ?> prepareForWhereHas(Builder<?, ?, ?> builder, BuilderAnyWrapper customBuilder);

    /**
     * whereHasIn的本地键
     * @return whereHasIn的本地键
     * @see #prepareForWhereHasIn(Builder, BuilderAnyWrapper)
     */
    String localKeyForWhereHasIn();

    /**
     * 实现 whereHasIn
     * @param builder 当前查询构造器
     * @param customBuilder 查询构造器包装
     * @return 查询构造器
     * @see #localKeyForWhereHasIn()
     */
    Builder<?, ?, ?> prepareForWhereHasIn(Builder<?, ?, ?> builder, BuilderAnyWrapper customBuilder);

    /**
     * 增加关联关系
     * @param theRecord 当前record
     * @param targetRecords 目标的recordList
     * @param relationDataMap 仅 @BelongsToMany 时有效, 增加额外信息到中间表
     * @return 受影响的行数
     */
    int attach(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap);

    /**
     * 增加关联关系
     * @param theRecord 当前record
     * @param targetPrimaryKeyValues 目标的recordList的主键集合
     * @param relationDataMap 仅 @BelongsToMany 时有效, 增加额外信息到中间表
     * @return 受影响的行数
     */
    int attach(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues, Map<String, Object> relationDataMap);

    /**
     * 解除所有关联关系
     * @param theRecord 当前record
     * @return 受影响的行数
     */
    int detach(Record<?, ?> theRecord);

    /**
     * 解除目标关联关系
     * @param theRecord 当前record
     * @param targetRecords 目标的recordList
     * @return 受影响的行数
     */
    int detach(Record<?, ?> theRecord, RecordList<?, ?> targetRecords);

    /**
     * 解除目标关联关系
     * @param theRecord 当前record
     * @param targetPrimaryKeyValues 目标的recordList的主键集合
     * @return 受影响的行数
     */
    int detach(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues);

    /**
     * 同步到关联关系, 任何不在指定范围的对应记录将会移除
     * @param theRecord 当前record
     * @param targetRecords 目标的recordList
     * @param relationDataMap 仅 @BelongsToMany 时有效, 增加额外信息到中间表
     * @return 受影响的行数
     */
    int sync(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap);

    /**
     * 同步到关联关系, 任何不在指定范围的对应记录将会移除
     * @param theRecord 当前record
     * @param targetPrimaryKeyValues 目标的recordList的主键集合
     * @param relationDataMap 仅 @BelongsToMany 时有效, 增加额外信息到中间表
     * @return 受影响的行数
     */
    int sync(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues, Map<String, Object> relationDataMap);

    /**
     * 切换关系, 如果指定关系已存在，则解除，如果指定关系不存在，则增加
     * @param theRecord 当前record
     * @param targetRecords 目标的recordList
     * @param relationDataMap 仅 @BelongsToMany 时有效, 增加额外信息到中间表
     * @return 受影响的行数
     */
    int toggle(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap);

    /**
     * 切换关系, 如果指定关系已存在，则解除，如果指定关系不存在，则增加
     * @param theRecord 当前record
     * @param targetPrimaryKeyValues 目标的recordList的主键集合
     * @param relationDataMap 仅 @BelongsToMany 时有效, 增加额外信息到中间表
     * @return 受影响的行数
     */
    int toggle(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues, Map<String, Object> relationDataMap);

}
