package gaarason.database.query;

import gaarason.database.appointment.JoinType;
import gaarason.database.appointment.OrderBy;
import gaarason.database.appointment.SqlType;
import gaarason.database.config.ConversionConfig;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.query.Grammar;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.ContainerProvider;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.util.FormatUtils;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;
import java.util.*;

/**
 * 公用查询构造器
 * @param <T>
 * @param <K>
 * @author xt
 */
public abstract class CommonBuilder<T extends Serializable, K extends Serializable> extends MiddleBuilder<T, K> {

    protected CommonBuilder(GaarasonDataSource gaarasonDataSource, Model<T, K> model, Grammar grammar) {
        super(gaarasonDataSource, model, grammar);
    }

    @Override
    public Builder<T, K> whereRaw(@Nullable String sqlPart) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            grammar.pushWhere(sqlPart, "and");
        }
        return this;
    }

    @Override
    public Builder<T, K> whereRaw(@Nullable Collection<String> sqlParts) {
        if (!ObjectUtils.isEmpty(sqlParts)) {
            for (String sqlPart : sqlParts) {
                whereRaw(sqlPart);
            }
        }
        return this;
    }

    @Override
    public Builder<T, K> where(String column, String symbol, Object value) {
        String sqlPart = column(column) + symbol + formatValue(value);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereIgnoreNull(String column, String symbol, @Nullable Object value) {
        return ObjectUtils.isNull(value) ? this : where(column, symbol, value);
    }

    @Override
    public Builder<T, K> where(String column, @Nullable Object value) {
        return ObjectUtils.isNull(value) ? whereNull(column) : where(column, "=", value);
    }

    @Override
    public Builder<T, K> whereIgnoreNull(String column, @Nullable Object value) {
        return ObjectUtils.isNull(value) ? this : where(column, value);
    }

    @Override
    public Builder<T, K> where(T entity) {
        final Map<String, Object> columnValueMap = ModelShadowProvider.columnValueMap(entity);
        return where(columnValueMap);
    }

    @Override
    public Builder<T, K> where(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            where(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Builder<T, K> whereIgnoreNull(@Nullable Map<String, Object> map) {
        if (!ObjectUtils.isNull(map)) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                whereIgnoreNull(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Override
    public Builder<T, K> whereLike(String column, @Nullable Object value) {
        return whereIgnoreNull(column, "like", value);
    }

    @Override
    public Builder<T, K> whereLike(@Nullable T entity) {
        final Map<String, Object> columnValueMap = ModelShadowProvider.columnValueMap(entity);
        return whereLike(columnValueMap);
    }

    @Override
    public Builder<T, K> whereLike(@Nullable Map<String, Object> map) {
        if (ObjectUtils.isEmpty(map)) {
            return this;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            whereLike(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Builder<T, K> whereMayLike(String column, @Nullable Object value) {
        String s = ContainerProvider.getBean(ConversionConfig.class).castNullable(value, String.class);
        if (!ObjectUtils.isNull(s) && (s.endsWith("%") || s.startsWith("%"))) {
            return whereLike(column, value);
        } else {
            return where(column, value);
        }
    }

    @Override
    public Builder<T, K> whereMayLike(@Nullable T entity) {
        final Map<String, Object> columnValueMap = ModelShadowProvider.columnValueMap(entity);
        return whereMayLike(columnValueMap);
    }

    @Override
    public Builder<T, K> whereMayLike(@Nullable Map<String, Object> map) {
        if (ObjectUtils.isEmpty(map)) {
            return this;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            whereMayLike(entry.getKey(), entry.getValue());
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
    public Builder<T, K> whereInIgnoreEmpty(String column, @Nullable Collection<?> valueList) {
        return ObjectUtils.isEmpty(valueList) ? this : whereIn(column, valueList);
    }

    @Override
    public Builder<T, K> whereIn(String column, Object... valueArray) {
        Set<Object> valueSet = new HashSet<>(Arrays.asList(valueArray));
        return whereIn(column, valueSet);
    }

    @Override
    public Builder<T, K> whereInIgnoreEmpty(String column, @Nullable Object... valueArray) {
        return ObjectUtils.isEmpty(valueArray) ? this : whereIn(column, valueArray);
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
    public Builder<T, K> whereNotInIgnoreEmpty(String column, @Nullable Collection<?> valueList) {
        return ObjectUtils.isEmpty(valueList) ? this : whereNotIn(column, valueList);
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
    public Builder<T, K> whereNotInIgnoreEmpty(String column, @Nullable Object... valueArray) {
        return ObjectUtils.isEmpty(valueArray) ? this : whereNotIn(column, valueArray);
    }

    @Override
    public Builder<T, K> whereBetween(String column, Object min, Object max) {
        String sqlPart = column(column) + "between" + formatValue(min) + "and" + formatValue(max);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereNotBetween(String column, Object min, Object max) {
        String sqlPart = column(column) + "not between" + formatValue(min) + "and" + formatValue(max);
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
    public Builder<T, K> havingRaw(@Nullable String sqlPart) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            grammar.pushHaving(sqlPart, "and");
        }
        return this;
    }

    @Override
    public Builder<T, K> havingRaw(@Nullable Collection<String> sqlParts) {
        if (!ObjectUtils.isEmpty(sqlParts)) {
            for (String sqlPart : sqlParts) {
                havingRaw(sqlPart);
            }
        }
        return this;
    }

    @Override
    public Builder<T, K> having(String column, String symbol, Object value) {
        String sqlPart = column(column) + symbol + formatValue(value);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingIgnoreNull(String column, String symbol, @Nullable Object value) {
        return ObjectUtils.isNull(value) ? this : having(column, symbol, value);
    }

    @Override
    public Builder<T, K> having(String column, @Nullable Object value) {
        return ObjectUtils.isNull(value) ? havingNull(column) : having(column, "=", value);
    }

    @Override
    public Builder<T, K> havingIgnoreNull(String column, @Nullable Object value) {
        return ObjectUtils.isNull(value) ? this : having(column, value);
    }

    @Override
    public Builder<T, K> having(T entity) {
        final Map<String, Object> columnValueMap = ModelShadowProvider.columnValueMap(entity);
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
    public Builder<T, K> havingIgnoreNull(@Nullable Map<String, Object> map) {
        if (!ObjectUtils.isNull(map)) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                havingIgnoreNull(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Override
    public Builder<T, K> havingLike(String column, @Nullable Object value) {
        return havingIgnoreNull(column, "like", value);
    }

    @Override
    public Builder<T, K> havingLike(@Nullable T entity) {
        final Map<String, Object> columnValueMap = ModelShadowProvider.columnValueMap(entity);
        return havingLike(columnValueMap);
    }

    @Override
    public Builder<T, K> havingLike(@Nullable Map<String, Object> map) {
        if (ObjectUtils.isEmpty(map)) {
            return this;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            having(entry.getKey(), "like", entry.getValue());
        }
        return this;
    }

    @Override
    public Builder<T, K> havingMayLike(String column, @Nullable Object value) {
        String s = ContainerProvider.getBean(ConversionConfig.class).castNullable(value, String.class);
        if (!ObjectUtils.isNull(s) && (s.endsWith("%") || s.startsWith("%"))) {
            return havingLike(column, value);
        } else {
            return having(column, value);
        }
    }

    @Override
    public Builder<T, K> havingMayLike(@Nullable T entity) {
        final Map<String, Object> columnValueMap = ModelShadowProvider.columnValueMap(entity);
        return havingMayLike(columnValueMap);
    }

    @Override
    public Builder<T, K> havingMayLike(@Nullable Map<String, Object> map) {
        if (ObjectUtils.isEmpty(map)) {
            return this;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            havingMayLike(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Builder<T, K> havingIn(String column, Collection<?> valueList) {
        String sqlPart = column(column) + "in" + FormatUtils.bracket(formatValue(valueList));
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingInIgnoreEmpty(String column, @Nullable Collection<?> valueList) {
        return ObjectUtils.isEmpty(valueList) ? this : havingIn(column, valueList);
    }

    @Override
    public Builder<T, K> havingIn(String column, Object... valueArray) {
        Set<Object> valueSet = new HashSet<>(Arrays.asList(valueArray));
        return havingIn(column, valueSet);
    }

    @Override
    public Builder<T, K> havingInIgnoreEmpty(String column, @Nullable Object... valueArray) {
        return ObjectUtils.isEmpty(valueArray) ? this : havingIn(column, valueArray);
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
    public Builder<T, K> havingNotInIgnoreEmpty(String column, @Nullable Collection<?> valueList) {
        return ObjectUtils.isEmpty(valueList) ? this : havingNotIn(column, valueList);
    }

    @Override
    public Builder<T, K> havingNotIn(String column, Object... valueArray) {
        Set<Object> valueSet = new HashSet<>(Arrays.asList(valueArray));
        return havingNotIn(column, valueSet);
    }

    @Override
    public Builder<T, K> havingNotInIgnoreEmpty(String column, @Nullable Object... valueArray) {
        return ObjectUtils.isEmpty(valueArray) ? this : havingNotIn(column, valueArray);
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
        String sqlPart = column(column) + "not between" + formatValue(min) + "and" + formatValue(max);
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

    // todo
    @Override
    public Builder<T, K> selectFunction(String function, String parameter, @Nullable String alias) {
        String sqlPart =
                function + FormatUtils.bracket(parameter) + (alias == null ? "" : " as " + FormatUtils.quotes(alias));
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
        String sqlPart =
                function + FormatUtils.bracket(completeSql) + (alias == null ? "" : " as " + FormatUtils.quotes(alias));
        grammar.pushSelect(sqlPart);
        return this;
    }

    @Override
    public Builder<T, K> selectFunction(String function, GenerateSqlPartFunctionalInterface<T, K> closure) {
        return selectFunction(function, closure, null);

    }

    @Override
    public Builder<T, K> orderBy(@Nullable String column, OrderBy type) {
        if (null != column) {
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
        if (valueList.isEmpty()) {
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
    public Builder<T, K> data(String column, @Nullable Object value) {
        String sqlPart = column(column) + '=' + formatData(value);
        return data(sqlPart);
    }

    @Override
    public Builder<T, K> dataIgnoreNull(String column, @Nullable Object value) {
        return ObjectUtils.isNull(value) ? this : data(column, value);
    }

    @Override
    public Builder<T, K> data(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            data(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Builder<T, K> dataIgnoreNull(@Nullable Map<String, Object> map) {
        if (!ObjectUtils.isEmpty(map)) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                dataIgnoreNull(entry.getKey(), entry.getValue());
            }
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
    public Builder<T, K> joinRaw(String sqlPart) {
        grammar.pushJoin(sqlPart);
        return this;
    }

    @Override
    public Builder<T, K> join(String table, String column1, String symbol, String column2) {
        return join(JoinType.INNER, table, builder -> builder.whereColumn(column1, symbol, column2));
    }

    @Override
    public Builder<T, K> join(JoinType joinType, String table, String column1, String symbol, String column2) {
        return join(joinType, table, builder -> builder.whereColumn(column1, symbol, column2));
    }


    @Override
    public Builder<T, K> join(JoinType joinType, GenerateSqlPartFunctionalInterface<T, K> tempTable, String alias,
                              GenerateSqlPartFunctionalInterface<T, K> joinConditions) {
        String table = generateSql(tempTable) + alias;
        return join(joinType, table, joinConditions);
    }

    @Override
    public Builder<T, K> join(JoinType joinType, String table,
                              GenerateSqlPartFunctionalInterface<T, K> joinConditions) {
        String conditions = generateSqlPart(joinConditions);
        String sqlPart =
                FormatUtils.spaces(joinType.getOperation()) + "join " + table + FormatUtils.spaces("on") + conditions;
        return joinRaw(sqlPart);
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
}
