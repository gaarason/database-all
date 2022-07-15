package gaarason.database.support;

import gaarason.database.annotation.Primary;
import gaarason.database.appointment.EntityUseType;
import gaarason.database.appointment.FinalVariable;
import gaarason.database.appointment.ValueWrapper;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.core.Container;
import gaarason.database.exception.EntityAttributeInvalidException;
import gaarason.database.exception.RelationNotFoundException;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.util.ClassUtils;
import gaarason.database.util.EntityUtils;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 数据库实体信息
 */
public class EntityMember<T> extends Container.SimpleKeeper implements Serializable {
    /**
     * 实体类型
     */
    private final Class<T> entityClass;

    /**
     * 数据表名
     */
    private final String tableName;

    /**
     * 主键信息
     */
    @Nullable
    private PrimaryKeyMember primaryKeyMember;

    /**
     * `属性名`对应的`普通`字段数组
     */
    private final Map<String, FieldMember> javaFieldMap = new LinkedHashMap<>();

    /**
     * `数据库字段`名对应的`普通`字段数组
     */
    private final Map<String, FieldMember> columnFieldMap = new LinkedHashMap<>();

    /**
     * `属性名`对应的`关系`字段数组
     */
    private final Map<String, FieldRelationMember> relationFieldMap = new LinkedHashMap<>();

    /**
     * 关联关系分析标记
     */
    private volatile boolean relationFlag = false;

    /**
     * 构造
     * @param container 容器
     * @param entityClass 实体类型
     */
    public EntityMember(Container container, Class<T> entityClass) {
        super(container);
        this.entityClass = entityClass;
        this.tableName = EntityUtils.tableName(entityClass);
        primitiveFieldDeal();
    }

    /**
     * 全新的实体对象
     * @return 全新的实体对象
     */
    public T newInstance() {
        return ClassUtils.newInstance(entityClass);
    }

    /**
     * 查询entity中的指定字段信息
     * @param fieldName 实体中的属性名
     * @return 字段信息
     * @throws EntityAttributeInvalidException 无效的字段
     */
    public FieldMember getFieldMemberByFieldName(String fieldName) throws EntityAttributeInvalidException {
        FieldMember fieldMember = javaFieldMap.get(fieldName);
        if (ObjectUtils.isEmpty(fieldMember)) {
            throw new EntityAttributeInvalidException(fieldName, entityClass);
        }
        return fieldMember;
    }

    /**
     * 查询entity中的指定字段信息
     * @param columnName 实体中的数据库列名
     * @return 字段信息
     * @throws EntityAttributeInvalidException 无效的字段
     */
    public FieldMember getFieldMemberByColumnName(String columnName) throws EntityAttributeInvalidException {
        FieldMember fieldMember = columnFieldMap.get(columnName);
        if (ObjectUtils.isEmpty(fieldMember)) {
            throw new EntityAttributeInvalidException(columnName, entityClass);
        }
        return fieldMember;
    }

    /**
     * 查询entity中的指定关系字段信息
     * @param fieldName 实体中的属性名
     * @return 关系字段信息
     * @throws EntityAttributeInvalidException 无效的字段
     */
    public FieldRelationMember getFieldRelationMemberByFieldName(String fieldName) throws EntityAttributeInvalidException {
        Map<String, FieldRelationMember> relationMemberMap = getRelationFieldMap();
        FieldRelationMember fieldRelationMember = relationMemberMap.get(fieldName);
        if (ObjectUtils.isEmpty(fieldRelationMember)) {
            throw new RelationNotFoundException(fieldName, entityClass);
        }
        return fieldRelationMember;
    }

    /**
     * 将实体对象中有效的数据库字段, 转化为简单Map
     * @param entity 实体对象
     * @param type 实体的使用目的
     * @return 简单Map
     */
    public Map<String, Object> toSimpleMap(T entity, EntityUseType type) {
        return toMap(entity, type, (fieldMember, value) -> {

            // todo 类型转化 ???
            return value;
        });
    }

    /**
     * 将实体对象中有效的数据库字段, 转化为包装Map
     * @param entity 实体对象
     * @param type 实体的使用目的
     * @return 简单Map
     */
    public Map<String, ValueWrapper> toWrapperMap(T entity, EntityUseType type) {
        return toMap(entity, type, FieldMember::wrap);
    }

    /**
     * 将实体对象中有效的数据库字段, 转化为指定Map
     * @param entity 实体对象
     * @param type 实体的使用目的
     * @param function 结果转化方式
     * @param <W> 结果类型
     * @return MAP
     */
    public <W> Map<String, W> toMap(T entity, EntityUseType type, BiFunction<FieldMember, Object, W> function) {
        if (ObjectUtils.isNull(entity)) {
            return Collections.emptyMap();
        }
        // 结果集
        Map<String, W> columnValueMap = new LinkedHashMap<>();

        // 属性信息集合 (ColumnName 为key)
        Map<String, FieldMember> columnFieldMap = getColumnFieldMap();
        for (Map.Entry<String, FieldMember> entry : columnFieldMap.entrySet()) {
            // 属性信息
            FieldMember fieldMember = entry.getValue();
            // 值
            Object value = fieldMember.fieldGet(entity);
            // 填充
            value = fieldMember.fill(entity, value, type);
            // 有效则加入 结果集
            if (fieldMember.effective(value, type)) {
                // 包装下
                columnValueMap.put(entry.getKey(), function.apply(fieldMember, value));
            }
        }
        return columnValueMap;
    }

    /**
     * 延迟载入关联关系
     * @return 关联关系map
     */
    public Map<String, FieldRelationMember> getRelationFieldMap() {
        if (relationFlag) {
            return relationFieldMap;
        } else {
            synchronized (this) {
                if (!relationFlag) {
                    // 处理关联关系
                    relationFieldDeal();
                    relationFlag = true;
                }
                return relationFieldMap;
            }
        }
    }

    /**
     * 将Map中有效的数据库字段, 赋值到全新的实体对象
     * @param columnValueMap MAP对象
     * @param type 实体的使用目的
     * @param function 结果转化方式
     * @param <W> 结果类型
     * @return 实体对象
     */
    public <W> T toEntity(Map<String, W> columnValueMap, EntityUseType type,
        BiFunction<FieldMember, W, Object> function) {
        T entity = newInstance();
        if (ObjectUtils.isNull(columnValueMap)) {
            return entity;
        }

        // 属性信息集合 (ColumnName 为key)
        Map<String, FieldMember> columnFieldMap = getColumnFieldMap();
        for (Map.Entry<String, FieldMember> entry : columnFieldMap.entrySet()) {
            // 属性信息
            FieldMember fieldMember = entry.getValue();
            // 值
            W w = columnValueMap.get(fieldMember.getColumnName());
            Object value = function.apply(fieldMember, w);

            // 有效则赋值
            if (fieldMember.effective(value, type)) {
                fieldMember.fieldSet(entity, value);
            }
        }
        return entity;
    }

    /**
     * 普通属性处理
     */
    private void primitiveFieldDeal() {
        // 返回实体中的所有属性(public/protected/private/default),含static, 含父类, 不含接口的
        List<Field> fields = EntityUtils.getDeclaredFieldsContainParent(entityClass);

        for (Field field : fields) {
            // 跳过(静态属性 or 非基本类型(由关联关系去处理))
            if (EntityUtils.isStaticField(field) || !EntityUtils.isBasicField(field)) {
                continue;
            }

            // 设置属性是可访问
            field.setAccessible(true);

            // 字段对象
            FieldMember fieldMember = new FieldMember(container, field);

            // 处理主键@Primary信息, 没有注解的就不是主键
            if (field.isAnnotationPresent(Primary.class)) {
                primaryKeyMember = new PrimaryKeyMember(container, fieldMember);
            }

            // 属性名 索引键入
            dealJavaFieldMap(fieldMember);

            // 数据库字段名 索引键入
            dealColumnMap(fieldMember);
        }
    }

    /**
     * `属性名`名对应的`普通`字段数组
     * @param fieldMember 数据库字段信息
     */
    private void dealJavaFieldMap(FieldMember fieldMember) {
        javaFieldMap.put(fieldMember.getField().getName(), fieldMember);
    }

    /**
     * `数据库字段`名对应的`普通`字段数组
     * @param fieldMember 数据库字段信息
     */
    private void dealColumnMap(FieldMember fieldMember) {
        if (fieldMember.getColumn().inDatabase()) {
            columnFieldMap.put(fieldMember.getColumnName(), fieldMember);
        }
    }

    /**
     * 补充关系字段信息
     */
    private void relationFieldDeal() {
        List<Field> fields = EntityUtils.getDeclaredFieldsContainParent(entityClass);
        for (Field field : fields) {
            // 关联关系
            if (effectiveRelationField(field)) {

                // 设置属性是可访问
                field.setAccessible(true);

                // 支持子类查找
                Model<?, ?> model = container.getBean(ModelShadowProvider.class)
                    .getByEntityClass(entityClass)
                    .getModel();

                // 对象实例
                FieldRelationMember fieldRelationMember = new FieldRelationMember(container, field, model);

                // 关联关系记录
                relationFieldMap.put(fieldRelationMember.getName(), fieldRelationMember);
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

    // ---------------------------- simple getter ---------------------------- //

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public String getTableName() {
        return tableName;
    }

    @Nullable
    public PrimaryKeyMember getPrimaryKeyMember() {
        return primaryKeyMember;
    }

    public Map<String, FieldMember> getJavaFieldMap() {
        return javaFieldMap;
    }

    public Map<String, FieldMember> getColumnFieldMap() {
        return columnFieldMap;
    }
}
