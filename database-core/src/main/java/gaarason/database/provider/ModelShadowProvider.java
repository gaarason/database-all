package gaarason.database.provider;

import gaarason.database.annotation.*;
import gaarason.database.appointment.FinalVariable;
import gaarason.database.appointment.LambdaInfo;
import gaarason.database.config.ConversionConfig;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.contract.support.IdGenerator;
import gaarason.database.contract.support.ReflectionScan;
import gaarason.database.eloquent.relation.BelongsToManyQueryRelation;
import gaarason.database.eloquent.relation.BelongsToQueryRelation;
import gaarason.database.eloquent.relation.HasOneOrManyQueryRelation;
import gaarason.database.exception.*;
import gaarason.database.lang.Nullable;
import gaarason.database.support.SoftCache;
import gaarason.database.util.EntityUtils;
import gaarason.database.util.LambdaUtils;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static gaarason.database.util.EntityUtils.columnName;

/**
 * Model信息大全
 * @author xt
 */
public final class ModelShadowProvider {

    /**
     * Model Class做为索引
     */
    private static final Map<Class<? extends Model<? extends Serializable, ? extends Serializable>>, ModelInfo<? extends Serializable, ? extends Serializable>> MODEL_INDEX_MAP = new ConcurrentHashMap<>();

    /**
     * Model proxy Class做为索引
     */
    private static final Map<Class<?>, ModelInfo<? extends Serializable, ? extends Serializable>> MODEL_PROXY_INDEX_MAP = new ConcurrentHashMap<>();

    /**
     * Entity Class作为索引
     */
    private static final Map<Class<? extends Serializable>, ModelInfo<? extends Serializable, ? extends Serializable>> ENTITY_INDEX_MAP = new ConcurrentHashMap<>();

    /**
     * table name 作为索引
     */
    private static final Map<String, ModelInfo<? extends Serializable, ? extends Serializable>> TABLE_NAME_INDEX_MAP = new ConcurrentHashMap<>();

    /**
     * 缓存lambda风格的列名, 与为String风格的列名的映射
     */
    private static final SoftCache<Class<?>, String> LAMBDA_COLUMN_NAME_CACHE = new SoftCache<>();

    /**
     * 缓存lambda风格的属性名, 与为String风格的属性名的映射
     */
    private static final SoftCache<Class<?>, String> LAMBDA_FIELD_NAME_CACHE = new SoftCache<>();

    static {
        // 一轮初始化模型的基本信息(主键类型/实体类型/模型类型/表名等等), 并构建索引(实体索引/模型索引), 不存在依赖递归等复杂情况
        // 并过滤不需要的model, 比如抽象类等
        initModelInformation();

        // 二轮补充基础字段信息
        // Model实例化存储
        primitiveFieldDeal();

        // 三轮补充关联关系字段信息
        // RelationSubQuery实例化存储, 依赖第二轮结果
        relationFieldDeal();
    }

    private ModelShadowProvider() {

    }

    /**
     * 查询Model信息
     * @param model 模型
     * @param <T> 实体类
     * @param <K> 主键类型
     * @return 格式化后的Model信息
     */
    public static <T extends Serializable, K extends Serializable> ModelInfo<T, K> get(Model<T, K> model) {
        return getByModelClass(ObjectUtils.typeCast(model.getClass()));
    }

    /**
     * 查询Model信息
     * @param modelClass 模型类
     * @param <T> 实体类
     * @param <K> 主键类型
     * @return 格式化后的Model信息
     */
    public static <T extends Serializable, K extends Serializable> ModelInfo<T, K> getByModelClass(
        Class<? extends Model<T, K>> modelClass) {
        ModelInfo<?, ?> result1 = MODEL_PROXY_INDEX_MAP.get(modelClass);
        if (null == result1) {
            ModelInfo<?, ?> result2 = MODEL_INDEX_MAP.get(modelClass);
            if (null == result2) {
                throw new ModelInvalidException(modelClass);
            }
            return ObjectUtils.typeCast(result2);
        }
        return ObjectUtils.typeCast(result1);
    }

    /**
     * 查询Model信息
     * @param clazz 实体类(可查找泛型)
     * @return 格式化后的Model信息
     */
    public static <T extends Serializable> ModelInfo<T, Serializable> getByEntityClass(Class<T> clazz) {
        ModelInfo<? extends Serializable, ? extends Serializable> result = ENTITY_INDEX_MAP.get(clazz);
        if (null == result) {
            throw new EntityInvalidException(clazz);
        }
        return ObjectUtils.typeCast(result);
    }

    /**
     * 查询Model信息
     * @param tableName 数据表名
     * @return 格式化后的Model信息
     */
    public static <T extends Serializable> ModelInfo<T, Serializable> getByTableName(String tableName) {
        ModelInfo<? extends Serializable, ? extends Serializable> result = TABLE_NAME_INDEX_MAP.get(tableName);
        if (null == result) {
            throw new TableInvalidException(tableName);
        }
        return ObjectUtils.typeCast(result);
    }

    /**
     * 将lambda风格的属性名, 解析为String类型
     * 这个过程使用缓存进行加速, 5-10倍
     * @param func lambda风格的属性名
     * @param <T> 实体类型
     * @return 列名
     */
    public static <T extends Serializable> String getFieldNameByLambdaWithCache(ColumnFunctionalInterface<T> func) {
        Class<?> clazz = func.getClass();
        String fieldName =  LAMBDA_FIELD_NAME_CACHE.get(clazz);
        if (fieldName == null) {
            synchronized (LAMBDA_FIELD_NAME_CACHE) {
                fieldName =  LAMBDA_FIELD_NAME_CACHE.get(clazz);
                if (fieldName == null) {
                    // 解析 lambda
                    LambdaInfo<T> lambdaInfo = LambdaUtils.parse(func);
                    // 属性名
                    fieldName = lambdaInfo.getFieldName();
                    // 常量化后, 加入缓存
                    LAMBDA_FIELD_NAME_CACHE.put(clazz, fieldName.intern());
                }
            }
        }
        return fieldName;
    }

    /**
     * 将lambda风格的列名, 解析为String类型, 会检测是否是有效的列名
     * 这个过程使用缓存进行加速, 5-10倍
     * @param func lambda风格的列名
     * @param <T> 实体类型
     * @return 列名
     */
    public static <T extends Serializable> String getColumnNameByLambdaWithCache(ColumnFunctionalInterface<T> func) {
        Class<?> clazz = func.getClass();
        String columnName =LAMBDA_COLUMN_NAME_CACHE.get(clazz);
        if (columnName == null) {
            synchronized (LAMBDA_COLUMN_NAME_CACHE) {
                columnName = LAMBDA_COLUMN_NAME_CACHE.get(clazz);
                if (columnName == null) {
                    // 解析 lambda
                    columnName = parseLambda(func);
                    // 常量化后, 加入缓存
                    LAMBDA_COLUMN_NAME_CACHE.put(clazz, columnName.intern());
                }
            }
        }
        return columnName;
    }

    /**
     * 解析lambda
     * @param func lambda风格
     * @param <T> 实体类型
     * @return FieldInfo
     */
    private static <T extends Serializable> String parseLambda(ColumnFunctionalInterface<T> func) {
        // 解析 lambda
        LambdaInfo<T> lambdaInfo = LambdaUtils.parse(func);
        // 实例类
        Class<T> entityClass = lambdaInfo.getEntityCLass();
        // 属性名
        String fieldNameLocal = lambdaInfo.getFieldName();
        // Model信息
        ModelInfo<? extends Serializable, ? extends Serializable> modelInfo = ENTITY_INDEX_MAP.get(entityClass);

        if(ObjectUtils.isEmpty(modelInfo)){
            return lambdaInfo.getColumnName();
        }
        // 字段信息
        FieldInfo fieldInfo = modelInfo.javaFieldMap.get(fieldNameLocal);
        // 优先使用缓存的字段信息
        return ObjectUtils.isEmpty(fieldInfo) ? lambdaInfo.getColumnName() : fieldInfo.columnName;
    }

    /**
     * 查询entity 中的指定字段信息
     * @param clazz 实体类(可查找泛型)
     * @param fieldName 实体中的属性
     * @param <T> 实体类型
     * @return 字段信息
     * @throws EntityAttributeInvalidException 无效的字段
     */
    public static <T extends Serializable> FieldInfo getFieldInfoByEntityClass(Class<T> clazz, String fieldName)
        throws EntityAttributeInvalidException, EntityInvalidException {
        final ModelInfo<T, Serializable> modelInfo = getByEntityClass(clazz);
        final FieldInfo fieldInfo = modelInfo.getJavaFieldMap().get(fieldName);
        if (ObjectUtils.isEmpty(fieldInfo)) {
            throw new EntityAttributeInvalidException(fieldName, clazz);
        }
        return fieldInfo;
    }

    /**
     * 通过entity解析对应的字段和值组成的map, 忽略不符合规则的字段
     * @param entity 数据表实体对象
     * @param <T> 数据表实体类
     * @return 字段对值的映射
     */
    public static <T extends Serializable> Map<String, Object> columnValueMap(@Nullable T entity) {
        // 结果集
        Map<String, Object> columnValueMap = new HashMap<>();
        if (ObjectUtils.isNull(entity)) {
            return columnValueMap;
        }
        // 属性信息集合
        Map<String, FieldInfo> columnFieldMap = getByEntityClass(entity.getClass()).getColumnFieldMap();
        for (Map.Entry<String, FieldInfo> entry : columnFieldMap.entrySet()) {
            // 属性信息
            FieldInfo fieldInfo = entry.getValue();
            // 值
            Object value = fieldGet(fieldInfo, entity);
            // 有效则加入 结果集
            if (effectiveField(fieldInfo, value)) {
                columnValueMap.put(entry.getKey(), EntityUtils.valueFormat(value));
            }
        }
        return columnValueMap;
    }

    /**
     * 通过entity解析对应的字段和值组成的map, 忽略不符合规则的字段
     * @param entity 数据表实体对象
     * @param insertType 新增?
     * @param <T> 数据表实体类
     * @return 字段对值的映射
     */
    public static <T extends Serializable> Map<String, Object> columnValueMap(@Nullable T entity, boolean insertType) {
        // 结果集
        Map<String, Object> columnValueMap = new HashMap<>();
        if (ObjectUtils.isNull(entity)) {
            return columnValueMap;
        }
        // 属性信息集合
        Map<String, FieldInfo> columnFieldMap = getByEntityClass(entity.getClass()).getColumnFieldMap();
        for (Map.Entry<String, FieldInfo> entry : columnFieldMap.entrySet()) {
            // 属性信息
            FieldInfo fieldInfo = entry.getValue();
            // 值
            Object value = fieldGet(fieldInfo, entity);
            // 有效则加入 结果集
            if (effectiveField(fieldInfo, value, insertType)) {
                columnValueMap.put(entry.getKey(), EntityUtils.valueFormat(value));
            }
        }
        return columnValueMap;
    }

    /**
     * 通过entity解析对应的字段组成的list
     * 忽略不符合规则的字段
     * @param entity 数据表实体对象
     * @param <T> 数据表实体类
     * @param insertType 新增?
     * @return 列名组成的list
     */
    public static <T extends Serializable> List<String> columnNameList(T entity, boolean insertType) {
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
     * 通过entity解析对应的字段组成的set
     * 忽略不符合规则的字段
     * @param entity 数据表实体对象
     * @param <T> 数据表实体类
     * @param insertType 新增?
     * @return 列名组成的set
     */
    public static <T extends Serializable> Set<String> columnNameSet(T entity, boolean insertType) {
        final List<String> columnNameList = columnNameList(entity, insertType);
        return new LinkedHashSet<>(columnNameList);
    }

    /**
     * 通过entity解析对应的字段的值组成的list, 忽略不符合规则的字段
     * @param entity 数据表实体对象
     * @param <T> 数据表实体类
     * @param columnNames 有效的属性名
     * @return 字段的值组成的list
     */
    public static <T extends Serializable> List<Object> valueList(T entity, Collection<String> columnNames) {
        // 结果集
        List<Object> valueList = new ArrayList<>();
        // 属性信息集合
        Map<String, FieldInfo> columnFieldMap = getByEntityClass(entity.getClass()).getColumnFieldMap();
        for (Map.Entry<String, FieldInfo> entry : columnFieldMap.entrySet()) {
            // 属性信息
            FieldInfo fieldInfo = entry.getValue();
            // 加入需要的数据
            if (columnNames.contains(entry.getKey())) {
                valueList.add(EntityUtils.valueFormat(fieldGet(fieldInfo, entity)));
            }
        }
        return valueList;
    }

    /**
     * 将数据库查询结果赋值给entity的field
     * 需要 field.setAccessible(true)
     * @param fieldInfo 字段信息
     * @param stringColumnMap 元数据map
     * @param entity 数据表实体对象
     */
    public static <T extends Serializable, K extends Serializable> void fieldAssignment(FieldInfo fieldInfo,
                                                                                        Map<String, gaarason.database.appointment.Column> stringColumnMap,
                                                                                        T entity,
                                                                                        Record<T, K> theRecord)
        throws TypeNotSupportedException {
        gaarason.database.appointment.Column column = stringColumnMap.get(fieldInfo.columnName);
        if (column != null) {
            try {
                // 属性赋值
                Object value = column.getValue();
                fieldInfo.field.set(entity, value);
                // 主键值记录
                if (fieldInfo.field.isAnnotationPresent(Primary.class) && value != null) {
                    theRecord.setOriginalPrimaryKeyValue(ObjectUtils.typeCast(value));
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new TypeNotSupportedException(e.getMessage(), e);
            }

        }
    }

    /**
     * 设置 entity 对象的自增属性值
     * @param <T> 数据表实体类
     * @param <K> 数据表主键类型
     * @param entity 数据表实体对象
     * @param id 数据库生成的id
     * @throws IllegalAccessRuntimeException 反射赋值异常
     */
    public static <T extends Serializable, K extends Serializable> void setPrimaryKeyValue(T entity, @Nullable K id) {
        // 属性信息集合
        FieldInfo primaryKeyFieldInfo = getByEntityClass(entity.getClass()).getPrimaryKeyFieldInfo();
        if (null != primaryKeyFieldInfo) {
            fieldSet(primaryKeyFieldInfo, entity, id);
        }
    }

    /**
     * 获取 entity 对象的主键值
     * @param <T> 数据表实体类
     * @param <K> 数据表主键类型
     * @param entity 数据表实体对象
     * @throws IllegalAccessRuntimeException 反射赋值异常
     */
    @Nullable
    public static <T extends Serializable, K extends Serializable> K getPrimaryKeyValue(T entity) {
        // 属性信息集合
        FieldInfo primaryKeyFieldInfo = getByEntityClass(entity.getClass()).getPrimaryKeyFieldInfo();
        if (null != primaryKeyFieldInfo) {
            Object value = fieldGet(primaryKeyFieldInfo, entity);
            if (value != null) {
                return ObjectUtils.typeCast(value);
            }
        }
        return null;
    }

    /**
     * 获取属性的值
     * @param fieldInfo 属性信息
     * @param obj 对象
     * @return 值
     * @throws IllegalAccessRuntimeException 反射取值异常
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
     * @param fieldInfo 属性信息
     * @param obj 对象
     * @param value 值
     * @throws IllegalAccessRuntimeException 反射赋值异常
     */
    public static void fieldSet(FieldInfo fieldInfo, Object obj, @Nullable Object value) {
        try {
            fieldInfo.getField().set(obj, value);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException(e);
        }
    }

    /**
     * 是否有效字段
     * @param fieldInfo 字段
     * @param value 字段值
     * @return 有效
     */
    private static boolean effectiveField(FieldInfo fieldInfo, @Nullable Object value) {
        return fieldInfo.nullable || value != null;
    }

    /**
     * 是否有效字段
     * @param fieldInfo 字段
     * @param value 字段值
     * @param insertType 是否是新增,会通过字段上的注解column(insertable, updatable)进行忽略
     * @return 有效
     */
    private static boolean effectiveField(FieldInfo fieldInfo, @Nullable Object value, boolean insertType) {
        // 不可插入 or 不可更新
        if (insertType ? !fieldInfo.insertable : !fieldInfo.updatable) {
            return false;
        }

        return fieldInfo.nullable || value != null;
    }

    /**
     * 初始化模型的基本信息(主键类型/实体类型/模型类型/表名等等), 并构建索引(实体索引/模型索引)
     */
    private static void initModelInformation() {
        // 通过静态扫描, 获取所有 Model 的子类 (含抽象类等)
        Set<Class<? extends Model<?, ?>>> modelClasses = ContainerProvider.getBean(ReflectionScan.class).scanModels();

        // 所有 Model 的子类 (含抽象类等)进行初始化分析
        for (Class<? extends Model<?, ?>> modelClass : modelClasses) {
            // 初始化模型的基本信息, 并构建索引
            initModelInformation(ObjectUtils.typeCast(modelClass));
        }
    }

    /**
     * 初始化模型的基本信息, 并构建索引
     * @param modelClass 模型类
     * @param <T> 实体类
     * @param <K> 主键类型
     */
    private static <T extends Serializable, K extends Serializable> void initModelInformation(
        Class<? extends Model<T, K>> modelClass) {
        // 模型信息 初始化
        ModelInfo<T, K> modelInfo = new ModelInfo<>();
        // 模型信息 设置 model 的 java类
        modelInfo.modelClass = modelClass;
        try {
            // 模型信息 设置详情信息
            modelDeal(modelInfo);
        } catch (Throwable e) {
            // 父类, 抽象类跳过
            return;
        }
        // 建立实体类索引 (建立后, 可支持通过entity查询)
        ENTITY_INDEX_MAP.put(modelInfo.entityClass, modelInfo);
        // 建立模型类索引 (建立后, 可支持通过model查询)
        MODEL_INDEX_MAP.put(modelClass, modelInfo);
        // 建立表名索引 (建立后, 可支持通过tableName查询)
        TABLE_NAME_INDEX_MAP.put(modelInfo.tableName, modelInfo);
    }

    /**
     * 模型信息 设置详情信息
     * @param modelInfo Model信息
     * @param <T> 实体类
     * @param <K> 主键类型
     */
    private static <T extends Serializable, K extends Serializable> void modelDeal(ModelInfo<T, K> modelInfo) {
        // 设置实体的java类
        modelInfo.entityClass = ObjectUtils.getGenerics(modelInfo.modelClass, 0);
        // 设置主键的java类
        modelInfo.primaryKeyClass = ObjectUtils.getGenerics(modelInfo.modelClass, 1);
        // 设置表名
        modelInfo.tableName = EntityUtils.tableName(modelInfo.entityClass);
    }

    /**
     * 补充基本字段信息
     */
    private static void primitiveFieldDeal() {
        // 处理所有模型
        for (Map.Entry<Class<? extends Model<?, ?>>, ModelInfo<?, ?>> entry : MODEL_INDEX_MAP.entrySet()) {
            ModelInfo<?, ?> modelInfo = entry.getValue();
            // 单个模型处理
            primitiveFieldDeal(modelInfo);
        }
    }

    /**
     * 补充基本字段信息
     * @param modelInfo Model信息
     * @param <T> 实体类
     * @param <K> 主键类型
     */
    private static <T extends Serializable, K extends Serializable> void primitiveFieldDeal(ModelInfo<T, K> modelInfo) {
        // 实体类型
        Class<T> entityClass = modelInfo.entityClass;

        // 获取模型对象 (是否是单例, 仅取决于Model实例化工厂), 但是缓存之后就是单例的了~
        modelInfo.model = ModelInstanceProvider.getModel(modelInfo.modelClass);

        // 模型代理索引建立
        MODEL_PROXY_INDEX_MAP.put(modelInfo.model.getClass(), modelInfo);

        // 返回 实体 中的所有属性(public/protected/private)包含父类的
        List<Field> fields = EntityUtils.getDeclaredFieldsContainParent(entityClass);

        for (Field field : fields) {
            // 跳过(静态属性 or 非基本类型(由关联关系去处理))
            if (EntityUtils.isStaticField(field) || !EntityUtils.isBasicField(field)) {
                continue;
            }

            // 设置属性是可访问
            field.setAccessible(true);

            // 对象实例
            FieldInfo fieldInfo = new FieldInfo();
            fieldInfo.field = field;
            fieldInfo.name = field.getName();
            fieldInfo.javaType = field.getType();
            // todo 应该优先使用数据库默认值 DatabaseShadowProvider , 当默认值不存在时, 再才使用如下方法
            fieldInfo.defaultValue = ContainerProvider.getBean(ConversionConfig.class)
                .getDefaultValueByJavaType(fieldInfo.javaType);

            // 数据库属性
            fieldInfo.column = field.isAnnotationPresent(Column.class) ? field.getAnnotation(Column.class) : null;

            // 数据库列名
            fieldInfo.columnName = columnName(field);

            // 处理主键@Primary信息
            dealPrimaryAnnotation(fieldInfo, modelInfo);

            // 处理列@Column信息
            dealColumnAnnotation(fieldInfo, modelInfo);

            // 属性名 索引键入
            modelInfo.javaFieldMap.put(fieldInfo.name, fieldInfo);

            // 数据库字段名 索引键入
            modelInfo.columnFieldMap.put(fieldInfo.columnName, fieldInfo);
        }
    }


    /**
     * 处理主键@Primary信息
     * @param fieldInfo 字段信息
     * @param modelInfo 格式化后的Model信息
     * @param <T> 实体类
     * @param <K> 主键类型
     */
    private static <T extends Serializable, K extends Serializable> void dealPrimaryAnnotation(FieldInfo fieldInfo,
                                                                                               ModelInfo<T, K> modelInfo) {
        // 没有注解的就不是主键
        if (!fieldInfo.field.isAnnotationPresent(Primary.class)) {
            return;
        }

        Primary primary = fieldInfo.field.getAnnotation(Primary.class);

        // 主键 索引键入
        modelInfo.primaryKeyDefinition = true;
        modelInfo.primaryKeyFieldInfo = fieldInfo;
        modelInfo.primaryKeyIncrement = primary.increment();
        modelInfo.primaryKeyColumnName = fieldInfo.columnName;
        modelInfo.primaryKeyName = fieldInfo.field.getName();

        // 主键类型检测( 实体上的主键的类型是否与模型上的泛型一致 )
        if (!modelInfo.primaryKeyClass.equals(fieldInfo.field.getType())) {
            throw new InvalidPrimaryKeyTypeException(
                "The primary key type [" + fieldInfo.field.getType() + "] of the entity does not match with the " +
                    "generic [" + modelInfo.primaryKeyClass + "]");
        }

        // 主键auto生成器选择
        if (primary.idGenerator().isAssignableFrom(IdGenerator.Auto.class)) {
            if (fieldInfo.javaType == Long.class) {
                modelInfo.primaryKeyIdGenerator = ObjectUtils.typeCast(
                    ContainerProvider.getBean(IdGenerator.SnowFlakesID.class));
            } else if (fieldInfo.javaType == String.class) {
                if (fieldInfo.column != null) {
                    if (fieldInfo.column.length() >= 36) {
                        modelInfo.primaryKeyIdGenerator = ObjectUtils.typeCast(
                            ContainerProvider.getBean(IdGenerator.UUID36.class));
                    } else if (fieldInfo.column.length() >= 32) {
                        modelInfo.primaryKeyIdGenerator = ObjectUtils.typeCast(
                            ContainerProvider.getBean(IdGenerator.UUID32.class));
                    }
                } else {
                    modelInfo.primaryKeyIdGenerator = ObjectUtils.typeCast(
                        ContainerProvider.getBean(IdGenerator.UUID32.class));
                }
            } else {
                modelInfo.primaryKeyIdGenerator = ObjectUtils.typeCast(
                    ContainerProvider.getBean(IdGenerator.Never.class));
            }
        } else {
            modelInfo.primaryKeyIdGenerator = ObjectUtils.typeCast(ContainerProvider.getBean(primary.idGenerator()));
        }
    }

    /**
     * 处理列@Column信息
     * @param fieldInfo 字段信息
     * @param modelInfo 格式化后的Model信息
     * @param <T> 实体类
     * @param <K> 主键类型
     */
    private static <T extends Serializable, K extends Serializable> void dealColumnAnnotation(FieldInfo fieldInfo,
                                                                                              ModelInfo<T, K> modelInfo) {
        Column column = fieldInfo.column;

        // 属性名 可新增的字段 索引键入
        if (column != null && !column.insertable()) {
            fieldInfo.insertable = false;
        } else {
            modelInfo.javaFieldInsertMap.put(fieldInfo.name, fieldInfo);
        }

        // 属性名 可更新的字段 索引键入
        if (column != null && !column.updatable()) {
            fieldInfo.updatable = false;
        } else {
            modelInfo.javaFieldUpdateMap.put(fieldInfo.name, fieldInfo);
        }

        // 属性名 可 null
        if (column != null && column.nullable()) {
            fieldInfo.nullable = true;
        }
    }

    /**
     * 补充关系字段信息
     */
    private static void relationFieldDeal() {
        for (Map.Entry<Class<? extends Model<?, ?>>, ModelInfo<?, ?>> entry : MODEL_INDEX_MAP.entrySet()) {
            relationFieldDeal(entry.getValue());
        }
    }

    /**
     * 补充关系字段信息
     * @param modelInfo Model信息
     * @param <T> 实体类
     * @param <K> 主键类型
     */
    private static <T extends Serializable, K extends Serializable> void relationFieldDeal(ModelInfo<T, K> modelInfo) {

        Class<T> entityClass = modelInfo.entityClass;

        List<Field> fields = EntityUtils.getDeclaredFieldsContainParent(entityClass);

        for (Field field : fields) {
            // 关联关系
            if (effectiveRelationField(field)) {
                field.setAccessible(true);

                // 对象实例
                RelationFieldInfo relationFieldInfo = new RelationFieldInfo();
                relationFieldInfo.field = field;
                relationFieldInfo.name = field.getName();
                relationFieldInfo.javaType = field.getType();
                relationFieldInfo.collection = ObjectUtils.isCollection(field.getType());

                relationFieldInfo.javaRealType = relationFieldInfo.collection ?
                    ObjectUtils.getGenerics((ParameterizedType) field.getGenericType(), 0) : relationFieldInfo.javaType;

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
    private static boolean effectiveRelationField(Field field) {
        // 非静态类型
        boolean isNotStatic = !EntityUtils.isStaticField(field);
        // 非基础类型
        boolean isNotBasicType = !EntityUtils.isBasicField(field);
        // 有相应的注解
        boolean hasRelationAnnotation = false;

        for (Class<? extends Annotation> relationAnnotation : FinalVariable.RELATION_ANNOTATIONS) {
            if (field.isAnnotationPresent(relationAnnotation)) {
                hasRelationAnnotation = true;
                break;
            }
        }
        return isNotStatic && isNotBasicType && hasRelationAnnotation;
    }


}
