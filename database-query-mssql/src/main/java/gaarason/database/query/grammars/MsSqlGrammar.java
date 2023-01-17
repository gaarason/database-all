package gaarason.database.query.grammars;

/**
 * mssql 语法分析
 * @author xt
 */
public class MsSqlGrammar extends BaseGrammar {

    private static final long serialVersionUID = 1L;

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
