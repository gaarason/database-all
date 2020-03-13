package gaarason.database.query;

import gaarason.database.connections.ProxyDataSource;
import gaarason.database.contracts.Grammar;
import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.*;
import gaarason.database.eloquent.enums.JoinType;
import gaarason.database.eloquent.enums.OrderBy;
import gaarason.database.eloquent.enums.SqlType;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.query.grammars.MySqlGrammar;
import gaarason.database.utils.EntityUtil;
import gaarason.database.utils.FormatUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MySqlBuilder<T> extends Builder<T> {

    public MySqlBuilder(ProxyDataSource dataSourceModel, Model<T> model, Class<T> entityClass) {
        super(dataSourceModel, model, entityClass);
    }

    @Override
    Grammar grammarFactory() {
        return new MySqlGrammar(EntityUtil.tableName(entityClass));
    }


    @Override
    public Builder<T> whereRaw(String sqlPart) {
        grammar.pushWhere(sqlPart, "and");
        return this;
    }

    @Override
    public Builder<T> where(String column, String symbol, String value) {
        String sqlPart = FormatUtil.column(column) + symbol + formatValue(value);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T> where(String column, String value) {
        return where(column, "=", value);
    }

    @Override
    public Builder<T> whereSubQuery(String column, String symbol, String completeSql) {
        String sqlPart = FormatUtil.column(column) + symbol + FormatUtil.bracket(completeSql);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T> whereSubQuery(String column, String symbol, GenerateSqlPart<T> closure) {
        String completeSql = generateSql(closure);
        String sqlPart     = FormatUtil.column(column) + symbol + completeSql;
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T> whereIn(String column, List<Object> valueList) {
        String sqlPart = FormatUtil.column(column) + "in" + FormatUtil.bracket(formatValue(valueList));
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T> whereInRaw(String column, String sql) {
        String sqlPart = FormatUtil.column(column) + "in" + FormatUtil.bracket(sql);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T> whereIn(String column, GenerateSqlPart<T> closure) {
        String sqlPart = generateSql(closure);
        return whereInRaw(column, sqlPart);
    }

    @Override
    public Builder<T> whereNotIn(String column, List<Object> valueList) {
        String sqlPart = FormatUtil.column(column) + "not in" + FormatUtil.bracket(formatValue(valueList));
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T> whereNotInRaw(String column, String sql) {
        String sqlPart = FormatUtil.column(column) + "not in" + FormatUtil.bracket(sql);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T> whereNotIn(String column, GenerateSqlPart<T> closure) {
        String sqlPart = generateSql(closure);
        return whereNotInRaw(column, sqlPart);
    }

    @Override
    public Builder<T> whereBetween(String column, String min, String max) {
        String sqlPart = FormatUtil.column(column) + "between" + formatValue(min) + "and" + formatValue(max);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T> whereNotBetween(String column, String min, String max) {
        String sqlPart =
            FormatUtil.column(column) + "not between" + formatValue(min) + "and" + formatValue(max);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T> whereNull(String column) {
        String sqlPart = FormatUtil.column(column) + "is null";
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T> whereNotNull(String column) {
        String sqlPart = FormatUtil.column(column) + "is not null";
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T> whereExistsRaw(String sql) {
        String sqlPart = "exists " + FormatUtil.bracket(sql);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T> whereExists(GenerateSqlPart<T> closure) {
        String sql = generateSql(closure);
        return whereExistsRaw(sql);
    }

    @Override
    public Builder<T> whereNotExistsRaw(String sql) {
        String sqlPart = "not exists " + FormatUtil.bracket(sql);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T> whereNotExists(GenerateSqlPart<T> closure) {
        String sql = generateSql(closure);
        return whereNotExistsRaw(sql);
    }

    @Override
    public Builder<T> whereColumn(String column1, String symbol, String column2) {
        String sqlPart = FormatUtil.column(column1) + symbol + FormatUtil.column(column2);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T> whereColumn(String column1, String column2) {
        return whereColumn(column1, "=", column2);
    }

    @Override
    public Builder<T> andWhere(GenerateSqlPart<T> closure) {
        String sqlPart = generateSqlPart(closure);
        grammar.pushWhere(sqlPart, "and");
        return this;
    }

    @Override
    public Builder<T> orWhere(GenerateSqlPart<T> closure) {
        String sqlPart = generateSqlPart(closure);
        grammar.pushWhere(sqlPart, "or");
        return this;
    }

    @Override
    public Builder<T> havingRaw(String sqlPart) {
        grammar.pushHaving(sqlPart, "and");
        return this;
    }

    @Override
    public Builder<T> having(String column, String symbol, String value) {
        String sqlPart = FormatUtil.column(column) + symbol + formatValue(value);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T> having(String column, String value) {
        return having(column, "=", value);
    }

    @Override
    public Builder<T> havingIn(String column, List<Object> valueList) {
        String sqlPart = FormatUtil.column(column) + "in" + FormatUtil.bracket(formatValue(valueList));
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T> havingInRaw(String column, String sql) {
        String sqlPart = FormatUtil.column(column) + "in" + FormatUtil.bracket(sql);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T> havingIn(String column, GenerateSqlPart<T> closure) {
        String sqlPart = generateSql(closure);
        return havingInRaw(column, sqlPart);
    }

    @Override
    public Builder<T> havingNotIn(String column, List<Object> valueList) {
        String sqlPart = FormatUtil.column(column) + "not in" + FormatUtil.bracket(formatValue(valueList));
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T> havingNotInRaw(String column, String sql) {
        String sqlPart = FormatUtil.column(column) + "not in" + FormatUtil.bracket(sql);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T> havingNotIn(String column, GenerateSqlPart<T> closure) {
        String sqlPart = generateSql(closure);
        return havingNotInRaw(column, sqlPart);
    }

    @Override
    public Builder<T> havingBetween(String column, String min, String max) {
        String sqlPart = FormatUtil.column(column) + "between" + formatValue(min) + "and" + formatValue(max);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T> havingNotBetween(String column, String min, String max) {
        String sqlPart =
            FormatUtil.column(column) + "not between" + formatValue(min) + "and" + formatValue(max);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T> havingNull(String column) {
        String sqlPart = FormatUtil.column(column) + "is null";
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T> havingNotNull(String column) {
        String sqlPart = FormatUtil.column(column) + "is not null";
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T> havingExistsRaw(String sql) {
        String sqlPart = "exists " + FormatUtil.bracket(sql);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T> havingExists(GenerateSqlPart<T> Closure) {
        String sql = generateSql(Closure);
        return havingExistsRaw(sql);
    }

    @Override
    public Builder<T> havingNotExistsRaw(String sql) {
        String sqlPart = "not exists " + FormatUtil.bracket(sql);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T> havingNotExists(GenerateSqlPart<T> Closure) {
        String sql = generateSql(Closure);
        return havingNotExistsRaw(sql);
    }

    @Override
    public Builder<T> havingColumn(String column1, String symbol, String column2) {
        String sqlPart = FormatUtil.column(column1) + symbol + FormatUtil.column(column2);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T> havingColumn(String column1, String column2) {
        return havingColumn(column1, "=", column2);
    }

    @Override
    public Builder<T> andHaving(GenerateSqlPart<T> closure) {
        String sqlPart = generateSqlPart(closure);
        grammar.pushHaving(sqlPart, "and");
        return this;
    }

    @Override
    public Builder<T> orHaving(GenerateSqlPart<T> closure) {
        String sqlPart = generateSqlPart(closure);
        grammar.pushHaving(sqlPart, "or");
        return this;
    }

    @Override
    public Builder<T> from(String table) {
        grammar.pushFrom(FormatUtil.column(table));
        return this;
    }

    @Override
    public Builder<T> select(String column) {
        String sqlPart = FormatUtil.column(column);
        grammar.pushSelect(sqlPart);
        return this;
    }

    @Override
    public Builder<T> select(String... columnArray) {
        for (String column : columnArray) {
            select(column);
        }
        return this;
    }

    @Override
    public Builder<T> select(List<String> columnList) {
        for (String column : columnList) {
            select(column);
        }
        return this;
    }

    @Override
    public Builder<T> selectFunction(String function, String parameter, @Nullable String alias) {
        String sqlPart = function + FormatUtil.bracket(parameter) + (alias == null ? "" :
            " as " + FormatUtil.quotes(alias));
        grammar.pushSelect(sqlPart);
        return this;
    }

    @Override
    public Builder<T> selectFunction(String function, GenerateSqlPart<T> closure, @Nullable String alias) {
        String completeSql = generateSql(closure);
        String sqlPart = function + FormatUtil.bracket(completeSql) + (alias == null ? "" :
            " as " + FormatUtil.quotes(alias));
        grammar.pushSelect(sqlPart);
        return this;
    }

    @Override
    public Builder<T> orderBy(String column, OrderBy type) {
        String sqlPart = FormatUtil.column(column) + " " + type.getOperation();
        grammar.pushOrderBy(sqlPart);
        return this;
    }

    @Override
    public Builder<T> orderBy(String column) {
        return orderBy(column, OrderBy.ASC);
    }

    @Override
    public Builder<T> limit(int offset, int take) {
        String sqlPart = String.valueOf(offset) + ',' + take;
        grammar.pushLimit(sqlPart);
        return this;
    }

    @Override
    public Builder<T> limit(int take) {
        String sqlPart = String.valueOf(take);
        grammar.pushLimit(sqlPart);
        return this;
    }

    @Override
    public Builder<T> groupRaw(String sqlPart) {
        grammar.pushGroup(sqlPart);
        return this;
    }

    @Override
    public Builder<T> group(String column) {
        String sqlPart = FormatUtil.column(column);
        return groupRaw(sqlPart);
    }

    @Override
    public Builder<T> group(String... columnArray) {
        for (String column : columnArray) {
            group(column);
        }
        return this;
    }

    @Override
    public Builder<T> group(List<String> columnList) {
        for (String column : columnList) {
            group(column);
        }
        return this;
    }

    @Override
    public Record<T> firstOrFail() throws SQLRuntimeException, EntityNotFoundException {
        limit(1);
        return querySql();
    }

    @Override
    public String toSql(SqlType sqlType) {
        String       sql           = grammar.generateSql(sqlType);
        List<String> parameterList = grammar.getParameterList(sqlType);
        return String.format(sql.replace(" ? ", "\"%s\""), parameterList.toArray());
    }

    @Override
    @Nullable
    public Record<T> first() throws SQLRuntimeException {
        try {
            return firstOrFail();
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    @Override
    public RecordList<T> get() throws SQLRuntimeException {
        return querySqlList();
    }

    @Override
    public int insert() throws SQLRuntimeException {
        return updateSql(SqlType.INSERT);
    }

    @Override
    public int insert(T entity) throws SQLRuntimeException {
        // 获取entity所有有效sql字段
        List<String> columnNameList = EntityUtil.columnNameList(entity, true);
        // 获取entity所有有效字段的值
        List<String> valueList = EntityUtil.valueList(entity, columnNameList);
        // 字段加入grammar
        select(columnNameList);
        // 字段的值加入grammar
        value(valueList);
        // 执行
        return insert();
    }

    @Override
    public int insert(List<T> entityList) throws SQLRuntimeException {
        // 获取entity所有有效字段
        List<String>       columnNameList = EntityUtil.columnNameList(entityList.get(0), true);
        List<List<String>> valueListList  = new ArrayList<>();
        for (T entity : entityList) {
            // 获取entity所有有效字段的值
            List<String> valueList = EntityUtil.valueList(entity, columnNameList);
            valueListList.add(valueList);
        }
        // 字段加入grammar
        select(columnNameList);
        // 字段的值加入grammar
        valueList(valueListList);
        // 执行
        return insert();
    }

    @Override
    public int update() throws SQLRuntimeException {
        return updateSql(SqlType.UPDATE);
    }

    @Override
    public int update(T entity) throws SQLRuntimeException {
        // 获取entity所有有效字段对其值得映射
        Map<String, String> stringStringMap = EntityUtil.columnValueMap(entity, false);

        data(stringStringMap);
        // 执行
        return update();
    }

    /**
     * 格式化参数类型,到绑定参数
     * @param value 参数
     * @return 参数占位符?
     */
    private String formatValue(String value) {
        return FormatUtil.value(value, grammar);
    }

    /**
     * 格式化参数类型,到绑定参数
     * @param value 参数
     * @return 参数占位符?
     */
    private String formatData(String value) {
        return FormatUtil.data(value, grammar);
    }

    /**
     * 格式化参数类型,到绑定参数
     * @param valueList 参数
     * @return 参数占位符?
     */
    private String formatValue(List<Object> valueList) {
        return FormatUtil.value(valueList, grammar);
    }

    @Override
    public Builder<T> value(List<String> valueList) {
        StringBuilder sqlPartBuilder = new StringBuilder("(");
        for (String value : valueList) {
            String stub = FormatUtil.data(value, grammar);
            sqlPartBuilder.append(stub).append(',');
        }
        String sqlPart = sqlPartBuilder.deleteCharAt(sqlPartBuilder.length() - 1).append(')').toString();
        grammar.pushValue(sqlPart);
        return this;
    }

    @Override
    public Builder<T> valueList(List<List<String>> valueList) {
        for (List<String> value : valueList) {
            value(value);
        }
        return this;
    }

    @Override
    public Builder<T> data(String sqlPart) {
        grammar.pushData(sqlPart);
        return this;
    }

    @Override
    public Builder<T> data(String column, String value) {
        String sqlPart = FormatUtil.column(column) + '=' + formatData(value);
        return data(sqlPart);
    }

    @Override
    public Builder<T> data(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            data(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Builder<T> dataIncrement(String column, int steps) {
        String sqlPart = FormatUtil.column(column) + '=' + FormatUtil.column(column) + '+' + steps;
        return data(sqlPart);
    }

    @Override
    public Builder<T> dataDecrement(String column, int steps) {
        String sqlPart = FormatUtil.column(column) + '=' + FormatUtil.column(column) + '-' + steps;
        return data(sqlPart);
    }

    @Override
    public Long count(String column) {
        this.grammar.forAggregates();
        String              alias    = UUID.randomUUID().toString();
        Map<String, Object> countMap = selectFunction("count", column, alias).firstOrFail().toMap();
        return (Long) countMap.get(alias);
    }

    @Override
    public String max(String column) {

        String              alias    = UUID.randomUUID().toString();
        Map<String, Object> countMap = selectFunction("max", column, alias).firstOrFail().toMap();
        return countMap.get(alias).toString();
    }

    @Override
    public String min(String column) {
        String              alias    = UUID.randomUUID().toString();
        Map<String, Object> countMap = selectFunction("min", column, alias).firstOrFail().toMap();
        return countMap.get(alias).toString();
    }

    @Override
    public String avg(String column) {
        String              alias    = UUID.randomUUID().toString();
        Map<String, Object> countMap = selectFunction("avg", column, alias).firstOrFail().toMap();
        return countMap.get(alias).toString();
    }

    @Override
    public String sum(String column) {
        String              alias    = UUID.randomUUID().toString();
        Map<String, Object> countMap = selectFunction("sum", column, alias).firstOrFail().toMap();
        return countMap.get(alias).toString();
    }

    @Override
    public Builder<T> sharedLock() {
        grammar.pushLock("lock in share mode");
        return this;
    }

    @Override
    public Builder<T> lockForUpdate() {
        grammar.pushLock("for update");
        return this;
    }

    @Override
    public Builder<T> union(GenerateSqlPart<T> closure) {
        String sqlPart = generateSql(closure);
        grammar.pushUnion(sqlPart, "union");
        return this;
    }

    @Override
    public Builder<T> unionAll(GenerateSqlPart<T> closure) {
        String sqlPart = generateSql(closure);
        grammar.pushUnion(sqlPart, "union all");
        return this;
    }

    @Override
    public Builder<T> join(String table, String column1, String symbol, String column2) {
        return join(JoinType.INNER, table, column1, symbol, column2);
    }

    @Override
    public Builder<T> join(JoinType joinType, String table, String column1, String symbol, String column2) {
        String sqlPart =
            FormatUtil.spaces(joinType.getOperation()) + "join " + FormatUtil.backQuote(table) + FormatUtil.spaces(
                "on") +
                FormatUtil.column(column1) + symbol + FormatUtil.column(column2);
        grammar.pushJoin(sqlPart);
        return this;
    }

    @Override
    public Builder<T> inRandomOrder(String field) {
        Builder<T> sameSubBuilder1 = model.newQuery();
        Builder<T> sameSubBuilder2 = model.newQuery();
        String  maxSql          = sameSubBuilder1.selectFunction("max", field, null).toSql(SqlType.SELECT);
        String  minSql          = sameSubBuilder2.selectFunction("min", field, null).toSql(SqlType.SELECT);
        String  floorSql        = "rand()*((" + maxSql + ")-(" + minSql + "))+(" + minSql + ")";
        // select floor(rand()*((select max(`$key`) from $from)-(select min(`$key`) from $from))+(select min(`$key`) from $from))
        // select * from `student` where `id`in(select floor(rand()*((select max(`id`) from `student`)-(select min
        // (`id`) from `student`))+(select min(`id`) from `student`))) limit 5
        return whereSubQuery(field, "in", builder -> builder.selectFunction("floor", floorSql, null));
    }
}
