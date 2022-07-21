package gaarason.database.contract.eloquent;

import gaarason.database.appointment.Column;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
import gaarason.database.contract.record.Friendly;
import gaarason.database.contract.record.OperationLambda;
import gaarason.database.contract.record.RelationshipLambda;
import gaarason.database.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 结果集
 * @author xt
 */
public interface Record<T, K> extends Friendly<T, K>, OperationLambda<T, K>,
    RelationshipLambda<T, K> {

    /**
     * 本表元数据
     * <数据库字段名 -> 字段信息>
     * @return 本表元数据
     */
    Map<String, Column> getMetadataMap();

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
     * @return 数据实体
     */
    T getEntity();

    /**
     * 数据实体(将外部实体的覆盖到自身, 会更改引用)
     * @param entity 实体对象
     * @return 数据实体
     */
    T getEntity(T entity);

    /**
     * 数据实体(将外部实体的属性合并到自身)
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
     * 关联关系 Builder 补充设置
     * @return <属性 -> GenerateSqlPart>
     */
    Map<String, GenerateSqlPartFunctionalInterface<T, K>> getRelationBuilderMap();

    /**
     * 关联关系 Builder 补充设置
     * @param relationBuilderMap <属性 -> GenerateSqlPart>
     */
    void setRelationBuilderMap(HashMap<String, GenerateSqlPartFunctionalInterface<T, K>> relationBuilderMap);

    /**
     * 关联关系 递归 设置
     * @return <属性 -> RelationshipRecordWith>
     */
    Map<String, RelationshipRecordWithFunctionalInterface> getRelationRecordMap();

    /**
     * 关联关系 递归 设置
     * @param relationRecordMap <属性 -> RelationshipRecordWith>
     */
    void setRelationRecordMap(HashMap<String, RelationshipRecordWithFunctionalInterface> relationRecordMap);

}
