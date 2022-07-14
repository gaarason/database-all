package gaarason.database.support;

import gaarason.database.annotation.BelongsTo;
import gaarason.database.annotation.BelongsToMany;
import gaarason.database.annotation.HasOneOrMany;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.relation.RelationSubQuery;
import gaarason.database.core.Container;
import gaarason.database.eloquent.relation.BelongsToManyQueryRelation;
import gaarason.database.eloquent.relation.BelongsToQueryRelation;
import gaarason.database.eloquent.relation.HasOneOrManyQueryRelation;
import gaarason.database.exception.IllegalAccessRuntimeException;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

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
     * 是否是集合
     */
    private final boolean collection;

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
     * 当本类型是非集合时, 此处等价于 javaType
     * 当本类型是集合时, 为集合中的泛型(不支持MAP等多泛型的)
     */
    private final Class<?> javaRealType;

    /**
     * 关联关系注解
     */
    private final RelationSubQuery relationSubQuery;

    /**
     * @param container 容器
     * @param field 字段
     * @param model 数据模型
     */
    public FieldRelationMember(Container container, Field field, Model<?, ?> model) {
        super(container);
        ModelShadowProvider modelShadowProvider = container.getBean(ModelShadowProvider.class);
        this.field = field;
        this.name = field.getName();
        this.javaType = field.getType();
        this.collection = ObjectUtils.isCollection(field.getType());

        this.javaRealType = collection ?
            ObjectUtils.getGenerics((ParameterizedType) field.getGenericType(), 0) : javaType;

        if (field.isAnnotationPresent(BelongsTo.class)) {
            relationSubQuery = new BelongsToQueryRelation(field, modelShadowProvider, model);
        } else if (field.isAnnotationPresent(BelongsToMany.class)) {
            relationSubQuery = new BelongsToManyQueryRelation(field, modelShadowProvider, model);
        } else if (field.isAnnotationPresent(HasOneOrMany.class)) {
            relationSubQuery = new HasOneOrManyQueryRelation(field, modelShadowProvider, model);
        } else {
            throw new RuntimeException();
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

    public boolean isCollection() {
        return collection;
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
