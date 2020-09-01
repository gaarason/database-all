package gaarason.database.conversion;

import gaarason.database.contracts.eloquent.relations.SubQuery;
import gaarason.database.contracts.function.GenerateRecordList;
import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.annotations.BelongsTo;
import gaarason.database.eloquent.annotations.BelongsToMany;
import gaarason.database.eloquent.annotations.HasOneOrMany;
import gaarason.database.eloquent.relations.BelongsToManyQuery;
import gaarason.database.eloquent.relations.BelongsToQuery;
import gaarason.database.eloquent.relations.HasOneOrManyQuery;
import gaarason.database.exception.EntityNewInstanceException;
import gaarason.database.exception.RelationAnnotationNotSupportedException;
import gaarason.database.support.Column;
import gaarason.database.support.RecordFactory;
import gaarason.database.utils.EntityUtil;

import java.lang.reflect.Field;
import java.util.*;

public class ToObject<T, K> {

    /**
     * 当前结果集
     */
    private final RecordList<T, K> records;

    /**
     * 是否启用关联关系
     * 在启用时, 需要手动指定(with)才会生效
     * 在不启用时, 即使手动指定(with)也不会生效
     */
    private final boolean attachedRelationship;


    /**
     * 基本对象转化
     * @param record               结果集
     * @param attachedRelationship 是否启用关联关系
     */
    public ToObject(Record<T, K> record, boolean attachedRelationship) {
        List<Record<T, K>> records = new ArrayList<>();
        records.add(record);
        this.attachedRelationship = attachedRelationship;
        this.records = RecordFactory.newRecordList(records);
    }

    public ToObject(List<Record<T, K>> records, boolean attachedRelationship) {
        this.attachedRelationship = attachedRelationship;
        this.records = RecordFactory.newRecordList(records);
    }

    public ToObject(RecordList<T, K> records, boolean attachedRelationship) {
        this.attachedRelationship = attachedRelationship;
        this.records = records;
    }


    public T toObject() {
        return toObjectList().get(0);
    }


    public T toObject(Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        return toObjectList(cacheRelationRecordList).get(0);
    }

    public List<T> toObjectList() {
        Map<String, RecordList<?, ?>> cacheRelationRecordList = new HashMap<>();
        return toObjectList(cacheRelationRecordList);

    }

    /**
     * 转化为对象列表
     * @return 对象列表
     */
    public List<T> toObjectList(Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        // 同级数据源
        List<Map<String, Column>> originalMetadataMapList = records.getOriginalMetadataMapList();

        List<T> list = new ArrayList<>();
        // 关联关系的临时性缓存
        for (Record<T, K> record : records) {
            Class<T> entityClass = record.getModel().getEntityClass();
            // 某个字段的总数据集
            Field[] fields = entityClass.getDeclaredFields();
            try {
                // 实体类的对象
                T entity = entityClass.newInstance();
                for (Field field : fields) {
                    field.setAccessible(true); // 设置属性是可访问
                    // 普通赋值
                    EntityUtil.fieldAssignment(field, record.getMetadataMap(), entity, record);

                    // 获取关系的预处理
                    GenerateSqlPart generateSqlPart =
                        record.getRelationBuilderMap().get(field.getName());
                    RelationshipRecordWith relationshipRecordWith = record.getRelationRecordMap().get(field.getName());

                    if (generateSqlPart == null || relationshipRecordWith == null || !attachedRelationship) {
                        continue;
                    }

                    // 关联关系处理器
                    SubQuery subQuery;

                    if (field.isAnnotationPresent(HasOneOrMany.class)) {
                        subQuery = new HasOneOrManyQuery(field);
                    } else if (field.isAnnotationPresent(BelongsTo.class)) {
                        subQuery = new BelongsToQuery(field);
                    } else if (field.isAnnotationPresent(BelongsToMany.class)) {
                        subQuery = new BelongsToManyQuery(field);
                    } else {
                        throw new RelationAnnotationNotSupportedException(Arrays.toString(field.getAnnotations()));
                    }

                    // sql数组
                    String[] sqlArr = subQuery.dealBatchSql(originalMetadataMapList, generateSqlPart);

                    // 本级关系查询
                    RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
                        field.getName(), record.getModel().getTableName(), () -> subQuery.dealBatch(sqlArr),
                        relationshipRecordWith, sqlArr);

                    // 递归处理下级关系, 并筛选当前 record 所需要的属性
                    List<?> objects = subQuery.filterBatchRecord(record, relationshipRecordList,
                        cacheRelationRecordList);

                    // 是否是集合
                    if (Arrays.asList(field.getType().getInterfaces()).contains(Collection.class)) {
                        // 设置字段值
                        field.set(entity, objects);
                    } else {
                        // 设置字段值
                        field.set(entity, objects.size() == 0 ? null : objects.get(0));
                    }
                }
                list.add(entity);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new EntityNewInstanceException(e.getMessage(), e);
            }
        }
        return list;
    }

    /**
     * 在内存缓存中优先查找目标值
     * @param cacheRelationRecordList 缓存map
     * @param key                     目标值
     * @param tableName               表名
     * @param closure                 真实业务逻辑实现
     * @param relationshipRecordWith  record 实现
     * @param sqlArr                  sql数组
     * @return 批量结果集
     */
    private RecordList<?, ?> getRecordListInCache(Map<String, RecordList<?, ?>> cacheRelationRecordList, String key,
                                                  String tableName, GenerateRecordList closure,
                                                  RelationshipRecordWith relationshipRecordWith, String[] sqlArr) {
        // 缓存keyName
        String cacheKeyName = key + "|" + Arrays.toString(sqlArr) + "|" + tableName;

        // 有缓存有直接返回, 没有就执行后返回
        RecordList<?, ?> recordList = cacheRelationRecordList.computeIfAbsent(cacheKeyName,
            theKey -> closure.generate());

        // 使用复制结果
        RecordList<?, ?> recordsCopy = RecordFactory.copyRecordList(recordList);

        // 赋值关联关系
        for (Record<?, ?> record : recordsCopy) {
            relationshipRecordWith.generate(record);
        }
        return recordsCopy;
    }

}
