package gaarason.database.contract.builder;

import gaarason.database.appointment.SqlType;
import gaarason.database.contract.function.ToSqlFunctionalInterface;

/**
 * 调试
 * @author xt
 */
public interface Debug {

    /**
     * 转化为数据库查询语句(不会执行)
     * @param sqlType 查询/更新
     * @return 数据库查询语句
     */
    String toSql(SqlType sqlType);

    /**
     * 转化为数据库查询语句(不会执行)
     * @param sqlType 查询/更新
     * @param closure SQL生成的方式
     * @return 数据库查询语句
     */
    String toSql(SqlType sqlType, ToSqlFunctionalInterface closure);
}
