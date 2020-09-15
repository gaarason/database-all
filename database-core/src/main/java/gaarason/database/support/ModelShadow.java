package gaarason.database.support;

import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.relation.RelationSubQuery;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.annotations.Column;
import gaarason.database.eloquent.annotations.*;
import gaarason.database.eloquent.relation.BelongsToManyQueryRelation;
import gaarason.database.eloquent.relation.BelongsToQueryRelation;
import gaarason.database.eloquent.relation.HasOneOrManyQueryRelation;
import gaarason.database.exception.InvalidEntityException;
import gaarason.database.exception.InvalidPrimaryKeyTypeException;
import gaarason.database.util.EntityUtil;
import gaarason.database.util.ObjectUtil;
import lombok.Data;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Model信息大全
 */
public class ModelShadow {

    /**
     * 反射
     */
    public final static Reflections reflections = new Reflections("");

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
     * Model Class做为索引
     */
    protected static Map<Class<?>, ModelInformation<?, ?>> ModelMap = new ConcurrentHashMap<>();

    /**
     * Entity Class作为索引
     */
    protected static Map<Class<?>, ModelInformation<?, ?>> EntityMap = new ConcurrentHashMap<>();


    static {
        Set<Class<? extends Model>> modelClasses = reflections.getSubTypesOf(Model.class);

        // 一轮初始化
        for (Class<? extends Model> modelClass : modelClasses) {
            initModelInformation(modelClass);
        }
        // 二轮补充
        for (Class<?> modelClass : ModelMap.keySet()) {
            ModelInformation<?, ?> modelInformation = ModelMap.get(modelClass);
            primitiveFieldDeal(modelInformation);
        }

        // 三轮补充
        for (Class<?> modelClass : ModelMap.keySet()) {
            ModelInformation<?, ?> modelInformation = ModelMap.get(modelClass);
            relationFieldDeal(modelInformation);
        }
    }

    // todo
    public static void newInstance() {
        // 正确的实例化一个model
        System.out.println("test");
    }

    /**
     * 查询Model信息
     * @param model 模型
     * @param <T>   实体类
     * @param <K>   主键类型
     * @return 格式化后的Model信息
     */
    public static <T, K> ModelInformation<T, K> get(Model<T, K> model) {
        // 类型转化
        return ObjectUtil.typeCast(ModelMap.get(model.getClass()));
    }

    /**
     * 查询Model信息
     * @param entityClass 实体类
     * @return 格式化后的Model信息
     */
    public static <T, K> ModelInformation<T, K> get(Class<K> entityClass) {
        ModelInformation<?, ?> result = EntityMap.get(entityClass);
        if (null == result) {
            throw new InvalidEntityException("Entity[" + entityClass + "] have no information in the Shadow.");
        }
        return ObjectUtil.typeCast(result);
    }

    /**
     * 构建索引
     * @param modelClass 模型类
     * @param <T>        实体类
     * @param <K>        主键类型
     */
    protected static <T, K> void initModelInformation(Class<? extends Model> modelClass) {
        ModelInformation<T, K> modelInformation = new ModelInformation<>();
        modelInformation.modelClass = modelClass;
        try {
            // 模型信息
            modelDeal(modelInformation);
        } catch (Throwable e) {
            // 父类, 抽象类跳过
            return;
        }
        // 建立实体类索引
        EntityMap.put(modelInformation.entityClass, modelInformation);
        ModelMap.put(modelClass, modelInformation);
    }

    /**
     * 补充Model信息
     * @param modelInformation Model信息
     * @param <T>              实体类
     * @param <K>              主键类型
     */
    @SuppressWarnings("unchecked")
    protected static <T, K> void modelDeal(ModelInformation<T, K> modelInformation) {
        modelInformation.entityClass = (Class<T>) ((ParameterizedType) modelInformation.modelClass.getGenericSuperclass()).getActualTypeArguments()[0];
        modelInformation.primaryKeyClass = (Class<K>) ((ParameterizedType) modelInformation.modelClass.getGenericSuperclass()).getActualTypeArguments()[1];
        modelInformation.tableName = EntityUtil.tableName(modelInformation.entityClass);
    }

    /**
     * 补充基本字段信息
     * @param modelInformation Model信息
     * @param <T>              实体类
     * @param <K>              主键类型
     */
    protected static <T, K> void primitiveFieldDeal(ModelInformation<T, K> modelInformation) {
        Class<T> entityClass = modelInformation.entityClass;
        Field[] fields = entityClass.getDeclaredFields();

        for (Field field : fields) {
            // 静态属性, 不处理
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            FieldInfo fieldInfo = newFieldInfo(field);

            // 非基本类型, 一般是关联关系
            if (!effectiveRelationField(field)) {
                // 数据库属性
                fieldInfo.column = field.isAnnotationPresent(Column.class) ? field.getAnnotation(Column.class) : null;

                // 数据库列名
                fieldInfo.columnName = EntityUtil.columnName(field);

                // 主键处理
                if (field.isAnnotationPresent(Primary.class)) {
                    Primary primary = field.getAnnotation(Primary.class);
                    modelInformation.primaryKeyIncrement = primary.increment();
                    modelInformation.primaryKeyColumnName = fieldInfo.columnName;
                    modelInformation.primaryKeyName = field.getName();
                    // 主键类型检测
                    if (!modelInformation.primaryKeyClass.equals(field.getType())) {
                        throw new InvalidPrimaryKeyTypeException(
                                "The primary key type [" + field.getType() + "] of the entity does not match with the " +
                                        "generic [" + modelInformation.primaryKeyClass + "]");
                    }
                }

                // 属性名 索引键入
                modelInformation.javaFieldMap.put(fieldInfo.name, fieldInfo);
                // 数据库字段名 索引键入
                modelInformation.columnFieldMap.put(fieldInfo.columnName, fieldInfo);

            }
        }
    }

    /**
     * 补充关系字段信息
     * @param modelInformation Model信息
     * @param <T>              实体类
     * @param <K>              主键类型
     */
    protected static <T, K> void relationFieldDeal(ModelInformation<T, K> modelInformation) {

        Class<T> entityClass = modelInformation.entityClass;
        Field[] fields = entityClass.getDeclaredFields();

        for (Field field : fields) {
            // 静态属性, 不处理
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            FieldInfo fieldInfo = newFieldInfo(field);

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
        }
    }

    /**
     * 通用属性赋值
     * @param field 字段
     * @return FieldInfo
     */
    protected static FieldInfo newFieldInfo(Field field){
        // 设置属性是可访问
        field.setAccessible(true);

        // 对象实例
        FieldInfo fieldInfo = new FieldInfo();
        fieldInfo.field = field;
        fieldInfo.name = field.getName();
        fieldInfo.javaType = field.getType();
        fieldInfo.collection = Arrays.asList(field.getType().getInterfaces()).contains(Collection.class);
        return fieldInfo;
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

    /**
     * 格式化后的Model信息
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

}
