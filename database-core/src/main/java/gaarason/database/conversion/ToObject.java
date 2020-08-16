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


    /**
     * 基本对象转化
     * @param record 结果集
     * @param attachedRelationship 是否启用关联关系
     */
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

                    if (!attachedRelationship) {
                        continue;
                    }

                    // 获取关系的预处理
                    GenerateSqlPart generateSqlPart =
                        record.getRelationBuilderMap().get(field.getName());
                    RelationshipRecordWith relationshipRecordWith = record.getRelationRecordMap().get(field.getName());
                    if (generateSqlPart == null || relationshipRecordWith == null || !attachedRelationship) {
//                        System.out.println(" 跳过关联关系 field :"+ field.getName()  );
//                        System.out.println(" 跳过关联关系 :"+ generateSqlPart + relationshipRecordWith );


                        if(field.getName().equals("students")){
                            System.out.println(" 跳过关联关系 field :"+ field  );

                        }

                        continue;
                    }

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

                    RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
                        field.getName(),
                        () -> subQuery.dealBatch(sameLevelAllMetadataMapList, generateSqlPart,
                            relationshipRecordWith), relationshipRecordWith);
                    // 筛选当前 record 所需要的属性
                    field.set(entity, subQuery.filterBatch(record, relationshipRecordList, cacheRelationRecordList));

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
     * @param closure                 真实业务逻辑实现
     * @return 批量结果集
     */
    private RecordList<?, ?> getRecordListInCache(Map<String, RecordList<?, ?>> cacheRelationRecordList,
                                                  String key, GenerateRecordList closure,
                                                  RelationshipRecordWith relationshipRecordWith) {

        String ff = key + "|tableName:" + records.get(0).getModel().getTableName();
        // 关系model的结果集, 本地先取值
        RecordList<?, ?> recordList = cacheRelationRecordList.get(ff);
        // 本地取值为空, 则查询数据
        if (recordList == null) {
            // 执行生成
            recordList = closure.generate();
            // 赋值本地, 以便下次使用
            cacheRelationRecordList.put(ff, recordList);
        }

        for (Record<?, ?> record : recordList) {
            // 清空重置with & 赋值with
            relationshipRecordWith.generate(record.withClear());
        }

        return recordList;
    }

}
