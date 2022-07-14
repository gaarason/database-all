package gaarason.database.support;

import gaarason.database.annotation.Column;
import gaarason.database.appointment.EntityUseType;
import gaarason.database.appointment.FieldStrategy;
import gaarason.database.appointment.ValueWrapper;
import gaarason.database.config.ConversionConfig;
import gaarason.database.contract.support.FieldFill;
import gaarason.database.core.Container;
import gaarason.database.exception.IllegalAccessRuntimeException;
import gaarason.database.lang.Nullable;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * 数据库字段信息
 */
public class FieldMember extends Container.SimpleKeeper implements Serializable {

    /**
     * 注解默认值, 所需字段媒介
     */
    @Column
    private static final Object DEFAULT_COLUMN_ANNOTATION_FIELD = new Object();

    /**
     * 注解默认值
     */
    private static final Column DEFAULT_COLUMN_ANNOTATION;

    static {
        try {
            DEFAULT_COLUMN_ANNOTATION = FieldMember.class.getDeclaredField("DEFAULT_COLUMN_ANNOTATION_FIELD")
                .getAnnotation(Column.class);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Field (已经 设置属性是可访问)
     */
    private final Field field;

    /**
     * 数据库列名
     */
    private final String columnName;

    /**
     * 默认值
     */
    @Nullable
    private final Object defaultValue;

    /**
     * 数据库列信息
     * @see Column
     */
    private final Column column;

    /**
     * 字段填充
     */
    private final FieldFill fieldFill;

    public FieldMember(Container container, Field field) {
        super(container);
        this.field = field;
        this.column =
            field.isAnnotationPresent(Column.class) ? field.getAnnotation(Column.class) : DEFAULT_COLUMN_ANNOTATION;
        this.columnName = ObjectUtils.isEmpty(column.name()) ? StringUtils.humpToLine(field.getName()) : column.name();


        // todo 应该优先使用数据库默认值 DatabaseShadowProvider , 当默认值不存在时, 再才使用如下方法
        this.defaultValue = getContainer().getBean(ConversionConfig.class).getDefaultValueByJavaType(field.getType());

        this.fieldFill = container.getBean(column.fill());
    }

    /**
     * 填充
     * @param entity 实体对象
     * @param originalValue 原始值
     * @param type 实体的使用目的
     * @return 填充的值
     */
    @Nullable
    public Object fill(Object entity, @Nullable Object originalValue, EntityUseType type) {
        switch (type) {
            case INSERT:
                return fieldFill.inserting(entity, originalValue);
            case UPDATE:
                return fieldFill.updating(entity, originalValue);
            default:
                return fieldFill.condition(entity, originalValue);
        }
    }

    /**
     * 根据策略, 判断是否有效
     * @param originalValue 原始值
     * @param type 实体的使用目的
     * @return 是否有效
     */
    public boolean effective(@Nullable Object originalValue, EntityUseType type) {
        // 当前策略
        FieldStrategy fieldStrategy;
        switch (type) {
            case INSERT:
                fieldStrategy = column.insertStrategy();
                break;
            case UPDATE:
                fieldStrategy = column.updateStrategy();
                break;
            default:
                fieldStrategy = column.conditionStrategy();
                break;
        }
        switch (fieldStrategy) {
            case NEVER:
                return false;
            case DEFAULT:
            case NOT_NULL:
                return !ObjectUtils.isNull(originalValue);
            case NOT_EMPTY:
                return !ObjectUtils.isEmpty(originalValue);
            default:
                return true;
        }
    }

    /**
     * 包装
     * @param originalValue 原始值
     * @return 包含JDBC类型的参数引用
     */
    public ValueWrapper wrap(@Nullable Object originalValue) {
        return new ValueWrapper(originalValue, column.jdbcType());
    }

    /**
     * 获取属性的值
     * @param obj 对象
     * @return 值
     * @throws IllegalAccessRuntimeException 反射取值异常
     */
    @Nullable
    public Object fieldGet(Object obj) {
        try {
            return getField().get(obj);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException(e);
        }
    }

    /**
     * 设置属性的值
     * @param obj 对象
     * @param value 值
     * @throws IllegalAccessRuntimeException 反射赋值异常
     */
    public void fieldSet(Object obj, @Nullable Object value) {
        try {
            getField().set(obj, value);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException(e);
        }
    }

    // ---------------------------- simple getter ---------------------------- //

    public Field getField() {
        return field;
    }

    public String getColumnName() {
        return columnName;
    }

    public Column getColumn() {
        return column;
    }

    @Nullable
    public Object getDefaultValue() {
        return defaultValue;
    }

    public FieldFill getFieldFill() {
        return fieldFill;
    }
}
