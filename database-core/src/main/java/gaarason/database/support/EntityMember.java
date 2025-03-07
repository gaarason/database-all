package gaarason.database.support;

import gaarason.database.annotation.Primary;
import gaarason.database.annotation.base.Relation;
import gaarason.database.appointment.EntityUseType;
import gaarason.database.appointment.FinalVariable;
import gaarason.database.appointment.ValueWrapper;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.core.Container;
import gaarason.database.exception.EntityAttributeInvalidException;
import gaarason.database.exception.PrimaryKeyNotFoundException;
import gaarason.database.exception.RelationNotFoundException;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.util.ClassUtils;
import gaarason.database.util.EntityUtils;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

/**
 * 数据库实体信息
 */
public class EntityMember<T, K> extends Container.SimpleKeeper implements Serializable {

    /**
     * 实体类型
     */
    private final Class<T> entityClass;

    /**
     * 数据表名
     */
    private final String tableName;

    /**
     * `属性名`对应的`普通`字段数组
     */
    private final Map<String, FieldMember<?>> javaFieldMap = new LinkedHashMap<>();

    /**
     * `数据库字段`名对应的`普通`字段数组
     */
    private final Map<String, FieldMember<?>> columnFieldMap = new LinkedHashMap<>();

    /**
     * `属性名`对应的`关系`字段数组
     */
    private final Map<String, FieldRelationMember> relationFieldMap = new LinkedHashMap<>();

    /**
     * 可查询的`数据库字段`数组
     */
    private final List<String> selectColumnList = new LinkedList<>();

    /**
     * 可查询的`数据库字段`字符
     * key : 反引号, 双引号
     * value :`id`,`name`,`age`
     * @see #getSelectColumnString(String, Supplier)
     */
    private final Map<String, String> selectColumnStringMap = new HashMap<>();

    /**
     * 主键信息
     */
    @Nullable
    private PrimaryKeyMember<K> primaryKeyMember;

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
    public FieldMember<?> getFieldMemberByFieldName(String fieldName) throws EntityAttributeInvalidException {
        FieldMember<?> fieldMember = javaFieldMap.get(fieldName);
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
    public FieldMember<?> getFieldMemberByColumnName(String columnName) throws EntityAttributeInvalidException {
        FieldMember<?> fieldMember = columnFieldMap.get(columnName);
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
    public FieldRelationMember getFieldRelationMemberByFieldName(String fieldName)
        throws EntityAttributeInvalidException {
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
     * @return 简单Map
     */
    public Map<String, Object> toSimpleMap(@Nullable T entity) {
        if (ObjectUtils.isNull(entity)) {
            return Collections.emptyMap();
        }
        // 结果集
        Map<String, Object> columnValueMap = new LinkedHashMap<>();
        // 属性信息集合 (ColumnName 为key)
        Map<String, FieldMember<?>> columnFieldMap = this.columnFieldMap;
        for (Map.Entry<String, FieldMember<?>> entry : columnFieldMap.entrySet()) {
            // 属性信息
            FieldMember<?> fieldMember = entry.getValue();
            // 值
            Object value = fieldMember.fieldGet(entity);
            // 加入 结果集
            columnValueMap.put(entry.getKey(), value);
        }
        return columnValueMap;
    }

    /**
     * 将实体对象中有效的数据库字段, 转化为指定Map
     * 根据字段上的注解对字段进行`填充`及`序列化`
     * @param entity 实体对象
     * @param type 实体的使用目的
     * @param backFill 是否回填
     * @return MAP
     */
    public Map<String, Object> toFillMap(@Nullable T entity, EntityUseType type, boolean backFill) {
        if (ObjectUtils.isNull(entity)) {
            return Collections.emptyMap();
        }
        // 结果集
        Map<String, Object> columnValueMap = new LinkedHashMap<>();

        // 属性信息集合 (ColumnName 为key)
        Map<String, FieldMember<?>> columnFieldMap = this.columnFieldMap;
        for (Map.Entry<String, FieldMember<?>> entry : columnFieldMap.entrySet()) {
            // 属性信息
            FieldMember<?> fieldMember = entry.getValue();
            // 值
            ValueWrapper<?> valueWrapper = fieldMember.fieldFillGet(entity, type, backFill);
            // 有效则加入 结果集
            if (valueWrapper.isValid()) {
                // 序列化
                columnValueMap.put(entry.getKey(),
                    fieldMember.serialize(ObjectUtils.typeCastNullable(valueWrapper.getValue())));
            }
        }
        return columnValueMap;
    }

    /**
     * 将Map中的数据库字段, 反序列化后, 赋值到全新的实体对象
     * @param columnValueMap MAP对象
     * @return 实体对象
     */
    public T toEntity(Map<String, Object> columnValueMap) {
        T entity = newInstance();
        if (ObjectUtils.isNull(columnValueMap)) {
            return entity;
        }

        // 属性信息集合 (ColumnName 为key)
        Map<String, FieldMember<?>> columnFieldMap = this.columnFieldMap;
        for (Map.Entry<String, FieldMember<?>> entry : columnFieldMap.entrySet()) {
            // 属性信息
            FieldMember<?> fieldMember = entry.getValue();
            // 值
            if (columnValueMap.containsKey(fieldMember.getColumnName())) {
                Object w = columnValueMap.get(fieldMember.getColumnName());
                // 反序列化
                Object value = fieldMember.deserialize(w);
                // 赋值
                fieldMember.fieldSet(entity, value);
            }
        }
        return entity;
    }

    /**
     * 延迟获取关联关系
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
     * 普通属性处理
     */
    private void primitiveFieldDeal() {
        // 返回实体中的所有属性(public/protected/private/default), 不含static, 含父类, 不含接口的
        List<Field> fields = EntityUtils.getDeclaredFieldsContainParentWithoutStatic(entityClass);

        for (Field field : fields) {

            /*
             * 跳过 关联关系相关字段
             */
            if (effectiveRelationField(field)) {
                continue;
            }

            // 设置属性是可访问
            field.setAccessible(true);

            // 字段对象
            FieldMember<?> fieldMember = new FieldMember<>(container, field);

            // 处理主键@Primary信息, 没有注解的就不是主键
            if (field.isAnnotationPresent(Primary.class)) {
                primaryKeyMember = new PrimaryKeyMember<>(container, fieldMember);
            }

            // 属性名 索引键入
            dealJavaFieldMap(fieldMember);

            // 数据库字段名 索引键入
            dealColumnMap(fieldMember);

            // 可查询的数据库字段
            dealSelectColumnList(fieldMember);
        }
    }

    /**
     * `属性名`名对应的`普通`字段数组
     * @param fieldMember 数据库字段信息
     */
    private void dealJavaFieldMap(FieldMember<?> fieldMember) {
        // 属性重名的情况下, 子类优先
        if (!javaFieldMap.containsKey(fieldMember.getField().getName())) {
            javaFieldMap.put(fieldMember.getField().getName(), fieldMember);
        }
    }

    /**
     * `数据库字段`名对应的`普通`字段数组
     * @param fieldMember 数据库字段信息
     */
    private void dealColumnMap(FieldMember<?> fieldMember) {
        // 字段重名的情况下, 子类优先
        if (fieldMember.getColumn().inDatabase() && !columnFieldMap.containsKey(fieldMember.getColumnName())) {
            columnFieldMap.put(fieldMember.getColumnName(), fieldMember);
        }
    }

    /**
     * 可查询的`数据库字段`数组
     * @param fieldMember 数据库字段信息
     */
    private void dealSelectColumnList(FieldMember<?> fieldMember) {
        if (fieldMember.getColumn().inDatabase() && fieldMember.getColumn().selectable() &&
            !selectColumnList.contains(fieldMember.getColumnName())) {
            selectColumnList.add(fieldMember.getColumnName());
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
                Model<?, ?, ?> model = container.getBean(ModelShadowProvider.class)
                    .getByEntityClass(entityClass)
                    .getModel();

                // 对象实例
                FieldRelationMember fieldRelationMember = new FieldRelationMember(container, field, model);

                // 关联关系记录
                if (!relationFieldMap.containsKey(fieldRelationMember.getName())) {
                    relationFieldMap.put(fieldRelationMember.getName(), fieldRelationMember);
                }
            }
        }
    }

    /**
     * 是否有效的关联关系字段
     * @param field 字段
     * @return yes/no
     */
    private static boolean effectiveRelationField(Field field) {
        // 静态类型
        if (EntityUtils.isStaticField(field)) {
            return false;
        }
        // 基础类型
        if (EntityUtils.isBasicField(field)) {
            return false;
        }

        // 注解查找
        Annotation[] annotations = field.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();

            // 预置注解
            if (FinalVariable.RELATION_ANNOTATIONS.contains(annotationType)) {
                return true;
            }

            // 自定义注解
            if (null != annotationType.getAnnotation(Relation.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取主键信息
     * @return 主键信息
     * @throws PrimaryKeyNotFoundException 主键信息未知
     */
    public PrimaryKeyMember<K> getPrimaryKeyMemberOrFail() throws PrimaryKeyNotFoundException {
        if (primaryKeyMember == null) {
            throw new PrimaryKeyNotFoundException();
        }
        return primaryKeyMember;
    }

    /**
     * 存取 column string
     * @param symbol 反引号, 双引号
     * @param func 获取 column string
     * @return column string
     */
    public String getSelectColumnString(String symbol, Supplier<String> func) {
        String columnString = selectColumnStringMap.get(symbol);
        if(columnString == null) {
            synchronized (selectColumnStringMap) {
                columnString = selectColumnStringMap.get(symbol);
                if(columnString == null) {
                    columnString = func.get();
                    selectColumnStringMap.put(symbol, columnString);
                }
            }
        }
        return columnString;
    }

    // ---------------------------- simple getter ---------------------------- //

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public String getTableName() {
        return tableName;
    }

    @Nullable
    public PrimaryKeyMember<K> getPrimaryKeyMember() {
        return primaryKeyMember;
    }

    public Map<String, FieldMember<?>> getJavaFieldMap() {
        return javaFieldMap;
    }

    public Map<String, FieldMember<?>> getColumnFieldMap() {
        return columnFieldMap;
    }

    public List<String> getSelectColumnList() {
        return selectColumnList;
    }
}
