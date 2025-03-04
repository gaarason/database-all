package gaarason.database.contract.eloquent;

import gaarason.database.contract.function.BuilderAnyWrapper;
import gaarason.database.contract.function.RecordWrapper;
import gaarason.database.contract.record.Friendly;
import gaarason.database.contract.record.OperationLambda;
import gaarason.database.contract.record.RelationshipLambda;
import gaarason.database.contract.support.ExtendedSerializable;
import gaarason.database.lang.Nullable;

import java.io.Serializable;
import java.util.Map;

/**
 * 结果集
 * @author xt
 */
public interface Record<T, K> extends Friendly<T, K>, OperationLambda<T, K>,
    RelationshipLambda<T, K>, ExtendedSerializable {

    /**
     * 本表元数据
     * <数据库字段名 -> 字段信息>
     * @return 本表元数据
     */
    Map<String, Object> getMetadataMap();

    /**
     * 是否已经绑定具体的数据
     * @return bool
     */
    boolean isHasBind();

    /**
     * 数据模型
     * @return 数据模型
     */
    Model<T, K> getModel();

    /**
     * 原Sql
     * @return 原Sql
     */
    String getOriginalSql();

    /**
     * 数据实体
     * 此对于此实体的修改, 可以通过其相关的 record 进行保存(save)等操作
     * @return 数据实体
     */
    T getEntity();

    /**
     * 数据实体(将外部实体的覆盖到自身, 会更改引用)
     * 可以通过其相关的 record 进行保存(save)等操作
     * @param entity 实体对象
     * @return 数据实体
     */
    T getEntity(T entity);

    /**
     * 数据实体(将外部实体的属性合并到自身)
     * 可以通过其相关的 record 进行保存(save)等操作
     * @param entity 实体对象
     * @return 查询结果集
     */
    Record<T, K> fillEntity(T entity);

    /**
     * 主键值
     * @return 主键值
     */
    @Nullable
    K getOriginalPrimaryKeyValue();

    /**
     * 主键值
     * @param value 主键值
     */
    void setOriginalPrimaryKeyValue(K value);

    /**
     * 关联关系 补充设置
     * @return 关联关系 补充设置
     */
    Map<String, Relation> getRelationMap();

    /**
     * 反序列化到指定结果集
     * @param bytes 序列化byte[]
     * @param <M> 实体类型
     * @param <N> 主键类型
     * @return 结果集对象
     */
    static <M, N> Record<M, N> deserialize(byte[] bytes) {
        return ExtendedSerializable.deserialize(bytes);
    }

    /**
     * 反序列化到指定结果集
     * @param serializeStr 序列化String
     * @param <M> 实体类型
     * @param <N> 主键类型
     * @return 结果集对象
     */
    static <M, N> Record<M, N> deserialize(String serializeStr) {
        return ExtendedSerializable.deserialize(serializeStr);
    }

    /**
     * 关联关系信息
     */
    class Relation implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 是否关联关系操作
         */
        final public boolean relationOperation;

        /**
         * 关联关系属性
         * 此属性上必然存在关联关系注解
         */
        final public String relationFieldName;

        /**
         * 操作构造器包装
         */
        final public BuilderAnyWrapper operationBuilder;

        /**
         * 查询构造器包装
         */
        final public BuilderAnyWrapper customBuilder;

        /**
         * 查询结果集包装
         */
        final public RecordWrapper recordWrapper;

        public Relation(String relationFieldName, BuilderAnyWrapper operationBuilder,
                BuilderAnyWrapper customBuilder, RecordWrapper recordWrapper) {
            this.relationOperation = true;
            this.operationBuilder = operationBuilder;
            this.relationFieldName = relationFieldName;
            this.customBuilder = customBuilder;
            this.recordWrapper = recordWrapper;
        }

        public Relation(String relationFieldName, BuilderAnyWrapper customBuilder, RecordWrapper recordWrapper) {
            this.relationOperation = false;
            this.operationBuilder = BuilderAnyWrapper.empty();
            this.relationFieldName = relationFieldName;
            this.customBuilder = customBuilder;
            this.recordWrapper = recordWrapper;
        }
    }
}
