package gaarason.database.query.grammars;

import gaarason.database.appointment.SqlType;
import gaarason.database.exception.TypeNotSupportedException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Function;

/**
 * Informix 方言语法分析
 * <p>
 * 适用于: Informix, GBase 8s, 星瑞格(SinoDB)
 * 分页使用 SKIP m FIRST n 语法(置于 SELECT 关键字之后)
 * @author xt
 */
public class InformixGrammar extends BaseGrammar {

    private static final long serialVersionUID = 1L;

    /**
     * 暂存 SKIP/FIRST 子句, 在 generateSql 时插入 SELECT 之后
     */
    private String skipFirstClause = "";
    private Collection<Object> skipFirstParameters = new LinkedList<>();

    public InformixGrammar(String tableName) {
        super(tableName, "\"");
    }

    @Override
    public String getKeyword(SQLPartType sqlPartType) {
        if (sqlPartType == SQLPartType.LIMIT) {
            return "";
        }
        return super.getKeyword(sqlPartType);
    }

    @Override
    public void formatLimit(Object offset, Object take, Collection<Object> parameters) {
        skipFirstParameters = new LinkedList<>();
        skipFirstClause = "skip " + replaceValueAndFillParameters(offset, skipFirstParameters) +
            " first " + replaceValueAndFillParameters(take, skipFirstParameters);
        parameters.addAll(skipFirstParameters);
    }

    @Override
    public void formatLimit(Object take, Collection<Object> parameters) {
        skipFirstParameters = new LinkedList<>();
        skipFirstClause = "first " + replaceValueAndFillParameters(take, skipFirstParameters);
        parameters.addAll(skipFirstParameters);
    }

    @Override
    public SQLPartInfo generateSql(SqlType sqlType) {
        SQLPartInfo result = super.generateSql(sqlType);
        if (!skipFirstClause.isEmpty() && sqlType == SqlType.SELECT) {
            String sql = result.getSqlString();
            // 将 SKIP/FIRST 插入 SELECT 关键字之后
            if (sql.startsWith("select ")) {
                sql = "select " + skipFirstClause + " " + sql.substring(7);
            }
            return new SQLPartInfo(sql, result.getParameters());
        }
        return result;
    }

    @Override
    public String formatUpsertSuffix(Collection<String> quotedColumns, Function<String, String> bracketFn) {
        throw new TypeNotSupportedException(
            "Informix does not support upsert via ON DUPLICATE KEY UPDATE");
    }
}
