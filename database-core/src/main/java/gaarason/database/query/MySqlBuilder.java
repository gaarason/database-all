package gaarason.database.query;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.query.Grammar;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.appointment.JoinType;
import gaarason.database.eloquent.appointment.OrderBy;
import gaarason.database.eloquent.appointment.SqlType;
import gaarason.database.exception.AggregatesNotSupportedGroupException;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.InsertNotSuccessException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.query.grammars.MySqlGrammar;
import gaarason.database.util.FormatUtil;

import java.util.*;

public class MySqlBuilder<T, K> extends BaseBuilder<T, K> {

    public MySqlBuilder(GaarasonDataSource gaarasonDataSource, Model<T, K> model, Class<T> entityClass) {
        super(gaarasonDataSource, model, entityClass);
    }

    @Override
    Grammar grammarFactory() {
        return new MySqlGrammar(ModelShadowProvider.getByEntity(entityClass).getTableName());
    }


    @Override
    public Builder<T, K> whereRaw(String sqlPart) {
        if (!"".equals(sqlPart)) {
            grammar.pushWhere(sqlPart, "and");
        }
        return this;
    }

    @Override
    public Builder<T, K> where(String column, String symbol, String value) {
        String sqlPart = FormatUtil.column(column) + symbol + formatValue(value);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> where(String column, String value) {
        return where(column, "=", value);
    }

    @Override
    public Builder<T, K> whereSubQuery(String column, String symbol, String completeSql) {
        String sqlPart = FormatUtil.column(column) + symbol + FormatUtil.bracket(completeSql);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereSubQuery(String column, String symbol, GenerateSqlPartFunctionalInterface closure) {
        String completeSql = generateSql(closure);
        String sqlPart     = FormatUtil.column(column) + symbol + completeSql;
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereIn(String column, Collection<?> valueList) {
        String sqlPart = FormatUtil.column(column) + "in" + FormatUtil.bracket(formatValue(valueList));
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereIn(String column, String... valueArray) {
        Set<Object> valueSet = new HashSet<>(Arrays.asList(valueArray));
        return whereIn(column, valueSet);
    }

    @Override
    public Builder<T, K> whereInRaw(String column, String sql) {
        String sqlPart = FormatUtil.column(column) + "in" + FormatUtil.bracket(sql);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereIn(String column, GenerateSqlPartFunctionalInterface closure) {
        String sqlPart = generateSql(closure);
        return whereInRaw(column, sqlPart);
    }

    @Override
    public Builder<T, K> whereNotIn(String column, Collection<?> valueList) {
        String sqlPart = FormatUtil.column(column) + "not in" + FormatUtil.bracket(formatValue(valueList));
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereNotInRaw(String column, String sql) {
        String sqlPart = FormatUtil.column(column) + "not in" + FormatUtil.bracket(sql);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereNotIn(String column, GenerateSqlPartFunctionalInterface closure) {
        String sqlPart = generateSql(closure);
        return whereNotInRaw(column, sqlPart);
    }

    @Override
    public Builder<T, K> whereNotIn(String column, String... valueArray) {
        Set<Object> valueSet = new HashSet<>(Arrays.asList(valueArray));
        return whereNotIn(column, valueSet);
    }

    @Override
    public Builder<T, K> whereBetween(String column, String min, String max) {
        String sqlPart = FormatUtil.column(column) + "between" + formatValue(min) + "and" + formatValue(max);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereNotBetween(String column, String min, String max) {
        String sqlPart =
            FormatUtil.column(column) + "not between" + formatValue(min) + "and" + formatValue(max);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereNull(String column) {
        String sqlPart = FormatUtil.column(column) + "is null";
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereNotNull(String column) {
        String sqlPart = FormatUtil.column(column) + "is not null";
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereExistsRaw(String sql) {
        String sqlPart = "exists " + FormatUtil.bracket(sql);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereExists(GenerateSqlPartFunctionalInterface closure) {
        String sql = generateSql(closure);
        return whereExistsRaw(sql);
    }

    @Override
    public Builder<T, K> whereNotExistsRaw(String sql) {
        String sqlPart = "not exists " + FormatUtil.bracket(sql);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereNotExists(GenerateSqlPartFunctionalInterface closure) {
        String sql = generateSql(closure);
        return whereNotExistsRaw(sql);
    }

    @Override
    public Builder<T, K> whereColumn(String column1, String symbol, String column2) {
        String sqlPart = FormatUtil.column(column1) + symbol + FormatUtil.column(column2);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereColumn(String column1, String column2) {
        return whereColumn(column1, "=", column2);
    }

    @Override
    public Builder<T, K> andWhere(GenerateSqlPartFunctionalInterface closure) {
        String sqlPart = generateSqlPart(closure);
        grammar.pushWhere(sqlPart, "and");
        return this;
    }

    @Override
    public Builder<T, K> orWhere(GenerateSqlPartFunctionalInterface closure) {
        String sqlPart = generateSqlPart(closure);
        grammar.pushWhere(sqlPart, "or");
        return this;
    }

    @Override
    public Builder<T, K> havingRaw(String sqlPart) {
        grammar.pushHaving(sqlPart, "and");
        return this;
    }

    @Override
    public Builder<T, K> having(String column, String symbol, String value) {
        String sqlPart = FormatUtil.column(column) + symbol + formatValue(value);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> having(String column, String value) {
        return having(column, "=", value);
    }

    @Override
    public Builder<T, K> havingIn(String column, Collection<?> valueList) {
        String sqlPart = FormatUtil.column(column) + "in" + FormatUtil.bracket(formatValue(valueList));
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingIn(String column, String... valueArray) {
        Set<Object> valueSet = new HashSet<>(Arrays.asList(valueArray));
        return havingIn(column, valueSet);
    }

    @Override
    public Builder<T, K> havingInRaw(String column, String sql) {
        String sqlPart = FormatUtil.column(column) + "in" + FormatUtil.bracket(sql);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingIn(String column, GenerateSqlPartFunctionalInterface closure) {
        String sqlPart = generateSql(closure);
        return havingInRaw(column, sqlPart);
    }

    @Override
    public Builder<T, K> havingNotIn(String column, Collection<?> valueList) {
        String sqlPart = FormatUtil.column(column) + "not in" + FormatUtil.bracket(formatValue(valueList));
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingNotIn(String column, String... valueArray) {
        Set<Object> valueSet = new HashSet<>(Arrays.asList(valueArray));
        return havingNotIn(column, valueSet);
    }

    @Override
    public Builder<T, K> havingNotInRaw(String column, String sql) {
        String sqlPart = FormatUtil.column(column) + "not in" + FormatUtil.bracket(sql);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingNotIn(String column, GenerateSqlPartFunctionalInterface closure) {
        String sqlPart = generateSql(closure);
        return havingNotInRaw(column, sqlPart);
    }

    @Override
    public Builder<T, K> havingBetween(String column, String min, String max) {
        String sqlPart = FormatUtil.column(column) + "between" + formatValue(min) + "and" + formatValue(max);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingNotBetween(String column, String min, String max) {
        String sqlPart =
            FormatUtil.column(column) + "not between" + formatValue(min) + "and" + formatValue(max);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingNull(String column) {
        String sqlPart = FormatUtil.column(column) + "is null";
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingNotNull(String column) {
        String sqlPart = FormatUtil.column(column) + "is not null";
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingExistsRaw(String sql) {
        String sqlPart = "exists " + FormatUtil.bracket(sql);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingExists(GenerateSqlPartFunctionalInterface Closure) {
        String sql = generateSql(Closure);
        return havingExistsRaw(sql);
    }

    @Override
    public Builder<T, K> havingNotExistsRaw(String sql) {
        String sqlPart = "not exists " + FormatUtil.bracket(sql);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingNotExists(GenerateSqlPartFunctionalInterface Closure) {
        String sql = generateSql(Closure);
        return havingNotExistsRaw(sql);
    }

    @Override
    public Builder<T, K> havingColumn(String column1, String symbol, String column2) {
        String sqlPart = FormatUtil.column(column1) + symbol + FormatUtil.column(column2);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingColumn(String column1, String column2) {
        return havingColumn(column1, "=", column2);
    }

    @Override
    public Builder<T, K> andHaving(GenerateSqlPartFunctionalInterface closure) {
        String sqlPart = generateSqlPart(closure);
        grammar.pushHaving(sqlPart, "and");
        return this;
    }

    @Override
    public Builder<T, K> orHaving(GenerateSqlPartFunctionalInterface closure) {
        String sqlPart = generateSqlPart(closure);
        grammar.pushHaving(sqlPart, "or");
        return this;
    }

    @Override
    public Builder<T, K> from(String table) {
        grammar.pushFrom(FormatUtil.column(table));
        return this;
    }

    @Override
    public Builder<T, K> select(String column) {
        String sqlPart = FormatUtil.column(column);
        grammar.pushSelect(sqlPart);
        return this;
    }

    @Override
    public Builder<T, K> select(String... columnArray) {
        for (String column : columnArray) {
            select(column);
        }
        return this;
    }

    @Override
    public Builder<T, K> select(Collection<String> columnList) {
        for (String column : columnList) {
            select(column);
        }
        return this;
    }

    @Override
    public Builder<T, K> selectFunction(String function, String parameter, @Nullable String alias) {
        String sqlPart = function + FormatUtil.bracket(parameter) + (alias == null ? "" :
            " as " + FormatUtil.quotes(alias));
        grammar.pushSelect(sqlPart);
        return this;
    }

    @Override
    public Builder<T, K> selectFunction(String function, GenerateSqlPartFunctionalInterface closure,
                                        @Nullable String alias) {
        String completeSql = generateSql(closure);
        String sqlPart = function + FormatUtil.bracket(completeSql) + (alias == null ? "" :
            " as " + FormatUtil.quotes(alias));
        grammar.pushSelect(sqlPart);
        return this;
    }

    @Override
    public Builder<T, K> orderBy(String column, OrderBy type) {
        String sqlPart = FormatUtil.column(column) + " " + type.getOperation();
        grammar.pushOrderBy(sqlPart);
        return this;
    }

    @Override
    public Builder<T, K> orderBy(String column) {
        return orderBy(column, OrderBy.ASC);
    }

    @Override
    public Builder<T, K> limit(int offset, int take) {
        String sqlPart = String.valueOf(offset) + ',' + take;
        grammar.pushLimit(sqlPart);
        return this;
    }

    @Override
    public Builder<T, K> limit(int take) {
        String sqlPart = String.valueOf(take);
        grammar.pushLimit(sqlPart);
        return this;
    }

    @Override
    public Builder<T, K> groupRaw(String sqlPart) {
        grammar.pushGroup(sqlPart);
        return this;
    }

    @Override
    public Builder<T, K> group(String column) {
        String sqlPart = FormatUtil.column(column);
        return groupRaw(sqlPart);
    }

    @Override
    public Builder<T, K> group(String... columnArray) {
        for (String column : columnArray) {
            group(column);
        }
        return this;
    }

    @Override
    public Builder<T, K> group(Collection<String> columnList) {
        for (String column : columnList) {
            group(column);
        }
        return this;
    }

    @Override
    public String toSql(SqlType sqlType) {
        String       sql           = grammar.generateSql(sqlType);
        List<String> parameterList = grammar.getParameterList(sqlType);
        return String.format(sql.replace(" ? ", "\"%s\""), parameterList.toArray());
    }

    @Override
    public Record<T, K> find(K id) throws SQLRuntimeException {
        return where(model.getPrimaryKeyColumnName(), id.toString()).first();
    }

    @Override
    public Record<T, K> findOrFail(K id) throws EntityNotFoundException, SQLRuntimeException {
        return where(model.getPrimaryKeyColumnName(), id.toString()).firstOrFail();
    }

    @Override
    public Record<T, K> first() throws SQLRuntimeException {
        try {
            return firstOrFail();
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    @Override
    public Record<T, K> firstOrFail() throws SQLRuntimeException, EntityNotFoundException {
        limit(1);
        return querySql();
    }

    @Override
    public RecordList<T, K> get() throws SQLRuntimeException {
        return querySqlList();
    }

    @Override
    public int insert() throws SQLRuntimeException {
        return updateSql(SqlType.INSERT);
    }

    @Override
    public int insert(T entity) throws SQLRuntimeException {
        // 获取entity所有有效sql字段
        List<String> columnNameList = ModelShadowProvider.columnNameList(entity, true);
        // 获取entity所有有效字段的值
        List<String> valueList = ModelShadowProvider.valueList(entity, columnNameList);
        // 字段加入grammar
        select(columnNameList);
        // 字段的值加入grammar
        value(valueList);
        // 执行
        return insert();
    }

    @Override
    public int insert(List<T> entityList) throws SQLRuntimeException {
        // entityList处理
        beforeBatchInsert(entityList);
        // 执行
        return insert();
    }

    @Override
    public K insertGetId() throws SQLRuntimeException {
        String       sql           = grammar.generateSql(SqlType.INSERT);
        List<String> parameterList = grammar.getParameterList(SqlType.INSERT);
        return executeGetId(sql, parameterList);
    }

    @Override
    public K insertGetId(T entity) throws SQLRuntimeException {
        // 获取entity所有有效sql字段
        List<String> columnNameList = ModelShadowProvider.columnNameList(entity, true);
        // 获取entity所有有效字段的值
        List<String> valueList = ModelShadowProvider.valueList(entity, columnNameList);
        // 字段加入grammar
        select(columnNameList);
        // 字段的值加入grammar
        value(valueList);
        // 执行, 并获取主键id
        K primaryId = insertGetId();
        // 赋值主键
        ModelShadowProvider.setPrimaryId(entity, primaryId);
        // 返回主键
        return primaryId;
    }

    @Override
    public K insertGetIdOrFail() throws SQLRuntimeException, InsertNotSuccessException {
        K id = insertGetId();
        if (id == null) {
            throw new InsertNotSuccessException();
        }
        return id;
    }

    @Override
    public K insertGetIdOrFail(T entity) throws SQLRuntimeException, InsertNotSuccessException {
        K id = insertGetId(entity);
        if (id == null) {
            throw new InsertNotSuccessException();
        }
        return id;
    }

    @Override
    public List<K> insertGetIds() throws SQLRuntimeException {
        // sql 组装
        String       sql           = grammar.generateSql(SqlType.INSERT);
        List<String> parameterList = grammar.getParameterList(SqlType.INSERT);
        return executeGetIds(sql, parameterList);
    }

    @Override
    public List<K> insertGetIds(List<T> entityList) throws SQLRuntimeException {
        // entityList处理
        beforeBatchInsert(entityList);
        return insertGetIds();
    }

    /**
     * 批量插入数据, entityList处理
     * @param entityList 数据实体对象列表
     */
    private void beforeBatchInsert(List<T> entityList) {
        // 获取entity所有有效字段
        List<String>       columnNameList = ModelShadowProvider.columnNameList(entityList.get(0), true);
        List<List<String>> valueListList  = new ArrayList<>();
        for (T entity : entityList) {
            // 获取entity所有有效字段的值
            List<String> valueList = ModelShadowProvider.valueList(entity, columnNameList);
            valueListList.add(valueList);
        }
        // 字段加入grammar
        select(columnNameList);
        // 字段的值加入grammar
        valueList(valueListList);
    }

    @Override
    public int update() throws SQLRuntimeException {
        return updateSql(SqlType.UPDATE);
    }

    @Override
    public int update(T entity) throws SQLRuntimeException {
        // 获取entity所有有效字段对其值得映射
        Map<String, String> stringStringMap = ModelShadowProvider.columnValueMap(entity, false);

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
    private String formatValue(Collection<?> valueList) {
        return FormatUtil.value(valueList, grammar);
    }

    @Override
    public Builder<T, K> value(List<String> valueList) {
        if (valueList.size() == 0) {
            grammar.pushValue("()");
            return this;
        }
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
    public Builder<T, K> valueList(List<List<String>> valueList) {
        for (List<String> value : valueList) {
            value(value);
        }
        return this;
    }

    @Override
    public Builder<T, K> data(String sqlPart) {
        grammar.pushData(sqlPart);
        return this;
    }

    @Override
    public Builder<T, K> data(String column, String value) {
        String sqlPart = FormatUtil.column(column) + '=' + formatData(value);
        return data(sqlPart);
    }

    @Override
    public Builder<T, K> data(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            data(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Builder<T, K> dataIncrement(String column, int steps) {
        String sqlPart = FormatUtil.column(column) + '=' + FormatUtil.column(column) + '+' + steps;
        return data(sqlPart);
    }

    @Override
    public Builder<T, K> dataDecrement(String column, int steps) {
        String sqlPart = FormatUtil.column(column) + '=' + FormatUtil.column(column) + '-' + steps;
        return data(sqlPart);
    }

    @Override
    public Long count(String column) {
        if (grammar.hasGroup()) {
            throw new AggregatesNotSupportedGroupException("Not support group when using count(), please retry " +
                "by selectFunction()");
        }
        this.grammar.forAggregates();
        String              alias    = UUID.randomUUID().toString();
        Map<String, Object> countMap = selectFunction("count", column, alias).firstOrFail().toMap();
        return (Long) countMap.get(alias);
    }

    @Override
    public String max(String column) {
        if (grammar.hasGroup()) {
            throw new AggregatesNotSupportedGroupException("Not support group when using max(), please retry " +
                "by selectFunction()");
        }
        String              alias    = UUID.randomUUID().toString();
        Map<String, Object> countMap = selectFunction("max", column, alias).firstOrFail().toMap();
        return countMap.get(alias).toString();
    }

    @Override
    public String min(String column) {
        if (grammar.hasGroup()) {
            throw new AggregatesNotSupportedGroupException("Not support group when using min(), please retry " +
                "by selectFunction()");
        }
        String              alias    = UUID.randomUUID().toString();
        Map<String, Object> countMap = selectFunction("min", column, alias).firstOrFail().toMap();
        return countMap.get(alias).toString();
    }

    @Override
    public String avg(String column) {
        if (grammar.hasGroup()) {
            throw new AggregatesNotSupportedGroupException("Not support group when using avg(), please retry " +
                "by selectFunction()");
        }
        String              alias    = UUID.randomUUID().toString();
        Map<String, Object> countMap = selectFunction("avg", column, alias).firstOrFail().toMap();
        return countMap.get(alias).toString();
    }

    @Override
    public String sum(String column) {
        if (grammar.hasGroup()) {
            throw new AggregatesNotSupportedGroupException("Not support group when using sum(), please retry " +
                "by selectFunction()");
        }
        String              alias    = UUID.randomUUID().toString();
        Map<String, Object> countMap = selectFunction("sum", column, alias).firstOrFail().toMap();
        return countMap.get(alias).toString();
    }

    @Override
    public Builder<T, K> sharedLock() {
        grammar.pushLock("lock in share mode");
        return this;
    }

    @Override
    public Builder<T, K> lockForUpdate() {
        grammar.pushLock("for update");
        return this;
    }

    @Override
    public Builder<T, K> union(GenerateSqlPartFunctionalInterface closure) {
        String sqlPart = generateSql(closure);
        grammar.pushUnion(sqlPart, "union");
        return this;
    }

    @Override
    public Builder<T, K> unionAll(GenerateSqlPartFunctionalInterface closure) {
        String sqlPart = generateSql(closure);
        grammar.pushUnion(sqlPart, "union all");
        return this;
    }

    @Override
    public Builder<T, K> join(String table, String column1, String symbol, String column2) {
        return join(JoinType.INNER, table, column1, symbol, column2);
    }

    @Override
    public Builder<T, K> join(JoinType joinType, String table, String column1, String symbol, String column2) {
        String sqlPart =
            FormatUtil.spaces(joinType.getOperation()) + "join " + FormatUtil.backQuote(table) + FormatUtil.spaces(
                "on") +
                FormatUtil.column(column1) + symbol + FormatUtil.column(column2);
        grammar.pushJoin(sqlPart);
        return this;
    }

    @Override
    public Builder<T, K> inRandomOrder(String field) {
        Builder<T, K> sameSubBuilder1 = model.newQuery();
        Builder<T, K> sameSubBuilder2 = model.newQuery();
        String        maxSql          = sameSubBuilder1.selectFunction("max", field, null).toSql(SqlType.SELECT);
        String        minSql          = sameSubBuilder2.selectFunction("min", field, null).toSql(SqlType.SELECT);
        String        floorSql        = "rand()*((" + maxSql + ")-(" + minSql + "))+(" + minSql + ")";
        // select floor(rand()*((select max(`$key`) from $from)-(select min(`$key`) from $from))+(select min(`$key`) from $from))
        // select * from `student` where `id`in(select floor(rand()*((select max(`id`) from `student`)-(select min
        // (`id`) from `student`))+(select min(`id`) from `student`))) limit 5
        return whereSubQuery(field, "in", builder -> builder.selectFunction("floor", floorSql, null));
    }
}
