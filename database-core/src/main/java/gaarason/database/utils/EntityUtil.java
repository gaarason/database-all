package gaarason.database.utils;

import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.annotations.Column;
import gaarason.database.eloquent.annotations.Primary;
import gaarason.database.eloquent.annotations.Table;
import gaarason.database.exception.IllegalAccessRuntimeException;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

public class EntityUtil {

    /**
     * 通过entity解析对应的字段和值组成的map, 忽略不符合规则的字段
     * @param entity     数据表实体对象
     * @param insertType 新增?
     * @param <T, K>        数据表实体类
     * @return 字段对值的映射
     */
    public static <T, K> Map<String, String> columnValueMap(T entity, boolean insertType) {
        Map<String, String> columnValueMap = new HashMap<>();
        Field[]             fields         = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            Object value = fieldGet(field, entity);
            if (effectiveField(field, value, insertType)) {
                columnValueMap.put(columnName(field), valueFormat(value));
            }
        }
        return columnValueMap;
    }

    /**
     * 通过entity解析对应的字段组成的list,忽略不符合规则的字段
     * @param entity     数据表实体对象
     * @param <T, K>        数据表实体类
     * @param insertType 新增?
     * @return 字段组成的list
     */
    public static <T, K> List<String> columnNameList(T entity, boolean insertType) {
        List<String> columnList = new ArrayList<>();
        Field[]      fields     = entity.getClass().getDeclaredFields();
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
     * @param <T, K>        数据表实体类
     * @param insertType 新增?
     * @return 字段的值组成的list
     */
    public static <T, K> List<String> valueList(T entity, boolean insertType) {
        List<String> valueList = new ArrayList<>();
        Field[]      fields    = entity.getClass().getDeclaredFields();
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
     * @param <T, K>            数据表实体类
     * @param columnNameList 有效的属性名
     * @return 字段的值组成的list
     */
    public static <T, K> List<String> valueList(T entity, List<String> columnNameList) {
        List<String> valueList = new ArrayList<>();
        Field[]      fields    = entity.getClass().getDeclaredFields();
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
    public static <T, K> String tableName(Class<T> entityClass) {
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
     * 设置entity对象的自增属性值
     * @param <T, K>    数据表实体类
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
