package gaarason.database.query;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.query.Grammar;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.appointment.JoinType;
import gaarason.database.eloquent.appointment.OrderBy;
import gaarason.database.eloquent.appointment.SqlType;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.query.grammars.MySqlGrammar;
import gaarason.database.util.FormatUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Mysql sql生成器
 * @param <T>
 * @param <K>
 * @author xt
 */
public class MySqlBuilder<T extends Serializable, K extends Serializable> extends MiddleBuilder<T, K> {

    public MySqlBuilder(GaarasonDataSource gaarasonDataSource, Model<T, K> model, Class<T> entityClass) {
        super(gaarasonDataSource, model, entityClass);
    }

    @Override
    Grammar grammarFactory() {
        return new MySqlGrammar(ModelShadowProvider.getByEntityClass(entityClass).getTableName());
    }


    @Override
    public Builder<T, K> whereRaw(String sqlPart) {
        if (!"".equals(sqlPart)) {
            grammar.pushWhere(sqlPart, "and");
        }
        return this;
    }

    @Override
    public Builder<T, K> where(String column, String symbol, Object value) {
        String sqlPart = column(column) + symbol + formatValue(value);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> where(String column, Object value) {
        return where(column, "=", value);
    }

    @Override
    public Builder<T, K> where(T entity) {
        final Map<String, Object> columnValueMap = ModelShadowProvider.columnValueMap(entity, true);
        return where(columnValueMap);
    }

    @Override
    public Builder<T, K> where(Map<String, Object> map){
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            where(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Builder<T, K> whereSubQuery(String column, String symbol, String completeSql) {
        String sqlPart = column(column) + symbol + FormatUtils.bracket(completeSql);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereSubQuery(String column, String symbol, GenerateSqlPartFunctionalInterface<T, K> closure) {
        String completeSql = generateSql(closure);
        String sqlPart = column(column) + symbol + completeSql;
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereIn(String column, Collection<?> valueList) {
        String sqlPart = column(column) + "in" + FormatUtils.bracket(formatValue(valueList));
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereIn(String column, Object... valueArray) {
        Set<Object> valueSet = new HashSet<>(Arrays.asList(valueArray));
        return whereIn(column, valueSet);
    }

    @Override
    public Builder<T, K> whereInRaw(String column, String sql) {
        String sqlPart = column(column) + "in" + FormatUtils.bracket(sql);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereIn(String column, GenerateSqlPartFunctionalInterface<T, K> closure) {
        String sqlPart = generateSql(closure);
        return whereInRaw(column, sqlPart);
    }

    @Override
    public Builder<T, K> whereNotIn(String column, Collection<?> valueList) {
        String sqlPart = column(column) + "not in" + FormatUtils.bracket(formatValue(valueList));
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereNotInRaw(String column, String sql) {
        String sqlPart = column(column) + "not in" + FormatUtils.bracket(sql);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereNotIn(String column, GenerateSqlPartFunctionalInterface<T, K> closure) {
        String sqlPart = generateSql(closure);
        return whereNotInRaw(column, sqlPart);
    }

    @Override
    public Builder<T, K> whereNotIn(String column, Object... valueArray) {
        Set<Object> valueSet = new HashSet<>(Arrays.asList(valueArray));
        return whereNotIn(column, valueSet);
    }

    @Override
    public Builder<T, K> whereBetween(String column, Object min, Object max) {
        String sqlPart = column(column) + "between" + formatValue(min) + "and" + formatValue(max);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereNotBetween(String column, Object min, Object max) {
        String sqlPart =
            column(column) + "not between" + formatValue(min) + "and" + formatValue(max);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereNull(String column) {
        String sqlPart = column(column) + "is null";
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereNotNull(String column) {
        String sqlPart = column(column) + "is not null";
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereExistsRaw(String sql) {
        String sqlPart = "exists " + FormatUtils.bracket(sql);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereExists(GenerateSqlPartFunctionalInterface<T, K> closure) {
        String sql = generateSql(closure);
        return whereExistsRaw(sql);
    }

    @Override
    public Builder<T, K> whereNotExistsRaw(String sql) {
        String sqlPart = "not exists " + FormatUtils.bracket(sql);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereNotExists(GenerateSqlPartFunctionalInterface<T, K> closure) {
        String sql = generateSql(closure);
        return whereNotExistsRaw(sql);
    }

    @Override
    public Builder<T, K> whereColumn(String column1, String symbol, String column2) {
        String sqlPart = column(column1) + symbol + column(column2);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereColumn(String column1, String column2) {
        return whereColumn(column1, "=", column2);
    }

    @Override
    public Builder<T, K> andWhere(GenerateSqlPartFunctionalInterface<T, K> closure) {
        String sqlPart = generateSqlPart(closure);
        grammar.pushWhere(sqlPart, "and");
        return this;
    }

    @Override
    public Builder<T, K> orWhere(GenerateSqlPartFunctionalInterface<T, K> closure) {
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
    public Builder<T, K> having(String column, String symbol, Object value) {
        String sqlPart = column(column) + symbol + formatValue(value);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> having(String column, Object value) {
        return having(column, "=", value);
    }

    @Override
    public Builder<T, K> having(T entity) {
        final Map<String, Object> columnValueMap = ModelShadowProvider.columnValueMap(entity, true);
        return having(columnValueMap);
    }

    @Override
    public Builder<T, K> having(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            having(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Builder<T, K> havingIn(String column, Collection<?> valueList) {
        String sqlPart = column(column) + "in" + FormatUtils.bracket(formatValue(valueList));
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingIn(String column, Object... valueArray) {
        Set<Object> valueSet = new HashSet<>(Arrays.asList(valueArray));
        return havingIn(column, valueSet);
    }

    @Override
    public Builder<T, K> havingInRaw(String column, String sql) {
        String sqlPart = column(column) + "in" + FormatUtils.bracket(sql);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingIn(String column, GenerateSqlPartFunctionalInterface<T, K> closure) {
        String sqlPart = generateSql(closure);
        return havingInRaw(column, sqlPart);
    }

    @Override
    public Builder<T, K> havingNotIn(String column, Collection<?> valueList) {
        String sqlPart = column(column) + "not in" + FormatUtils.bracket(formatValue(valueList));
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingNotIn(String column, Object... valueArray) {
        Set<Object> valueSet = new HashSet<>(Arrays.asList(valueArray));
        return havingNotIn(column, valueSet);
    }

    @Override
    public Builder<T, K> havingNotInRaw(String column, String sql) {
        String sqlPart = column(column) + "not in" + FormatUtils.bracket(sql);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingNotIn(String column, GenerateSqlPartFunctionalInterface<T, K> closure) {
        String sqlPart = generateSql(closure);
        return havingNotInRaw(column, sqlPart);
    }

    @Override
    public Builder<T, K> havingBetween(String column, Object min, Object max) {
        String sqlPart = column(column) + "between" + formatValue(min) + "and" + formatValue(max);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingNotBetween(String column, Object min, Object max) {
        String sqlPart =
            column(column) + "not between" + formatValue(min) + "and" + formatValue(max);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingNull(String column) {
        String sqlPart = column(column) + "is null";
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingNotNull(String column) {
        String sqlPart = column(column) + "is not null";
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingExistsRaw(String sql) {
        String sqlPart = "exists " + FormatUtils.bracket(sql);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingExists(GenerateSqlPartFunctionalInterface<T, K> closure) {
        String sql = generateSql(closure);
        return havingExistsRaw(sql);
    }

    @Override
    public Builder<T, K> havingNotExistsRaw(String sql) {
        String sqlPart = "not exists " + FormatUtils.bracket(sql);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingNotExists(GenerateSqlPartFunctionalInterface<T, K> closure) {
        String sql = generateSql(closure);
        return havingNotExistsRaw(sql);
    }

    @Override
    public Builder<T, K> havingColumn(String column1, String symbol, String column2) {
        String sqlPart = column(column1) + symbol + column(column2);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingColumn(String column1, String column2) {
        return havingColumn(column1, "=", column2);
    }

    @Override
    public Builder<T, K> andHaving(GenerateSqlPartFunctionalInterface<T, K> closure) {
        String sqlPart = generateSqlPart(closure);
        grammar.pushHaving(sqlPart, "and");
        return this;
    }

    @Override
    public Builder<T, K> orHaving(GenerateSqlPartFunctionalInterface<T, K> closure) {
        String sqlPart = generateSqlPart(closure);
        grammar.pushHaving(sqlPart, "or");
        return this;
    }

    @Override
    public Builder<T, K> select(String column) {
        String sqlPart = column(column);
        grammar.pushSelect(sqlPart);
        return this;
    }

    @Override
    public Builder<T, K> selectRaw(String column) {
        grammar.pushSelect(column);
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
        String sqlPart = function + FormatUtils.bracket(parameter) + (alias == null ? "" :
            " as " + FormatUtils.quotes(alias));
        grammar.pushSelect(sqlPart);
        return this;
    }

    @Override
    public Builder<T, K> selectFunction(String function, String parameter) {
        return selectFunction(function, parameter, null);
    }

    @Override
    public Builder<T, K> selectFunction(String function, GenerateSqlPartFunctionalInterface<T, K> closure,
        @Nullable String alias) {
        String completeSql = generateSql(closure);
        String sqlPart = function + FormatUtils.bracket(completeSql) + (alias == null ? "" :
            " as " + FormatUtils.quotes(alias));
        grammar.pushSelect(sqlPart);
        return this;
    }

    @Override
    public Builder<T, K> selectFunction(String function, GenerateSqlPartFunctionalInterface<T, K> closure) {
        return selectFunction(function, closure, null);
    }

    @Override
    public Builder<T, K> orderBy(@Nullable String column, OrderBy type) {
        if (column != null) {
            String sqlPart = column(column) + " " + type.getOperation();
            grammar.pushOrderBy(sqlPart);
        }
        return this;
    }

    @Override
    public Builder<T, K> orderBy(@Nullable String column) {
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
        String sqlPart = column(column);
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
    public Builder<T, K> value(List<Object> valueList) {
        if (valueList.size() == 0) {
            grammar.pushValue("()");
            return this;
        }
        StringBuilder sqlPartBuilder = new StringBuilder("(");
        for (Object value : valueList) {
            String stub = formatData(value);
            sqlPartBuilder.append(stub).append(',');
        }
        String sqlPart = sqlPartBuilder.deleteCharAt(sqlPartBuilder.length() - 1).append(')').toString();
        grammar.pushValue(sqlPart);
        return this;
    }

    @Override
    public Builder<T, K> valueList(List<List<Object>> valueList) {
        for (List<Object> value : valueList) {
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
    public Builder<T, K> data(String column, Object value) {
        String sqlPart = column(column) + '=' + formatData(value);
        return data(sqlPart);
    }

    @Override
    public Builder<T, K> data(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            data(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Builder<T, K> dataIncrement(String column, int steps) {
        String sqlPart = column(column) + '=' + column(column) + '+' + steps;
        return data(sqlPart);
    }

    @Override
    public Builder<T, K> dataDecrement(String column, int steps) {
        String sqlPart = column(column) + '=' + column(column) + '-' + steps;
        return data(sqlPart);
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
    public Builder<T, K> union(GenerateSqlPartFunctionalInterface<T, K> closure) {
        String sqlPart = generateSql(closure);
        grammar.pushUnion(sqlPart, "union");
        return this;
    }

    @Override
    public Builder<T, K> unionAll(GenerateSqlPartFunctionalInterface<T, K> closure) {
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
            FormatUtils.spaces(joinType.getOperation()) + "join " + column(table) + FormatUtils.spaces(
                "on") +
                column(column1) + symbol + column(column2);
        grammar.pushJoin(sqlPart);
        return this;
    }

    @Override
    public Builder<T, K> inRandomOrder(String field) {
        Builder<T, K> sameSubBuilder1 = model.newQuery();
        Builder<T, K> sameSubBuilder2 = model.newQuery();
        String maxSql = sameSubBuilder1.selectFunction("max", field, null).toSql(SqlType.SELECT);
        String minSql = sameSubBuilder2.selectFunction("min", field, null).toSql(SqlType.SELECT);
        String floorSql = "rand()*((" + maxSql + ")-(" + minSql + "))+(" + minSql + ")";
        // select floor(rand()*((select max(`$key`) from $from)-(select min(`$key`) from $from))+(select min(`$key`) from $from))
        // select * from `student` where `id`in(select floor(rand()*((select max(`id`) from `student`)-(select min
        // (`id`) from `student`))+(select min(`id`) from `student`))) limit 5
        return whereSubQuery(field, "in", builder -> builder.selectFunction("floor", floorSql, null));
    }

    /**
     * 给字段加上引号
     * @param something 字段 eg: sum(order.amount) AS sum_price
     * @return eg: sum(`order`.`amount`) AS `sum_price`
     */
    protected static String column(String something) {
        return FormatUtils.backQuote(something, "`");
    }
}
