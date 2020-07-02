package gaarason.database.conversion;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.annotations.*;
import gaarason.database.eloquent.relations.BelongsToManyQuery;
import gaarason.database.eloquent.relations.BelongsToQuery;
import gaarason.database.eloquent.relations.HasManyQuery;
import gaarason.database.eloquent.relations.HasOneQuery;
import gaarason.database.exception.EntityNewInstanceException;
import gaarason.database.exception.TypeNotSupportedException;
import gaarason.database.support.Column;
import gaarason.database.utils.EntityUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToObject<T, K> {

    private Record<T, K> record;

    public ToObject(Record<T, K> record) {
        this.record = record;
    }


    // something
    private Map<String, List<dd>> ddMap = new HashMap<>();



    @Data
    @AllArgsConstructor
    static class dd{
        // HasOne, HasMany, BelongsToMany, BelongsTo
        String type;

        Field field;

        // 主键名
        String primaryKeyName;

        // 主键值
        String primaryKeyValue;
    }

    public void put(String field, dd e){
        List<dd> dds = ddMap.get(field);
        if(dds != null){
            dds.add(e);
        }else{
            List<dd> dds1 = new ArrayList<>();
            dds1.add(e);
            ddMap.put(field, dds1);
        }
    }


    public T toObject() {
        return toObject(record.getModel().getEntityClass(), record.getMetadataMap(), true);
    }


    public <V> V toObject(Class<V> entityClass, Map<String, Column> stringColumnMap,
                          boolean attachedRelationship) {
        // 获取所有字段
        Field[] fields = entityClass.getDeclaredFields();
        try {
            // 实例化对象
            V entity = entityClass.newInstance();
            // 普通字段赋值, 主键赋值
            fieldAssignment(fields, stringColumnMap, entity);
            // 关联关系查询&赋值
            if (attachedRelationship) {
                fieldRelationAssignment(fields, stringColumnMap, entity);
            }
            return entity;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new EntityNewInstanceException(e.getMessage());
        }

    }

    /**
     * 将数据库查询结果赋值给entity的field
     * @param fields          属性
     * @param stringColumnMap 元数据map
     * @param entity          数据表实体对象
     */
    private <V> void fieldAssignment(Field[] fields, Map<String, Column> stringColumnMap, V entity)
        throws TypeNotSupportedException {
        for (Field field : fields) {
            String columnName = EntityUtil.columnName(field);
            Column column     = stringColumnMap.get(columnName);
            if (column == null) {
                continue;
            }
            field.setAccessible(true); // 设置属性是可访问
            try {
                Object value = EntityUtil.columnFill(field, column.getValue());
                field.set(entity, value);
                // 主键值记录
                if (field.isAnnotationPresent(Primary.class)) {
                    record.setOriginalPrimaryKeyValue(value);
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new TypeNotSupportedException(e.getMessage());
            }
        }
    }

    /**
     * 将关联关系的数据库查询结果赋值给entity的field
     * @param fields          属性
     * @param stringColumnMap 元数据map
     * @param entity          数据表实体对象
     */
    private <V> void fieldRelationAssignment(Field[] fields, Map<String, Column> stringColumnMap, V entity)
        throws TypeNotSupportedException {
        for (Field field : fields) {
            // 获取关系的预处理
            GenerateSqlPart        generateSqlPart        = record.getRelationBuilderMap().get(field.getName());
            RelationshipRecordWith relationshipRecordWith = record.getRelationRecordMap().get(field.getName());
            if (generateSqlPart == null || relationshipRecordWith == null) {
                continue;
            }
            field.setAccessible(true);
            Object relationshipEntity;
            // 关联关系查询&赋值
            if (field.isAnnotationPresent(HasOne.class)) {
                put(field.getName(), new dd("HasOne", field, stringColumnMap, generateSqlPart, relationshipRecordWith));


                relationshipEntity = HasOneQuery.dealSingle(field, stringColumnMap, generateSqlPart,
                    relationshipRecordWith);
            } else if (field.isAnnotationPresent(HasMany.class)) {
                relationshipEntity = HasManyQuery.dealSingle(field, stringColumnMap, generateSqlPart,
                    relationshipRecordWith);
            } else if (field.isAnnotationPresent(BelongsToMany.class)) {
                relationshipEntity = BelongsToManyQuery.dealSingle(field, stringColumnMap, generateSqlPart,
                    relationshipRecordWith);
            } else if (field.isAnnotationPresent(BelongsTo.class)) {
                relationshipEntity = BelongsToQuery.dealSingle(field, stringColumnMap, generateSqlPart,
                    relationshipRecordWith);
            } else {
                continue;
            }
            try {
                field.set(entity, relationshipEntity);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new TypeNotSupportedException(e.getMessage());
            }
        }
    }
}
