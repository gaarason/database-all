package gaarason.database.query.grammars;

/**
 * mssql 语法分析基类
 * @author xt
 */
public class MsSqlGrammar extends BaseGrammar {

    public MsSqlGrammar(String tableName) {
        super(tableName);
    }

    /**
     * 开启标识列插入显式值
     * @param table 表名
     * @return 语句
     */
    protected static String identityInsertOn(String table) {
        return "set IDENTITY_INSERT " + table + " ON ";
    }

}
