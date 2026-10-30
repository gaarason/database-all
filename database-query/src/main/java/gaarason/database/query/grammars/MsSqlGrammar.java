package gaarason.database.query.grammars;

import gaarason.database.appointment.SqlType;
import gaarason.database.exception.TypeNotSupportedException;
import gaarason.database.lang.Nullable;

import java.util.Collection;
import java.util.function.Function;

/**
 * SQL Server 方言语法分析
 * @author xt
 */
public class MsSqlGrammar extends BaseGrammar {

    private static final long serialVersionUID = 1L;

    /**
     * 仅限制条数时的 TOP 取值, 为 null 表示使用 OFFSET/FETCH
     */
    @Nullable
    private String topTake;

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
        topTake = null;
        // 取值已经过 limitNumberToLong 校验, 内联可以避免无排序补位时的占位符顺序错乱
        String sqlPart = "offset " + limitNumberToLong(offset, "SQL Server") + " rows fetch next " +
            limitNumberToLong(take, "SQL Server") + " rows only";
        set(SQLPartType.LIMIT, sqlPart, parameters);
    }

    @Override
    public void formatLimit(Object take, Collection<Object> parameters) {
        // OFFSET/FETCH 强制要求 ORDER BY, 仅限制条数时改用不需要排序的 TOP
        clear(SQLPartType.LIMIT);
        topTake = String.valueOf(limitNumberToLong(take, "SQL Server"));
    }

    @Override
    public SQLPartInfo generateSql(SqlType sqlType) {
        SQLPartInfo result = super.generateSql(sqlType);
        if (sqlType != SqlType.SELECT) {
            return result;
        }
        String sql = result.getSqlString();
        if (topTake != null) {
            if (sql.startsWith("select ")) {
                sql = "select top " + topTake + " " + sql.substring("select ".length());
            }
            return new SQLPartInfo(sql, result.getParameters());
        }
        if (!isEmpty(SQLPartType.LIMIT) && isEmpty(SQLPartType.ORDER)) {
            // 没排序时补齐一个无副作用的排序, 以满足 OFFSET/FETCH 的语法要求
            int index = sql.indexOf(" offset ");
            if (index >= 0) {
                sql = sql.substring(0, index) + " order by (select null)" + sql.substring(index);
            }
            return new SQLPartInfo(sql, result.getParameters());
        }
        return result;
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
