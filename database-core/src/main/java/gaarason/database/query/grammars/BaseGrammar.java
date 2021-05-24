package gaarason.database.query.grammars;

import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
import gaarason.database.contract.query.Grammar;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.appointment.SqlType;
import gaarason.database.exception.CloneNotSupportedRuntimeException;
import gaarason.database.exception.InvalidSQLTypeException;
import gaarason.database.util.FormatUtil;
import gaarason.database.util.ObjectUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class BaseGrammar implements Grammar, Serializable {

    /**
     * column -> [ GenerateSqlPart , RelationshipRecordWith ]
     */
    protected Map<String, Object[]> withMap = new HashMap<>();

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

    protected final List<String> valueList = new ArrayList<>();

    protected final List<String> whereParameterList = new ArrayList<>();

    protected final List<String> dataParameterList = new ArrayList<>();


    public BaseGrammar(String tableName) {
        table = tableName;
    }

    @Override
    public void pushWhere(String something, String relationship) {
        if (where == null) {
            where = something;
        } else {
            where += FormatUtil.spaces(relationship) + something;
        }
    }

    @Override
    public void pushHaving(String something, String relationship) {
        if (having == null) {
            having = something;
        } else {
            having += FormatUtil.spaces(relationship) + something;
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
            union = " " + unionType + FormatUtil.bracket(something);
        } else {
            union += unionType + FormatUtil.bracket(something);
        }
    }

    protected String dealSelect() {
        return null == select ? "*" : select;
    }

    protected String dealColumn() {
        return null == select ? "" : FormatUtil.bracket(select);
    }

    protected String dealFromSelect() {
        return null == from ? " from " + dealTable() : " from " + from;
    }

    protected String dealFrom() {
        return null == from ? dealTable() : from;
    }

    protected String dealForceIndex() {
        return null == forceIndex ? "" : " force index" + FormatUtil.bracket(forceIndex);
    }

    protected String dealIgnoreIndex() {
        return null == ignoreIndex ? "" : " ignore index" + FormatUtil.bracket(ignoreIndex);
    }

    protected String dealTable() {
        return FormatUtil.backQuote(table, "`");
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
        if (valueList.size() == 0) {
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
        String sql;
        switch (sqlType) {
            case REPLACE:
                return "replace into " + dealFrom() + dealColumn() + " values" + dealValue();
            case INSERT:
                return "insert into " + dealFrom() + dealColumn() + " values" + dealValue();
            case SELECT:
                sql = "select " + dealSelect() + dealFromSelect() + dealForceIndex() + dealIgnoreIndex();
                break;
            case UPDATE:
                sql = "update " + dealFrom() + dealForceIndex() + dealIgnoreIndex() + " set" + dealData();
                break;
            case DELETE:
                sql = "delete from " + dealFrom() + dealForceIndex() + dealIgnoreIndex();
                break;
            case SUB_QUERY:
                sql = "";
                break;
            default:
                throw new InvalidSQLTypeException();
        }

        sql += dealJoin() + dealWhere(sqlType) + dealGroup() + dealHaving(
            sqlType) + dealOrderBy() + dealLimit() + dealLock();

        if (union != null) {
            sql = FormatUtil.bracket(sql);
        }

        sql += dealUnion();

        return sql;
    }

    @Override
    public List<String> getParameterList(SqlType sqlType) {
        if (sqlType != SqlType.INSERT)
            dataParameterList.addAll(whereParameterList);
        return dataParameterList;
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
    public String getGroup() {
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
    public void pushWith(String column, GenerateSqlPartFunctionalInterface builderClosure,
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

    /**
     * 深度copy
     * @return 和当前属性值一样的全新对象
     * @throws CloneNotSupportedRuntimeException 克隆异常
     */
    @Override
    public Grammar deepCopy() throws CloneNotSupportedRuntimeException {
        // 暂存
        Map<String, Object[]> withMapTemp = withMap;
        // 移除
        withMap = null;
        // 拷贝
        BaseGrammar baseGrammar = ObjectUtil.deepCopy(this);
        // 还原
        baseGrammar.withMap = withMap = withMapTemp;
        return baseGrammar;
    }
}
