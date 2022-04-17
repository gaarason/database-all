package gaarason.database.contract.builder;

import gaarason.database.appointment.JoinType;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;

import java.io.Serializable;

/**
 * 连接
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Join<T extends Serializable, K extends Serializable> {


    /**
     * 连接查询, 加入sql片段
     * @param sqlPart sql片段
     * @return 查询构造器
     */
    Builder<T, K> joinRaw(String sqlPart);

    /**
     * 连接查询
     * @param table   连接的表名
     * @param column1 字段1
     * @param symbol  关系
     * @param column2 字段2
     * @return 查询构造器
     */
    Builder<T, K> join(String table, String column1, String symbol, String column2);

    /**
     * 连接查询
     * @param joinType 连接类型
     * @param table    连接的表名
     * @param column1  字段1
     * @param symbol   关系
     * @param column2  字段2
     * @return 查询构造器
     */
    Builder<T, K> join(JoinType joinType, String table, String column1, String symbol,
        String column2);

    /**
     * 连接查询
     * @param joinType       连接类型
     * @param table          连接的表名
     * @param joinConditions 连接条件
     * @return 查询构造器
     */
    Builder<T, K> join(JoinType joinType, String table, GenerateSqlPartFunctionalInterface<T, K> joinConditions);

    /**
     * 连接查询(含子查询)
     * @param joinType       连接类型
     * @param tempTable      临时表
     * @param alias          临时表别名
     * @param joinConditions 连接条件
     * @return 查询构造器
     */
    Builder<T, K> join(JoinType joinType, GenerateSqlPartFunctionalInterface<T, K> tempTable, String alias,
        GenerateSqlPartFunctionalInterface<T, K> joinConditions);
}
