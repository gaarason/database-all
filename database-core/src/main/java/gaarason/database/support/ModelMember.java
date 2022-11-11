package gaarason.database.support;

import gaarason.database.contract.eloquent.Model;
import gaarason.database.core.Container;
import gaarason.database.exception.InvalidPrimaryKeyTypeException;
import gaarason.database.provider.ModelInstanceProvider;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;

/**
 * 格式化后的Model信息
 */
public class ModelMember<T, K> extends Container.SimpleKeeper
    implements Serializable {

    /**
     * model 类型
     */
    private final Class<? extends Model<T, K>> modelClass;

    /**
     * entity 类型
     */
    private final Class<T> entityClass;

    /**
     * 主键类型
     */
    private final Class<K> primaryKeyClass;

    /**
     * entity 信息
     */
    private final EntityMember<T, K> entityMember;

    /**
     * model对象
     */
    private final Model<T, K> model;

    public ModelMember(Container container, Class<? extends Model<T, K>> modelClass) {
        super(container);
        this.modelClass = modelClass;
        this.entityClass = ObjectUtils.getGenerics(modelClass, 0);
        this.primaryKeyClass = ObjectUtils.getGenerics(modelClass, 1);
        this.entityMember = new EntityMember<>(container, entityClass);

        typeCheck();

        // 获取模型对象 (是否是单例, 仅取决于Model实例化工厂), 但是缓存之后就是单例的了~
        this.model = container.getBean(ModelInstanceProvider.class).getModel(modelClass);
    }

    /**
     * 一个简单的检测, 可以避免大量的问题
     */
    private void typeCheck() {
        PrimaryKeyMember<K> primaryKeyMember = entityMember.getPrimaryKeyMember();

        // 主键类型检测( 实体上的主键的类型是否与模型上的泛型一致)
        if (primaryKeyMember != null &&
            !primaryKeyMember.getFieldMember().getField().getType().equals(primaryKeyClass)) {
            throw new InvalidPrimaryKeyTypeException(
                "The primary key type [" + primaryKeyMember.getFieldMember().getField().getType() +
                    "] of the entity does not match with the generic [" + primaryKeyClass + "]");
        }
    }

    // ---------------------------- simple getter ---------------------------- //

    public Class<? extends Model<T, K>> getModelClass() {
        return modelClass;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public EntityMember<T, K> getEntityMember() {
        return entityMember;
    }

    public Class<K> getPrimaryKeyClass() {
        return primaryKeyClass;
    }

    public Model<T, K> getModel() {
        return model;
    }
}
