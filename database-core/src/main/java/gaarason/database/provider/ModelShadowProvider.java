package gaarason.database.provider;

import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.relation.RelationSubQuery;
import gaarason.database.contract.support.IdGenerator;
import gaarason.database.contract.support.ReflectionScan;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.annotation.*;
import gaarason.database.eloquent.appointment.FinalVariable;
import gaarason.database.eloquent.relation.BelongsToManyQueryRelation;
import gaarason.database.eloquent.relation.BelongsToQueryRelation;
import gaarason.database.eloquent.relation.HasOneOrManyQueryRelation;
import gaarason.database.exception.IllegalAccessRuntimeException;
import gaarason.database.exception.InvalidEntityException;
import gaarason.database.exception.InvalidPrimaryKeyTypeException;
import gaarason.database.exception.TypeNotSupportedException;
import gaarason.database.util.EntityUtil;
import gaarason.database.util.ObjectUtil;
import lombok.Data;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static gaarason.database.util.EntityUtil.columnName;

/**
 * Model信息大全
 */
final public class ModelShadowProvider {

    /**
     * Model Class做为索引
     */
    final protected static Map<Class<? extends Model<?, ?>>, ModelInfo<?, ?>> modelIndexMap = new ConcurrentHashMap<>();

    /**
     * Model proxy Class做为索引
     */
    final protected static Map<Class<?>, ModelInfo<?, ?>> modelProxyIndexMap = new ConcurrentHashMap<>();

    /**
     * Entity Class作为索引
     */
    final protected static Map<Class<?>, ModelInfo<?, ?>> entityIndexMap = new ConcurrentHashMap<>();

    /**
     * id生成器
     */
    @Getter
    final protected static IdGenerators idGenerators;

    static {
        // 静态初始化
        idGenerators = new IdGenerators(
            ContainerProvider.getBean(IdGenerator.SnowFlakesID.class),
            ContainerProvider.getBean(IdGenerator.UUID32.class),
            ContainerProvider.getBean(IdGenerator.UUID36.class),
            ContainerProvider.getBean(IdGenerator.Never.class),
            ContainerProvider.getBean(IdGenerator.Custom.class)
        );

        // 一轮初始化Model基础信息, 不存在依赖递归等复杂情况
        // 并过滤不需要的model, 比如抽象类等
        initModelInformation();

        // 二轮补充基础字段信息
        // Model实例化存储
        primitiveFieldDeal();

        // 三轮补充关联关系字段信息
        // RelationSubQuery实例化存储, 依赖第二轮结果
        relationFieldDeal();
    }

    /**
     * 查询Model信息
     * @param model 模型
     * @param <T>   实体类
     * @param <K>   主键类型
     * @return 格式化后的Model信息
     */
    public static <T, K> ModelInfo<T, K> get(Model<T, K> model) {
        return getByModelClass(ObjectUtil.typeCast(model.getClass()));
    }

    /**
     * 查询Model信息
     * @param modelClass 模型类
     * @param <T>        实体类
     * @param <K>        主键类型
     * @return 格式化后的Model信息
     */
    public static <T, K> ModelInfo<T, K> getByModelClass(Class<? extends Model<T, K>> modelClass) {
        ModelInfo<?, ?> result1 = modelIndexMap.get(modelClass);
        if (null == result1) {
            ModelInfo<?, ?> result2 = modelProxyIndexMap.get(modelClass);
            if (null == result2) {
                throw new InvalidEntityException("Model class[" + modelClass + "] have no information in the Shadow.");
            }
            return ObjectUtil.typeCast(result2);
        }
        return ObjectUtil.typeCast(result1);
    }

    /**
     * 查询Model信息
     * @param clazz 实体类(可查找泛型)
     * @return 格式化后的Model信息
     */
    public static <T, K> ModelInfo<T, K> getByEntityClass(Class<T> clazz) {
        ModelInfo<?, ?> result = entityIndexMap.get(clazz);
        if (null == result) {
            throw new InvalidEntityException("Entity class[" + clazz + "] have no information in the Shadow.");
        }
        return ObjectUtil.typeCast(result);
    }

    /**
     * 通过entity解析对应的字段和值组成的map, 忽略不符合规则的字段
     * @param entity     数据表实体对象
     * @param insertType 新增?
     * @param <T>        数据表实体类
     * @return 字段对值的映射
     */
    public static <T> Map<String, String> columnValueMap(T entity, boolean insertType) {
        // 结果集
        Map<String, String> columnValueMap = new HashMap<>();
        // 属性信息集合
        Map<String, FieldInfo> columnFieldMap = getByEntityClass(entity.getClass()).getColumnFieldMap();
        for (Map.Entry<String, FieldInfo> entry : columnFieldMap.entrySet()) {
            // 属性信息
            FieldInfo fieldInfo = entry.getValue();
            // 值
            Object value = fieldGet(fieldInfo, entity);
            // 有效则加入 结果集
            if (effectiveField(fieldInfo, value, insertType)) {
                columnValueMap.put(entry.getKey(), EntityUtil.valueFormat(value));
            }
        }
        return columnValueMap;
    }

    /**
     * 通过entity解析对应的字段组成的list
     * 忽略不符合规则的字段
     * @param entity     数据表实体对象
     * @param <T>        数据表实体类
     * @param insertType 新增?
     * @return 列名组成的list
     */
    public static <T> List<String> columnNameList(T entity, boolean insertType) {
        // 结果集
        List<String> columnList = new ArrayList<>();
        // 属性信息集合
        Map<String, FieldInfo> columnFieldMap = getByEntityClass(entity.getClass()).getColumnFieldMap();
        for (Map.Entry<String, FieldInfo> entry : columnFieldMap.entrySet()) {
            // 属性信息
            FieldInfo fieldInfo = entry.getValue();
            // 值
            Object value = fieldGet(fieldInfo, entity);
            // 有效则加入 结果集
            if (effectiveField(fieldInfo, value, insertType)) {
                columnList.add(entry.getKey());
            }
        }
        return columnList;
    }

    /**
     * 通过entity解析对应的字段的值组成的list, 忽略不符合规则的字段
     * @param entity         数据表实体对象
     * @param <T>            数据表实体类
     * @param columnNameList 有效的属性名
     * @return 字段的值组成的list
     */
    public static <T> List<String> valueList(T entity, List<String> columnNameList) {
        // 结果集
        List<String> valueList = new ArrayList<>();
        // 属性信息集合
        Map<String, FieldInfo> columnFieldMap = getByEntityClass(entity.getClass()).getColumnFieldMap();
        for (Map.Entry<String, FieldInfo> entry : columnFieldMap.entrySet()) {
            // 属性信息
            FieldInfo fieldInfo = entry.getValue();
            // 加入需要的数据
            if (columnNameList.contains(entry.getKey())) {
                valueList.add(EntityUtil.valueFormat(fieldGet(fieldInfo, entity)));
            }
        }
        return valueList;
    }

    /**
     * 将数据库查询结果赋值给entity的field
     * 需要 field.setAccessible(true)
     * @param fieldInfo       字段信息
     * @param stringColumnMap 元数据map
     * @param entity          数据表实体对象
     */
    public static <T, K> void fieldAssignment(FieldInfo fieldInfo,
                                              Map<String, gaarason.database.support.Column> stringColumnMap,
                                              T entity, Record<T, K> record) throws TypeNotSupportedException {
        gaarason.database.support.Column column = stringColumnMap.get(fieldInfo.columnName);
        if (column != null) {
            try {
                Object value = EntityUtil.columnFill(fieldInfo.field, column.getValue());
                fieldInfo.field.set(entity, value);
                // 主键值记录
                if (fieldInfo.field.isAnnotationPresent(Primary.class) && value != null) {
                    record.setOriginalPrimaryKeyValue(ObjectUtil.typeCast(value));
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new TypeNotSupportedException(e.getMessage(), e);
            }

        }
    }

    /**
     * 设置 entity 对象的自增属性值
     * @param <T>    数据表实体类
     * @param <K>    数据表主键类型
     * @param entity 数据表实体对象
     * @param id     数据库生成的id
     * @throws IllegalAccessRuntimeException 反射赋值异常
     */
    public static <T, K> void setPrimaryKeyValue(T entity, @Nullable K id) {
        // 属性信息集合
        FieldInfo primaryKeyFieldInfo = getByEntityClass(entity.getClass()).getPrimaryKeyFieldInfo();
        if (null != primaryKeyFieldInfo) {
            fieldSet(primaryKeyFieldInfo.field, entity, id);
        }
    }

    /**
     * 获取 entity 对象的主键值
     * @param <T>    数据表实体类
     * @param <K>    数据表主键类型
     * @param entity 数据表实体对象
     * @throws IllegalAccessRuntimeException 反射赋值异常
     */
    @Nullable
    public static <T, K> K getPrimaryKeyValue(T entity) {
        // 属性信息集合
        FieldInfo primaryKeyFieldInfo = getByEntityClass(entity.getClass()).getPrimaryKeyFieldInfo();
        if (null != primaryKeyFieldInfo) {
            Object value = fieldGet(primaryKeyFieldInfo, entity);
            if (value != null) {
                return ObjectUtil.typeCast(value);
            }
        }
        return null;
    }

    /**
     * 获取属性的值
     * @param fieldInfo 属性信息
     * @param obj       对象
     * @return 值
     * @throws IllegalAccessRuntimeException 反射赋值异常
     */
    @Nullable
    public static Object fieldGet(FieldInfo fieldInfo, Object obj) {
        try {
            return fieldInfo.getField().get(obj);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException(e);
        }
    }

    /**
     * 设置属性的值
     * @param field 属性
     * @param obj   对象
     * @param value 值
     */
    protected static void fieldSet(Field field, Object obj, @Nullable Object value) {
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException(e);
        }
    }

    /**
     * 是否有效字段
     * @param fieldInfo  字段
     * @param value      字段值
     * @param insertType 是否是新增,会通过字段上的注解column(insertable, updatable)进行忽略
     * @return 有效
     */
    protected static boolean effectiveField(FieldInfo fieldInfo, @Nullable Object value, boolean insertType) {
        // 不可插入 or 不可更新
        if (insertType ? !fieldInfo.insertable : !fieldInfo.updatable) {
            return false;
        }

        return fieldInfo.nullable || value != null;
    }

    /**
     * 构建索引
     */
    protected static void initModelInformation() {
        Set<Class<? extends Model<?, ?>>> modelClasses = ContainerProvider.getBean(ReflectionScan.class).scanModels();

        for (Class<? extends Model<?, ?>> modelClass : modelClasses) {
            initModelInformation(ObjectUtil.typeCast(modelClass));
        }
    }

    /**
     * 构建索引
     * @param modelClass 模型类
     * @param <T>        实体类
     * @param <K>        主键类型
     */
    protected static <T, K> void initModelInformation(Class<? extends Model<T, K>> modelClass) {
        ModelInfo<T, K> modelInfo = new ModelInfo<>();
        modelInfo.modelClass = modelClass;
        try {
            // 模型信息
            modelDeal(modelInfo);
        } catch (Throwable e) {
            // 父类, 抽象类跳过
            return;
        }
        // 建立实体类索引
        entityIndexMap.put(modelInfo.entityClass, modelInfo);
        modelIndexMap.put(modelClass, modelInfo);
    }

    /**
     * 补充Model信息
     * @param modelInfo Model信息
     * @param <T>       实体类
     * @param <K>       主键类型
     */
    protected static <T, K> void modelDeal(ModelInfo<T, K> modelInfo) {
        modelInfo.entityClass = ObjectUtil.getGenerics(modelInfo.modelClass, 0);
        modelInfo.primaryKeyClass = ObjectUtil.getGenerics(modelInfo.modelClass, 1);
        modelInfo.tableName = EntityUtil.tableName(modelInfo.entityClass);
    }

    /**
     * 补充基本字段信息
     */
    protected static void primitiveFieldDeal() {
        for (Map.Entry<Class<? extends Model<?, ?>>, ModelInfo<?, ?>> entry : modelIndexMap.entrySet()) {
            ModelInfo<?, ?> modelInfo = entry.getValue();
            primitiveFieldDeal(modelInfo);
        }
    }

    /**
     * 补充基本字段信息
     * @param modelInfo Model信息
     * @param <T>       实体类
     * @param <K>       主键类型
     */
    protected static <T, K> void primitiveFieldDeal(ModelInfo<T, K> modelInfo) {
        Class<T> entityClass = modelInfo.entityClass;
        // 模型实体缓存
        modelInfo.model = ModelInstanceProvider.getModel(modelInfo.modelClass);
        // 模型代理索引建立
        modelProxyIndexMap.put(modelInfo.model.getClass(), modelInfo);


//        Field[] fields = entityClass.getDeclaredFields();

        List<Field> fields = EntityUtil.getDeclaredFieldsContainParent(entityClass);

        for (Field field : fields) {
            // 非静态 基本类型
            if (!EntityUtil.isStaticField(field) && EntityUtil.isBasicField(field)) {
                // 设置属性是可访问
                field.setAccessible(true);
                // 对象实例
                FieldInfo fieldInfo = new FieldInfo();
                fieldInfo.field = field;
                fieldInfo.name = field.getName();
                fieldInfo.javaType = field.getType();
                // todo 应该优先使用数据库默认值, 当默认值不存在时, 再才使用如下方法
                fieldInfo.defaultValue = Number.class.isAssignableFrom(fieldInfo.javaType) ? "0" : "";

                // 数据库属性
                fieldInfo.column = field.isAnnotationPresent(Column.class) ? field.getAnnotation(Column.class) : null;

                // 数据库列名
                fieldInfo.columnName = columnName(field);

                // 主键处理
                if (field.isAnnotationPresent(Primary.class)) {
                    Primary primary = field.getAnnotation(Primary.class);
                    // 主键 索引键入
                    modelInfo.primaryKeyDefinition = true;
                    modelInfo.primaryKeyFieldInfo = fieldInfo;
                    modelInfo.primaryKeyIncrement = primary.increment();
                    modelInfo.primaryKeyColumnName = fieldInfo.columnName;
                    modelInfo.primaryKeyName = field.getName();
                    // 主键类型检测
                    if (!modelInfo.primaryKeyClass.equals(Object.class) && !modelInfo.primaryKeyClass.equals(field.getType())) {
                        throw new InvalidPrimaryKeyTypeException(
                            "The primary key type [" + field.getType() + "] of the entity does not match with the " +
                                "generic [" + modelInfo.primaryKeyClass + "]");
                    }
                    // 主键生成器选择
                    switch (primary.idGenerator()) {
                        case SNOW_FLAKES_ID:
                            modelInfo.primaryKeyIdGenerator = ObjectUtil.typeCast(idGenerators.snowFlakesID);
                            break;
                        case UUID_36:
                            modelInfo.primaryKeyIdGenerator = ObjectUtil.typeCast(idGenerators.uuid36);
                            break;
                        case UUID_32:
                            modelInfo.primaryKeyIdGenerator = ObjectUtil.typeCast(idGenerators.uuid32);
                            break;
                        case NEVER:
                            modelInfo.primaryKeyIdGenerator = ObjectUtil.typeCast(idGenerators.never);
                            break;
                        case CUSTOM:
                            modelInfo.primaryKeyIdGenerator = ObjectUtil.typeCast(idGenerators.custom);
                            break;
                        default:
                            // auto
                            if (fieldInfo.javaType == Long.class) {
                                modelInfo.primaryKeyIdGenerator = ObjectUtil.typeCast(idGenerators.snowFlakesID);
                            } else if (fieldInfo.javaType == String.class && fieldInfo.column != null) {
                                if (fieldInfo.column.length() >= 36) {
                                    modelInfo.primaryKeyIdGenerator = ObjectUtil.typeCast(idGenerators.uuid36);
                                } else if (fieldInfo.column.length() >= 32) {
                                    modelInfo.primaryKeyIdGenerator = ObjectUtil.typeCast(idGenerators.uuid32);
                                }
                            } else {
                                modelInfo.primaryKeyIdGenerator = ObjectUtil.typeCast(idGenerators.never);
                            }
                    }
                }

                // 属性名 索引键入
                modelInfo.javaFieldMap.put(fieldInfo.name, fieldInfo);

                // 数据库字段名 索引键入
                modelInfo.columnFieldMap.put(fieldInfo.columnName, fieldInfo);

                // 属性名 可新增的字段 索引键入
                Column column = fieldInfo.column;
                if (column == null || column.insertable()) {
                    fieldInfo.insertable = true;
                    modelInfo.javaFieldInsertMap.put(fieldInfo.name, fieldInfo);
                }

                // 属性名 可更新的字段 索引键入
                if (column == null || column.updatable()) {
                    fieldInfo.updatable = true;
                    modelInfo.javaFieldUpdateMap.put(fieldInfo.name, fieldInfo);
                }

                // 属性名 可 null
                if (column == null || !column.nullable()) {
                    fieldInfo.nullable = false;
                }
            }
        }
    }

    /**
     * 补充关系字段信息
     */
    protected static void relationFieldDeal() {
        for (Map.Entry<Class<? extends Model<?, ?>>, ModelInfo<?, ?>> entry : modelIndexMap.entrySet()) {
            relationFieldDeal(entry.getValue());
        }
    }

    /**
     * 补充关系字段信息
     * @param modelInfo Model信息
     * @param <T>       实体类
     * @param <K>       主键类型
     */
    protected static <T, K> void relationFieldDeal(ModelInfo<T, K> modelInfo) {

        Class<T> entityClass = modelInfo.entityClass;

        List<Field> fields = EntityUtil.getDeclaredFieldsContainParent(entityClass);

//        Field[] fields = entityClass.getDeclaredFields();

        for (Field field : fields) {
            // 关联关系
            if (effectiveRelationField(field)) {
                field.setAccessible(true);

                // 对象实例
                RelationFieldInfo relationFieldInfo = new RelationFieldInfo();
                relationFieldInfo.field = field;
                relationFieldInfo.name = field.getName();
                relationFieldInfo.javaType = field.getType();
                relationFieldInfo.collection = ObjectUtil.isCollection(field.getType());

                relationFieldInfo.javaRealType = relationFieldInfo.collection ? ObjectUtil.getGenerics((ParameterizedType) field.getGenericType(), 0)
                    : relationFieldInfo.javaType;

                if (field.isAnnotationPresent(BelongsTo.class)) {
                    relationFieldInfo.relationSubQuery = new BelongsToQueryRelation(field);
                } else if (field.isAnnotationPresent(BelongsToMany.class)) {
                    relationFieldInfo.relationSubQuery = new BelongsToManyQueryRelation(field);
                } else if (field.isAnnotationPresent(HasOneOrMany.class)) {
                    relationFieldInfo.relationSubQuery = new HasOneOrManyQueryRelation(field);
                } else {
                    continue;
                }
                // 关联关系记录
                modelInfo.relationFieldMap.put(relationFieldInfo.name, relationFieldInfo);
            }
        }
    }

    /**
     * 是否有效的关联关系字段
     * @param field 字段
     * @return yes/no
     */
    protected static boolean effectiveRelationField(Field field) {
        // 非静态类型
        boolean isNotStatic = !EntityUtil.isStaticField(field);
        // 非基础类型
        boolean isNotBasicType = !EntityUtil.isBasicField(field);
        // 有相应的注解
        boolean hasRelationAnnotation = false;

        for (Class<? extends Annotation> relationAnnotation : FinalVariable.relationAnnotations) {
            if (field.isAnnotationPresent(relationAnnotation)) {
                hasRelationAnnotation = true;
                break;
            }
        }
        return isNotStatic && isNotBasicType && hasRelationAnnotation;
    }

    /**
     * 格式化后的Model信息
     */
    @Data
    public static class ModelInfo<T, K> {

        /**
         * model类
         */
        protected volatile Class<? extends Model<T, K>> modelClass;

        /**
         * model对象
         */
        protected volatile Model<T, K> model;

        /**
         * entity类
         */
        protected volatile Class<T> entityClass;

        /**
         * 主键信息是否存在
         */
        private volatile boolean primaryKeyDefinition = false;

        /**
         * 主键列名(并非一定是实体的属性名)
         */
        @Nullable
        protected volatile String primaryKeyColumnName;

        /**
         * 主键名(实体的属性名)
         */
        @Nullable
        protected volatile String primaryKeyName;

        /**
         * 主键自动生成
         */
        @Nullable
        protected volatile IdGenerator<K> primaryKeyIdGenerator;

        /**
         * 主键自增
         */
        @Nullable
        protected volatile Boolean primaryKeyIncrement;

        /**
         * 主键信息
         */
        @Nullable
        protected volatile FieldInfo primaryKeyFieldInfo;

        /**
         * 主键类型
         */
        protected volatile Class<K> primaryKeyClass;

        /**
         * 数据库表名
         */
        protected volatile String tableName;

        /**
         * `属性名`对应的`普通`字段数组
         */
        protected volatile Map<String, FieldInfo> javaFieldMap = new LinkedHashMap<>();

        /**
         * `数据库字段`名对应的`普通`字段数组
         */
        protected volatile Map<String, FieldInfo> columnFieldMap = new LinkedHashMap<>();

        /**
         * `属性名`名对应的`普通`字段数组, 可新增的字段
         */
        protected volatile Map<String, FieldInfo> javaFieldInsertMap = new LinkedHashMap<>();

        /**
         * `属性名`名对应的`普通`字段数组, 可更新的字段
         */
        protected volatile Map<String, FieldInfo> javaFieldUpdateMap = new LinkedHashMap<>();

        /**
         * `属性名`对应的`关系`字段数组
         */
        protected volatile Map<String, RelationFieldInfo> relationFieldMap = new LinkedHashMap<>();

    }

    /**
     * 字段信息
     */
    @Data
    public static class FieldInfo {

        /**
         * Field
         */
        protected volatile Field field;

        /**
         * 属性名
         */
        protected volatile String name;

        /**
         * 字段名
         */
        protected volatile String columnName;

        /**
         * 可新增
         */
        protected volatile boolean insertable = false;

        /**
         * 可更新
         */
        protected volatile boolean updatable = false;

        /**
         * 可 null
         */
        protected volatile boolean nullable = true;

        /**
         * java中的字段类型
         */
        protected volatile Class<?> javaType;

        /**
         * 默认值
         */
        protected volatile String defaultValue;

        /**
         * column 注解
         */
        @Nullable
        protected volatile Column column;
    }


    /**
     * 字段信息
     */
    @Data
    public static class RelationFieldInfo {

        /**
         * 是否是集合
         */
        protected volatile boolean collection;

        /**
         * Field
         */
        protected volatile Field field;

        /**
         * 属性名
         */
        protected volatile String name;

        /**
         * java中的字段类型
         */
        protected volatile Class<?> javaType;

        /**
         * java中的字段类型
         * 当本类型是非集合时, 此处等价于 javaType
         * 当本类型是集合时, 为集合中的泛型(不支持MAP等多泛型的)
         */
        protected volatile Class<?> javaRealType;

        /**
         * 关联关系注解
         */
        protected volatile RelationSubQuery relationSubQuery;
    }

    @Data
    public static class IdGenerators {

        /**
         * 雪花id生成器
         */
        protected final IdGenerator.SnowFlakesID snowFlakesID;

        /**
         * uuid32位生成器
         */
        protected final IdGenerator.UUID32 uuid32;

        /**
         * uuid36位生成器
         */
        protected final IdGenerator.UUID36 uuid36;

        /**
         * 无生成
         */
        protected final IdGenerator.Never never;

        /**
         * 无生成
         */
        protected final IdGenerator.Custom custom;
    }

}
