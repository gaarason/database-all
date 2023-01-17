package gaarason.database.contract.function;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 在数据库连接中执行sql
 * @param <U>
 * @author xt
 */
@FunctionalInterface
public interface ExecSqlWithinConnectionFunctionalInterface<U> {

    /**
     * 在数据库连接中执行sql
     * @param preparedStatement sql准备
     * @return 响应
     * @throws SQLException sql异常
     */
    U execute(PreparedStatement preparedStatement) throws SQLException;
}
