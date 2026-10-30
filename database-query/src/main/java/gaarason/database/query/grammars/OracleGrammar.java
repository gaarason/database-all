package gaarason.database.query.grammars;

import gaarason.database.appointment.SqlType;
import gaarason.database.exception.TypeNotSupportedException;
import gaarason.database.lang.Nullable;

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

    /**
     * ROWNUM 上界, 取前 N 条时为 N, 分页时为 offset + take
     */
    @Nullable
    private String rownumUpper;

    /**
     * ROWNUM 下界, 仅分页(offset 大于 0)时有值
     */
    @Nullable
    private String rownumLower;

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
        // ROWNUM 在排序之前分配, 因此需要双层子查询: 内层排序, 外层过滤行号区间
        long numericOffset = limitNumberToLong(offset, "Oracle");
        long numericTake = limitNumberToLong(take, "Oracle");
        rownumUpper = String.valueOf(numericOffset + numericTake);
        rownumLower = numericOffset > 0 ? String.valueOf(numericOffset) : null;
    }

    @Override
    public void formatLimit(Object take, Collection<Object> parameters) {
        rownumUpper = String.valueOf(limitNumberToLong(take, "Oracle"));
        rownumLower = null;
    }

    @Override
    public SQLPartInfo generateSql(SqlType sqlType) {
        SQLPartInfo result = super.generateSql(sqlType);
        if (sqlType != SqlType.SELECT || rownumUpper == null) {
            return result;
        }
        // 取值已经过 limitNumberToLong 校验, 内联可以避免包裹后的占位符顺序错乱
        if (rownumLower == null) {
            return new SQLPartInfo("select * from (" + result.getSqlString() + ") where rownum <= " + rownumUpper,
                result.getParameters());
        }
        return new SQLPartInfo(
            "select * from ( select row_.*, rownum rownum_ from (" + result.getSqlString() + ") row_ where rownum <= "
                + rownumUpper + " ) where rownum_ > " + rownumLower, result.getParameters());
    }

    @Override
    public String formatUpsertSuffix(Collection<String> quotedColumns, Function<String, String> bracketFn) {
        throw new TypeNotSupportedException(
            "Oracle does not support upsert via ON DUPLICATE KEY UPDATE, use MERGE statement instead");
    }
}
