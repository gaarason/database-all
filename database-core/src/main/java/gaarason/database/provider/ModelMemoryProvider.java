package gaarason.database.provider;

import gaarason.database.contracts.eloquent.relations.SubQuery;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.annotations.BelongsTo;
import gaarason.database.eloquent.annotations.BelongsToMany;
import gaarason.database.eloquent.annotations.Column;
import gaarason.database.eloquent.annotations.HasOneOrMany;
import gaarason.database.eloquent.relations.BelongsToManyQuery;
import gaarason.database.eloquent.relations.BelongsToQuery;
import gaarason.database.eloquent.relations.HasOneOrManyQuery;
import gaarason.database.utils.StringUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModelMemoryProvider {

    /**
     * 允许的java属性
     */
    public final static Class<?>[] basicType = new Class[]{Boolean.class, Byte.class, Character.class, Short.class,
        Integer.class, Long.class, Float.class, Double.class, BigInteger.class, Date.class};

    /**
     * 关联关系注解
     */
    public final static Class<? extends Annotation>[] relationAnnotations = new Class[]{HasOneOrMany.class,
        BelongsTo.class, BelongsToMany.class};

    /**
     * 启用
     */
    protected static boolean enable = true;

    /**
     * 惰性
     */
    protected static boolean lazy = true;

    /**
     * Model做为索引
     */
    protected static Map<Class<?>, Information<?, ?>> ModelMap = new ConcurrentHashMap<>();

    /**
     * Entity作为索引
     */
    protected static Map<Class<?>, Information<?, ?>> EntityMap = new ConcurrentHashMap<>();

    /**
     * 格式化后的信息
     */
    public static class Information<T, K> {

        /**
         * model类
         */
        protected Class<?> modelClass;

        /**
         * model对象
         */
        protected Model<T, K> model;

        /**
         * entity类
         */
        protected Class<T> entityClass;

        /**
         * 主键列名(并非一定是实体的属性名)
         */
        protected String primaryKeyColumnName;

        /**
         * 主键名(实体的属性名)
         */
        protected String primaryKeyName;

        /**
         * 主键自增
         */
        protected boolean primaryKeyIncrement;

        /**
         * 主键类型
         */
        protected Class<K> primaryKeyClass;

        /**
         * 数据库表名
         */
        protected String tableName;

        /**
         * 属性名对应的字段数组
         */
        protected Map<String, FieldInfo> javaFieldMap = new ConcurrentHashMap<>();

        /**
         * 数据库字段名对应的字段数组
         */
        protected Map<String, FieldInfo> columnFieldMap = new ConcurrentHashMap<>();

        /**
         * 关联关系属性名对应的字段数组
         */
        protected Map<String, FieldInfo> relationFieldMap = new ConcurrentHashMap<>();

    }

    /**
     * 字段信息
     */
    public static class FieldInfo {

        /**
         * Field
         */
        protected Field field;

        /**
         * 属性名
         */
        protected String name;

        /**
         * 字段名
         */
        protected String columnName;

        /**
         * java中的字段类型
         */
        protected Class<?> javaType;

        /**
         * 数据库中的字段类型
         */
//        protected String jdbcType;

        /**
         * column 注解
         */
        @Nullable
        protected Column column;

        /**
         * 关联关系注解
         */
        @Nullable
        protected SubQuery subQuery;
    }

    /**
     * 开关
     * @param isEnable 启用
     * @param islLazy  惰性
     */
    public static void turnOnOrOff(boolean isEnable, boolean islLazy) {
        enable = isEnable;
        lazy = islLazy;
    }

    /**
     * @param model
     * @param <T>
     * @param <K>
     */
    public static <T, K> void get(Model<T, K> model) {
        ModelMap.computeIfAbsent(model.getClass(), key -> {

            Information<T, K> information = new Information<>();
            information.modelClass = key;
            information.model = model;
            information.tableName = model.getTableName();
            information.entityClass = model.getEntityClass();
            information.primaryKeyClass = model.getPrimaryKeyClass();
            information.primaryKeyColumnName = model.getPrimaryKeyColumnName();
            information.primaryKeyIncrement = model.isPrimaryKeyIncrement();
            information.primaryKeyName = information.primaryKeyName;

//            FieldInfo fieldInfo = new FieldInfo();

            fieldDeal(information);
//            information.classFieldMap

            return null;
        });
    }

    /**
     * @param entity
     * @param <T>
     * @param <K>
     */
    public static <T, K> void get(Class<?> entity) {

    }


    protected static <T, K> void fieldDeal(Information<T, K> information) {

        Class<T> entityClass = information.entityClass;
        Field[]  fields      = entityClass.getDeclaredFields();

        for (Field field : fields) {
            // 静态属性, 暂不处理
            if (Modifier.isStatic(field.getModifiers())) {
                break;
            }
            // 设置属性是可访问
            field.setAccessible(true);
            FieldInfo fieldInfo = new FieldInfo();
            fieldInfo.field = field;
            fieldInfo.name = field.getName();
            fieldInfo.javaType = field.getType();


            // 非基本类型, 一般是关联关系
            if (!field.getType().isPrimitive() && !Arrays.asList(basicType).contains(field.getType())) {
                if (field.isAnnotationPresent(BelongsTo.class)) {
                    fieldInfo.subQuery = new BelongsToQuery(field);
                    information.javaFieldMap.put(fieldInfo.name, fieldInfo);
                } else if (field.isAnnotationPresent(BelongsToMany.class)) {
                    fieldInfo.subQuery = new BelongsToManyQuery(field);
                    information.javaFieldMap.put(fieldInfo.name, fieldInfo);
                } else if (field.isAnnotationPresent(HasOneOrMany.class)) {
                    fieldInfo.subQuery = new HasOneOrManyQuery(field);
                    information.javaFieldMap.put(fieldInfo.name, fieldInfo);
                } else {
                    break;
                }
            }


            String nameInColumn = "";
            // 数据库属性
            if (field.isAnnotationPresent(Column.class)) {
                fieldInfo.column = field.getAnnotation(Column.class);
                // 注解中的名字
                nameInColumn = fieldInfo.column.name();
            }

            fieldInfo.columnName = "".equals(nameInColumn)
                ? StringUtil.lineToHump(fieldInfo.name, false)
                : nameInColumn;


//            fieldInfo
        }

    }

    /**
     * 是否四有效的关联关系字段
     * @param field
     * @return
     */
    protected boolean effectiveRelationField(Field field) {
        // 非基础类型
        boolean isNotPrimitive = !field.getType().isPrimitive();
        // 非包装类型
        boolean isNotBasicType = !Arrays.asList(basicType).contains(field.getType());
        // 有相应的注解
        boolean hasRelationAnnotation = false;
        for (Class<? extends Annotation> relationAnnotation : relationAnnotations) {
            if (field.isAnnotationPresent(relationAnnotation)) {
                hasRelationAnnotation = true;
            }
        }
        return isNotPrimitive && isNotBasicType && hasRelationAnnotation;

    }

}
