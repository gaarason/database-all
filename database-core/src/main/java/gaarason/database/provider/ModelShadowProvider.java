package gaarason.database.provider;

import gaarason.database.appointment.Column;
import gaarason.database.appointment.EntityUseType;
import gaarason.database.appointment.LambdaInfo;
import gaarason.database.bootstrap.ContainerBootstrap;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.core.Container;
import gaarason.database.exception.EntityAttributeInvalidException;
import gaarason.database.exception.EntityInvalidException;
import gaarason.database.exception.IllegalAccessRuntimeException;
import gaarason.database.exception.ModelInvalidException;
import gaarason.database.lang.Nullable;
import gaarason.database.logging.Log;
import gaarason.database.logging.LogFactory;
import gaarason.database.support.*;
import gaarason.database.util.LambdaUtils;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Model信息大全
 * @author xt
 */
public class ModelShadowProvider extends Container.SimpleKeeper {

    private static final Log LOGGER = LogFactory.getLog(ModelShadowProvider.class);

    /**
     * 持久信息
     * 需要手动初始化
     */
    static class Persistence {
        /**
         * Model Class做为索引
         */
        private final Map<Class<? extends Model<?, ?>>, ModelMember<?, ?>> modelIndexMap = new ConcurrentHashMap<>();

        /**
         * Model proxy Class做为索引
         */
        private final Map<Class<?>, ModelMember<?, ?>> modelProxyIndexMap = new ConcurrentHashMap<>();

        /**
         * Entity Class作为索引
         */
        private final Map<Class<?>, ModelMember<?, ?>> entityIndexMap = new ConcurrentHashMap<>();
    }

    /**
     * 缓存信息
     * 内存不足时自动清理. 因此需要惰性使用
     */
    static class Cache {

        /**
         * Entity 缓存
         */
        private final SoftCache<Class<?>, EntityMember<?>> entity = new SoftCache<>();

        /**
         * 缓存lambda风格的列名, 与为String风格的列名的映射
         */
        private final SoftCache<Class<?>, String> lambdaColumnName = new SoftCache<>();

        /**
         * 缓存lambda风格的属性名, 与为String风格的属性名的映射
         */
        private final SoftCache<Class<?>, String> lambdaFieldName = new SoftCache<>();
    }

    /**
     * 持久信息
     */
    private final Persistence persistence = new Persistence();

    /**
     * 缓存信息
     */
    private final Cache cache = new Cache();


    public ModelShadowProvider(Container container) {
        super(container);
    }

    /**
     * 通过反射等手段, 得到model信息
     * @param modelClasses model类型的集合
     */
    public void loadModels(Collection<Class<? extends Model<Serializable, Serializable>>> modelClasses) {
        synchronized (persistence) {
            // 所有 Model 的子类 (含抽象类等)进行初始化分析
            // 初始化模型的基本信息, 并构建索引
            for (Class<? extends Model<Serializable, Serializable>> modelClass : modelClasses) {
                ModelMember<?, ?> modelMember;
                try {
                    modelMember = new ModelMember<>(container, modelClass);
                } catch (ClassCastException ignore) {
                    // 父类, 抽象类跳过
                    continue;
                }

                persistence.modelIndexMap.put(modelClass, modelMember);
                persistence.modelProxyIndexMap.put(modelMember.getModel().getClass(), modelMember);
                persistence.entityIndexMap.put(modelMember.getEntityClass(), modelMember);
            }
        }
    }

    /**
     * 通过反射等手段, 刷新model信息
     * @param modelClasses model类型的集合
     */
    public void refreshModels(Collection<Class<? extends Model<Serializable, Serializable>>> modelClasses) {
        loadModels(modelClasses);
    }

    /**
     * 卸载model信息
     * @param modelClasses model类型的集合
     */
    public void unloadModels(Collection<Class<? extends Model<Serializable, Serializable>>> modelClasses) {
        synchronized (persistence) {
            for (Class<? extends Model<Serializable, Serializable>> modelClass : modelClasses) {
                ModelMember<?, ?> modelMember = persistence.modelIndexMap.remove(modelClass);
                if (!ObjectUtils.isEmpty(modelMember)) {
                    persistence.entityIndexMap.remove(modelMember.getEntityClass());
                    persistence.modelProxyIndexMap.entrySet().removeIf(entry -> entry.getValue() == modelMember);
                }
            }
        }

    }

    /**
     * 查询Model信息
     * @param model 模型
     * @param <T> 实体类
     * @param <K> 主键类型
     * @return 格式化后的Model信息
     */
    public <T extends Serializable, K extends Serializable> ModelMember<T, K> get(Model<T, K> model) {
        return getByModelClass(ObjectUtils.typeCast(model.getClass()));
    }

    /**
     * 查询Model信息
     * @param modelClass 模型类
     * @param <T> 实体类
     * @param <K> 主键类型
     * @return 格式化后的Model信息
     */
    public <T extends Serializable, K extends Serializable> ModelMember<T, K> getByModelClass(
        Class<? extends Model<T, K>> modelClass) {
        ModelMember<?, ?> result1 = persistence.modelProxyIndexMap.get(modelClass);
        if (null == result1) {
            ModelMember<?, ?> result2 = persistence.modelIndexMap.get(modelClass);
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
    public <T> ModelMember<? super T, ?> getByEntityClass(Class<T> clazz) {
        ModelMember<? extends Serializable, ? extends Serializable> result = persistence.entityIndexMap.get(clazz);
        if (null == result) {
            throw new EntityInvalidException(clazz);
        }
        return ObjectUtils.typeCast(result);
    }

    /**
     * 解析实体类
     * @param anyEntityClass 任意实体类(可查找泛型)
     * @return 格式化后的Entity信息
     */
    public <T> EntityMember<T> parseAnyEntityWithCache(Class<T> anyEntityClass) {
        EntityMember<?> entityMember = cache.entity.get(anyEntityClass);
        if (entityMember == null) {
            synchronized (cache.entity) {
                entityMember = cache.entity.get(anyEntityClass);
                if (entityMember == null) {
                    // 优先使用 持久化的信息
                    ModelMember<?, ?> modelMember = persistence.entityIndexMap.get(anyEntityClass);
                    entityMember = null == modelMember ? new EntityMember<>(container, anyEntityClass) :
                        modelMember.getEntityMember();
                    // 更新缓存
                    cache.entity.put(anyEntityClass, entityMember);

                }
            }
        }
        return ObjectUtils.typeCast(entityMember);
    }

    /**
     * 将lambda风格的属性名, 解析为String类型
     * 这个过程使用缓存进行加速, 5-10倍
     * @param func lambda风格的属性名
     * @param <T> 实体类型
     * @return 属性名
     */
    public <T extends Serializable> String parseFieldNameByLambdaWithCache(ColumnFunctionalInterface<T> func) {
        Class<?> clazz = func.getClass();
        String fieldName = cache.lambdaFieldName.get(clazz);
        if (fieldName == null) {
            synchronized (cache.lambdaFieldName) {
                fieldName = cache.lambdaFieldName.get(clazz);
                if (fieldName == null) {
                    // 解析 lambda
                    LambdaInfo<T> lambdaInfo = LambdaUtils.parse(func);
                    // 属性名
                    fieldName = lambdaInfo.getFieldName();
                    // 常量化后, 加入缓存
                    cache.lambdaFieldName.put(clazz, fieldName.intern());
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
    public <T extends Serializable> String parseColumnNameByLambdaWithCache(ColumnFunctionalInterface<T> func) {
        Class<?> clazz = func.getClass();
        String columnName = cache.lambdaColumnName.get(clazz);
        if (columnName == null) {
            synchronized (cache.lambdaColumnName) {
                columnName = cache.lambdaColumnName.get(clazz);
                if (columnName == null) {
                    // 解析 lambda
                    columnName = parseLambda(func);
                    // 常量化后, 加入缓存
                    cache.lambdaColumnName.put(clazz, columnName.intern());
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
    private <T extends Serializable> String parseLambda(ColumnFunctionalInterface<T> func) {
        // 解析 lambda
        LambdaInfo<T> lambdaInfo = LambdaUtils.parse(func);
        // 实例类
        Class<T> entityClass = lambdaInfo.getEntityCLass();
        // 属性名
        String fieldNameLocal = lambdaInfo.getFieldName();
        // 字段信息
        FieldMember fieldMember = getFieldByAnyEntityClass(entityClass, fieldNameLocal);
        // 优先使用缓存的字段信息
        return ObjectUtils.isEmpty(fieldMember) ? lambdaInfo.getColumnName() : fieldMember.getColumnName();
    }

    /**
     * 查询entity 中的指定字段信息
     * @param entityClass 实体类(可查找泛型)
     * @param fieldName 实体中的属性
     * @param <T> 实体类型
     * @return 字段信息
     * @throws EntityAttributeInvalidException 无效的字段
     */
    public <T extends Serializable> FieldMember getFieldByAnyEntityClass(Class<T> entityClass, String fieldName)
        throws EntityAttributeInvalidException, EntityInvalidException {

        // 字段信息
        FieldMember fieldMember = parseAnyEntityWithCache(entityClass).getJavaFieldMap().get(fieldName);

        if (ObjectUtils.isEmpty(fieldMember)) {
            throw new EntityAttributeInvalidException(fieldName, entityClass);
        }
        return fieldMember;
    }

    /**
     * 通过entity解析对应的字段和值组成的map, 忽略不符合规则的字段
     * @param anyEntity 数据表实体对象
     * @param type 实体的使用目的
     * @param <T> 数据表实体类
     * @return 字段对值的映射
     */
    public <T> Map<String, Object> columnValueMap(@Nullable T anyEntity, EntityUseType type) {
        if (ObjectUtils.isNull(anyEntity)) {
            return Collections.emptyMap();
        }
        EntityMember<?> entityMember = parseAnyEntityWithCache(anyEntity.getClass());
        return entityMember.toSimpleMap(ObjectUtils.typeCast(anyEntity), type);
    }

    /**
     * 通过entity解析对应的字段组成的list
     * 忽略不符合规则的字段
     * @param anyEntity 数据表实体对象
     * @param type 实体的使用目的
     * @param <T> 数据表实体类
     * @return 列名组成的list
     */
    public <T extends Serializable> Set<String> columnNameSet(@Nullable T anyEntity, EntityUseType type) {
        if (ObjectUtils.isNull(anyEntity)) {
            return Collections.emptySet();
        }
        return columnValueMap(anyEntity, type).keySet();
    }

    /**
     * 通过entity解析对应的字段的值组成的list, 忽略不符合规则的字段
     * @param anyEntity 数据表实体对象
     * @param <T> 数据表实体类
     * @param columnNames 有效的属性名
     * @return 字段的值组成的list
     */
    public <T extends Serializable> List<Object> valueList(@Nullable T anyEntity, Collection<String> columnNames) {
        if (ObjectUtils.isNull(anyEntity)) {
            return Collections.emptyList();
        }
        EntityMember<?> entityMember = parseAnyEntityWithCache(anyEntity.getClass());
        Map<String, FieldMember> columnFieldMap = entityMember.getColumnFieldMap();

        // 结果集
        List<Object> valueList = new ArrayList<>();

        for (Map.Entry<String, FieldMember> entry : columnFieldMap.entrySet()) {
            // 加入需要的数据
            if (columnNames.contains(entry.getKey())) {
                FieldMember fieldMember = entry.getValue();
                valueList.add(fieldMember.fieldGet(anyEntity));
            }
        }
        return valueList;
    }

//    /**
//     * 格式化值到字符串
//     * 关键
//     * @param value 原值 (实体的属性)
//     * @return 字符串
//     */
//    @Nullable
//    public String valueFormat(@Nullable Object value) {
//        if (value instanceof Date) {
//            return LocalDateUtils.SIMPLE_DATE_FORMAT.get().format(value);
//        } else if (value instanceof Boolean) {
//            return (boolean) value ? "1" : "0";
//        } else {
//            return getContainer().getBean(ConversionConfig.class).castNullable(value, String.class);
//        }
//    }

    /**
     * 将结果集中的原信息, 赋值到实体
     * @param anyEntity 数据表实体对象
     * @param theRecord 查询结果集
     * @param <T> 数据表实体类
     * @param <K> 数据表主键类型
     */
    public <T extends Serializable, K extends Serializable> void entityAssignment(T anyEntity, Record<T, K> theRecord) {
        Map<String, Column> metadataMap = theRecord.getMetadataMap();
        EntityMember<?> entityMember = parseAnyEntityWithCache(anyEntity.getClass());
        // 数据库字段
        for (Map.Entry<String, FieldMember> fieldMemberEntry : entityMember.getColumnFieldMap().entrySet()) {
            FieldMember fieldMember = fieldMemberEntry.getValue();
            Column column = metadataMap.get(fieldMember.getColumnName());
            if (column != null) {
                Object value = column.getValue();
                fieldMember.fieldSet(anyEntity, value);
            }
        }
        // 数据库主键
        PrimaryKeyMember primaryKeyMember = entityMember.getPrimaryKeyMember();
        if (primaryKeyMember != null) {
            Column primaryKeyColumn = metadataMap.get(primaryKeyMember.getFieldMember().getColumnName());
            if (primaryKeyColumn != null) {
                theRecord.setOriginalPrimaryKeyValue(ObjectUtils.typeCast(primaryKeyColumn.getValue()));
            }
        }
    }

    /**
     * 设置 entity 对象的自增属性值
     * @param anyEntity 数据表实体对象
     * @param id 数据库生成的id
     * @param <T> 数据表实体类
     * @param <K> 数据表主键类型
     */
    public <T, K> void setPrimaryKeyValue(T anyEntity, @Nullable K id) {
        // 属性信息集合
        EntityMember<?> entityMember = parseAnyEntityWithCache(anyEntity.getClass());

        PrimaryKeyMember primaryKeyMember = entityMember.getPrimaryKeyMember();
        if (null != primaryKeyMember) {
            primaryKeyMember.getFieldMember().fieldSet(anyEntity, id);
        }
    }

    /**
     * 获取 entity 对象的主键值
     * @param anyEntity 数据表实体对象
     * @param <T> 数据表实体类
     * @param <K> 数据表主键类型
     * @throws IllegalAccessRuntimeException 反射赋值异常
     */
    @Nullable
    public <T, K> K getPrimaryKeyValue(T anyEntity) {
        // 属性信息集合
        EntityMember<?> entityMember = parseAnyEntityWithCache(anyEntity.getClass());

        PrimaryKeyMember primaryKeyMember = entityMember.getPrimaryKeyMember();
        if (null != primaryKeyMember) {
            Object value = primaryKeyMember.getFieldMember().fieldGet(anyEntity);
            if (value != null) {
                return ObjectUtils.typeCast(value);
            }
        }
        return null;
    }

}
