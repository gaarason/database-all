package gaarason.database.contract.builder;

import gaarason.database.appointment.JoinType;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.BuilderWrapper;
import gaarason.database.lang.Nullable;

import java.util.Collection;

/**
 * 连接
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Join<B extends Builder<B, T, K>, T, K> {


    /**
     * 连接查询, 加入sql片段
     * @param sqlPart sql片段
     * @return 查询构造器
     */
    B joinRaw(@Nullable String sqlPart);

    /**
     * 连接查询, 加入sql片段
     * @param sqlPart sql片段
     * @param parameters 绑定的参数
     * @return 查询构造器
     */
    B joinRaw(@Nullable String sqlPart, @Nullable Collection<?> parameters);

    /**
     * 连接查询
     * @param joinTable 连接的表的表名(带表别名) eg : table as t
     * @param localColumn 本表的字段(不带别名) eg : r_id
     * @param symbol 关系 eg : =
     * @param joinTableColumn 连接的表的字段(带表别名) eg : t.for_r_id
     * @return 查询构造器
     */
    B join(String joinTable, String localColumn, String symbol, String joinTableColumn);

    /**
     * 连接查询
     * @param joinType 连接类型
     * @param joinTable 连接的表的表名(带表别名) eg : table as t
     * @param localColumn 本表的字段(不带别名) eg : r_id
     * @param symbol 关系 eg : =
     * @param joinTableColumn 连接的表的字段(带表别名) eg : t.for_r_id
     * @return 查询构造器
     */
    B join(JoinType joinType, String joinTable, String localColumn, String symbol, String joinTableColumn);

    /**
     * 连接查询
     * @param joinType 连接类型
     * @param joinTable 连接的表名
     * @param joinConditions 连接条件
     * @return 查询构造器
     */
    B join(JoinType joinType, String joinTable, BuilderWrapper<B, T, K> joinConditions);

    /**
     * 连接查询(含子查询)
     * @param joinType 连接类型
     * @param tempTable 临时表
     * @param alias 临时表别名
     * @param joinConditions 连接条件
     * @return 查询构造器
     */
    B join(JoinType joinType, BuilderWrapper<B, T, K> tempTable, String alias,
        BuilderWrapper<B, T, K> joinConditions);
}
