package gaarason.database.test;

import gaarason.database.appointment.SqlType;
import gaarason.database.contract.query.Grammar;
import gaarason.database.exception.base.BaseException;
import gaarason.database.query.grammars.FirebirdGrammar;
import gaarason.database.query.grammars.InformixGrammar;
import gaarason.database.query.grammars.MsSqlGrammar;
import gaarason.database.query.grammars.OracleGrammar;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 方言分页语法测试(纯单测, 不依赖数据库).
 * <p>
 * 关注两件事:
 * 1. 生成的 SQL 语法在对应数据库成立(子句位置正确);
 * 2. SQL 中的占位符数量与绑定参数数量一致(否则绑定错位).
 */
@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class GrammarLimitTests {

    /**
     * 统计占位符数量
     */
    private static int placeholderCount(String sql) {
        int count = 0;
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?') {
                count++;
            }
        }
        return count;
    }

    /**
     * 占位符与绑定参数必须一一对应
     */
    private static void assertParameterBound(String sql, Collection<Object> parameters) {
        Assert.assertEquals("占位符数量与绑定参数数量不一致, sql: " + sql, placeholderCount(sql),
            parameters.size());
    }

    /**
     * 加入一个排序片段, 用于复现 "分页 + 排序" 的真实场景
     */
    private static void addOrderBy(Grammar grammar) {
        grammar.addSmartSeparator(Grammar.SQLPartType.ORDER, "\"t\".\"id\" desc", null, ",");
    }

    // ----------------------------- Informix ----------------------------- //

    /**
     * Informix: SKIP/FIRST 的取值必须随 SQL 一起下发, 且子句位于 SELECT 之后
     */
    @Test
    public void informix分页保留绑定参数() {
        InformixGrammar grammar = new InformixGrammar("t");
        grammar.formatLimit(5, 10, new ArrayList<>());

        Grammar.SQLPartInfo sqlPartInfo = grammar.generateSql(SqlType.SELECT);
        String sql = sqlPartInfo.getSqlString();
        log.info("informix limit sql: {}", sql);

        assertParameterBound(sql, sqlPartInfo.getParameters());
        // IDS 11.50+ 的 ANSI 写法: SELECT FIRST n SKIP m ...
        Assert.assertTrue("FIRST/SKIP 应紧随 SELECT 关键字, sql: " + sql,
            sql.startsWith("select first 10 skip 5 "));
    }

    /**
     * Informix: 仅 take 时为首 n 条
     */
    @Test
    public void informix仅限制条数() {
        InformixGrammar grammar = new InformixGrammar("t");
        grammar.formatLimit(10, new ArrayList<>());

        Grammar.SQLPartInfo sqlPartInfo = grammar.generateSql(SqlType.SELECT);
        String sql = sqlPartInfo.getSqlString();
        log.info("informix take sql: {}", sql);

        assertParameterBound(sql, sqlPartInfo.getParameters());
        Assert.assertTrue("FIRST 应紧随 SELECT 关键字, sql: " + sql, sql.startsWith("select first 10 "));
    }

    // ----------------------------- Oracle 11g ----------------------------- //

    /**
     * Oracle 11g: ROWNUM 不能出现在 ORDER BY 之后, 必须包裹子查询
     */
    @Test
    public void oracle11g仅限制条数需要子查询包裹() {
        OracleGrammar grammar = new OracleGrammar("t");
        addOrderBy(grammar);
        grammar.formatLimit(10, new ArrayList<>());

        Grammar.SQLPartInfo sqlPartInfo = grammar.generateSql(SqlType.SELECT);
        String sql = sqlPartInfo.getSqlString();
        log.info("oracle take sql: {}", sql);

        assertParameterBound(sql, sqlPartInfo.getParameters());
        Assert.assertTrue("应先排序再取前 N 条, sql: " + sql, sql.startsWith("select * from ("));
        Assert.assertTrue("ROWNUM 条件应位于外层, sql: " + sql, sql.contains("order by"));
        Assert.assertTrue("ROWNUM 条件应在 ORDER BY 之后生效, sql: " + sql,
            sql.indexOf("rownum") > sql.indexOf("order by"));
        Assert.assertTrue("sql: " + sql, sql.endsWith("where rownum <= 10"));
    }

    /**
     * Oracle 11g: 带偏移的分页需要双层子查询, 而不是直接抛异常
     */
    @Test
    public void oracle11g分页可用() {
        OracleGrammar grammar = new OracleGrammar("t");
        addOrderBy(grammar);
        grammar.formatLimit(5, 10, new ArrayList<>());

        Grammar.SQLPartInfo sqlPartInfo = grammar.generateSql(SqlType.SELECT);
        String sql = sqlPartInfo.getSqlString();
        log.info("oracle paginate sql: {}", sql);

        assertParameterBound(sql, sqlPartInfo.getParameters());
        Assert.assertTrue("内层需要 rownum 上界(offset+take), sql: " + sql, sql.contains("rownum <= 15"));
        Assert.assertTrue("外层需要行号下界(offset), sql: " + sql, sql.contains("rownum_ > 5"));
        Assert.assertTrue("ROWNUM 上界应在 ORDER BY 之后, sql: " + sql,
            sql.indexOf("rownum <=") > sql.indexOf("order by"));
    }

    // ----------------------------- SQL Server ----------------------------- //

    /**
     * SQL Server: OFFSET/FETCH 强制要求 ORDER BY, 仅限制条数时应改用 TOP
     */
    @Test
    public void mssql无排序时仅限制条数() {
        MsSqlGrammar grammar = new MsSqlGrammar("t");
        grammar.formatLimit(10, new ArrayList<>());

        Grammar.SQLPartInfo sqlPartInfo = grammar.generateSql(SqlType.SELECT);
        String sql = sqlPartInfo.getSqlString();
        log.info("mssql take sql: {}", sql);

        assertParameterBound(sql, sqlPartInfo.getParameters());
        Assert.assertTrue("应使用 select top N, sql: " + sql, sql.startsWith("select top 10 "));
        Assert.assertFalse("TOP 语法不需要 OFFSET/FETCH, sql: " + sql, sql.contains("offset"));
    }

    /**
     * SQL Server: 带偏移的分页在没有排序时需要补齐排序
     */
    @Test
    public void mssql分页需要排序() {
        MsSqlGrammar grammar = new MsSqlGrammar("t");
        grammar.formatLimit(5, 10, new ArrayList<>());

        Grammar.SQLPartInfo sqlPartInfo = grammar.generateSql(SqlType.SELECT);
        String sql = sqlPartInfo.getSqlString();
        log.info("mssql paginate sql: {}", sql);

        assertParameterBound(sql, sqlPartInfo.getParameters());
        Assert.assertTrue("sql: " + sql, sql.contains("offset 5 rows fetch next 10 rows only"));
        Assert.assertTrue("OFFSET/FETCH 前必须有 ORDER BY, sql: " + sql,
            sql.indexOf("order by") >= 0 && sql.indexOf("order by") < sql.indexOf("offset"));
    }

    /**
     * SQL Server: 已有排序时不重复补排序
     */
    @Test
    public void mssql分页已有排序时保持原样() {
        MsSqlGrammar grammar = new MsSqlGrammar("t");
        addOrderBy(grammar);
        grammar.formatLimit(5, 10, new ArrayList<>());

        String sql = grammar.generateSql(SqlType.SELECT).getSqlString();
        log.info("mssql paginate with order sql: {}", sql);

        Assert.assertFalse("已有排序时不应再补占位排序, sql: " + sql, sql.contains("(select null)"));
        Assert.assertTrue("sql: " + sql, sql.contains("\"t\".\"id\" desc"));
    }

    // ----------------------------- Firebird ----------------------------- //

    /**
     * Firebird: ROWS 子句只接受常量区间, 不能写成算术表达式
     */
    @Test
    public void firebird分页区间为常量() {
        FirebirdGrammar grammar = new FirebirdGrammar("t");
        grammar.formatLimit(5, 10, new ArrayList<>());

        Grammar.SQLPartInfo sqlPartInfo = grammar.generateSql(SqlType.SELECT);
        String sql = sqlPartInfo.getSqlString();
        log.info("firebird paginate sql: {}", sql);

        assertParameterBound(sql, sqlPartInfo.getParameters());
        Assert.assertTrue("ROWS 需要 1-based 闭区间, sql: " + sql, sql.endsWith("rows 6 to 15"));
    }

    /**
     * Firebird: 仅限制条数时区间起点为 1
     */
    @Test
    public void firebird仅限制条数() {
        FirebirdGrammar grammar = new FirebirdGrammar("t");
        grammar.formatLimit(10, new ArrayList<>());

        Grammar.SQLPartInfo sqlPartInfo = grammar.generateSql(SqlType.SELECT);
        String sql = sqlPartInfo.getSqlString();
        log.info("firebird take sql: {}", sql);

        assertParameterBound(sql, sqlPartInfo.getParameters());
        Assert.assertTrue("ROWS 需要完整区间, sql: " + sql, sql.endsWith("rows 1 to 10"));
    }

    // ----------------------------- 参数校验 ----------------------------- //

    /**
     * 非数字的 limit 取值应当快速失败, 而不是拼进 SQL 或延后到绑定阶段
     */
    @Test(expected = BaseException.class)
    public void 非数字分页参数快速失败() {
        InformixGrammar grammar = new InformixGrammar("t");
        grammar.formatLimit("abc", 10, new ArrayList<>());
    }

    /**
     * 负的 limit 取值应当快速失败
     */
    @Test(expected = BaseException.class)
    public void 负数分页参数快速失败() {
        MsSqlGrammar grammar = new MsSqlGrammar("t");
        grammar.formatLimit(-1, new ArrayList<>());
    }
}
