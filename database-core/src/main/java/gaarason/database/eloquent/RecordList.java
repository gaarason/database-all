package gaarason.database.eloquent;

import gaarason.database.cache.TableCache;
import gaarason.database.contracts.function.FilterRecordAttribute;
import gaarason.database.contracts.function.GenerateRecordList;
import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.contracts.record.FriendlyListORM;
import gaarason.database.contracts.record.RelationshipListORM;
import gaarason.database.eloquent.annotations.BelongsTo;
import gaarason.database.eloquent.annotations.BelongsToMany;
import gaarason.database.eloquent.annotations.HasMany;
import gaarason.database.eloquent.annotations.HasOne;
import gaarason.database.eloquent.relations.BelongsToManyQuery;
import gaarason.database.eloquent.relations.BelongsToQuery;
import gaarason.database.eloquent.relations.HasManyQuery;
import gaarason.database.eloquent.relations.HasOneQuery;
import gaarason.database.exception.EntityNewInstanceException;
import gaarason.database.exception.ModelNewInstanceException;
import gaarason.database.exception.base.BaseException;
import gaarason.database.support.Column;
import gaarason.database.utils.EntityUtil;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.*;

public class RecordList<T, K> extends ArrayList<Record<T, K>> implements FriendlyListORM<T, K>,
    RelationshipListORM<T, K> {

    /**
     * 元数据
     */
    @Getter
    @Setter
    private List<Map<String, Column>> originalMetadataMapList;

    /**
     * sql
     */
    @Getter
    @Setter
    private String originalSql;

    /**
     * 转化为对象列表
     * @return 对象列表
     */
    @Override
    public List<T> toObjectList() {

        TableCache<Record<?, ?>> tableCache = new TableCache<>();

        return toObjectList(tableCache);
    }


    List<T> toObjectList__new(TableCache<Record<?, ?>> tableCache) {
        List<T> list = new ArrayList<>();
        for (Record<T, K> record : this) {
            Class<T> entityClass = record.getModel().getEntityClass();
            // 某个字段的总数据集
            Field[] fields = entityClass.getDeclaredFields();
            try {
                // 实体类的对象
                T entity = entityClass.newInstance();
                for (Field field : fields) {
                    field.setAccessible(true); // 设置属性是可访问
                    doField(field, record, entity, tableCache);

                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new EntityNewInstanceException(e.getMessage());
            }
        }
    }

    void doField(Field field, Record<T, K> record, T entity, TableCache<Record<?, ?>> tableCache)
        throws IllegalAccessException {
// 普通赋值
        EntityUtil.fieldAssignment(field, record.getMetadataMap(), entity, record);
        // 获取关系的预处理
        GenerateSqlPart generateSqlPart =
            record.relationBuilderMap.get(field.getName());
        RelationshipRecordWith relationshipRecordWith = record.relationRecordMap.get(field.getName());
        if (generateSqlPart == null || relationshipRecordWith == null) {
            continue;
        }
        // 关联关系赋值
        // 一对一
        if (field.isAnnotationPresent(HasOne.class)) {
            // 关系model的结果集, 优先内存查找
            RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
                field.getName(),
                () -> HasOneQuery.dealBatch(field, originalMetadataMapList, generateSqlPart,
                    relationshipRecordWith));
            // 筛选当前 record 所需要的属性
            field.set(entity, HasOneQuery.filterBatch(field, record, relationshipRecordList));
        }
        // 一对多
        else if (field.isAnnotationPresent(HasMany.class)) {
            // 关系model的结果集, 优先内存查找
            RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
                field.getName(),
                () -> HasManyQuery.dealBatch(field, originalMetadataMapList, generateSqlPart,
                    relationshipRecordWith));
            // 筛选当前 record 所需要的属性
            field.set(entity, HasManyQuery.filterBatch(field, record, relationshipRecordList));
        }
        // 多对多
        else if (field.isAnnotationPresent(BelongsToMany.class)) {
            // 关系model的结果集, 优先内存查找
            RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
                field.getName(), () -> BelongsToManyQuery.dealBatch(field, record.getMetadataMap(),
                    originalMetadataMapList, generateSqlPart, relationshipRecordWith));
            // 筛选当前 record 所需要的属性
            field.set(entity, BelongsToManyQuery.filterBatch(field, record, relationshipRecordList));
        }
        // 逆向一对一
        else if (field.isAnnotationPresent(BelongsTo.class)) {
            // 关系model的结果集, 优先内存查找
            RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
                field.getName(),
                () -> BelongsToQuery.dealBatch(field, originalMetadataMapList, generateSqlPart,
                    relationshipRecordWith));
            // 筛选当前 record 所需要的属性
            field.set(entity, BelongsToQuery.filterBatch(field, record, relationshipRecordList));
        }
    }



    /**
     * 转化为对象列表
     * @return 对象列表
     */
    @SuppressWarnings("unchecked")
    List<T> toObjectList(TableCache<Record<?, ?>> tableCache) {
        List<T> list = new ArrayList<>();

        // 关联关系的临时性缓存
        Map<String, RecordList<?, ?>> cacheRelationRecordList = new HashMap<>();


        for (Record<T, K> record : this) {
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
                        record.relationBuilderMap.get(field.getName());
                    RelationshipRecordWith relationshipRecordWith = record.relationRecordMap.get(field.getName());
                    if (generateSqlPart == null || relationshipRecordWith == null) {
                        continue;
                    }
                    // 关联关系赋值
                    // 一对一
                    if (field.isAnnotationPresent(HasOne.class)) {
                        // 关系model的结果集, 优先内存查找
                        RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
                            field.getName(),
                            () -> HasOneQuery.dealBatch(field, originalMetadataMapList, generateSqlPart,
                                relationshipRecordWith));
                        // 筛选当前 record 所需要的属性
                        field.set(entity, HasOneQuery.filterBatch(field, record, relationshipRecordList));
                    }
                    // 一对多
                    else if (field.isAnnotationPresent(HasMany.class)) {
                        // 关系model的结果集, 优先内存查找
                        RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
                            field.getName(),
                            () -> HasManyQuery.dealBatch(field, originalMetadataMapList, generateSqlPart,
                                relationshipRecordWith));
                        // 筛选当前 record 所需要的属性
                        field.set(entity, HasManyQuery.filterBatch(field, record, relationshipRecordList));
                    }
                    // 多对多
                    else if (field.isAnnotationPresent(BelongsToMany.class)) {
                        // 关系model的结果集, 优先内存查找
                        RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
                            field.getName(), () -> BelongsToManyQuery.dealBatch(field, record.getMetadataMap(),
                                originalMetadataMapList, generateSqlPart, relationshipRecordWith));
                        // 筛选当前 record 所需要的属性
                        field.set(entity, BelongsToManyQuery.filterBatch(field, record, relationshipRecordList));
                    }
                    // 逆向一对一
                    else if (field.isAnnotationPresent(BelongsTo.class)) {
                        // 关系model的结果集, 优先内存查找
                        RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
                            field.getName(),
                            () -> BelongsToQuery.dealBatch(field, originalMetadataMapList, generateSqlPart,
                                relationshipRecordWith));
                        // 筛选当前 record 所需要的属性
                        field.set(entity, BelongsToQuery.filterBatch(field, record, relationshipRecordList));
                    }
                }
                list.add(entity);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new EntityNewInstanceException(e.getMessage());
            }
        }
        return list;
    }


    /**
     * 转化为对象列表(不查询关联关系)
     * @return 对象列表
     */
    public List<T> toObjectWithoutRelationship() {
        List<T> list = new ArrayList<>();
        for (Record<T, K> record : this) {
            list.add(record.toObjectWithoutRelationship());
        }
        return list;
    }

    /**
     * 转化为map list
     * @return mapList
     */
    @Override
    public List<Map<String, Object>> toMapList() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Record<T, K> record : this) {
            list.add(record.toMap());
        }
        return list;
    }

    /**
     * 过滤成list
     * @return 单个字段列表
     */
    @Override
    public List<Object> toList(FilterRecordAttribute<T, K> filterRecordAttribute) {
        List<Object> list = new ArrayList<>();
        for (Record<T, K> record : this) {
            Object result = filterRecordAttribute.filter(record);
            if (null == result)
                continue;
            list.add(result);
        }
        return list;
    }


    @Override
    public RecordList<T, K> with(String column) {
        return with(column, (builder) -> builder);
    }

    @Override
    public <TO, KO> RecordList<T, K> with(String column, GenerateSqlPart<TO, KO> builderClosure) {
        return with(column, builderClosure, (record) -> record);
    }

    @Override
    public <TO, KO> RecordList<T, K> with(String column, GenerateSqlPart<TO, KO> builderClosure,
                                          RelationshipRecordWith<TO, KO> recordListClosure) {
        for (Record<T, K> tkRecord : this) {
            // 赋值关联关系过滤
            tkRecord.with(column, builderClosure, recordListClosure);
        }
        return this;
    }

    /**
     * 在内存缓存中优先查找目标值
     * @param cacheRelationRecordList 缓存map
     * @param key                     目标值
     * @param closure                 真实业务逻辑实现
     * @return 批量结果集
     */
    private static RecordList<?, ?> getRecordListInCache(Map<String, RecordList<?, ?>> cacheRelationRecordList,
                                                         String key, GenerateRecordList<?, ?> closure) {
        // 关系model的结果集, 本地先取值
        RecordList<?, ?> recordList = cacheRelationRecordList.get(key);
        // 本地取值为空, 则查询数据
        if (recordList == null) {
            // 执行生成
            recordList = closure.generate();
            // 赋值本地, 以便下次使用
            cacheRelationRecordList.put(key, recordList);
        }
        return recordList;
    }

    /**
     * @param tableCache
     * @param tableName
     * @param closure
     * @return
     */
    private static List<Record<?, ?>> getRecordListInCache(TableCache<Record<?, ?>> tableCache, String tableName,
                                                           Set<String> ids, GenerateRecordList<?, ?> closure) {
        // 关系model的结果集, 本地先取值
//        Record<?, ?> record = tableCache.getOne(tableName, key);
        List<Record<?, ?>> manyInCache = tableCache.getMany(tableName, ids);

        // 本地取值为空, 则查询数据
        if (manyInCache == null) {
            // 执行生成
            RecordList<?, ?> generate = closure.generate();
            // 赋值本地, 以便下次使用
            for (Record<?, ?> recordNew : generate) {
                Object primaryKeyValue = recordNew.getOriginalPrimaryKeyValue();
                if (null == primaryKeyValue) {
                    throw new BaseException("primaryKeyValue error.");
                }
                tableCache.putOne(tableName, primaryKeyValue.toString(), recordNew);
            }
            manyInCache = tableCache.getMany(tableName, ids);
        }
        return manyInCache;
    }

    @SuppressWarnings("unchecked")
    protected static <T, K> Model<T, K> getModelInstance(Class<? extends Model> modelClass) {
        try {
            return modelClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ModelNewInstanceException(e.getMessage());
        }
    }

}
