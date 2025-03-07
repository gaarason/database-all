package gaarason.database.support;

import gaarason.database.annotation.BelongsTo;
import gaarason.database.annotation.BelongsToMany;
import gaarason.database.annotation.HasOneOrMany;
import gaarason.database.annotation.base.Relation;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.relation.RelationSubQuery;
import gaarason.database.core.Container;
import gaarason.database.eloquent.relation.BelongsToManyQueryRelation;
import gaarason.database.eloquent.relation.BelongsToQueryRelation;
import gaarason.database.eloquent.relation.HasOneOrManyQueryRelation;
import gaarason.database.exception.IllegalAccessRuntimeException;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.util.ClassUtils;
import gaarason.database.util.EntityUtils;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 数据库关联关系字段信息
 * @see gaarason.database.annotation.Column
 */
public class FieldRelationMember extends Container.SimpleKeeper implements Serializable {

    /**
     * Field (已经 设置属性是可访问)
     */
    private final Field field;

    /**
     * 是否是复数(数组/集合)
     */
    private final boolean plural;

    /**
     * 属性名
     */
    private final String name;

    /**
     * java中的字段类型
     */
    private final Class<?> javaType;

    /**
     * java中的字段类型
     * 当本类型是非复数时, 此处等价于 javaType
     * 当本类型是复数时, 为集合中的泛型(不支持MAP等多泛型的)
     */
    private final Class<?> javaRealType;

    /**
     * java中的字段类型组成的空数组
     * eg : String[]
     */
    private final Object[] javaRealTypeEmptyArray;

    /**
     * 关联关系注解处理器
     */
    private final RelationSubQuery relationSubQuery;

    /**
     * @param container 容器
     * @param field 字段
     * @param model 数据模型
     */
    public FieldRelationMember(Container container, Field field, Model<?, ?, ?> model) {
        super(container);
        ModelShadowProvider modelShadowProvider = container.getBean(ModelShadowProvider.class);
        this.field = field;
        this.name = field.getName();
        this.javaType = field.getType();
        // 集合或者数组
        this.plural = ObjectUtils.isCollection(field.getType()) || javaType.isArray();

        this.javaRealType = EntityUtils.getRealClass(field);

        this.javaRealTypeEmptyArray = ObjectUtils.typeCast(Array.newInstance(javaRealType, 0));

        // 预置注解
        if (field.isAnnotationPresent(BelongsTo.class)) {
            relationSubQuery = new BelongsToQueryRelation(field, modelShadowProvider, model);
        } else if (field.isAnnotationPresent(BelongsToMany.class)) {
            relationSubQuery = new BelongsToManyQueryRelation(field, modelShadowProvider, model);
        } else if (field.isAnnotationPresent(HasOneOrMany.class)) {
            relationSubQuery = new HasOneOrManyQueryRelation(field, modelShadowProvider, model);
        } else {
            // 自定义注解
            flag:
            {
                Annotation[] annotations = field.getDeclaredAnnotations();
                for (Annotation annotation : annotations) {
                    Class<? extends Annotation> annotationClass = annotation.annotationType();
                    // 自定义注解
                    Relation annotationClassAnnotation = annotationClass.getAnnotation(Relation.class);
                    if (null != annotationClassAnnotation) {
                        relationSubQuery = ClassUtils.newInstance(annotationClassAnnotation.value(),
                            new Class<?>[]{Field.class, ModelShadowProvider.class, Model.class},
                            new Object[]{field, modelShadowProvider, model});
                        break flag;
                    }
                }
                throw new RuntimeException();
            }
        }
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
     * 设置属性的值
     * a. 支持向集合中赋值
     * b. 支持 list(linkedList/ArrayList) 与 set(LinkedHashSet) 集合
     * @param obj 对象
     * @param value 值
     * @throws IllegalAccessRuntimeException 反射赋值异常
     */
    public void fieldSet(Object obj, @Nullable List<?> value) {
        Object readyValue;
        if (value == null) {
            readyValue = null;
        } else if (ArrayList.class.isAssignableFrom(javaType)) {
            readyValue = new ArrayList<>(value);
        } else if (List.class.isAssignableFrom(javaType)) {
            readyValue = new LinkedList<>(value);
        } else if (Set.class.isAssignableFrom(javaType)) {
            readyValue = new LinkedHashSet<>(value);
        } else if (javaType.isArray()) {
            readyValue = value.toArray(javaRealTypeEmptyArray);
        } else {
            readyValue = value;
        }
        fieldSet(obj, readyValue);
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

    // ---------------------------- simple getter ---------------------------- //

    public Field getField() {
        return field;
    }

    public boolean isPlural() {
        return plural;
    }

    public String getName() {
        return name;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public Class<?> getJavaRealType() {
        return javaRealType;
    }

    public RelationSubQuery getRelationSubQuery() {
        return relationSubQuery;
    }
}
