package gaarason.database.util;

import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.annotation.Column;
import gaarason.database.eloquent.annotation.Table;
import gaarason.database.eloquent.appointment.FinalVariable;
import gaarason.database.exception.TypeNotSupportedException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 实体处理工具
 */
public class EntityUtil {

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
     * 判断字段是否静态
     * @param field 字段
     * @return 是否
     */
    public static boolean isStaticField(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    /**
     * 判断字段是否基本类型(含包装类型)
     * @param field 字段
     * @return 是否
     */
    public static boolean isBasicField(Field field) {
        return field.getType().isPrimitive() || FinalVariable.allowFieldTypes.contains(field.getType());
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
     * 将数据库查询结果赋值给 任意 entityList
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
     * 将数据库查询结果赋值给 任意 entity
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
     * 格式化值到字符串
     * @param value 原值
     * @return 字符串
     */
    @Nullable
    public static String valueFormat(@Nullable Object value) {
        if (value instanceof Date) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return formatter.format(value);
        } else if (value instanceof Boolean) {
            return (Boolean) value ? "1" : "0";
        } else
            return value == null ? null : value.toString();
    }


    /**
     * 返回 clazz 中的所有属性(public/protected/private)包含父类的
     * @param clazz 类型
     * @return 所有字段
     */
    public static List<Field> getDeclaredFieldsContainParent(Class<?> clazz) {
        List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        Class<?> superclass = clazz.getSuperclass();
        if (!superclass.equals(Object.class)) {
            fields.addAll(getDeclaredFieldsContainParent(superclass));
        }
        return fields;
    }

    /**
     * 返回 clazz 中的指定属性(public/protected/private)
     * @param clazz 类型
     * @param name  属性名称
     * @return 属性对应的 Field
     * @throws NoSuchFieldException 不存在这个属性
     */
    public static Field getDeclaredFieldContainParent(Class<?> clazz, String name) throws NoSuchFieldException {
        Class<?> superclass = clazz.getSuperclass();
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (!superclass.equals(Object.class)) {
                return getDeclaredFieldContainParent(superclass, name);
            }
            throw e;
        }
    }

}
