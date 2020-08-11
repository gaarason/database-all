package gaarason.database.conversion;

import gaarason.database.contracts.function.GenerateRecordList;
import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.annotations.BelongsTo;
import gaarason.database.eloquent.annotations.BelongsToMany;
import gaarason.database.eloquent.annotations.HasMany;
import gaarason.database.eloquent.annotations.HasOne;
import gaarason.database.eloquent.relations.BelongsToManyQueryBase;
import gaarason.database.eloquent.relations.BelongsToQueryBase;
import gaarason.database.eloquent.relations.HasManyQueryBase;
import gaarason.database.eloquent.relations.HasOneQueryBase;
import gaarason.database.exception.EntityNewInstanceException;
import gaarason.database.support.Column;
import gaarason.database.support.RecordFactory;
import gaarason.database.utils.EntityUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToObject<T, K> {

    /**
     * 原数据是 records 还是 record
     */
    private boolean isList = true;


    private RecordList<T, K> records;

    /**
     * 是否启用关联关系
     * 在启用时, 需要手动指定(with)才会生效
     * 在不启用时, 即使手动指定(with)也不会生效
     */
    private boolean attachedRelationship;


    public ToObject(Record<T, K> record, boolean attachedRelationship) {
        List<Record<T, K>> records = new ArrayList<>();
        records.add(record);
        isList = false;
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


        List<Map<String, Column>> sameLevelAllMetadataMapList = records.getSameLevelAllMetadataMapList();

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

                    if( !attachedRelationship){
                        continue;
                    }

                    // 获取关系的预处理
                    GenerateSqlPart generateSqlPart =
                        record.getRelationBuilderMap().get(field.getName());
                    RelationshipRecordWith relationshipRecordWith = record.getRelationRecordMap().get(field.getName());
                    if (generateSqlPart == null || relationshipRecordWith == null || !attachedRelationship) {
//                        System.out.println(" continue ");
                        continue;
                    }
                    // 关联关系赋值
                    // 一对一
                    if (field.isAnnotationPresent(HasOne.class)) {
                        // 关系model的结果集, 优先内存查找
//                        RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
//                            field.getName(), () -> HasOneQuery.dealBatch(field, originalMetadataMapList, generateSqlPart,
//                                relationshipRecordWith));
                        RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
                            field.getName(),
                            () -> HasOneQueryBase.dealBatch(field, sameLevelAllMetadataMapList, generateSqlPart,
                                relationshipRecordWith));
                        // 筛选当前 record 所需要的属性
                        field.set(entity, HasOneQueryBase.filterBatch(field, record, relationshipRecordList, cacheRelationRecordList));
                    }
                    // 一对多
                    else if (field.isAnnotationPresent(HasMany.class)) {
                        // 关系model的结果集, 优先内存查找
//                        RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
//                            field.getName(), () -> HasManyQuery.dealBatch(field, originalMetadataMapList, generateSqlPart,
//                                relationshipRecordWith));
                        RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
                            field.getName(),
                            () -> HasManyQueryBase.dealBatch(field, sameLevelAllMetadataMapList, generateSqlPart,
                                relationshipRecordWith));
                        // 筛选当前 record 所需要的属性
                        field.set(entity, HasManyQueryBase.filterBatch(field, record, relationshipRecordList, cacheRelationRecordList));
                    }
                    // 多对多
                    else if (field.isAnnotationPresent(BelongsToMany.class)) {
                        // 关系model的结果集, 优先内存查找
//                        RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
//                            field.getName(), () -> BelongsToManyQuery.dealBatch(field, record.getMetadataMap(),
//                                originalMetadataMapList, generateSqlPart, relationshipRecordWith));
                        RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
                            field.getName(), () -> BelongsToManyQueryBase.dealBatch(field, record.getMetadataMap(),
                                sameLevelAllMetadataMapList, generateSqlPart, relationshipRecordWith));
                        // 筛选当前 record 所需要的属性
                        field.set(entity, BelongsToManyQueryBase.filterBatch(field, record, relationshipRecordList, cacheRelationRecordList));
                    }
                    // 逆向一对一
                    else if (field.isAnnotationPresent(BelongsTo.class)) {
                        // 关系model的结果集, 优先内存查找
//                        RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
//                            field.getName(), () -> BelongsToQuery.dealBatch(field, originalMetadataMapList, generateSqlPart,
//                                relationshipRecordWith));
                        RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
                            field.getName(),
                            () -> BelongsToQueryBase.dealBatch(field, sameLevelAllMetadataMapList, generateSqlPart,
                                relationshipRecordWith));
                        // 筛选当前 record 所需要的属性
                        field.set(entity, BelongsToQueryBase.filterBatch(field, record, relationshipRecordList, cacheRelationRecordList));
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
     * 在内存缓存中优先查找目标值
     * @param cacheRelationRecordList 缓存map
     * @param key                     目标值
     * @param closure                 真实业务逻辑实现
     * @return 批量结果集
     */
    private RecordList<?, ?> getRecordListInCache(Map<String, RecordList<?, ?>> cacheRelationRecordList,
                                                         String key, GenerateRecordList closure) {

        String ff = key + "|" + records.get(0).getModel().getTableName();
        // 关系model的结果集, 本地先取值
        RecordList<?, ?> recordList = cacheRelationRecordList.get(ff);
        // 本地取值为空, 则查询数据
        if (recordList == null) {
            // 执行生成
            recordList = closure.generate();
            // 赋值本地, 以便下次使用
            cacheRelationRecordList.put(ff, recordList);
        }
        return recordList;


        // model(表名) - 字段名   ------>  RecordList<?, ?>


    }

}
