package gaarason.database.provider;

import gaarason.database.contract.eloquent.relation.RelationSubQuery;
import gaarason.database.contract.provider.ModelMemory;
import gaarason.database.core.lang.Nullable;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.eloquent.annotations.BelongsTo;
import gaarason.database.eloquent.annotations.BelongsToMany;
import gaarason.database.eloquent.annotations.Column;
import gaarason.database.eloquent.annotations.HasOneOrMany;
import gaarason.database.eloquent.relation.BelongsToManyQueryRelation;
import gaarason.database.eloquent.relation.BelongsToQueryRelation;
import gaarason.database.eloquent.relation.HasOneOrManyQueryRelation;
import gaarason.database.util.StringUtil;
import lombok.Data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModelMemoryProvider implements ModelMemory {

    /**
     * 允许的java属性
     */
    public final static Class<?>[] basicType = new Class[]{Boolean.class, Byte.class, Character.class, Short.class,
        Integer.class, Long.class, Float.class, Double.class, BigInteger.class, Date.class};

    /**
     * 关联关系注解
     */
    public final static Class<? extends Annotation>[] relationAnnotations = new Class[]{HasOneOrMany.class,
        BelongsTo.class, BelongsToMany.class};

    /**
     * Model做为索引
     */
    protected static Map<Class<?>, ModelInformation<?, ?>> ModelMap = new ConcurrentHashMap<>();

    /**
     * Entity作为索引
     */
    protected static Map<Class<?>, ModelInformation<?, ?>> EntityMap = new ConcurrentHashMap<>();

    /**
     * 格式化后的信息
     */
    @Data
    public static class ModelInformation<T, K> {

        /**
         * model类
         */
        protected Class<?> modelClass;

        /**
         * model对象
         */
        protected Model<T, K> model;

        /**
         * entity类
         */
        protected Class<T> entityClass;

        /**
         * 主键列名(并非一定是实体的属性名)
         */
        protected String primaryKeyColumnName;

        /**
         * 主键名(实体的属性名)
         */
        protected String primaryKeyName;

        /**
         * 主键自增
         */
        protected boolean primaryKeyIncrement;

        /**
         * 主键类型
         */
        protected Class<K> primaryKeyClass;

        /**
         * 数据库表名
         */
        protected String tableName;

        /**
         * `属性名`对应的`普通`字段数组
         */
        protected Map<String, FieldInfo> javaFieldMap = new ConcurrentHashMap<>();

        /**
         * `数据库字段`名对应的`普通`字段数组
         */
        protected Map<String, FieldInfo> columnFieldMap = new ConcurrentHashMap<>();

        /**
         * `属性名`对应的`关系`字段数组
         */
        protected Map<String, FieldInfo> relationFieldMap = new ConcurrentHashMap<>();

    }

    /**
     * 字段信息
     */
    @Data
    public static class FieldInfo {

        /**
         * 是否是集合
         */
        protected boolean collection;

        /**
         * Field
         */
        protected Field field;

        /**
         * 属性名
         */
        protected String name;

        /**
         * 字段名
         */
        protected String columnName;

        /**
         * java中的字段类型
         */
        protected Class<?> javaType;

        /**
         * 数据库中的字段类型
         */
//        protected String jdbcType;

        /**
         * column 注解
         */
        @Nullable
        protected Column column;

        /**
         * 关联关系注解
         */
        @Nullable
        protected RelationSubQuery relationSubQuery;
    }

    /**
     * @param model
     * @param <T>
     * @param <K>
     */
    public static <T, K> ModelInformation<?, ?> get(Model<T, K> model) {
        return ModelMap.computeIfAbsent(model.getClass(), key -> {

            ModelInformation<T, K> modelInformation = new ModelInformation<>();
            modelInformation.modelClass = key;
            modelInformation.model = model;
            modelInformation.tableName = model.getTableName();
            modelInformation.entityClass = model.getEntityClass();
            modelInformation.primaryKeyClass = model.getPrimaryKeyClass();
            modelInformation.primaryKeyColumnName = model.getPrimaryKeyColumnName();
            modelInformation.primaryKeyIncrement = model.isPrimaryKeyIncrement();
            modelInformation.primaryKeyName = model.getPrimaryKeyName();
            // 字段信息
            fieldDeal(modelInformation);

            // 建立实体类索引
            EntityMap.put(modelInformation.entityClass, modelInformation);
            return modelInformation;
        });
    }

    /**
     * @param entity
     */
    public static ModelInformation<?, ?> get(Class<?> entity) {
        ModelInformation<?, ?> modelInformation = EntityMap.get(entity);
        if (null == modelInformation) {
            // todo
            throw new RuntimeException();
        }
        return modelInformation;
    }


    /**
     * 补充字段信息
     * @param modelInformation
     * @param <T>
     * @param <K>
     */
    protected static <T, K> void fieldDeal(ModelInformation<T, K> modelInformation) {

        Class<T> entityClass = modelInformation.entityClass;
        Field[]  fields      = entityClass.getDeclaredFields();

        for (Field field : fields) {
            // 静态属性, 不处理
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            // 设置属性是可访问
            field.setAccessible(true);

            // 挤兑对象实例
            FieldInfo fieldInfo = new FieldInfo();
            fieldInfo.field = field;
            fieldInfo.name = field.getName();
            fieldInfo.javaType = field.getType();
            fieldInfo.collection = Arrays.asList(field.getType().getInterfaces()).contains(Collection.class);

            // 非基本类型, 一般是关联关系
            if (effectiveRelationField(field)) {
                if (field.isAnnotationPresent(BelongsTo.class)) {
                    fieldInfo.relationSubQuery = new BelongsToQueryRelation(field);
                } else if (field.isAnnotationPresent(BelongsToMany.class)) {
                    fieldInfo.relationSubQuery = new BelongsToManyQueryRelation(field);
                } else if (field.isAnnotationPresent(HasOneOrMany.class)) {
                    fieldInfo.relationSubQuery = new HasOneOrManyQueryRelation(field);
                } else {
                    break;
                }
                // 关联关系记录
                modelInformation.relationFieldMap.put(fieldInfo.name, fieldInfo);
            }
            // 基本类型(数据库字段)
            else {
                String nameInColumn = "";
                // 数据库属性
                if (field.isAnnotationPresent(Column.class)) {
                    fieldInfo.column = field.getAnnotation(Column.class);
                    // 注解中的名字
                    nameInColumn = fieldInfo.column.name();
                }
                fieldInfo.columnName = "".equals(nameInColumn)
                    ? StringUtil.lineToHump(fieldInfo.name, false)
                    : nameInColumn;

                // 属性名 索引键入
                modelInformation.javaFieldMap.put(fieldInfo.name, fieldInfo);
                // 数据库字段名 索引键入
                modelInformation.columnFieldMap.put(fieldInfo.columnName, fieldInfo);

            }
        }

    }

    /**
     * 是否有效的关联关系字段
     * @param field 字段
     * @return yes/no
     */
    protected static boolean effectiveRelationField(Field field) {
        // 非基础类型
        boolean isNotPrimitive = !field.getType().isPrimitive();
        // 非包装类型
        boolean isNotBasicType = !Arrays.asList(basicType).contains(field.getType());
        // 有相应的注解
        boolean hasRelationAnnotation = false;
        for (Class<? extends Annotation> relationAnnotation : relationAnnotations) {
            if (field.isAnnotationPresent(relationAnnotation)) {
                hasRelationAnnotation = true;
                break;
            }
        }
        return isNotPrimitive && isNotBasicType && hasRelationAnnotation;
    }

}
