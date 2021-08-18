package gaarason.database.contract.eloquent;

import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
import gaarason.database.contract.record.Friendly;
import gaarason.database.contract.record.Operation;
import gaarason.database.contract.record.Relationship;
import gaarason.database.core.lang.Nullable;
import gaarason.database.support.Column;

import java.io.Serializable;
import java.util.Map;

/**
 * 结果集
 * @author xt
 */
public interface Record<T, K> extends Friendly<T, K>, Operation<T, K>, Relationship<T, K>, Serializable {

    /**
     * 本表元数据
     * <数据库字段名 -> 字段信息>
     * @return 本表元数据
     */
    Map<String, Column> getMetadataMap();

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
    Map<String, GenerateSqlPartFunctionalInterface> getRelationBuilderMap();

    /**
     * 关联关系 Builder 补充设置
     * @param relationBuilderMap <属性 -> GenerateSqlPart>
     */
    void setRelationBuilderMap(Map<String, GenerateSqlPartFunctionalInterface> relationBuilderMap);

    /**
     * 关联关系 递归 设置
     * @return <属性 -> RelationshipRecordWith>
     */
    Map<String, RelationshipRecordWithFunctionalInterface> getRelationRecordMap();

    /**
     * 关联关系 递归 设置
     * @param relationRecordMap <属性 -> RelationshipRecordWith>
     */
    void setRelationRecordMap(Map<String, RelationshipRecordWithFunctionalInterface> relationRecordMap);

}
