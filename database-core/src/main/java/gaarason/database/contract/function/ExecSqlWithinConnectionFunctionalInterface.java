package gaarason.database.contract.function;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface ExecSqlWithinConnectionFunctionalInterface<U> {
    U execute(PreparedStatement preparedStatement) throws SQLException;
}
