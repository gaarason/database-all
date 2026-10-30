package gaarason.database.query.grammars;

import gaarason.database.appointment.SqlType;
import gaarason.database.exception.TypeNotSupportedException;

import java.util.Collection;
import java.util.function.Function;

/**
 * Informix 方言语法分析
 * <p>
 * 适用于: Informix, GBase 8s, 星瑞格(SinoDB)
 * 分页使用 FIRST n SKIP m 语法(置于 SELECT 关键字之后)
 * @author xt
 */
public class InformixGrammar extends BaseGrammar {

    private static final long serialVersionUID = 1L;

    /**
     * 暂存 FIRST/SKIP 子句(以空格结尾), 在 generateSql 时插入 SELECT 之后
     */
    private String firstSkipClause = "";

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
        // 取值已经过 limitNumberToLong 校验, 内联可以避免子句前置带来的占位符顺序错乱
        firstSkipClause = "first " + limitNumberToLong(take, "Informix") + " skip " +
            limitNumberToLong(offset, "Informix") + " ";
    }

    @Override
    public void formatLimit(Object take, Collection<Object> parameters) {
        firstSkipClause = "first " + limitNumberToLong(take, "Informix") + " ";
    }

    @Override
    public SQLPartInfo generateSql(SqlType sqlType) {
        SQLPartInfo result = super.generateSql(sqlType);
        if (!firstSkipClause.isEmpty() && sqlType == SqlType.SELECT) {
            String sql = result.getSqlString();
            // 将 FIRST/SKIP 插入 SELECT 关键字之后
            if (sql.startsWith("select ")) {
                sql = "select " + firstSkipClause + sql.substring("select ".length());
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
