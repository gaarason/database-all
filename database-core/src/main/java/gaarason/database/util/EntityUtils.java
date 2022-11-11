package gaarason.database.util;

import gaarason.database.annotation.Column;
import gaarason.database.annotation.Table;
import gaarason.database.appointment.FinalVariable;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.exception.IllegalAccessRuntimeException;
import gaarason.database.lang.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 实体处理工具
 * @author xt
 */
public final class EntityUtils {

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
     * 判断字段是否可以赋值null
     * @param field 字段
     * @return 是否
     */
    public static boolean isFieldCanBeNull(Field field) {
        return !FinalVariable.NOT_ACCEPT_NULL_TYPES.contains(field.getType());
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
     * 将 complementEntity 中的属性赋值到 baseEntity 上
     * 1. 针对于非静态属性 2. complementEntity中所有的null属性都会跳过 (所以一定要使用包装类型来声明 entity)
     * @param baseEntity 基本实体对象(不会被修改)
     * @param complementEntity 合并实体对象
     * @param <T> 实体类型
     * @return 合并后的对象
     */
    public static <T> T entityMerge(T baseEntity, T complementEntity) {
        final T copyEntity = ObjectUtils.deepCopy(baseEntity);
        entityMergeReference(copyEntity, complementEntity, true);
        return copyEntity;
    }

    /**
     * 将 complementEntity 中的属性赋值到 baseEntity 上
     * 1. 针对于非静态属性
     * @param baseEntity 基本实体对象(会被直接修改)
     * @param complementEntity 合并实体对象
     * @param skipNull 是否跳过complementEntity中值为null的属性
     * @param <T> 实体类型
     */
    public static <T> void entityMergeReference(T baseEntity, T complementEntity, boolean skipNull) {
        try {
            List<Field> fields = getDeclaredFieldsContainParentWithoutStatic(complementEntity.getClass());
            for (Field field : fields) {
                field.setAccessible(true);
                final Object val = field.get(complementEntity);
                if (!skipNull || val != null) {
                    field.set(baseEntity, val);
                }
            }
        } catch (Throwable e) {
            throw new IllegalAccessRuntimeException(e);
        }
    }

    /**
     * 返回 clazz 中的所有属性(public/protected/private/default),含static, 含父类, 不含接口的
     * 移除属性名包含this$的属性
     * @param clazz 类型
     * @return 所有字段列表(子类的更加靠前)
     */
    public static List<Field> getDeclaredFieldsContainParent(Class<?> clazz) {
        List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));

        // 简单的避免内部类的影响, 当然这个方式可能会误判, 但是的确简单
        fields.removeIf(next -> next.getName().contains("this$"));

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && !superclass.equals(Object.class)) {
            fields.addAll(getDeclaredFieldsContainParent(superclass));
        }
        return fields;
    }

    /**
     * 返回 clazz 中的所有属性(public/protected/private/default), 含父类, 不含static, 不含接口的
     * 移除属性名包含this$的属性
     * @param clazz 类型
     * @return 所有字段列表(子类的更加靠前)
     */
    public static List<Field> getDeclaredFieldsContainParentWithoutStatic(Class<?> clazz) {
        List<Field> containStatic = getDeclaredFieldsContainParent(clazz);
        return containStatic.stream().filter(field -> !EntityUtils.isStaticField(field)).collect(Collectors.toList());
    }

    /**
     * 返回 clazz 中的指定属性(public/protected/private)
     * 依次向上, 找到为止
     * @param clazz 类型
     * @param name 属性名称
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

    /**
     * 尝试在entity的内部类中, 找到其对应的model
     * @param anyEntityClass 实体类
     * @return model类
     */
    @Nullable
    public static Class<? extends Model<?, ?>> inferModelClassOnEntity(Class<?> anyEntityClass) {
        // 所有内部类
        for (Class<?> maybeModelClass : anyEntityClass.getDeclaredClasses()) {
            // 是否是 Model
            if (Model.class.isAssignableFrom(maybeModelClass)) {
                /*
                 * 进一步检查 Entity
                 * 不是抽象类, 且泛型参数正确
                 */
                if (!Modifier.isAbstract(maybeModelClass.getModifiers())) {
                    try {
                        if (anyEntityClass.equals(ObjectUtils.getGenerics(maybeModelClass, 0))) {
                            return ObjectUtils.typeCast(maybeModelClass);
                        }
                    } catch (Throwable ignore) {
                    }
                }
            }
        }
        return null;
    }

}
