package gaarason.database.query.grammars;

import gaarason.database.exception.TypeNotSupportedException;

import java.util.Collection;
import java.util.function.Function;

/**
 * Oracle 12c 及以上方言语法分析
 * <p>
 * 适用于: Oracle 12c+, 达梦(DM), Derby, 虚谷(Xugu), GOLDILOCKS 等使用
 * OFFSET n ROWS FETCH NEXT m ROWS ONLY 分页语法的数据库
 * @author xt
 */
public class Oracle12cGrammar extends BaseGrammar {

    private static final long serialVersionUID = 1L;

    public Oracle12cGrammar(String tableName) {
        super(tableName, "\"");
    }

    public Oracle12cGrammar(String tableName, String symbol) {
        super(tableName, symbol);
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
            "This database does not support upsert via ON DUPLICATE KEY UPDATE, use MERGE statement instead");
    }
}
