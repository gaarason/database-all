package gaarason.database.query.grammars;

import gaarason.database.appointment.SqlType;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.function.BuilderWrapper;
import gaarason.database.contract.function.RecordWrapper;
import gaarason.database.contract.query.Alias;
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
    protected final Map<String, Object[]> withMap = new HashMap<>();

    /**
     * 关联关系
     */
    protected final Map<String, Record.Relation> relationMap = new HashMap<>();

    /**
     * SQL片段信息MAP
     */
    protected final Map<SQLPartType, List<SQLPartInfo>> SQLPartMap = new HashMap<>();

    /**
     * 符号, 用于避免关键字冲突
     */
    protected final String symbol;

    /**
     * 别名
     */
    protected Alias alias;

    /**
     * 历史别名
     */
    protected List<Alias> historyAliasList = new ArrayList<>();

    /**
     * 是否使用别名
     */
    protected boolean useAlisa = false;

    protected BaseGrammar(String tableName, String symbol) {
        this.alias = new Alias(tableName, tableName + "_" + hashCode());
        this.symbol = symbol;
    }

    @Override
    public String symbol() {
        return symbol;
    }

    @Override
    public void useAlias(boolean isUse) {
        useAlisa = isUse;
    }

    @Override
    public void alias(Alias alias) {
        this.historyAliasList.add(this.alias);
        this.alias = alias;
    }

    @Override
    public Alias alias() {
        return alias;
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
        @Nullable Collection<Object> parameters, String separator) {
        if (isEmpty(sqlPartType)) {
            add(sqlPartType, sqlPartString, parameters);
        } else {
            add(sqlPartType, separator + sqlPartString, parameters);
        }
    }

    @Override
    public void addFirstSmartSeparator(SQLPartType sqlPartType, String sqlPartString,
        @Nullable Collection<Object> parameters, String separator) {
        if (isEmpty(sqlPartType)) {
            addFirst(sqlPartType, sqlPartString, parameters);
        } else {
            addFirst(sqlPartType, sqlPartString + separator, parameters);
        }
    }

    /**
     * 加入sql片段(片段尾部)
     * @param sqlPartType SQL片段类型
     * @param sqlPartString SQL片段
     * @param parameters 绑定参数集合
     */
    protected void add(SQLPartType sqlPartType, String sqlPartString, @Nullable Collection<Object> parameters) {
        // init list
        List<SQLPartInfo> sqlParts = SQLPartMap.computeIfAbsent(sqlPartType, k -> new LinkedList<>());
        // construct
        SQLPartInfo sqlPart = simpleInstanceSQLPartInfo(sqlPartString, parameters);
        // add
        sqlParts.add(sqlPart);
    }

    @Override
    public void clear(SQLPartType sqlPartType) {
        SQLPartMap.remove(sqlPartType);
    }

    /**
     * 加入sql片段(片段首部)
     * @param sqlPartType SQL片段类型
     * @param sqlPartString SQL片段
     * @param parameters 绑定参数集合
     */
    protected void addFirst(SQLPartType sqlPartType, String sqlPartString, @Nullable Collection<Object> parameters) {
        // init list
        List<SQLPartInfo> sqlParts = SQLPartMap.computeIfAbsent(sqlPartType, k -> new LinkedList<>());
        // construct
        SQLPartInfo sqlPart = simpleInstanceSQLPartInfo(sqlPartString, parameters);
        // add
        sqlParts.add(0, sqlPart);
    }

    @Override
    public void set(SQLPartType sqlPartType, String sqlPartString, @Nullable Collection<Object> parameters) {
        // construct
        SQLPartInfo sqlPart = simpleInstanceSQLPartInfo(sqlPartString, parameters);
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
        // 保留 别名占位符
        return instanceSQLPartInfoWithAliasPlaceHolder(sqlBuilder.toString(), allParameters);
    }

    protected void concatenate(SqlType sqlType, SQLPartType sqlPartType, StringBuilder sqlBuilder,
        Collection<Object> allParameters) {
        List<SQLPartInfo> sqlParts = SQLPartMap.get(sqlPartType);
        if (ObjectUtils.isEmpty(sqlParts)) {
            // 使用默认值
            sqlParts = getDefault(sqlPartType);
            if (ObjectUtils.isEmpty(sqlParts)) {
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

    protected void choreography(SqlType sqlType, StringBuilder sqlBuilder, Collection<Object> allParameters,
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
                useAlisa = false;
                sqlBuilder.append("replace into ");
                choreography(sqlType, sqlBuilder, allParameters, SQLPartType.TABLE, SQLPartType.COLUMN,
                    SQLPartType.VALUE, SQLPartType.LAST);
                return instanceSQLPartInfo(sqlBuilder.toString(), allParameters);
            case INSERT:
                useAlisa = false;
                sqlBuilder.append("insert into ");
                choreography(sqlType, sqlBuilder, allParameters, SQLPartType.TABLE, SQLPartType.COLUMN,
                    SQLPartType.VALUE, SQLPartType.LAST);
                return instanceSQLPartInfo(sqlBuilder.toString(), allParameters);
            case UPDATE:
                useAlisa = false;
                sqlBuilder.append("update ");
                choreography(sqlType, sqlBuilder, allParameters, SQLPartType.TABLE, SQLPartType.FORCE_INDEX,
                    SQLPartType.IGNORE_INDEX, SQLPartType.DATA);
                break;
            case DELETE:
                useAlisa = false;
                sqlBuilder.append("delete from ");
                choreography(sqlType, sqlBuilder, allParameters, SQLPartType.TABLE, SQLPartType.FORCE_INDEX,
                    SQLPartType.IGNORE_INDEX);
                break;
            case SELECT:
                useAlisa = true;
                choreography(sqlType, sqlBuilder, allParameters, SQLPartType.SELECT, SQLPartType.FROM,
                        SQLPartType.FORCE_INDEX, SQLPartType.IGNORE_INDEX);
                break;
            case SUB_QUERY:
                useAlisa = true;
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

        choreography(sqlType, sqlBuilder, allParameters, SQLPartType.LAST);

        return instanceSQLPartInfo(sqlBuilder.toString(), allParameters);
    }

    /**
     * 得到默认值
     */
    @Nullable
    protected List<SQLPartInfo> getDefault(SQLPartType type) {
        switch (type) {
            case TABLE:
                // `table_name`
                return Collections.singletonList(simpleInstanceSQLPartInfo(symbol + alias.getTable() + symbol, null));
            case FROM:
                // `table_name` as `alias`
                return Collections.singletonList(simpleInstanceSQLPartInfo(symbol + alias.getTable() + symbol + " as " + symbol + alias + symbol, null));
            case SELECT:
                return Collections.singletonList(simpleInstanceSQLPartInfo("*", null));
            case VALUE:
                return Collections.singletonList( simpleInstanceSQLPartInfo("()", null));
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

    /**
     * 获取 SQLPartInfo 对象
     * 并根据 useAlise 解析别名占位符 (替换掉所有占位符)
     * @param sqlString sql片段
     * @param parameters 绑定参数
     * @return SQL片段信息
     */
    protected SQLPartInfo instanceSQLPartInfo(String sqlString, @Nullable Collection<Object> parameters) {
        String sql;
        if (useAlisa) {
            // 历史处理
            for (Alias historyAlias : historyAliasList) {
                sqlString = sqlString.replace(historyAlias.getAliasPlaceHolder(), alias.getAlias());
            }
            sql = sqlString.replace(alias.getAliasPlaceHolder(), alias.getAlias());
        } else {
            // 历史处理
            for (Alias historyAlias : historyAliasList) {
                sqlString = sqlString.replace(symbol + historyAlias.getAliasPlaceHolder() + symbol + ".", "");
            }
            sql = sqlString.replace(symbol + alias.getAliasPlaceHolder() + symbol + ".", "");
        }
        return simpleInstanceSQLPartInfo(sql, parameters);
    }

    /**
     * 获取 SQLPartInfo 对象
     * 并统一别名占位符 (保留唯一一个别名占位符)
     * @param sqlString sql片段
     * @param parameters 绑定参数
     * @return SQL片段信息
     */
    protected SQLPartInfo instanceSQLPartInfoWithAliasPlaceHolder(String sqlString, @Nullable Collection<Object> parameters) {
        // 历史处理
        for (Alias historyAlias : historyAliasList) {
            sqlString = sqlString.replace(historyAlias.getAliasPlaceHolder(), alias.getAliasPlaceHolder());
        }
        return simpleInstanceSQLPartInfo(sqlString, parameters);
    }

    /**
     * 实例化对象
     * @param sqlString sql片段
     * @param parameters 绑定参数
     * @return SQL片段信息
     */
    protected SQLPartInfo simpleInstanceSQLPartInfo(String sqlString, @Nullable Collection<Object> parameters) {
        return new SQLPartInfo(sqlString, parameters);
    }
}
