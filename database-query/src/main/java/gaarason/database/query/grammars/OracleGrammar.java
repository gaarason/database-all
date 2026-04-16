package gaarason.database.query.grammars;

import gaarason.database.exception.TypeNotSupportedException;

import java.util.Collection;
import java.util.function.Function;

/**
 * Oracle 11g 及以下方言语法分析
 * <p>
 * 分页使用 ROWNUM 包裹子查询方式
 * @author xt
 */
public class OracleGrammar extends BaseGrammar {

    private static final long serialVersionUID = 1L;

    public OracleGrammar(String tableName) {
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
        // Oracle 11g ROWNUM 方式: 通过 WHERE 嵌套实现
        // 生成: ) WHERE ROWNUM <= (offset + take)) WHERE rn_ > offset
        // 需要在 generateSql 中配合子查询包裹, 此处仅存储参数
        throw new TypeNotSupportedException(
            "Oracle 11g ROWNUM-based pagination requires subquery wrapping. " +
            "Consider upgrading to Oracle 12c or using raw SQL for complex pagination.");
    }

    @Override
    public void formatLimit(Object take, Collection<Object> parameters) {
        // Oracle 11g: WHERE ROWNUM <= take
        String sqlPart = "ROWNUM <= " + replaceValueAndFillParameters(take, parameters);
        set(SQLPartType.LIMIT, sqlPart, parameters);
    }

    @Override
    public String formatUpsertSuffix(Collection<String> quotedColumns, Function<String, String> bracketFn) {
        throw new TypeNotSupportedException(
            "Oracle does not support upsert via ON DUPLICATE KEY UPDATE, use MERGE statement instead");
    }
}
