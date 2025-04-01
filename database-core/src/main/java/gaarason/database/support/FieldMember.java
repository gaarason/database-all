package gaarason.database.support;

import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.appointment.EntityUseType;
import gaarason.database.appointment.JDBCValueWrapper;
import gaarason.database.appointment.ValueWrapper;
import gaarason.database.config.ConversionConfig;
import gaarason.database.contract.support.FieldConversion;
import gaarason.database.contract.support.FieldFill;
import gaarason.database.contract.support.FieldStrategy;
import gaarason.database.contract.support.IdGenerator;
import gaarason.database.core.Container;
import gaarason.database.exception.FieldInvalidException;
import gaarason.database.exception.IllegalAccessRuntimeException;
import gaarason.database.lang.Nullable;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 数据库字段信息
 */
public class FieldMember<F> extends Container.SimpleKeeper implements Serializable {

    /**
     * 注解默认值, 所需字段媒介
     */
    @Primary(increment = false, idGenerator = IdGenerator.Never.class)
    @Column(fill = FieldFill.NotFill.class)
    private static final Object DEFAULT_COLUMN_ANNOTATION_FIELD = new Object();

    /**
     * Column 注解缺省值
     */
    private static final Column DEFAULT_COLUMN_ANNOTATION;

    /**
     * Primary 注解缺省值
     */
    private static final Primary DEFAULT_PRIMARY_ANNOTATION;

    static {
        try {
            DEFAULT_COLUMN_ANNOTATION = FieldMember.class.getDeclaredField("DEFAULT_COLUMN_ANNOTATION_FIELD")
                .getAnnotation(Column.class);
            DEFAULT_PRIMARY_ANNOTATION = FieldMember.class.getDeclaredField("DEFAULT_COLUMN_ANNOTATION_FIELD")
                .getAnnotation(Primary.class);
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
    private final F defaultValue;

    /**
     * 数据库主键信息
     * @see Primary
     */
    private final Primary primary;

    /**
     * id生成器
     */
    private final IdGenerator<?> idGenerator;

    /**
     * 数据库列信息
     * @see Column
     */
    private final Column column;

    /**
     * 字段填充
     */
    private final FieldFill fieldFill;

    /**
     * 字段插入策略
     */
    private final FieldStrategy insertStrategy;

    /**
     * 字段更新策略
     */
    private final FieldStrategy updateStrategy;

    /**
     * 字段条件策略
     */
    private final FieldStrategy conditionStrategy;

    /**
     * 序列化与反序列化
     */
    private final FieldConversion<?, ?> fieldConversion;

    public FieldMember(Container container, Field field) {
        super(container);
        this.field = field;
        this.column =
            field.isAnnotationPresent(Column.class) ? field.getAnnotation(Column.class) : DEFAULT_COLUMN_ANNOTATION;
        this.primary =
            field.isAnnotationPresent(Primary.class) ? field.getAnnotation(Primary.class) : DEFAULT_PRIMARY_ANNOTATION;
        this.columnName = ObjectUtils.isEmpty(column.name()) ? StringUtils.humpToLine(field.getName()) : column.name();
        this.idGenerator = dealIdGenerator();
        // todo 应该优先使用数据库默认值 DatabaseShadowProvider , 当默认值不存在时, 再才使用如下方法
        Object defaultValueByJavaType = getContainer().getBean(ConversionConfig.class)
            .getDefaultValueByJavaType(field.getType());
        this.defaultValue = defaultValueByJavaType == null ? null : ObjectUtils.typeCast(defaultValueByJavaType);

        this.fieldFill = container.getBean(column.fill());

        // 字段使用策略
        this.insertStrategy = dealFieldStrategy(EntityUseType.INSERT);
        this.updateStrategy = dealFieldStrategy(EntityUseType.UPDATE);
        this.conditionStrategy = dealFieldStrategy(EntityUseType.CONDITION);

        // 序列化与反序列化
        this.fieldConversion = container.getBean(column.conversion());
    }

    /**
     * 填充
     * @param entity 实体对象
     * @param field 属性字段, 应仅用于读取信息, 而非进行改动与赋值
     * @param originalValue 原始值
     * @param type 实体的使用目的
     * @return 填充的值
     */
    @Nullable
    public F fill(Object entity, Field field, @Nullable Object originalValue, EntityUseType type) {
        F nullableValue = ObjectUtils.typeCastNullable(originalValue);
        switch (type) {
            case INSERT:
                if (originalValue == null) {
                    /*
                     * 非 @Primary 修饰的字段, idGenerator.nextId() 的值一定等于 null
                     * 因此, 以下逻辑没有影响
                     */
                    nullableValue = ObjectUtils.typeCastNullable(idGenerator.nextId());
                }
                return fieldFill.inserting(entity, field, nullableValue);
            case UPDATE:
                return fieldFill.updating(entity, field, nullableValue);
            default:
                return fieldFill.condition(entity, field, nullableValue);
        }
    }

    /**
     * 根据策略, 判断是否有效
     * @param originalValue 原始值
     * @param type 实体的使用目的
     * @return 是否有效
     */
    public boolean effective(@Nullable Object originalValue, EntityUseType type) {
        // 非数据库字段, 则无效
        if (!column.inDatabase()) {
            return false;
        }
        // 当前策略
        FieldStrategy fieldStrategy;
        switch (type) {
            case INSERT:
                fieldStrategy = insertStrategy;
                break;
            case UPDATE:
                fieldStrategy = updateStrategy;
                break;
            default:
                fieldStrategy = conditionStrategy;
                break;
        }
        return fieldStrategy.enable(originalValue);
    }

    /**
     * 包装
     * @param originalValue 原始值
     * @return 包含JDBC类型的参数引用
     */
    public JDBCValueWrapper wrap(@Nullable F originalValue) {
        return new JDBCValueWrapper(originalValue, column.jdbcType());
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
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException(e);
        }
    }

    /**
     * 获取属性的值
     * 依次进行普通获取/填充/判断是否有效
     * 根据情况选择是否, 值有效时, 回填源对象(改变源对象)
     * 需要对本方法的返回值进行诊断, 以确定响应是否有效
     * @param entity 实体对象
     * @param type 实体的使用目的
     * @param backFill 是否回填
     * @return 值包装
     */
    public ValueWrapper<F> fieldFillGet(Object entity, EntityUseType type, boolean backFill) {
        // 普通获取
        Object value = fieldGet(entity);
        // 填充
        F valueAfterFill = fill(entity, field, value, type);
        // 判断是否有效
        if (!effective(valueAfterFill, type)) {
            return new ValueWrapper<>(false, valueAfterFill);
        }
        // 回填
        if (backFill) {
            fieldSet(entity, valueAfterFill);
        }
        return new ValueWrapper<>(true, valueAfterFill);
    }

    /**
     * 获取属性的值
     * @param entity 实体对象
     * @param type 实体的使用目的
     * @return 值
     * @throws FieldInvalidException 无效字段
     */
    @Nullable
    public F fieldFillGetOrFail(Object entity, EntityUseType type) throws FieldInvalidException {
        ValueWrapper<F> valueWrapper = fieldFillGet(entity, type, false);
        if (!valueWrapper.isValid()) {
            throw new FieldInvalidException(field, valueWrapper.getValue(), type);
        }
        return valueWrapper.getValue();
    }

    /**
     * 设置属性的值
     * @param obj 对象
     * @param value 值
     * @throws IllegalAccessRuntimeException 反射赋值异常
     */
    public void fieldSet(Object obj, @Nullable Object value) {
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException(e);
        }
    }

    /**
     * 字段序列化
     * @param originalValue 字段的原始值
     * @return 序列化后的值
     */
    @Nullable
    public Object serialize(@Nullable F originalValue) {
        return fieldConversion.serialize(field, ObjectUtils.typeCastNullable(originalValue));
    }

    /**
     * 从任意的结果进行反序列化, 以便赋值到实体属性
     * @param originalValue 字段的原始值
     * @return 反序列化后的值
     */
    @Nullable
    public F deserialize(@Nullable Object originalValue) {
        return ObjectUtils.typeCastNullable(
            fieldConversion.deserialize(field, ObjectUtils.typeCastNullable(originalValue)));
    }

    /**
     * 字段反序列化
     * @param resultSet 数据库结果集
     * @return 反序列化后的值
     */
    @Nullable
    public Object deserialize(ResultSet resultSet) throws SQLException {
        return fieldConversion.acquisition(field, resultSet, columnName);
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

    public FieldConversion<?, ?> getFieldConversion() {
        return fieldConversion;
    }

    // ---------------------------- private function ---------------------------- //

    /**
     * 当前场景下的策略
     * @param type 实体的使用目的
     * @return 策略
     */
    private FieldStrategy dealFieldStrategy(EntityUseType type) {
        // 当前策略
        Class<? extends FieldStrategy> fieldStrategy;
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
        // 当策略是DEFAULT时, 取用 strategy
        if (fieldStrategy.equals(FieldStrategy.Default.class)) {
            fieldStrategy = column.strategy();
        }
        return container.getBean(fieldStrategy);
    }

    /**
     * 主键auto生成器选择
     * @return 主键生成器
     */
    private IdGenerator<?> dealIdGenerator() {
        Class<?> keyJavaType = field.getType();
        /*
         * 自动化判断
         */
        if (primary.idGenerator().isAssignableFrom(IdGenerator.Auto.class)) {
            /*
             * 如果 increment == true, 那么使用数据库自增主键(程序不做任何事情)
             */
            if (primary.increment()) {
                return container.getBean(IdGenerator.Never.class);
            }

            /*
             * Long 则使用雪花算法
             */
            else if (keyJavaType == Long.class || keyJavaType == long.class) {
                return container.getBean(IdGenerator.SnowFlakesID.class);
            }

            /*
             * 根据String的长度, 选择 UUID36/UUID32
             */
            else if (keyJavaType == String.class) {
                if (column.length() >= 36) {
                    return container.getBean(IdGenerator.UUID36.class);
                } else {
                    return container.getBean(IdGenerator.UUID32.class);
                }
            }

            /*
             * 意料之外, 无能为力
             */
            else {
                return container.getBean(IdGenerator.Never.class);
            }
        }

        /*
         * 手动指定
         */
        else {
            return container.getBean(primary.idGenerator());
        }
    }

}
