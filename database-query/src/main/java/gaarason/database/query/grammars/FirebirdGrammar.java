package gaarason.database.query.grammars;

import gaarason.database.exception.TypeNotSupportedException;

import java.util.Collection;
import java.util.function.Function;

/**
 * Firebird 方言语法分析
 * <p>
 * 分页使用 ROWS m TO n 语法
 * @author xt
 */
public class FirebirdGrammar extends BaseGrammar {

    private static final long serialVersionUID = 1L;

    public FirebirdGrammar(String tableName) {
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
        // ROWS (offset+1) TO (offset+take)
        // Firebird ROWS 是 1-based: ROWS startRow TO endRow
        String sqlPart = "rows " + replaceValueAndFillParameters(offset, parameters) +
            " + 1 to " + replaceValueAndFillParameters(offset, parameters) +
            " + " + replaceValueAndFillParameters(take, parameters);
        set(SQLPartType.LIMIT, sqlPart, parameters);
    }

    @Override
    public void formatLimit(Object take, Collection<Object> parameters) {
        String sqlPart = "rows " + replaceValueAndFillParameters(take, parameters);
        set(SQLPartType.LIMIT, sqlPart, parameters);
    }

    @Override
    public String formatUpsertSuffix(Collection<String> quotedColumns, Function<String, String> bracketFn) {
        throw new TypeNotSupportedException(
            "Firebird does not support upsert via ON DUPLICATE KEY UPDATE, use UPDATE OR INSERT instead");
    }
}
