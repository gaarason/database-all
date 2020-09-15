package gaarason.database.util;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.annotations.Column;
import gaarason.database.eloquent.annotations.Primary;
import gaarason.database.eloquent.annotations.Table;
import gaarason.database.exception.ColumnNotFoundException;
import gaarason.database.exception.IllegalAccessRuntimeException;
import gaarason.database.exception.TypeNotSupportedException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

public class EntityUtil {

    public final static Class<?>[] allowType = new Class[]{Boolean.class, Byte.class, Character.class, Short.class,
            Integer.class, Long.class, Float.class, Double.class, BigInteger.class, Date.class};

    /**
     * 通过entity解析对应的字段和值组成的map, 忽略不符合规则的字段
     * @param entity     数据表实体对象
     * @param insertType 新增?
     * @param <T>        数据表实体类
     * @return 字段对值的映射
     */
    public static <T> Map<String, String> columnValueMap(T entity, boolean insertType) {
        Map<String, String> columnValueMap = new HashMap<>();
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            Object value = fieldGet(field, entity);
            if (effectiveField(field, value, insertType)) {
                columnValueMap.put(columnName(field), valueFormat(value));
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
     * @return 字段组成的list
     */
    public static <T> List<String> columnNameList(T entity, boolean insertType) {
        List<String> columnList = new ArrayList<>();
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            Object value = fieldGet(field, entity);
            if (effectiveField(field, value, insertType))
                columnList.add(columnName(field));
        }
        return columnList;
    }

    /**
     * 通过entity解析对应的字段的值组成的list, 忽略不符合规则的字段
     * @param entity     数据表实体对象
     * @param <T>        数据表实体类
     * @param insertType 新增?
     * @return 字段的值组成的list
     */
    public static <T> List<String> valueList(T entity, boolean insertType) {
        List<String> valueList = new ArrayList<>();
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            Object value = fieldGet(field, entity);
            if (effectiveField(field, value, insertType))
                valueList.add(valueFormat(value));
        }
        return valueList;
    }

    /**
     * 通过entity解析对应的字段的值组成的list, 忽略不符合规则的字段
     * @param entity         数据表实体对象
     * @param <T>            数据表实体类
     * @param columnNameList 有效的属性名
     * @return 字段的值组成的list
     */
    public static <T> List<String> valueList(T entity, List<String> columnNameList) {
        List<String> valueList = new ArrayList<>();
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (columnNameList.contains(columnName(field))) {
                valueList.add(valueFormat(fieldGet(field, entity)));
            }
        }
        return valueList;
    }

    /**
     * 通过entity解析对应的表名
     * @param entityClass 数据表实体类
     * @return 数据表名
     */
    public static <T> String tableName(Class<T> entityClass) {
        if (entityClass.isAnnotationPresent(Table.class)) {
            Table table = entityClass.getAnnotation(Table.class);
            return table.name();
        }
        String[] split = entityClass.getName().split("\\.");
        return StringUtil.humpToLine(split[split.length - 1]);
    }

    /**
     * 是否有效字段
     * @param field      字段
     * @param value      字段值
     * @param insertType 是否是新增,会通过字段上的注解column(insertable, updatable)进行忽略
     * @return 有效
     */
    public static boolean effectiveField(Field field, @Nullable Object value, boolean insertType) {
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            // 注解中已经标记不可新增或者更新
            if (insertType ? !column.insertable() : !column.updatable()) {
                return false;
            }
            // 注解中已经标记不可为null,但仍然为null
            return column.nullable() || value != null;
        }
        // 静态属性
        if (Modifier.isStatic(field.getModifiers())) {
            return false;
        }
        // 非基本类型
        if (!field.getType().isPrimitive() && !Arrays.asList(allowType).contains(field.getType())) {
            return false;
        }
        return value != null;
    }

    /**
     * 是否主键字段
     * @param field     字段
     * @param increment 自增
     * @return 有效
     */
    public static boolean isPrimaryField(Field field, boolean increment) {
        if (field.isAnnotationPresent(Primary.class)) {
            Primary primary = field.getAnnotation(Primary.class);
            return primary.increment() == increment;
        }
        return false;
    }

    /**
     * 获取属性的值
     * @param field 属性
     * @param obj   对象
     * @return 值
     */
    @Nullable
    public static Object fieldGet(Field field, Object obj) {
        try {
            field.setAccessible(true); // 设置些属性是可以访问的
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException(e);
        }
    }

    /**
     * 获取类属性对应的数据库字段名
     * @param field 属性
     * @return 数据库字段名
     */
    public static String columnName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            if (!"".equals(column.name())) {
                return column.name();
            }
        }
        return StringUtil.humpToLine(field.getName());
    }

    /**
     * 获取数据库字段名对应的类属性名
     * @param columnName 列名
     * @return 数据库字段名
     */
    public static String fieldName(Class<?> obj, String columnName) {
        // 不存在注解时, 则查找类中是否存在小驼峰的属性名
        String fieldNameTemp = StringUtil.lineToHump(columnName, false);
        String fieldName = null;
        for (Field field : obj.getDeclaredFields()) {
            // 优先精准匹配注解
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (columnName.equals(column.name())) {
                    return field.getName();
                }
            }
            // 同时查找小驼峰的属性名, 以备万一
            if (field.getName().equals(fieldNameTemp)) {
                fieldName = fieldNameTemp;
            }
        }
        if (fieldName != null) {
            return fieldName;
        }
        throw new ColumnNotFoundException(columnName + " not found in " + obj);
    }

    /**
     * 获取数据库字段名对应的类属性的值
     * @param columnName 列名
     * @return 类属性的值
     */
    public static Object getFieldValueByColumn(Object obj, String columnName) {
        // 不存在注解时, 则查找类中是否存在小驼峰的属性名
        String fieldNameTemp = StringUtil.lineToHump(columnName, false);
        Object value = null;
        Class<?> clazz = obj.getClass();
        try {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                // 优先精准匹配注解
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    if (columnName.equals(column.name())) {
                        // 精准命中直接返回
                        return field.get(obj);
                    }
                }
                // 同时查找小驼峰的属性名, 以备万一
                if (field.getName().equals(fieldNameTemp)) {
                    value = field.get(obj);
                }
            }
            if (value != null) {
                return value;
            }
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException(e);
        }
        throw new ColumnNotFoundException(columnName + " not found in " + obj);
    }

    /**
     * 获取数据库字段名对应的类属性的名
     * @param columnName 列名
     * @return 类属性的名
     */
    public static String getFieldNameByColumn(Object obj, String columnName) {
        // 不存在注解时, 则查找类中是否存在小驼峰的属性名
        String fieldNameTemp = StringUtil.lineToHump(columnName, false);
        String fieldName = null;
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            // 优先精准匹配注解
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (columnName.equals(column.name())) {
                    // 精准命中直接返回
                    return field.getName();
                }
            }
            // 同时查找小驼峰的属性名, 以备万一
            if (field.getName().equals(fieldNameTemp)) {
                fieldName = field.getName();
            }
        }
        if (fieldName == null) {
            throw new ColumnNotFoundException(columnName + " not found in " + clazz);
        }
        return fieldName;

    }

    /**
     * 获取数据库字段名对应的类属性的名
     * @param columnName 列名
     * @return 类属性的名
     */
    public static String getFieldNameByColumn(Class<?> clazz, String columnName) {
        // 不存在注解时, 则查找类中是否存在小驼峰的属性名
        String fieldNameTemp = StringUtil.lineToHump(columnName, false);
        String fieldName = null;
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            // 优先精准匹配注解
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (columnName.equals(column.name())) {
                    // 精准命中直接返回
                    return field.getName();
                }
            }
            // 同时查找小驼峰的属性名, 以备万一
            if (field.getName().equals(fieldNameTemp)) {
                fieldName = field.getName();
            }
        }
        if (fieldName == null) {
            throw new ColumnNotFoundException(columnName + " not found in " + clazz);
        }
        return fieldName;

    }

    /**
     * 设置entity对象的自增属性值
     * @param <T>    数据表实体类
     * @param <K>    数据表主键类型
     * @param entity 数据表实体对象
     * @param id     数据库生成的id
     * @throws IllegalAccessRuntimeException 反射赋值异常
     */
    public static <T, K> void setPrimaryId(T entity, @Nullable K id) {
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (isPrimaryField(field, true)) {
                try {
                    field.setAccessible(true); // 设置些属性是可以访问的
                    field.set(entity, id);
                    return;
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new IllegalAccessRuntimeException(e);
                }
            }
        }
    }

    /**
     * 用数据库字段填充类属性
     * @param field 属性
     * @param value 值
     * @return 数据库字段值, 且对应实体entity的数据类型
     */
    @Nullable
    public static Object columnFill(Field field, @Nullable Object value) {
        if (value == null)
            return null;
        switch (field.getType().toString()) {
            case "class java.lang.Byte":
                return Byte.valueOf(value.toString());
            case "class java.lang.String":
                return value.toString();
            case "class java.lang.Integer":
                return Integer.valueOf(value.toString());
            case "class java.lang.Long":
            case "class java.math.BigInteger":
                return Long.valueOf(value.toString());
            default:
                return value;
        }
    }

    /**
     * 将数据库查询结果赋值给entity的field
     * 需要 field.setAccessible(true)
     * @param field           属性
     * @param stringColumnMap 元数据map
     * @param entity          数据表实体对象
     */
    public static <T, K> void fieldAssignment(Field field, Map<String, gaarason.database.support.Column> stringColumnMap,
                                              T entity, Record<T, K> record)
            throws TypeNotSupportedException {
        String columnName = EntityUtil.columnName(field);
        gaarason.database.support.Column column = stringColumnMap.get(columnName);
        if (column != null) {
            try {
                Object value = EntityUtil.columnFill(field, column.getValue());
                field.set(entity, value);
                // 主键值记录
                if (field.isAnnotationPresent(Primary.class) && value != null) {
                    record.setOriginalPrimaryKeyValue(ObjectUtil.typeCast(value));
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new TypeNotSupportedException(e.getMessage(), e);
            }

        }
    }

    /**
     * 将数据库查询结果赋值给entityList
     * @param stringColumnMapList 源数据
     * @param entityClass         目标实体类
     * @param <T>                 目标实体类
     * @return 对象列表
     * @throws TypeNotSupportedException 实体不支持
     */
    public static <T> List<T> entityAssignment(List<Map<String, gaarason.database.support.Column>> stringColumnMapList,
                                               Class<T> entityClass)
            throws TypeNotSupportedException {
        List<T> entityList = new ArrayList<>();
        for (Map<String, gaarason.database.support.Column> stringColumnMap : stringColumnMapList) {
            T entity = entityAssignment(stringColumnMap, entityClass);
            entityList.add(entity);
        }
        return entityList;
    }

    /**
     * 将数据库查询结果赋值给entityList
     * @param stringColumnMap 源数据
     * @param entityClass     目标实体类
     * @param <T>             目标实体类
     * @return 对象列表
     * @throws TypeNotSupportedException 实体不支持
     */
    public static <T> T entityAssignment(Map<String, gaarason.database.support.Column> stringColumnMap,
                                         Class<T> entityClass) throws TypeNotSupportedException {
        try {
            T entity = entityClass.newInstance();
            for (Field field : entityClass.getDeclaredFields()) {
                field.setAccessible(true);
                String columnName = EntityUtil.columnName(field);
                gaarason.database.support.Column column = stringColumnMap.get(columnName);
                if (column != null) {
                    Object value = EntityUtil.columnFill(field, column.getValue());
                    field.set(entity, value);
                }
            }
            return entity;
        } catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
            throw new TypeNotSupportedException(e.getMessage(), e);
        }
    }

    /**
     * 格式化值到字符串
     * @param value 原值
     * @return 字符串
     */
    @Nullable
    private static String valueFormat(@Nullable Object value) {
        if (value instanceof Date) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return formatter.format(value);
        } else if (value instanceof Boolean) {
            return (Boolean) value ? "1" : "0";
        } else
            return value == null ? null : value.toString();
    }

}
