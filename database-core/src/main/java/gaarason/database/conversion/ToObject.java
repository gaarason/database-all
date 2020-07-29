package gaarason.database.conversion;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.annotations.BelongsTo;
import gaarason.database.eloquent.annotations.BelongsToMany;
import gaarason.database.eloquent.annotations.HasMany;
import gaarason.database.eloquent.annotations.HasOne;
import gaarason.database.exception.EntityNewInstanceException;
import gaarason.database.support.RecordFactory;
import gaarason.database.utils.EntityUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ToObject<T, K> {

    /**
     * 原数据是 records 还是 record
     */
    private boolean isList = true;


    private List<Object> o = new ArrayList<>();


    private RecordList<T, K> records;

    private Pretreatment<T, K> pretreatment = new Pretreatment<>();

    private Map<String, Set<Object>> relationKeySetMap = new HashMap<>();

    private Temp temp = new Temp();

    private static ThreadPoolExecutor rundddd = new ThreadPoolExecutor(20, 20, 1,
        TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(5));


    public ToObject(Record<T, K> record) {
        List<Record<T, K>> records = new ArrayList<>();
        records.add(record);
        isList = false;
        this.records = RecordFactory.newRecordList(records);
    }

    public ToObject(List<Record<T, K>> records) {
        this.records = RecordFactory.newRecordList(records);
    }

    public ToObject(RecordList<T, K> records) {
        this.records = records;
    }


    // something
    private Map<String, List<dd>> ddMap = new HashMap<>();


    @Data
    @AllArgsConstructor
    static class dd {
        // HasOne, HasMany, BelongsToMany, BelongsTo
        String type;

        Field field;

        // 主键名
        String primaryKeyName;

        // 主键值
        String primaryKeyValue;
    }

    public void put(String field) {

        System.out.println("增加 ---- " + field);

//        List<dd> dds = ddMap.get(field);
//        if(dds != null){
//            dds.add(e);
//        }else{
//            List<dd> dds1 = new ArrayList<>();
//            dds1.add(e);
//            ddMap.put(field, dds1);
//        }
    }


    public T toObject() {
        return toObjectList().get(0);
    }


    /**
     * 转化为对象列表
     * @return 对象列表
     */
    @SuppressWarnings("unchecked")
    public List<T> toObjectList() {

        List<T> list = new ArrayList<>();
        // 关联关系的临时性缓存
        Map<String, RecordList<?, ?>> cacheRelationRecordList = new HashMap<>();

//        // 使用相同的对象引用, 同级主键集合
//        Set<Object> primaryKeyValueSet = new HashSet<>();

        int level = 1;

        for (Record<T, K> record : records) {
            Class<T> entityClass = record.getModel().getEntityClass();
            // 某个字段的总数据集
            Field[] fields = entityClass.getDeclaredFields();
            try {
                // 实体类的对象
                T entity = entityClass.newInstance();
                // 普通赋值
                for (Field field : fields) {
                    // 设置属性是可访问
                    field.setAccessible(true);
//                    // 准备同级主键集合
//                    record.setOriginalPrimaryKeyValueSet(primaryKeyValueSet);
                    // 普通赋值
                    EntityUtil.fieldAssignment(field, record.getMetadataMap(), entity, record);
                }
                list.add(entity);
                // 关联关系 预处理
                for (Field field : fields) {
                    // 关联关系, 预处理
                    fieldRelationPretreatment(field, entity, record, level);
                }
                // 关联关系 执行
                temp.run();

            } catch (InstantiationException | IllegalAccessException e) {
                throw new EntityNewInstanceException(e.getMessage());
            }
        }
        System.out.println("===222===" + temp);
        System.out.println("===1=1==" + relationKeySetMap);
        return list;
    }

    public void fieldRelationPretreatment(Field field, T entity, Record record, int level) {
        // 获取关系的预处理
        Object generateSqlPart = record.getRelationBuilderMap().get(field.getName());

        // todo 是否需要处理关联关系
        Object relationshipRecordWith = record.getRelationRecordMap().get(field.getName());
        if (generateSqlPart == null || relationshipRecordWith == null) {
            return;
        }
        // 关联关系赋值
        // 一对一
        if (field.isAnnotationPresent(HasOne.class)) {

            // todo 暂存

            try {


                Class<?> type = field.getType();

                Object o222 = field.getType().newInstance();

                field.set(entity, o222);

                putRelationKeyValueSet2(field, record, type, record.getOriginalPrimaryKeyValue(), o222, "hasOne");


//                putRelationKeyValueSet(field.getDeclaringClass(), level, record.getOriginalPrimaryKeyValue());
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }


//            Pretreatment<T, K> tkPretreatment = new Pretreatment<>();
//            tkPretreatment.setField(field);
//            tkPretreatment.setGenerateSqlPart(generateSqlPart);
//            tkPretreatment.setRelationshipRecordWith(relationshipRecordWith);
//            tkPretreatment.setType(Pretreatment.relationType.HAS_ONE);
//            tkPretreatment.setPrimaryKeyName();
//            tkPretreatment.getPrimaryKeyValueSet().add()
//
//
//            // 关系model的结果集, 优先内存查找
//            RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
//                field.getName(), () -> HasOneQuery.dealBatch(field, originalMetadataMapList, generateSqlPart,
//                    relationshipRecordWith));
//            // 筛选当前 record 所需要的属性
////            field.set(entity, HasOneQuery.filterBatch(field, record, relationshipRecordList));
        }
        // 一对多
        else if (field.isAnnotationPresent(HasMany.class)) {

            putRelationKeyValueSet(field.getDeclaringClass(), level, record.getOriginalPrimaryKeyValue());


//            // 关系model的结果集, 优先内存查找
//            RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
//                field.getName(), () -> HasManyQuery.dealBatch(field, originalMetadataMapList, generateSqlPart,
//                    relationshipRecordWith));
//            // 筛选当前 record 所需要的属性
//            field.set(entity, HasManyQuery.filterBatch(field, record, relationshipRecordList));
        }
        // 多对多
        else if (field.isAnnotationPresent(BelongsToMany.class)) {

            putRelationKeyValueSet(field.getDeclaringClass(), level, record.getOriginalPrimaryKeyValue());


//            // 关系model的结果集, 优先内存查找
//            RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
//                field.getName(), () -> BelongsToManyQuery.dealBatch(field, record.getMetadataMap(),
//                    originalMetadataMapList, generateSqlPart, relationshipRecordWith));
//            // 筛选当前 record 所需要的属性
//            field.set(entity, BelongsToManyQuery.filterBatch(field, record, relationshipRecordList));
        }
        // 逆向一对一
        else if (field.isAnnotationPresent(BelongsTo.class)) {

            putRelationKeyValueSet(field.getDeclaringClass(), level, record.getOriginalPrimaryKeyValue());


//            // 关系model的结果集, 优先内存查找
//            RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
//                field.getName(), () -> BelongsToQuery.dealBatch(field, originalMetadataMapList, generateSqlPart,
//                    relationshipRecordWith));
//            // 筛选当前 record 所需要的属性
//            field.set(entity, BelongsToQuery.filterBatch(field, record, relationshipRecordList));
        }
    }


    public String createObjHashKey(Class<?> clazz, int level) {
        return clazz.getName() + "<@|@|@>" + level;
    }

    /**
     * 加入主键列表
     * @param clazz
     * @param level
     * @param relationKeyValue
     */
    public void putRelationKeyValueSet(Class<?> clazz, int level, Object relationKeyValue) {
        String      ObjHashKey = createObjHashKey(clazz, level);
        Set<Object> objects    = relationKeySetMap.computeIfAbsent(ObjHashKey, k -> new HashSet<>());
        objects.add(relationKeyValue);
    }


    /**
     * 加入主键列表
     */
    public void putRelationKeyValueSet2(Field field, Record record, Class<?> clazz, Object relationKeyValue,
                                        Object entityObj,
                                        String relationType) {
        temp.setList(true);
        temp.setEntity(clazz);
        temp.setRelation(relationType);
        temp.setField(field);

        // 获取关系的预处理
        GenerateSqlPart generateSqlPart = (GenerateSqlPart)record.getRelationBuilderMap().get(field.getName());
        RelationshipRecordWith relationshipRecordWith = (RelationshipRecordWith)record.getRelationRecordMap().get(field.getName());


        temp.getOriginalMetadataMapList().add(record.getMetadataMap());

        temp.setGenerateSqlPart(generateSqlPart);
        temp.setRelationshipRecordWith(relationshipRecordWith);
        temp.getIdEntityObj().computeIfAbsent(relationKeyValue, k -> entityObj);
    }

    /**
     * @param clazz
     * @param level
     * @return
     */
    public Set<Object> getRelationKeyValueSet(Class<?> clazz, int level) {
        String      ObjHashKey = createObjHashKey(clazz, level);
        Set<Object> objects    = relationKeySetMap.get(ObjHashKey);
        if (objects == null) {
            objects = new HashSet<>();
        }
        return objects;
    }
}
