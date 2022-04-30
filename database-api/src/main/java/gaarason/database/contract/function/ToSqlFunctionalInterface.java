package gaarason.database.contract.function;

import java.util.List;

/**
 * 生成sql
 * @author xt
 */
@FunctionalInterface
public interface ToSqlFunctionalInterface {

    /**
     * 进行sql与绑定的参数的拼接
     * @param sql SQL语句 eg: select * from table where id = ?
     * @param parameters 绑定的参数 eg: [3]
     * @return 可视的SQL语句 eg: select * from table where id = 3
     */
    String execute(String sql, List<String> parameters);

}
