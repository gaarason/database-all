package gaarason.database.query.grammars;

import gaarason.database.exception.TypeNotSupportedException;

import java.util.Collection;
import java.util.function.Function;

/**
 * DB2 方言语法分析
 * <p>
 * 分页使用 FETCH FIRST n ROWS ONLY 语法
 * @author xt
 */
public class Db2Grammar extends BaseGrammar {

    private static final long serialVersionUID = 1L;

    public Db2Grammar(String tableName) {
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
            " rows fetch first " + replaceValueAndFillParameters(take, parameters) + " rows only";
        set(SQLPartType.LIMIT, sqlPart, parameters);
    }

    @Override
    public void formatLimit(Object take, Collection<Object> parameters) {
        String sqlPart = "fetch first " + replaceValueAndFillParameters(take, parameters) + " rows only";
        set(SQLPartType.LIMIT, sqlPart, parameters);
    }

    @Override
    public String formatUpsertSuffix(Collection<String> quotedColumns, Function<String, String> bracketFn) {
        throw new TypeNotSupportedException(
            "DB2 does not support upsert via ON DUPLICATE KEY UPDATE, use MERGE statement instead");
    }
}
