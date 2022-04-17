package gaarason.database.query.grammars;

import gaarason.database.appointment.SqlType;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
import gaarason.database.contract.query.Grammar;
import gaarason.database.exception.CloneNotSupportedRuntimeException;
import gaarason.database.exception.GrammarException;
import gaarason.database.exception.InvalidSqlTypeException;
import gaarason.database.lang.Nullable;
import gaarason.database.util.FormatUtils;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 语法分析基类
 * @author xt
 */
public abstract class BaseGrammar implements Grammar, Serializable {

    /**
     * column -> [ GenerateSqlPart , RelationshipRecordWith ]
     * @see RelationshipRecordWithFunctionalInterface
     * @see GenerateSqlPartFunctionalInterface
     */
    protected final HashMap<String, Object[]> withMap;

    protected final String table;

    @Nullable
    protected String data;

    @Nullable
    protected String from;

    @Nullable
    protected String forceIndex;

    @Nullable
    protected String ignoreIndex;

    @Nullable
    protected String select;

    @Nullable
    protected String column;

    @Nullable
    protected String where;

    @Nullable
    protected String having;

    @Nullable
    protected String orderBy;

    @Nullable
    protected String group;

    @Nullable
    protected String limit;

    @Nullable
    protected String lock;

    @Nullable
    protected String join;

    @Nullable
    protected String union;

    protected final ArrayList<String> valueList;

    protected final ArrayList<String> whereParameterList;

    protected final ArrayList<String> dataParameterList;

    protected BaseGrammar(String tableName) {
        table = tableName;
        withMap = new HashMap<>();
        valueList = new ArrayList<>();
        whereParameterList = new ArrayList<>();
        dataParameterList = new ArrayList<>();
    }

    @Override
    public void pushWhere(String something, String relationship) {
        if (where == null) {
            where = something;
        } else {
            where += FormatUtils.spaces(relationship) + something;
        }
    }

    @Override
    public void pushHaving(String something, String relationship) {
        if (having == null) {
            having = something;
        } else {
            having += FormatUtils.spaces(relationship) + something;
        }
    }

    @Override
    public void pushValue(String something) {
        valueList.add(something);
    }

    @Override
    public void pushLock(String something) {
        lock = something;
    }

    @Override
    public void pushFrom(String something) {
        from = something;
    }

    @Override
    public void pushForceIndex(String indexName) {
        if (forceIndex == null) {
            forceIndex = indexName;
        } else {
            forceIndex += ',' + indexName;
        }
    }

    @Override
    public void pushIgnoreIndex(String indexName) {
        if (ignoreIndex == null) {
            ignoreIndex = indexName;
        } else {
            ignoreIndex += ',' + indexName;
        }
    }

    @Override
    public void pushSelect(String something) {
        if (select == null) {
            select = something;
        } else {
            select += ',' + something;
        }
    }

    @Override
    public void pushData(String something) {
        data = (data == null) ? something : data + ',' + something;
    }

    @Override
    public void pushOrderBy(String something) {
        if (orderBy == null) {
            orderBy = something;
        } else {
            orderBy += ',' + something;
        }
    }

    @Override
    public void pushLimit(String something) {
        limit = something;
    }

    @Override
    public void pushGroup(String something) {
        if (group == null) {
            group = something;
        } else {
            group += ',' + something;
        }
    }

    @Override
    public void pushColumn(String something) {
        column = (column == null) ? something : column + ',' + something;
    }

    @Override
    public void pushJoin(String something) {
        if (join == null) {
            join = something;
        } else {
            join += something;
        }
    }

    @Override
    public void pushUnion(String something, String unionType) {
        if (union == null) {
            union = " " + unionType + FormatUtils.bracket(something);
        } else {
            union += unionType + FormatUtils.bracket(something);
        }
    }

    protected String dealSelect() {
        return null == select ? "*" : select;
    }

    protected String dealColumn() {
        return null == select ? "" : FormatUtils.bracket(select);
    }

    protected String dealFromSelect() {
        return null == from ? " from " + dealTable() : " from " + from;
    }

    protected String dealFrom() {
        return null == from ? dealTable() : from;
    }

    protected String dealForceIndex() {
        return null == forceIndex ? "" : " force index" + FormatUtils.bracket(forceIndex);
    }

    protected String dealIgnoreIndex() {
        return null == ignoreIndex ? "" : " ignore index" + FormatUtils.bracket(ignoreIndex);
    }

    protected String dealTable() {
        return FormatUtils.backQuote(table, "`");
    }

    protected String dealWhere(SqlType sqlType) {
        String whereKeyword = sqlType == SqlType.SUB_QUERY ? "" : " where ";
        return null == where ? "" : whereKeyword + where;
    }

    protected String dealData() {
        return null == data ? "" : data;
    }

    protected String dealJoin() {
        return null == join ? "" : join;
    }

    protected String dealGroup() {
        return null == group ? "" : " group by " + group;
    }

    protected String dealHaving(SqlType sqlType) {
        String havingKeyword = sqlType == SqlType.SUB_QUERY ? "" : " having ";
        return having == null ? "" : havingKeyword + having;
    }

    protected String dealOrderBy() {
        return orderBy == null ? "" : " order by " + orderBy;
    }

    protected String dealLock() {
        return lock == null ? "" : " " + lock;
    }

    protected String dealValue() {
        if (valueList.isEmpty()) {
            return "()";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String value : valueList) {
            stringBuilder.append(value).append(',');
        }
        return stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
    }

    protected String dealLimit() {
        return limit == null ? "" : (" limit " + limit);
    }

    protected String dealUnion() {
        return union == null ? "" : union;
    }

    @Override
    public String generateSql(SqlType sqlType) {
        StringBuilder sqlBuilder = new StringBuilder();
        switch (sqlType) {
            case REPLACE:
                return sqlBuilder.append("replace into ").append(dealFrom()).append(dealColumn()).append(" values").append(dealValue()).toString();
            case INSERT:
                return sqlBuilder.append("insert into ").append(dealFrom()).append(dealColumn()).append(" values").append(dealValue()).toString();
            case SELECT:
                sqlBuilder.append("select ").append(dealSelect()).append(dealFromSelect()).append(dealForceIndex()).append(dealIgnoreIndex());
                break;
            case UPDATE:
                sqlBuilder.append("update ").append(dealFrom()).append(dealForceIndex()).append(dealIgnoreIndex()).append(" set").append(dealData());
                break;
            case DELETE:
                sqlBuilder.append("delete from ").append(dealFrom()).append(dealForceIndex()).append(dealIgnoreIndex());
                break;
            case SUB_QUERY:
                break;
            default:
                throw new InvalidSqlTypeException();
        }
        sqlBuilder.append(dealJoin()).append(dealWhere(sqlType)).append(dealGroup()).append(dealHaving(sqlType)).append(dealOrderBy()).append(
            dealLimit()).append(dealLock());

        if (union != null) {
            FormatUtils.bracket(sqlBuilder);
        }

        sqlBuilder.append(dealUnion());

        return sqlBuilder.toString();
    }

    @Override
    public List<String> getParameterList(SqlType sqlType) {
        final ArrayList<String> list = new ArrayList<>(dataParameterList);
        if (sqlType != SqlType.INSERT) {
            list.addAll(whereParameterList);
        }
        return list;
    }

    @Override
    public boolean hasSelect() {
        return null != select;
    }

    @Override
    public boolean hasWhere() {
        return null != where;
    }

    @Override
    public boolean hasGroup() {
        return null != group;
    }

    @Override
    public String getGroup() throws GrammarException {
        if (group == null) {
            throw new GrammarException("group is null");
        }
        return group;
    }

    @Override
    public boolean hasOrderBy() {
        return null != orderBy;
    }

    @Override
    public void forAggregates() {
        orderBy = null;
    }

    @Override
    public void pushWhereParameter(String value) {
        whereParameterList.add(value);
    }

    @Override
    public void pushDataParameter(String value) {
        dataParameterList.add(value);
    }

    /**
     * 记录with信息
     * @param column         所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure  所关联的Model的再一级关联
     */
    @Override
    public void pushWith(String column, GenerateSqlPartFunctionalInterface<?, ?> builderClosure,
        RelationshipRecordWithFunctionalInterface recordClosure) {
        withMap.put(column, new Object[]{builderClosure, recordClosure});
    }

    /**
     * 拉取with信息
     * @return map
     */
    @Override
    public Map<String, Object[]> pullWith() {
        return withMap;
    }

    @Override
    public Grammar deepCopy() throws CloneNotSupportedRuntimeException {
        return ObjectUtils.deepCopy(this);
    }
}
