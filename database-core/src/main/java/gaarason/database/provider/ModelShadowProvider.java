package gaarason.database.provider;

import gaarason.database.appointment.EntityUseType;
import gaarason.database.appointment.LambdaInfo;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.core.Container;
import gaarason.database.exception.*;
import gaarason.database.lang.Nullable;
import gaarason.database.logging.Log;
import gaarason.database.logging.LogFactory;
import gaarason.database.support.*;
import gaarason.database.util.EntityUtils;
import gaarason.database.util.LambdaUtils;
import gaarason.database.util.ObjectUtils;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Model信息大全
 * @author xt
 */
public class ModelShadowProvider extends Container.SimpleKeeper {

    private static final Log LOGGER = LogFactory.getLog(ModelShadowProvider.class);
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
     * @param modelClass model类型
     * @return 是否成功
     */
    public boolean loadModel(Class<? extends Model<?, ?, ?>> modelClass) {
        return loadModels(Collections.singleton(modelClass)) > 0;
    }

    /**
     * 通过反射等手段, 得到model信息
     * 因为关联关系的相关功能, 需要将存在关系的都提前准备好, 这样才能通过entity找到对应的model
     * @param modelClasses model类型的集合
     * @return 数量
     */
    public int loadModels(Collection<Class<? extends Model<?, ?, ?>>> modelClasses) {
        synchronized (persistence) {
            int i = 0;
            // 所有 Model 的子类 (含抽象类等)进行初始化分析
            // 初始化模型的基本信息, 并构建索引
            for (Class<? extends Model<?, ?, ?>> modelClass : modelClasses) {
                ModelMember<Object, Object> modelMember;
                // 接口跳过/抽象类跳过
                if (modelClass.isInterface() || Modifier.isAbstract(modelClass.getModifiers())) {
                    continue;
                }
                try {
                    modelMember = new ModelMember<>(container, ObjectUtils.typeCast(modelClass));
                } catch (TypeNotSupportedException | ClassCastException ignore) {
                    // 类型失败跳过
                    continue;
                }
                i++;
                persistence.modelIndexMap.put(modelClass, modelMember);
                persistence.modelProxyIndexMap.put(modelMember.getModel().getClass(), modelMember);
                persistence.entityIndexMap.put(modelMember.getEntityClass(), modelMember);
            }
            return i;
        }
    }

    /**
     * 通过反射等手段, 刷新model信息
     * @param modelClasses model类型的集合
     * @return 数量
     */
    public int refreshModels(Collection<Class<? extends Model<?, ?, ?>>> modelClasses) {
        return loadModels(modelClasses);
    }

    /**
     * 卸载model信息
     * @param modelClasses model类型的集合
     * @return 数量
     */
    public int unloadModels(Collection<Class<? extends Model<?, ?, ?>>> modelClasses) {
        synchronized (persistence) {
            int i = 0;
            for (Class<? extends Model<?, ?, ?>> modelClass : modelClasses) {
                ModelMember<?, ?> modelMember = persistence.modelIndexMap.remove(modelClass);
                if (!ObjectUtils.isEmpty(modelMember)) {
                    i++;
                    persistence.entityIndexMap.remove(modelMember.getEntityClass());
                    persistence.modelProxyIndexMap.entrySet().removeIf(entry -> entry.getValue() == modelMember);
                }
            }
            return i;
        }
    }

    /**
     * 查询Model信息
     * @param model 模型
     * @param <T> 实体类
     * @param <K> 主键类型
     * @return 格式化后的Model信息
     */
    public <T, K> ModelMember<T, K> get(Model<?, T, K> model) {
        return getByModelClass(ObjectUtils.typeCast(model.getClass()));
    }

    /**
     * 查询Model信息
     * @param modelClass 模型类
     * @param <T> 实体类
     * @param <K> 主键类型
     * @return 格式化后的Model信息
     */
    public <T, K> ModelMember<T, K> getByModelClass(Class<? extends Model<?, T, K>> modelClass) {
        ModelMember<?, ?> result;
        result = persistence.modelProxyIndexMap.get(modelClass);
        if (null == result) {
            result = persistence.modelIndexMap.get(modelClass);
            if (null == result) {
                /*
                 * 尝试动态解析一次
                 */
                if (loadModel(modelClass)) {
                    result = persistence.modelProxyIndexMap.get(modelClass);
                    if (null == result) {
                        result = persistence.modelIndexMap.get(modelClass);
                    }
                }
            }
        }

        if (null == result) {
            throw new ModelInvalidException(modelClass);
        }
        return ObjectUtils.typeCast(result);
    }

    /**
     * 查询Model信息
     * @param clazz 实体类(可查找泛型)
     * @return 格式化后的Model信息
     */
    public <T> ModelMember<? super T, ?> getByEntityClass(Class<T> clazz) {
        ModelMember<?, ?> result;

        result = persistence.entityIndexMap.get(clazz);
        if (null == result) {
            /*
             * 尝试动态解析一次
             * 仅对, model是其entity的内部类的情况有效, 其他情况下没有办法找(猜测)到对应的model
             * class SomeEntity {
             *     public static class Model implements Model<SomeEntity, K> {}
             * }
             */
            Class<? extends Model<?, ?, ?>> modelClass = EntityUtils.inferModelClassOnEntity(clazz);
            // 似乎找到了
            if (modelClass != null && loadModel(modelClass)) {
                result = persistence.entityIndexMap.get(clazz);
            }
        }
        if (null == result) {
            throw new EntityInvalidException(clazz);
        }
        return ObjectUtils.typeCast(result);
    }

    /**
     * 解析实体类
     * @param anyEntity 任意实体类(可查找泛型)
     * @return 格式化后的Entity信息
     */
    public <T, K> EntityMember<T, K> parseAnyEntityWithCache(T anyEntity) {
        return ObjectUtils.typeCast(parseAnyEntityWithCache(anyEntity.getClass()));
    }

    /**
     * 解析实体类
     * @param anyEntityClass 任意实体类(可查找泛型)
     * @return 格式化后的Entity信息
     */
    public <T, K> EntityMember<T, K> parseAnyEntityWithCache(Class<T> anyEntityClass) {
        EntityMember<?, ?> entityMember = cache.entity.get(anyEntityClass);
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
     * @param <F> 属性类型
     * @return 属性名
     */
    public <T, F> String parseFieldNameByLambdaWithCache(ColumnFunctionalInterface<T, F> func) {
        Class<?> clazz = func.getClass();
        String fieldName = cache.lambdaFieldName.get(clazz);
        if (fieldName == null) {
            synchronized (cache.lambdaFieldName) {
                fieldName = cache.lambdaFieldName.get(clazz);
                if (fieldName == null) {
                    // 解析 lambda
                    LambdaInfo<?> lambdaInfo = LambdaUtils.parse(func);
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
     * @param <F> 属性类型
     * @return 列名
     */
    public <T, F> String parseColumnNameByLambdaWithCache(ColumnFunctionalInterface<T, F> func) {
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
     * @param <F> 属性类型
     * @return FieldInfo
     */
    private <T, F> String parseLambda(ColumnFunctionalInterface<T, F> func) {
        // 解析 lambda
        LambdaInfo<T> lambdaInfo = LambdaUtils.parse(func);
        // 实例类
        Class<T> entityClass = lambdaInfo.getEntityCLass();
        // 属性名
        String fieldNameLocal = lambdaInfo.getFieldName();
        // 字段信息
        FieldMember<?> fieldMember = getFieldByAnyEntityClass(entityClass, fieldNameLocal);
        // 优先使用缓存的字段信息
        return ObjectUtils.isEmpty(fieldMember) ? lambdaInfo.getColumnName() : fieldMember.getColumnName();
    }

    /**
     * 查询entity 中的指定字段信息
     * @param anyEntityClass 实体类(可查找泛型)
     * @param fieldName 实体中的属性
     * @param <T> 实体类型
     * @return 字段信息
     * @throws EntityAttributeInvalidException 无效的字段
     */
    public <T> FieldMember<?> getFieldByAnyEntityClass(Class<T> anyEntityClass, String fieldName)
        throws EntityAttributeInvalidException, EntityInvalidException {

        // 字段信息
        FieldMember<?> fieldMember = parseAnyEntityWithCache(anyEntityClass).getJavaFieldMap().get(fieldName);

        if (ObjectUtils.isEmpty(fieldMember)) {
            throw new EntityAttributeInvalidException(fieldName, anyEntityClass);
        }
        return fieldMember;
    }

    /**
     * 通过entity解析对应的字段和值组成的map, 忽略不符合规则的字段
     * 根据字段上的注解对字段进行填充以及序列化
     * @param anyEntity 数据表实体对象
     * @param type 实体的使用目的
     * @param <T> 数据表实体类
     * @return 字段对值的映射
     */
    public <T> Map<String, Object> entityToMap(@Nullable T anyEntity, EntityUseType type) {
        if (ObjectUtils.isNull(anyEntity)) {
            return Collections.emptyMap();
        }
        return parseAnyEntityWithCache(anyEntity).toFillMap(anyEntity, type, false);
    }

    /**
     * 通过entity解析对应的字段和值组成的map, 忽略不符合规则的字段
     * 根据字段上的注解对字段进行填充/回填以及序列化
     * @param anyEntity 数据表实体对象
     * @param type 实体的使用目的
     * @param <T> 数据表实体类
     * @return 字段对值的映射
     */
    public <T> Map<String, Object> entityBackFillToMap(@Nullable T anyEntity, EntityUseType type) {
        if (ObjectUtils.isNull(anyEntity)) {
            return Collections.emptyMap();
        }
        return parseAnyEntityWithCache(anyEntity).toFillMap(anyEntity, type, true);
    }

    /**
     * 将结果集中的原信息, 赋值到实体
     * @param anyEntityClass 数据表实体对象
     * @param theRecord 查询结果集
     * @param <T> 数据表实体类
     * @return 实体
     */
    public <T> T entityAssignment(Class<T> anyEntityClass, Record<?, ?> theRecord) {
        Map<String, Object> metadataMap = theRecord.getMetadataMap();
        EntityMember<T, ?> entityMember = parseAnyEntityWithCache(anyEntityClass);
        T entity = entityMember.toEntity(metadataMap);

        // 数据库主键
        PrimaryKeyMember<?> primaryKeyMember = entityMember.getPrimaryKeyMember();
        if (primaryKeyMember != null) {
            Object primaryKeyValue = metadataMap.get(primaryKeyMember.getFieldMember().getColumnName());
            if (primaryKeyValue != null) {
                theRecord.setOriginalPrimaryKeyValue(ObjectUtils.typeCast(primaryKeyValue));
            }
        }
        return entity;
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
        EntityMember<?, ?> entityMember = parseAnyEntityWithCache(anyEntity.getClass());

        PrimaryKeyMember<?> primaryKeyMember = entityMember.getPrimaryKeyMember();
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
    public <T, K> K getPrimaryKeyValue(T anyEntity, EntityUseType type) {
        Object primaryKeyValue = parseAnyEntityWithCache(anyEntity.getClass()).getPrimaryKeyMemberOrFail()
            .getFieldMember()
            .fieldFillGetOrFail(anyEntity, type);
        return ObjectUtils.typeCastNullable(primaryKeyValue);
    }

    /**
     * 持久信息
     * 需要手动初始化
     */
    static class Persistence {
        /**
         * Model Class做为索引
         */
        private final Map<Class<? extends Model<?, ?, ?>>, ModelMember<?, ?>> modelIndexMap = new ConcurrentHashMap<>();

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
        private final SoftCache<Class<?>, EntityMember<?, ?>> entity = new SoftCache<>();

        /**
         * 缓存lambda风格的列名, 与为String风格的列名的映射
         */
        private final SoftCache<Class<?>, String> lambdaColumnName = new SoftCache<>();

        /**
         * 缓存lambda风格的属性名, 与为String风格的属性名的映射
         */
        private final SoftCache<Class<?>, String> lambdaFieldName = new SoftCache<>();
    }

}
