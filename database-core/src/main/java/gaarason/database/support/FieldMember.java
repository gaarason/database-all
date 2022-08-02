package gaarason.database.support;

import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.appointment.EntityUseType;
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
public class FieldMember extends Container.SimpleKeeper implements Serializable {

    /**
     * 注解默认值, 所需字段媒介
     */
    @Primary(increment = false, idGenerator = IdGenerator.Never.class)
    @Column
    private static final Object DEFAULT_COLUMN_ANNOTATION_FIELD = new Object();

    /**
     * 锁 - 序列化与反序列化默认值
     */
    private static final Object LOCK_OF_DEFAULT_CONVERSION = new Object();

    /**
     * 字段无效标记响应
     */
    private static final Object FIELD_INVALID_EXCEPTION = new Object();

    /**
     * 序列化与反序列化默认值
     */
    @Nullable
    private static FieldConversion DEFAULT_CONVERSION;

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
    private final Object defaultValue;

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
    private final FieldConversion fieldConversion;

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
        this.defaultValue = getContainer().getBean(ConversionConfig.class).getDefaultValueByJavaType(field.getType());

        this.fieldFill = container.getBean(column.fill());

        // 字段使用策略
        this.insertStrategy = dealFieldStrategy(EntityUseType.INSERT);
        this.updateStrategy = dealFieldStrategy(EntityUseType.UPDATE);
        this.conditionStrategy = dealFieldStrategy(EntityUseType.CONDITION);

        // 序列化与反序列化
        this.fieldConversion = dealFieldConversion();
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
                if (originalValue == null) {
                    originalValue = idGenerator.nextId();
                }
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
     * 根据结果类型判断是否有效
     * @param originalValue 原始值
     * @return 是否有效
     */
    public boolean effective(@Nullable Object originalValue) {
        return !FIELD_INVALID_EXCEPTION.equals(originalValue);
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
     * 获取属性的值
     * 依次进行普通获取/填充/判断是否有效/回填/序列化
     * 需要使用 effective(res) 对本方法的返回值进行诊断, 以确定响应是否有效
     * @param entity 实体对象
     * @param type 实体的使用目的
     * @return 值|FIELD_INVALID_EXCEPTION 无效字段
     */
    @Nullable
    public Object fieldGet(Object entity, EntityUseType type) throws FieldInvalidException {
        // 普通获取
        Object value = fieldGet(entity);
        // 填充
        Object valueAfterFill = fill(entity, value, type);
        // 判断是否有效
        if (!effective(valueAfterFill, type)) {
            return FIELD_INVALID_EXCEPTION;
        }
        // 回填
        fieldSet(entity, valueAfterFill);
        // 序列化后返回
        return fieldConversion.serialize(valueAfterFill);
    }

    /**
     * 获取属性的值
     * @param entity 实体对象
     * @param type 实体的使用目的
     * @return 值
     * @throws FieldInvalidException 无效字段
     */
    @Nullable
    public Object fieldGetOrFail(Object entity, EntityUseType type) throws FieldInvalidException {
        Object res = fieldGet(entity, type);
        if (!effective(res)) {
            throw new FieldInvalidException(field, res, type);
        }
        return res;
    }

    /**
     * 设置属性的值
     * @param obj 对象
     * @param value 值
     * @throws IllegalAccessRuntimeException 反射赋值异常
     */
    public void fieldSet(Object obj, @Nullable Object value) {
        try {
            // 反序列化后
            Object valueAfterDeserialize = fieldConversion.deserialize(getField(), value);
            getField().set(obj, valueAfterDeserialize);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException(e);
        }
    }

    /**
     * 设置属性的值
     * @param obj 对象
     * @param value 值
     * @param type 实体的使用目的
     * @throws FieldInvalidException 无效字段
     */
    public void fieldSetOrFail(Object obj, @Nullable Object value, EntityUseType type) throws FieldInvalidException {
        if (!effective(value, type)) {
            throw new FieldInvalidException();
        }
        fieldSet(obj, value);
    }

    /**
     * 设置属性的值
     * @param obj 对象
     * @param value 值
     * @param type 实体的使用目的
     * @throws FieldInvalidException 无效字段
     */
    public void fieldSet(Object obj, @Nullable Object value, EntityUseType type) {
        if (effective(value, type)) {
            fieldSet(obj, value);
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

    public FieldConversion getFieldConversion() {
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
        Class<?> keyJavaType = getField().getType();
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
                if (getColumn().length() >= 36) {
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

    /**
     * 序列与反序列化
     * @return 类型转化
     */
    private FieldConversion dealFieldConversion() {
        /*
         * 默认配置
         */
        if (FieldConversion.Default.class.equals(column.conversion())) {
            // 延迟获取默认配置
            FieldConversion defaultConversion = DEFAULT_CONVERSION;
            if (defaultConversion == null) {
                synchronized (LOCK_OF_DEFAULT_CONVERSION) {
                    defaultConversion = DEFAULT_CONVERSION;
                    if (defaultConversion == null) {
                        defaultConversion = DEFAULT_CONVERSION = new DefaultImpl(container);
                    }
                }
            }
            return defaultConversion;
        }
        return container.getBean(column.conversion());
    }

    /**
     * 序列化与反序列化
     * @see gaarason.database.contract.support.FieldConversion.Default
     */
    static class DefaultImpl extends Container.SimpleKeeper implements FieldConversion {

        private DefaultImpl(Container container) {
            super(container);
        }

        @Nullable
        @Override
        public Object serialize(@Nullable Object originalValue) {
            return originalValue;
        }

        @Nullable
        @Override
        public Object deserialize(Field field, ResultSet resultSet, String column) throws SQLException {
            return getContainer().getBean(ConversionConfig.class).getValueFromJdbcResultSet(field, resultSet, column);
        }

        @Nullable
        @Override
        public Object deserialize(Field field, @Nullable Object originalValue) {
            return getContainer().getBean(ConversionConfig.class).castNullable(originalValue, field.getType());
        }
    }

}
