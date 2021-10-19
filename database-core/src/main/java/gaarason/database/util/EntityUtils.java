package gaarason.database.util;

import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.annotation.Column;
import gaarason.database.eloquent.annotation.Table;
import gaarason.database.eloquent.appointment.FinalVariable;
import gaarason.database.exception.IllegalAccessRuntimeException;
import gaarason.database.exception.TypeNotSupportedException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 实体处理工具
 * @author xt
 */
public class EntityUtils {

    private EntityUtils() {
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
        return StringUtils.humpToLine(split[split.length - 1]);
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
        return FinalVariable.ALLOW_FIELD_TYPES.contains(field.getType());
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
        return StringUtils.humpToLine(field.getName());
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
                String columnName = EntityUtils.columnName(field);
                gaarason.database.support.Column column = stringColumnMap.get(columnName);
                if (column != null) {
                    field.set(entity, column.getValue());
                }
            }
            return entity;
        } catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
            throw new TypeNotSupportedException(e.getMessage(), e);
        }
    }

    /**
     * 将 complementEntity 中的属性赋值到 baseEntity 上
     * 1. 针对于单一非静态属性 2. complementEntity中所有的null属性都会跳过 (所以一定要使用包装类型来声明 entity)
     * @param baseEntity       基本实体对象(不会呗修改)
     * @param complementEntity 合并实体对象
     * @param <T>              实体类型
     * @return 合并后的对象
     */
    public static <T> T entityMerge(T baseEntity, T complementEntity) {
        final T copyEntity = ObjectUtils.deepCopy(baseEntity);
        try {
            for (Field field : complementEntity.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                final Object val = field.get(complementEntity);
                if (val != null) {
                    field.set(copyEntity, val);
                }
            }
            return copyEntity;
        } catch (Throwable e) {
            throw new IllegalAccessRuntimeException(e);
        }
    }

    /**
     * 将 complementEntity 中的属性赋值到 baseEntity 上
     * 1. 针对于单一非静态属性 2. complementEntity中所有的null属性都会跳过 (所以一定要使用包装类型来声明 entity)
     * @param baseEntity       基本实体对象(会被直接修改)
     * @param complementEntity 合并实体对象
     * @param <T>              实体类型
     */
    public static <T> void entityMergeReference(T baseEntity, T complementEntity) {
        try {
            for (Field field : complementEntity.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                final Object val = field.get(complementEntity);
                if (val != null) {
                    field.set(baseEntity, val);
                }
            }
        } catch (Throwable e) {
            throw new IllegalAccessRuntimeException(e);
        }
    }

    /**
     * 格式化值到字符串
     * 关键
     * @param value 原值 (实体的属性)
     * @return 字符串
     */
    @Nullable
    public static String valueFormat(@Nullable Object value) {
        if (value instanceof Date) {
            return LocalDateUtils.SIMPLE_DATE_FORMAT.get().format(value);
        } else if (value instanceof Boolean) {
            return (boolean) value ? "1" : "0";
        } else {
            return value == null ? null : value.toString();
        }
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
