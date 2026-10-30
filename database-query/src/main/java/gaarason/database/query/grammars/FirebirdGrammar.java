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
        // ROWS 是 1-based 闭区间, 且只接受常量(不接受算术表达式)
        long numericOffset = limitNumberToLong(offset, "Firebird");
        long numericTake = limitNumberToLong(take, "Firebird");
        set(SQLPartType.LIMIT, "rows " + (numericOffset + 1) + " to " + (numericOffset + numericTake), null);
    }

    @Override
    public void formatLimit(Object take, Collection<Object> parameters) {
        set(SQLPartType.LIMIT, "rows 1 to " + limitNumberToLong(take, "Firebird"), null);
    }

    @Override
    public String formatUpsertSuffix(Collection<String> quotedColumns, Function<String, String> bracketFn) {
        throw new TypeNotSupportedException(
            "Firebird does not support upsert via ON DUPLICATE KEY UPDATE, use UPDATE OR INSERT instead");
    }
}
