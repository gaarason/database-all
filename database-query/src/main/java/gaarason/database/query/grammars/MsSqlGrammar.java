package gaarason.database.query.grammars;

import gaarason.database.exception.TypeNotSupportedException;

import java.util.Collection;
import java.util.function.Function;

/**
 * SQL Server 方言语法分析
 * @author xt
 */
public class MsSqlGrammar extends BaseGrammar {

    private static final long serialVersionUID = 1L;

    public MsSqlGrammar(String tableName) {
        super(tableName, "\"");
    }

    @Override
    public String getKeyword(SQLPartType sqlPartType) {
        if (sqlPartType == SQLPartType.LIMIT) {
            return " ";
        }
        return super.getKeyword(sqlPartType);
    }

    @Override
    public void formatLimit(Object offset, Object take, Collection<Object> parameters) {
        String sqlPart = "offset " + replaceValueAndFillParameters(offset, parameters) +
            " rows fetch next " + replaceValueAndFillParameters(take, parameters) + " rows only";
        set(SQLPartType.LIMIT, sqlPart, parameters);
    }

    @Override
    public void formatLimit(Object take, Collection<Object> parameters) {
        formatLimit(0, take, parameters);
    }

    @Override
    public String formatUpsertSuffix(Collection<String> quotedColumns, Function<String, String> bracketFn) {
        throw new TypeNotSupportedException(
            "MSSQL does not support upsert via ON DUPLICATE KEY UPDATE, use MERGE statement instead");
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
