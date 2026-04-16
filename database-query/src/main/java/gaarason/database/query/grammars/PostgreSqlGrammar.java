package gaarason.database.query.grammars;

import gaarason.database.exception.TypeNotSupportedException;

import java.util.Collection;
import java.util.function.Function;

/**
 * PostgreSQL 方言语法分析
 * <p>
 * 适用于: PostgreSQL, Greenplum, openGauss, KingbaseES, H2, HSQL, SQLite, ClickHouse 等使用
 * LIMIT count OFFSET offset 分页语法的数据库
 * @author xt
 */
public class PostgreSqlGrammar extends BaseGrammar {

    private static final long serialVersionUID = 1L;

    public PostgreSqlGrammar(String tableName) {
        super(tableName, "\"");
    }

    public PostgreSqlGrammar(String tableName, String symbol) {
        super(tableName, symbol);
    }

    @Override
    public void formatLimit(Object offset, Object take, Collection<Object> parameters) {
        String sqlPart = replaceValueAndFillParameters(take, parameters) + " offset " +
            replaceValueAndFillParameters(offset, parameters);
        set(SQLPartType.LIMIT, sqlPart, parameters);
    }

    @Override
    public void formatLimit(Object take, Collection<Object> parameters) {
        String sqlPart = replaceValueAndFillParameters(take, parameters);
        set(SQLPartType.LIMIT, sqlPart, parameters);
    }

    @Override
    public String formatUpsertSuffix(Collection<String> quotedColumns, Function<String, String> bracketFn) {
        throw new TypeNotSupportedException(
            "PostgreSQL-family does not support upsert via ON DUPLICATE KEY UPDATE, use ON CONFLICT ... DO UPDATE instead");
    }
}
