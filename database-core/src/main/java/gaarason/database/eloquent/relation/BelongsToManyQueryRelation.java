package gaarason.database.eloquent.relation;

import gaarason.database.annotation.BelongsToMany;
import gaarason.database.appointment.JoinType;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.BuilderWrapper;
import gaarason.database.contract.query.Grammar;
import gaarason.database.core.Container;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.EntityMember;
import gaarason.database.support.FieldMember;
import gaarason.database.util.ObjectUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 关联关系 多对多
 * @author xt
 */
public class BelongsToManyQueryRelation extends BaseRelationSubQuery {

    private final BelongsToManyTemplate belongsToManyTemplate;

    /**
     * 人造关系键
     */
    private static final String RELATION_KEY = "@R@";

    /**
     * 是否多态 - 本表
     */
    private boolean enableLocalModelMorph;

    /**
     * 是否多态 - 目标表
     */
    private boolean enableTargetModelMorph;

    public BelongsToManyQueryRelation(Field field, ModelShadowProvider modelShadowProvider, Model<?, ?> model) {
        super(modelShadowProvider, model);
        belongsToManyTemplate = new BelongsToManyTemplate(field);
    }

    @Override
    @Nullable
    public Builder<?, ?> prepareRelationBuilder(List<Map<String, Object>> metadata) {
        return belongsToManyTemplate.relationModel.newQuery()
            .select(belongsToManyTemplate.foreignKeyForLocalModel, belongsToManyTemplate.foreignKeyForTargetModel)
            .whereIn(belongsToManyTemplate.foreignKeyForLocalModel,
                getColumnInMapList(metadata, belongsToManyTemplate.localModelLocalKey))
            .when(enableLocalModelMorph, builder -> builder.where(belongsToManyTemplate.morphKeyForLocalModel,
                belongsToManyTemplate.morphValueForLocalModel))
            .when(enableTargetModelMorph, builder -> builder.where(belongsToManyTemplate.morphKeyForTargetModel,
                belongsToManyTemplate.morphValueForTargetModel));
    }

    @Nullable
    @Override
    public Builder<?, ?> prepareTargetBuilder(List<Map<String, Object>> metadata,
        RecordList<?, ?> relationRecordList,
        BuilderWrapper<?, ?> operationBuilder, BuilderWrapper<?, ?> customBuilder) {

        // 关系表的数据
        List<Map<String, Object>> relationMaps = relationRecordList.toMapList();

        // 将中间表结果中的目标表外键, 转化为可以使用 where in 查询的 set
        Set<Object> targetModelForeignKeySet = new HashSet<>(relationMaps.size());
        for (Map<String, Object> map : relationMaps) {
            Object result = map.get(belongsToManyTemplate.foreignKeyForTargetModel);
            if (null == result) {
                continue;
            }
            targetModelForeignKeySet.add(result);
        }

        if (targetModelForeignKeySet.isEmpty()) {
            return null;
        }

        // 目标表查询构造器
        Builder<?, ?> targetBuilder = customBuilder.execute(
            ObjectUtils.typeCast(belongsToManyTemplate.targetModel.newQuery()));


        return selectFill(targetBuilder, belongsToManyTemplate.targetModel.getEntityClass())
            .whereIn(belongsToManyTemplate.targetModelLocalKey, targetModelForeignKeySet);

    }

    @Override
    @Nullable
    public Builder<?, ?> prepareTargetBuilderByRelationOperation(List<Map<String, Object>> metadata,
        RecordList<?, ?> relationRecordList, BuilderWrapper<?, ?> operationBuilder,
        BuilderWrapper<?, ?> customBuilder) {

        // 关系表的数据
        List<Map<String, Object>> relationMaps = relationRecordList.toMapList();

        // relationMaps 进行分组
        Map<Object, List<Object>> map = new HashMap<>();
        for (Map<String, Object> relationMap : relationMaps) {
            List<Object> objects = map.computeIfAbsent(
                relationMap.get(belongsToManyTemplate.foreignKeyForLocalModel), k -> new ArrayList<>());
            objects.add(relationMap.get(belongsToManyTemplate.foreignKeyForTargetModel));
        }

        Builder<?, ?> newQuery = belongsToManyTemplate.targetModel.newQuery();

        Builder<?, ?> opBuilder = operationBuilder.execute(
            ObjectUtils.typeCast(belongsToManyTemplate.targetModel.newQuery()));

        Builder<?, ?> targetBuilder = customBuilder.execute(
            ObjectUtils.typeCast(belongsToManyTemplate.targetModel.newQuery()));

        // 自定义统计处理
        Grammar grammar = targetBuilder.getGrammar();
        if (!grammar.isEmpty(Grammar.SQLPartType.GROUP)) {
            if (grammar.isEmpty(Grammar.SQLPartType.SELECT)) {
                Grammar.SQLPartInfo groupInfo = grammar.get(Grammar.SQLPartType.GROUP);
                targetBuilder.selectRaw(groupInfo.getSqlString(), groupInfo.getParameters());
            }
        }

        for (Map.Entry<Object, List<Object>> entry : map.entrySet()) {
            Object k = entry.getKey();
            List<Object> v = entry.getValue();

            Builder<?, ?> subBuilder = opBuilder.clone()
                .selectCustom(RELATION_KEY, k.toString())
                .from("a", tableBuilder -> tableBuilder.setAnyBuilder(targetBuilder)
                    .whereIn(belongsToManyTemplate.targetModelLocalKey, v));

            newQuery.unionAll(subBuilder);
        }

        // eg : (SELECT max(id) as 'studentMaxId',1 as xx from (select * from student where `id`in("1","2","3"))a)UNION all(SELECT max(id) as 'studentMaxId',2 as xx from (select * from student where `id`in("1","2"))a);

        return newQuery;
    }


    @Override
    public RecordList<?, ?> dealBatchForRelation(@Nullable Builder<?, ?> relationBuilder) {
        if (ObjectUtils.isEmpty(relationBuilder)) {
            return emptyRecordList();
        }
        return belongsToManyTemplate.relationModel.newQuery()
            .setBuilder(ObjectUtils.typeCast(relationBuilder))
            .get();
    }

    @Override
    public Container getContainer() {
        return belongsToManyTemplate.targetModel.getGaarasonDataSource().getContainer();
    }

    @Override
    public RecordList<?, ?> dealBatchForTarget(@Nullable Builder<?, ?> targetBuilder,
        RecordList<?, ?> relationRecordList) {

        // 目标表结果
        RecordList<?, ?> targetRecordList = dealBatchForTargetByRelationOperation(targetBuilder, relationRecordList);

        // 关系表的数据
        List<Map<String, Object>> relationMaps = relationRecordList.toMapList();

        // 循环关系表, 筛选本表需要的数据
        for (Record<?, ?> targetRecord : targetRecordList) {
            // 目标表的关系键的值
            Object targetKeyValue = targetRecord.getMetadataMap().get(belongsToManyTemplate.targetModelLocalKey);

            for (Map<String, Object> relationMap : relationMaps) {
                // 关系表中， 指示本表的关系键的值
                Object localModelKeyInMap = relationMap.get(belongsToManyTemplate.foreignKeyForLocalModel);
                // 关系表中， 指示目标表的关系键的值
                Object targetModelKeyInMap = relationMap.get(belongsToManyTemplate.foreignKeyForTargetModel);

                if (targetModelKeyInMap.equals(targetKeyValue)) {
                    // 存储到目标表的RecordList 上
                    // Map< localModelKeyInMap -> Set< targetModelKeyInMap > >
                    Set<Object> relationIds = targetRecordList.getCacheMap()
                        .computeIfAbsent(localModelKeyInMap, key -> new HashSet<>());
                    relationIds.add(targetModelKeyInMap);
                }
            }
        }
        return targetRecordList;
    }

    @Override
    public RecordList<?, ?> dealBatchForTargetByRelationOperation(@Nullable Builder<?, ?> targetBuilder,
        RecordList<?, ?> relationRecordList) {
        if (targetBuilder == null) {
            return emptyRecordList();
        }

        // 目标表结果
        return belongsToManyTemplate.targetModel.newQuery()
            .setAnyBuilder(targetBuilder)
            .get();
    }

    @Override
    public Map<String, Object> filterBatchRecordByRelationOperation(Record<?, ?> theRecord,
        RecordList<?, ?> targetRecordList, Map<String, RecordList<?, ?>> cacheRelationRecordList) {

        // 本表的关系键值
        Object value = theRecord.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey);

        return findObj(targetRecordList.getMetadata(), RELATION_KEY, value);
    }

    @Override
    public List<Object> filterBatchRecord(Record<?, ?> theRecord,
        RecordList<?, ?> targetRecordList,
        Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        // 目标关系表的外键字段名
        String targetModelLocalKey = belongsToManyTemplate.targetModelLocalKey;
        // 本表的关系键值
        Object localModelLocalKeyValue = theRecord.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey);

        // 本表应该关联的 目标表的关系键的集合
        Set<Object> targetModelLocalKayValueSet = targetRecordList.getCacheMap().get(localModelLocalKeyValue);

        List<Object> objectList = new ArrayList<>();

        if (!targetRecordList.getMetadata().isEmpty()) {
            // 实体信息
            EntityMember<?, ?> entityMember = modelShadowProvider.get(targetRecordList.get(0).getModel())
                .getEntityMember();
            // 字段信息
            FieldMember<?> fieldMember = entityMember.getFieldMemberByColumnName(targetModelLocalKey);


            List<?> objects = targetRecordList.toObjectList(cacheRelationRecordList);

            for (Object obj : objects) {
                // 目标表的关系键的值
                Object targetModelLocalKeyValue = fieldMember.fieldGet(obj);
                // 满足则加入
                if (targetModelLocalKayValueSet != null &&
                    targetModelLocalKayValueSet.contains(targetModelLocalKeyValue)) {
                    objectList.add(obj);
                }
            }
        }
        return objectList;
    }

    @Override
    public Builder<?, ?> prepareForWhereHas(BuilderWrapper<?, ?> customBuilder) {
        String localModelTableName = localModel.getTableName();
        String relationModelTableName = belongsToManyTemplate.relationModel.getTableName();
        String targetModelTableName = belongsToManyTemplate.targetModel.getTableName();
        // 完成SQL 大致是:  SELECT * FROM student WHERE id in (7,8,9,10) and not EXISTS
        // (select * from teacher INNER JOIN relationship_student_teacher on  `relationship_student_teacher`.teacher_id = `teacher`.id  where `relationship_student_teacher`.student_id = `student`.id );

        // 此处的SQL 大致是: select * from teacher INNER JOIN relationship_student_teacher on  `relationship_student_teacher`.teacher_id = `teacher`.id
        // where `relationship_student_teacher`.student_id = `student`.id
        return customBuilder.execute(ObjectUtils.typeCast(belongsToManyTemplate.targetModel.newQuery()))
            .join(JoinType.INNER, relationModelTableName, builder -> builder.whereColumn(
                    relationModelTableName + "." + belongsToManyTemplate.foreignKeyForTargetModel,
                    targetModelTableName + "." + belongsToManyTemplate.targetModelLocalKey)
                .when(enableTargetModelMorph, morphBuilder -> morphBuilder.where(
                    relationModelTableName + "." + belongsToManyTemplate.morphKeyForTargetModel,
                    belongsToManyTemplate.morphValueForTargetModel)))
            .whereColumn(relationModelTableName + "." + belongsToManyTemplate.foreignKeyForLocalModel,
                localModelTableName + "." + belongsToManyTemplate.localModelLocalKey)
            .when(enableLocalModelMorph,
                builder -> builder.where(relationModelTableName + "." + belongsToManyTemplate.morphKeyForLocalModel,
                    belongsToManyTemplate.morphValueForLocalModel));
    }

    @Override
    public int attach(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap) {
        if (targetRecords.isEmpty()) {
            return 0;
        }

        // 目标表的关系键(默认目标表的主键)
        Collection<Object> compatibleForeignKeyForTargetModelValues = targetRecords.toList(
            recordTemp -> recordTemp.getMetadataMap().get(belongsToManyTemplate.targetModelLocalKey));

        // 事物
        return belongsToManyTemplate.relationModel.newQuery()
            .transaction(() -> attachWithTargetModelLocalKeyValues(theRecord, compatibleForeignKeyForTargetModelValues,
                relationDataMap, true));
    }

    @Override
    public int attach(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues,
        Map<String, Object> relationDataMap) {
        if (targetPrimaryKeyValues.isEmpty()) {
            return 0;
        }

        // 事物
        return belongsToManyTemplate.relationModel.newQuery().transaction(() -> {
            // 目标表中的关系键的集合, 即使中间表中指向目标表的外键的集合
            Collection<Object> targetModelLocalKeyValues = targetModelLocalKeyValuesByPrimaryKeyValues(
                targetPrimaryKeyValues);

            return attachWithTargetModelLocalKeyValues(theRecord, targetModelLocalKeyValues, relationDataMap, true);
        });
    }

    @Override
    public int detach(Record<?, ?> theRecord) {
        // 本表的关系键值
        Object localModelLocalKeyValue = theRecord.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey);

        return setWhere(localModelLocalKeyValue, null)
            .delete();
    }

    @Override
    public int detach(Record<?, ?> theRecord, RecordList<?, ?> targetRecords) {
        // 目标表的关系键值列表
        List<Object> targetModelLocalKeyValues = targetRecords.toList(
            recordTemp -> recordTemp.getMetadataMap().get(belongsToManyTemplate.targetModelLocalKey));
        return detachWithTargetModelLocalKeyValues(theRecord, targetModelLocalKeyValues);
    }

    @Override
    public int detach(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues) {
        // 无需处理则直接返回
        if (targetPrimaryKeyValues.isEmpty()) {
            return 0;
        }
        // 事物
        return belongsToManyTemplate.relationModel.newQuery().transaction(() -> {
            // 目标表中的关系键的集合, 即使中间表中指向目标表的外键的集合
            Collection<Object> targetModelLocalKeyValues = targetModelLocalKeyValuesByPrimaryKeyValues(
                targetPrimaryKeyValues);
            return detachWithTargetModelLocalKeyValues(theRecord, targetModelLocalKeyValues);

        });

    }

    @Override
    public int sync(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap) {
        // 目标表的关系键值列表
        List<Object> targetModelLocalKeyValues = targetRecords.toList(
            recordTemp -> recordTemp.getMetadataMap().get(belongsToManyTemplate.targetModelLocalKey));
        // 事物
        return belongsToManyTemplate.relationModel.newQuery()
            .transaction(
                () -> syncWithTargetModelLocalKeyValues(theRecord, targetModelLocalKeyValues, relationDataMap));
    }

    @Override
    public int sync(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues,
        Map<String, Object> relationDataMap) {
        // 事物
        return belongsToManyTemplate.relationModel.newQuery().transaction(() -> {
            // 目标表中的关系键的集合, 即使中间表中指向目标表的外键的集合
            Collection<Object> targetModelLocalKeyValues = targetModelLocalKeyValuesByPrimaryKeyValues(
                targetPrimaryKeyValues);
            return syncWithTargetModelLocalKeyValues(theRecord, targetModelLocalKeyValues, relationDataMap);
        });
    }

    @Override
    public int toggle(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap) {
        if (targetRecords.isEmpty()) {
            return 0;
        }

        // 目标表的关系键值的集合
        List<Object> targetModelLocalKeyValues = targetRecords.toList(
            recordTemp -> recordTemp.getMetadataMap().get(belongsToManyTemplate.targetModelLocalKey));
        // 事物
        return belongsToManyTemplate.relationModel.newQuery()
            .transaction(
                () -> toggleWithTargetModelLocalKeyValues(theRecord, targetModelLocalKeyValues, relationDataMap));
    }

    @Override
    public int toggle(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues,
        Map<String, Object> relationDataMap) {
        if (targetPrimaryKeyValues.isEmpty()) {
            return 0;
        }
        // 事物
        return belongsToManyTemplate.relationModel.newQuery().transaction(() -> {
            // 目标表中的关系键值的集合, 即使中间表中指向目标表的外键的集合
            Collection<Object> targetModelLocalKeyValues = targetModelLocalKeyValuesByPrimaryKeyValues(
                targetPrimaryKeyValues);

            // 切换关系, 如果指定关系已存在，则解除，如果指定关系不存在，则增加
            return toggleWithTargetModelLocalKeyValues(theRecord, targetModelLocalKeyValues, relationDataMap);
        });
    }

    /**
     * 通过目标表的主键获取目标表的关系键
     * @param targetPrimaryKeyValues 目标表的主键集合
     * @return 目标表的关系键集合
     */
    protected Collection<Object> targetModelLocalKeyValuesByPrimaryKeyValues(
        Collection<Object> targetPrimaryKeyValues) {
        // 目标表中的关系键的集合, 即使中间表中指向目标表的外键的集合
        Collection<Object> targetModelLocalKeyValues;

        // 如果目标表的主键既是关系键, 那么可以简化
        if (belongsToManyTemplate.targetModelLocalKey.equals(
            belongsToManyTemplate.targetModel.getPrimaryKeyColumnName())) {
            // 目标表的主键 处理下 AbstractList
            targetModelLocalKeyValues = compatibleCollection(targetPrimaryKeyValues, belongsToManyTemplate.targetModel);

        } else {
            // 目标表中的关系键的集合, 即使中间表中指向目标表的外键的集合
            targetModelLocalKeyValues = belongsToManyTemplate.targetModel.newQuery()
                .select(belongsToManyTemplate.targetModelLocalKey)
                .whereIn(belongsToManyTemplate.targetModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)
                .get()
                .toOneColumnList();
        }
        return targetModelLocalKeyValues;
    }

    /**
     * 更加关联关系
     * @param theRecord 当前record
     * @param targetModelLocalKeyValues 目标表关系键集合 (已经经过兼容性处理了的)
     * @param relationDataMap 中间表新增是要附带的数据
     * @param checkAlreadyExist 是否多一次查询, 以验证关系是否已存在
     * @return 受影响的行数
     */
    protected int attachWithTargetModelLocalKeyValues(Record<?, ?> theRecord,
        Collection<Object> targetModelLocalKeyValues, Map<String, Object> relationDataMap, boolean checkAlreadyExist) {
        // 无需处理则直接返回
        if (targetModelLocalKeyValues.isEmpty()) {
            return 0;
        }

        // 本表的关系键值
        Object localModelLocalKeyValue = theRecord.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey);

        if (checkAlreadyExist) {
            // 查询中间表(relationModel)是否存在已经存在对应的关系
            List<Object> alreadyExistTargetModelLocalKeyValueList = setWhere(localModelLocalKeyValue,
                targetModelLocalKeyValues)
                .select(belongsToManyTemplate.foreignKeyForLocalModel, belongsToManyTemplate.foreignKeyForTargetModel)
                .get()
                .toList(recordTemp -> recordTemp.getMetadataMap().get(belongsToManyTemplate.foreignKeyForTargetModel));

            // 剔除已经存在的关系, 保留需要插入的ids
            targetModelLocalKeyValues.removeAll(alreadyExistTargetModelLocalKeyValueList);
        }

        // 无需处理则直接返回
        if (targetModelLocalKeyValues.isEmpty()) {
            return 0;
        }

        // 格式化, 并附带需要存储到中间表的信息
        List<String> columnList = new ArrayList<>();
        // 本表的关系键名
        columnList.add(belongsToManyTemplate.foreignKeyForLocalModel);
        // 目标表的关系键名
        columnList.add(belongsToManyTemplate.foreignKeyForTargetModel);
        // 本表的多态的键
        if (enableLocalModelMorph) {
            columnList.add(belongsToManyTemplate.morphKeyForLocalModel);
        }
        // 目标表的多态的键
        if (enableTargetModelMorph) {
            columnList.add(belongsToManyTemplate.morphKeyForTargetModel);
        }

        // 预处理中间表信息
        List<Object> middleValueList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : relationDataMap.entrySet()) {
            // 中间表中附加信息的键名
            columnList.add(entry.getKey());
            // 中间表中附加信息的值
            middleValueList.add(entry.getValue());
        }

        // 二维列表, 批量插入
        List<List<Object>> valuesList = new ArrayList<>();
        for (Object o : targetModelLocalKeyValues) {
            List<Object> valueList = new ArrayList<>();
            // 本表的关系键的值
            valueList.add(localModelLocalKeyValue);
            // 目标表的关系键的值
            valueList.add(o);
            // 本表的多态的值
            if (enableLocalModelMorph) {
                valueList.add(belongsToManyTemplate.morphValueForLocalModel);
            }
            // 目标表的多态的值
            if (enableTargetModelMorph) {
                valueList.add(belongsToManyTemplate.morphValueForTargetModel);
            }
            // 中间表数据
            valueList.addAll(middleValueList);

            // 加入二维列表
            valuesList.add(valueList);
        }

        // 批量插入
        return belongsToManyTemplate.relationModel.newQuery().column(columnList).valueList(valuesList).insert();
    }

    /**
     * 解除关联关系
     * @param theRecord 当前record
     * @param targetModelLocalKeyValues 目标表关系键集合 (已经经过兼容性处理了的)
     * @return 受影响的行数
     */
    protected int detachWithTargetModelLocalKeyValues(Record<?, ?> theRecord,
        Collection<Object> targetModelLocalKeyValues) {
        // 本表的关系键值
        Object localModelLocalKeyValue = theRecord.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey);

        return setWhere(localModelLocalKeyValue, targetModelLocalKeyValues)
            .delete();
    }

    /**
     * 同步到关联关系, 任何不在指定范围的对应记录将会移除
     * @param theRecord 当前record
     * @param targetModelLocalKeyValues 目标表关系键集合 (已经经过兼容性处理了的)
     * @param relationDataMap 中间表新增是要附带的数据
     * @return 受影响的行数
     */
    protected int syncWithTargetModelLocalKeyValues(Record<?, ?> theRecord,
        Collection<Object> targetModelLocalKeyValues, Map<String, Object> relationDataMap) {
        // 本表的关系键值
        Object localModelLocalKeyValue = theRecord.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey);


        // 现存的关联关系, 不需要据需存在的, 解除
        int detachNum = belongsToManyTemplate.relationModel.newQuery()
            .where(belongsToManyTemplate.foreignKeyForLocalModel, localModelLocalKeyValue)
            .whereNotIn(belongsToManyTemplate.foreignKeyForTargetModel, targetModelLocalKeyValues)
            .when(enableLocalModelMorph, builder -> builder.where(belongsToManyTemplate.morphKeyForLocalModel,
                belongsToManyTemplate.morphValueForLocalModel))
            .when(enableTargetModelMorph, builder -> builder.where(belongsToManyTemplate.morphKeyForTargetModel,
                belongsToManyTemplate.morphValueForTargetModel))
            .delete();

        // 执行更新
        int attachNum = attachWithTargetModelLocalKeyValues(theRecord, targetModelLocalKeyValues, relationDataMap,
            true);

        return attachNum + detachNum;
    }

    /**
     * 切换关系, 如果指定关系已存在，则解除，如果指定关系不存在，则增加
     * @param theRecord 当前record
     * @param targetModelLocalKeyValues 目标表关系键集合 (已经经过兼容性处理了的)
     * @param relationDataMap 中间表新增是要附带的数据
     * @return 受影响的行数
     */
    protected int toggleWithTargetModelLocalKeyValues(Record<?, ?> theRecord,
        Collection<Object> targetModelLocalKeyValues, Map<String, Object> relationDataMap) {
        if (targetModelLocalKeyValues.isEmpty()) {
            return 0;
        }
        // 本表的关系键值
        Object localModelLocalKeyValue = theRecord.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey);

        // 现存的关联关系 中间表指向目标表的外键值的集合
        List<Object> alreadyExistTargetModelLocalKeyValues = setWhere(localModelLocalKeyValue,
            targetModelLocalKeyValues)
            .select(belongsToManyTemplate.foreignKeyForTargetModel)
            .get()
            .toOneColumnList();

        // 现存的关联关系, 解除
        int detachNum =
            !targetModelLocalKeyValues.isEmpty() ? setWhere(localModelLocalKeyValue, targetModelLocalKeyValues)
                .delete() : 0;

        // 需要增加的关系(不存在就增加) 中间表指向目标表的外键值的集合
        Collection<Object> compatibleTargetModelLocalKeyValues = compatibleCollection(targetModelLocalKeyValues,
            belongsToManyTemplate.targetModel);
        compatibleTargetModelLocalKeyValues.removeAll(alreadyExistTargetModelLocalKeyValues);

        // 不存在的关系, 新增
        int attachNum = attachWithTargetModelLocalKeyValues(theRecord, compatibleTargetModelLocalKeyValues,
            relationDataMap, false);

        return attachNum + detachNum;
    }

    /**
     * 查询构造器条件设置
     * @param localModelLocalKeyValue 本表的关系键
     * @param targetModelLocalKeyValues 目标表的关系键集合
     * @return 查询构造器
     */
    protected Builder<?, ?> setWhere(Object localModelLocalKeyValue,
        @Nullable Collection<Object> targetModelLocalKeyValues) {
        return belongsToManyTemplate.relationModel.newQuery()
            .where(belongsToManyTemplate.foreignKeyForLocalModel, localModelLocalKeyValue)
            .whereInIgnoreEmpty(belongsToManyTemplate.foreignKeyForTargetModel, targetModelLocalKeyValues)
            .when(enableLocalModelMorph, builder -> builder.where(belongsToManyTemplate.morphKeyForLocalModel,
                belongsToManyTemplate.morphValueForLocalModel))
            .when(enableTargetModelMorph, builder -> builder.where(belongsToManyTemplate.morphKeyForTargetModel,
                belongsToManyTemplate.morphValueForTargetModel));
    }

    class BelongsToManyTemplate {

        final Model<?, ?> relationModel; // user_teacher

        final String foreignKeyForLocalModel;// user_id

        final String localModelLocalKey; // user.id

        final Model<?, ?> targetModel; // teacher

        final String foreignKeyForTargetModel; // teacher_id

        final String targetModelLocalKey;  // teacher.id

        final String morphKeyForLocalModel;

        final String morphValueForLocalModel;

        final String morphKeyForTargetModel;

        final String morphValueForTargetModel;

        BelongsToManyTemplate(Field field) {
            BelongsToMany belongsToMany = field.getAnnotation(BelongsToMany.class);
            relationModel = getModelInstance(belongsToMany.relationModel()); // user_teacher
            foreignKeyForLocalModel = belongsToMany.foreignKeyForLocalModel(); // user_id
            localModelLocalKey = belongsToMany.localModelLocalKey().isEmpty() ? getPrimaryKeyColumnName(localModel) :
                belongsToMany.localModelLocalKey(); // user.id
            targetModel = getModelInstance(field); // teacher
            foreignKeyForTargetModel = belongsToMany.foreignKeyForTargetModel(); // teacher_id
            targetModelLocalKey =
                    belongsToMany.targetModelLocalKey().isEmpty() ? getPrimaryKeyColumnName(targetModel) :
                    belongsToMany.targetModelLocalKey();  // teacher.id

            morphKeyForLocalModel = belongsToMany.morphKeyForLocalModel();
            morphValueForLocalModel = belongsToMany.morphValueForLocalModel().isEmpty() ? localModel.getTableName() :
                belongsToMany.morphValueForLocalModel();
            morphKeyForTargetModel = belongsToMany.morphKeyForTargetModel();
            morphValueForTargetModel =
                    belongsToMany.morphValueForTargetModel().isEmpty() ? targetModel.getTableName() :
                    belongsToMany.morphValueForTargetModel();

            enableLocalModelMorph = !morphKeyForLocalModel.isEmpty();
            enableTargetModelMorph = !morphKeyForTargetModel.isEmpty();
        }
    }
}
