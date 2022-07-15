package gaarason.database.query.grammars;

import gaarason.database.appointment.SqlType;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
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

    /**
     * 处理时, 需要用括号()包裹的
     */
    protected final static List<SQLPartType> PARENTHESES_ARE_REQUIRED = Arrays.asList(SQLPartType.FORCE_INDEX,
        SQLPartType.IGNORE_INDEX, SQLPartType.COLUMN);
    /**
     * column -> [ GenerateSqlPart , RelationshipRecordWith ]
     * @see RelationshipRecordWithFunctionalInterface
     * @see GenerateSqlPartFunctionalInterface
     */
    protected final HashMap<String, Object[]> withMap;
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
        SQLPartMap.put(sqlPartType, Collections.singletonList(sqlPart));

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
            return;
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

        // 默认值处理
        setDefault();


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
     * 默认值填充
     */
    protected void setDefault() {
        if (isEmpty(SQLPartType.TABLE)) {
            set(SQLPartType.TABLE, table, null);
            set(SQLPartType.FROM, table, null);
        }
        if (isEmpty(SQLPartType.SELECT)) {
            set(SQLPartType.SELECT, "*", null);
        }
        if (isEmpty(SQLPartType.VALUE)) {
            set(SQLPartType.VALUE, "()", null);
        }
    }

    /**
     * 记录with信息
     * @param column 所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure 所关联的Model的再一级关联
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
