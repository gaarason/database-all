package gaarason.database.contract.function;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface ExecSqlWithinConnection<U> {
    U exec(PreparedStatement preparedStatement) throws SQLException;
}
