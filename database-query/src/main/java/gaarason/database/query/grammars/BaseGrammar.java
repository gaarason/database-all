package gaarason.database.query.grammars;

import gaarason.database.appointment.SqlType;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.function.BuilderWrapper;
import gaarason.database.contract.function.RecordWrapper;
import gaarason.database.contract.query.Grammar;
import gaarason.database.exception.CloneNotSupportedRuntimeException;
import gaarason.database.exception.InvalidSqlTypeException;
import gaarason.database.lang.Nullable;
import gaarason.database.util.FormatUtils;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 语法分析基类
 * @author xt
 */
public abstract class BaseGrammar implements Grammar, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 处理时, 需要用括号()包裹的
     */
    protected final static Set<SQLPartType> PARENTHESES_ARE_REQUIRED = EnumSet.of(SQLPartType.FORCE_INDEX,
        SQLPartType.IGNORE_INDEX, SQLPartType.COLUMN);
    /**
     * column -> [ GenerateSqlPart , RelationshipRecordWith ]
     * @see RecordWrapper
     * @see BuilderWrapper
     */
    protected final Map<String, Object[]> withMap;

    /**
     * 关联关系
     */
    protected final Map<String, Record.Relation> relationMap;

    /**
     * 表名
     */
    protected final String table;
    /**
     * SQL片段信息MAP
     */
    protected final Map<SQLPartType, List<SQLPartInfo>> SQLPartMap;

    protected BaseGrammar(String tableName) {
        table = tableName;
        withMap = new HashMap<>();
        SQLPartMap = new HashMap<>();
        relationMap = new HashMap<>();
    }

    @Override
    public String replaceValueAndFillParameters(@Nullable Object value, Collection<Object> parameters) {
        parameters.add(value);
        return " ? ";
    }

    @Override
    public String replaceValuesAndFillParameters(Collection<?> values, Collection<Object> parameters,
        String separator) {
        return values.stream().map(e -> {
            parameters.add(e);
            return " ? ";
        }).collect(Collectors.joining(separator));
    }

    @Override
    public void addSmartSeparator(SQLPartType sqlPartType, String sqlPartString,
        @Nullable Collection<Object> parameters) {
        if (isEmpty(sqlPartType)) {
            add(sqlPartType, sqlPartString, parameters);
        } else {
            add(sqlPartType, ',' + sqlPartString, parameters);
        }
    }

    @Override
    public void addFirstSmartSeparator(SQLPartType sqlPartType, String sqlPartString,
        @Nullable Collection<Object> parameters) {
        if (isEmpty(sqlPartType)) {
            addFirst(sqlPartType, sqlPartString, parameters);
        } else {
            addFirst(sqlPartType, sqlPartString + ',', parameters);
        }
    }

    @Override
    public void addSmartSeparator(SQLPartType sqlPartType, String sqlPartString,
        @Nullable Collection<Object> parameters,
        String separator) {
        if (isEmpty(sqlPartType)) {
            add(sqlPartType, sqlPartString, parameters);
        } else {
            add(sqlPartType, separator + sqlPartString, parameters);
        }
    }

    @Override
    public void addFirstSmartSeparator(SQLPartType sqlPartType, String sqlPartString,
        @Nullable Collection<Object> parameters,
        String separator) {
        if (isEmpty(sqlPartType)) {
            addFirst(sqlPartType, sqlPartString, parameters);
        } else {
            addFirst(sqlPartType, sqlPartString + separator, parameters);
        }
    }

    @Override
    public void add(SQLPartType sqlPartType, String sqlPartString, @Nullable Collection<Object> parameters) {
        // init list
        List<SQLPartInfo> sqlParts = SQLPartMap.computeIfAbsent(sqlPartType, k -> new LinkedList<>());
        // construct
        SQLPartInfo sqlPart = new SQLPartInfo(sqlPartString, parameters);
        // add
        sqlParts.add(sqlPart);
    }

    @Override
    public void clear(SQLPartType sqlPartType) {
        SQLPartMap.remove(sqlPartType);
    }

    @Override
    public void addFirst(SQLPartType sqlPartType, String sqlPartString, @Nullable Collection<Object> parameters) {
        // init list
        List<SQLPartInfo> sqlParts = SQLPartMap.computeIfAbsent(sqlPartType, k -> new LinkedList<>());
        // construct
        SQLPartInfo sqlPart = new SQLPartInfo(sqlPartString, parameters);
        // add
        sqlParts.add(0, sqlPart);
    }

    @Override
    public void set(SQLPartType sqlPartType, String sqlPartString, @Nullable Collection<Object> parameters) {
        // construct
        SQLPartInfo sqlPart = new SQLPartInfo(sqlPartString, parameters);
        // put
        LinkedList<SQLPartInfo> objects = new LinkedList<>();
        objects.add(sqlPart);
        SQLPartMap.put(sqlPartType, objects);
    }

    @Override
    public boolean isEmpty(SQLPartType sqlPartType) {
        return ObjectUtils.isEmpty(SQLPartMap.get(sqlPartType));
    }

    @Override
    public SQLPartInfo get(SQLPartType sqlPartType) {
        StringBuilder sqlBuilder = new StringBuilder();
        Collection<Object> allParameters = new LinkedList<>();

        List<SQLPartInfo> sqlParts = SQLPartMap.computeIfAbsent(sqlPartType, k -> new ArrayList<>());
        // sql part
        for (SQLPartInfo sqlPart : sqlParts) {
            sqlBuilder.append(sqlPart.getSqlString());
            Collection<Object> parameters = sqlPart.getParameters();
            if (!ObjectUtils.isEmpty(parameters)) {
                allParameters.addAll(parameters);
            }
        }
        return new SQLPartInfo(sqlBuilder.toString(), allParameters);
    }

    @Override
    public void concatenate(SqlType sqlType, SQLPartType sqlPartType, StringBuilder sqlBuilder,
        Collection<Object> allParameters) {
        List<SQLPartInfo> sqlParts = SQLPartMap.get(sqlPartType);
        if (ObjectUtils.isEmpty(sqlParts)) {
            // 使用默认值
            sqlParts = getDefault(sqlPartType);
            if(ObjectUtils.isEmpty(sqlParts)) {
                return;
            }
        }

        // keyword
        sqlBuilder.append(sqlPartType.getKeyword());

        // begin
        if (PARENTHESES_ARE_REQUIRED.contains(sqlPartType)) {
            sqlBuilder.append('(');
        }

        // sql part
        for (SQLPartInfo sqlPart : sqlParts) {
            sqlBuilder.append(sqlPart.getSqlString());

            Collection<Object> parameters = sqlPart.getParameters();
            if (!ObjectUtils.isEmpty(parameters)) {
                allParameters.addAll(parameters);
            }
        }

        // end
        if (PARENTHESES_ARE_REQUIRED.contains(sqlPartType)) {
            sqlBuilder.append(')');
        }

    }

    void choreography(SqlType sqlType, StringBuilder sqlBuilder, Collection<Object> allParameters,
        SQLPartType... sqlPartTypes) {
        for (SQLPartType sqlPartType : sqlPartTypes) {
            concatenate(sqlType, sqlPartType, sqlBuilder, allParameters);
        }
    }

    @Override
    public SQLPartInfo generateSql(SqlType sqlType) {
        StringBuilder sqlBuilder = new StringBuilder();
        Collection<Object> allParameters = new LinkedList<>();

        switch (sqlType) {
            case REPLACE:
                sqlBuilder.append("replace into ");
                choreography(sqlType, sqlBuilder, allParameters, SQLPartType.TABLE, SQLPartType.COLUMN,
                    SQLPartType.VALUE);
                return new SQLPartInfo(sqlBuilder.toString(), allParameters);
            case INSERT:
                sqlBuilder.append("insert into ");
                choreography(sqlType, sqlBuilder, allParameters, SQLPartType.TABLE, SQLPartType.COLUMN,
                    SQLPartType.VALUE);
                return new SQLPartInfo(sqlBuilder.toString(), allParameters);

            case UPDATE:
                sqlBuilder.append("update ");
                choreography(sqlType, sqlBuilder, allParameters, SQLPartType.TABLE, SQLPartType.FORCE_INDEX,
                    SQLPartType.IGNORE_INDEX, SQLPartType.DATA);
                break;
            case SELECT:
                choreography(sqlType, sqlBuilder, allParameters, SQLPartType.SELECT, SQLPartType.FROM,
                    SQLPartType.FORCE_INDEX, SQLPartType.IGNORE_INDEX);
                break;
            case DELETE:
                sqlBuilder.append("delete");
                choreography(sqlType, sqlBuilder, allParameters, SQLPartType.FROM, SQLPartType.FORCE_INDEX,
                    SQLPartType.IGNORE_INDEX);
                break;
            case SUB_QUERY:
                break;
            default:
                throw new InvalidSqlTypeException();
        }

        choreography(sqlType, sqlBuilder, allParameters, SQLPartType.JOIN, SQLPartType.WHERE, SQLPartType.GROUP,
            SQLPartType.HAVING, SQLPartType.ORDER, SQLPartType.LIMIT, SQLPartType.LOCK);

        if (!isEmpty(SQLPartType.UNION)) {
            FormatUtils.bracket(sqlBuilder);
            choreography(sqlType, sqlBuilder, allParameters, SQLPartType.UNION);
        }

        return new SQLPartInfo(sqlBuilder.toString(), allParameters);
    }

    /**
     * 得到默认值
     */
    @Nullable
    protected List<SQLPartInfo> getDefault(SQLPartType type) {
        switch (type) {
            case TABLE:
            case FROM:
                return Collections.singletonList(new SQLPartInfo(table));
            case SELECT:
                return Collections.singletonList(new SQLPartInfo("*"));
            case VALUE:
                return Collections.singletonList( new SQLPartInfo("()"));
        }
        return null;
    }

    @Override
    public void pushRelation(String targetFieldName, Record.Relation relation) {
        relationMap.put(targetFieldName, relation);
    }

    @Override
    public Map<String, Record.Relation> pullRelation(){
        return relationMap;
    }

    @Override
    public Grammar deepCopy() throws CloneNotSupportedRuntimeException {
        return ObjectUtils.deepCopy(this);
    }

    @Override
    public void merger(Grammar grammar) {
        relationMap.putAll(grammar.pullRelation());

        for (SQLPartType type : SQLPartType.values()) {
            SQLPartInfo partInfo = grammar.get(type);
            add(type, partInfo.getSqlString(), partInfo.getParameters());
        }
    }
}
