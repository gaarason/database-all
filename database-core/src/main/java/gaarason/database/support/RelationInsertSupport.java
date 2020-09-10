package gaarason.database.support;

import gaarason.database.contracts.eloquent.relations.SubQuery;
import gaarason.database.contracts.function.GenerateRecordList;
import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.eloquent.Model;
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
import gaarason.database.utils.EntityUtil;

import java.lang.reflect.Field;
import java.util.*;

public class RelationInsertSupport<T, K> {

    /**
     * 需要处理的实体对象集合
     */
    private final Collection<T> entities;

    /**
     * 当前数据模型
     */
    private final Model<T, K> model;

    /**
     * 是否启用关联关系
     */
    private final boolean attachedRelationship;

    /**
     * 基本对象转化
     * @param entity               实体对象
     * @param model                数据模型
     * @param attachedRelationship 是否启用关联关系
     */
    public RelationInsertSupport(T entity, Model<T, K> model, boolean attachedRelationship) {
        List<T> entities = new ArrayList<>();
        entities.add(entity);
        this.entities = entities;
        this.model = model;
        this.attachedRelationship = attachedRelationship;
    }

    /**
     * 基本对象转化
     * @param entities             实体对象集合
     * @param model                数据模型
     * @param attachedRelationship 是否启用关联关系
     */
    public RelationInsertSupport(Collection<T> entities, Model<T, K> model, boolean attachedRelationship) {
        this.entities = entities;
        this.model = model;
        this.attachedRelationship = attachedRelationship;
    }


    public boolean insert() {
        // 检测关联关系是否存在


        // 暂不支持belongTo

        // 存在则开始事物, 搞起


return true;
        // return toObjectList().get(0);
    }


//    public T toObject(Map<String, RecordList<?, ?>> cacheRelationRecordList) {
//        return toObjectList(cacheRelationRecordList).get(0);
//    }
//
//    public List<T> toObjectList() {
//        Map<String, RecordList<?, ?>> cacheRelationRecordList = new HashMap<>();
//        return toObjectList(cacheRelationRecordList);
//
//    }

//    /**
//     * 转化为对象列表
//     * @return 对象列表
//     */
//    public List<T> toObjectList(Map<String, RecordList<?, ?>> cacheRelationRecordList) {
//        // 同级数据源
//        List<Map<String, Column>> originalMetadataMapList = records.getOriginalMetadataMapList();
//
//        List<T> list = new ArrayList<>();
//        // 关联关系的临时性缓存
//        for (Record<T, K> record : records) {
//            Class<T> entityClass = record.getModel().getEntityClass();
//            // 某个字段的总数据集
//            Field[] fields = entityClass.getDeclaredFields();
//            try {
//                // 实体类的对象
//                T entity = entityClass.newInstance();
//                for (Field field : fields) {
//                    field.setAccessible(true); // 设置属性是可访问
//                    // 普通赋值
//                    EntityUtil.fieldAssignment(field, record.getMetadataMap(), entity, record);
//
//                    // 获取关系的预处理
//                    GenerateSqlPart generateSqlPart =
//                            record.getRelationBuilderMap().get(field.getName());
//                    RelationshipRecordWith relationshipRecordWith = record.getRelationRecordMap().get(field.getName());
//
//                    if (generateSqlPart == null || relationshipRecordWith == null || !attachedRelationship) {
//                        continue;
//                    }
//
//                    // 关联关系处理器
//                    SubQuery subQuery;
//
//                    if (field.isAnnotationPresent(HasOneOrMany.class)) {
//                        subQuery = new HasOneOrManyQuery(field);
//                    } else if (field.isAnnotationPresent(BelongsTo.class)) {
//                        subQuery = new BelongsToQuery(field);
//                    } else if (field.isAnnotationPresent(BelongsToMany.class)) {
//                        subQuery = new BelongsToManyQuery(field);
//                    } else {
//                        throw new RelationAnnotationNotSupportedException(Arrays.toString(field.getAnnotations()));
//                    }
//
//                    // sql数组
//                    String[] sqlArr = subQuery.dealBatchSql(originalMetadataMapList, generateSqlPart);
//
//                    // 本级关系查询
//                    RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
//                            field.getName(), record.getModel().getTableName(), () -> subQuery.dealBatch(sqlArr),
//                            relationshipRecordWith, sqlArr);
//
//                    // 递归处理下级关系, 并筛选当前 record 所需要的属性
//                    List<?> objects = subQuery.filterBatchRecord(record, relationshipRecordList,
//                            cacheRelationRecordList);
//
//                    // 是否是集合
//                    if (Arrays.asList(field.getType().getInterfaces()).contains(Collection.class)) {
//                        // 设置字段值
//                        field.set(entity, objects);
//                    } else {
//                        // 设置字段值
//                        field.set(entity, objects.size() == 0 ? null : objects.get(0));
//                    }
//                }
//                list.add(entity);
//            } catch (InstantiationException | IllegalAccessException e) {
//                throw new EntityNewInstanceException(e.getMessage(), e);
//            }
//        }
//        return list;
//    }
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
