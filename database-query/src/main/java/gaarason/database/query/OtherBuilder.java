package gaarason.database.query;

import gaarason.database.appointment.AggregatesType;
import gaarason.database.appointment.JoinType;
import gaarason.database.appointment.SqlType;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.ToSqlFunctionalInterface;
import gaarason.database.contract.query.Grammar;
import gaarason.database.lang.Nullable;
import gaarason.database.util.FormatUtils;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * 公用查询构造器
 * @param <T>
 * @param <K>
 * @author xt
 */
public abstract class OtherBuilder<T, K> extends WhereBuilder<T, K> {

    @Override
    public <R> R aggregate(AggregatesType op, String column) {
        String alias = StringUtils.getRandomString(6);
        Builder<T, K> builder = this;

        if (!grammar.isEmpty(Grammar.SQLPartType.GROUP)) {
            if (grammar.isEmpty(Grammar.SQLPartType.SELECT)) {
                Grammar.SQLPartInfo groupInfo = grammar.get(Grammar.SQLPartType.GROUP);
                selectRaw(groupInfo.getSqlString(), groupInfo.getParameters());
            }

            builder = model.newQuery().from(alias + "sub", subBuilder -> this);
        }
        Map<String, Object> resMap = builder.selectFunction(op.toString(), column, alias).firstOrFail().toMap();
        return ObjectUtils.typeCast(resMap.get(alias));
    }

    @Override
    public Long count() {
        return count("*");
    }

    @Override
    public Long count(String column) {
        return aggregate(AggregatesType.count, column);
    }

    @Override
    public String max(String column) {
        Object aggregate = aggregate(AggregatesType.max, column);
        return String.valueOf(aggregate);
    }

    @Override
    public String min(String column) {
        Object aggregate = aggregate(AggregatesType.min, column);
        return String.valueOf(aggregate);
    }

    @Override
    public BigDecimal avg(String column) {
        return aggregate(AggregatesType.avg, column);
    }

    @Override
    public BigDecimal sum(String column) {
        return aggregate(AggregatesType.sum, column);
    }

    @Override
    public Builder<T, K> forceIndex(String indexName) {
        grammar.addSmartSeparator(Grammar.SQLPartType.FORCE_INDEX, FormatUtils.column(indexName), null);
        return this;
    }

    @Override
    public Builder<T, K> ignoreIndex(String indexName) {
        grammar.addSmartSeparator(Grammar.SQLPartType.IGNORE_INDEX, FormatUtils.column(indexName), null);
        return this;
    }

    @Override
    public Builder<T, K> fromRaw(@Nullable String sqlPart) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            grammar.set(Grammar.SQLPartType.FROM, sqlPart, null);
            grammar.set(Grammar.SQLPartType.TABLE, sqlPart, null);
        }
        return this;
    }

    @Override
    public Builder<T, K> fromRaw(@Nullable String sqlPart, Collection<?> parameters) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            grammar.set(Grammar.SQLPartType.FROM, sqlPart, ObjectUtils.typeCast(parameters));
            grammar.set(Grammar.SQLPartType.TABLE, sqlPart, ObjectUtils.typeCast(parameters));
        }
        return this;
    }

    @Override
    public Builder<T, K> from(String table) {
        return fromRaw(FormatUtils.column(table));
    }

    @Override
    public Builder<T, K> from(Object anyEntity) {
        String tableName = modelShadowProvider.parseAnyEntityWithCache(anyEntity.getClass()).getTableName();
        return from(tableName);
    }

    @Override
    public Builder<T, K> from(String alias, GenerateSqlPartFunctionalInterface<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sqlPart = FormatUtils.bracket(sqlPartInfo.getSqlString()) + alias;
        grammar.set(Grammar.SQLPartType.FROM, sqlPart, sqlPartInfo.getParameters());
        grammar.set(Grammar.SQLPartType.TABLE, sqlPart, sqlPartInfo.getParameters());
        return this;
    }

    @Override
    public Builder<T, K> from(String alias, String sql) {
        return fromRaw(FormatUtils.bracket(sql) + alias);
    }

    @Override
    public String toSql(SqlType sqlType) {
        return toSql(sqlType,
            ((sql, parameters) -> String.format(StringUtils.replace(sql, " ? ", "\"%s\""), parameters.toArray())));
    }

    @Override
    public String toSql(SqlType sqlType, ToSqlFunctionalInterface closure) {
        Grammar.SQLPartInfo sqlPartInfo = grammar.generateSql(sqlType);
        assert sqlPartInfo.getParameters() != null;
        return closure.execute(sqlPartInfo.getSqlString(), sqlPartInfo.getParameters());
    }

    @Override
    public Builder<T, K> value(@Nullable Collection<?> values) {
        if (ObjectUtils.isEmpty(values)) {
            grammar.addSmartSeparator(Grammar.SQLPartType.VALUE, "()", null);
            return this;
        }
        Collection<Object> parameters = new ArrayList<>();
        String sqlPart = FormatUtils.bracket(grammar.replaceValuesAndFillParameters(values, parameters, ","));
        grammar.addSmartSeparator(Grammar.SQLPartType.VALUE, sqlPart, parameters);
        return this;
    }

    @Override
    public Builder<T, K> valueList(Collection<? extends Collection<?>> valuesList) {
        for (Collection<?> values : valuesList) {
            value(values);
        }
        return this;
    }


    @Override
    public Builder<T, K> sharedLock() {
        grammar.set(Grammar.SQLPartType.LOCK, "lock in share mode", null);
        return this;
    }

    @Override
    public Builder<T, K> lockForUpdate() {
        grammar.set(Grammar.SQLPartType.LOCK, "for update", null);
        return this;
    }

    @Override
    public Builder<T, K> union(GenerateSqlPartFunctionalInterface<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        grammar.add(Grammar.SQLPartType.UNION, "union" + FormatUtils.bracket(sqlPartInfo.getSqlString()),
            sqlPartInfo.getParameters());
        return this;
    }

    @Override
    public Builder<T, K> unionAll(GenerateSqlPartFunctionalInterface<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        grammar.add(Grammar.SQLPartType.UNION, "union all" + FormatUtils.bracket(sqlPartInfo.getSqlString()),
            sqlPartInfo.getParameters());
        return this;
    }

    @Override
    public Builder<T, K> joinRaw(@Nullable String sqlPart) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            grammar.add(Grammar.SQLPartType.JOIN, sqlPart, null);
        }
        return this;
    }

    @Override
    public Builder<T, K> joinRaw(@Nullable String sqlPart, @Nullable Collection<?> parameters) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            grammar.add(Grammar.SQLPartType.JOIN, sqlPart,
                ObjectUtils.isEmpty(parameters) ? null : ObjectUtils.typeCast(parameters));
        }
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
        Grammar.SQLPartInfo tableInfo = generateSql(tempTable);
        String table = FormatUtils.bracket(tableInfo.getSqlString()) + alias;

        Grammar.SQLPartInfo conditions = generateSql(joinConditions, Grammar.SQLPartType.WHERE);
        String sqlPart = FormatUtils.spaces(joinType.getOperation()) + "join " + table + FormatUtils.spaces("on") +
            FormatUtils.bracket(conditions.getSqlString());

        Collection<Object> parameters = tableInfo.getParameters();
        assert parameters != null;
        assert conditions.getParameters() != null;
        parameters.addAll(conditions.getParameters());

        grammar.add(Grammar.SQLPartType.JOIN, sqlPart, parameters);

        return this;
    }

    @Override
    public Builder<T, K> join(JoinType joinType, String table,
        GenerateSqlPartFunctionalInterface<T, K> joinConditions) {
        Grammar.SQLPartInfo conditions = generateSql(joinConditions, Grammar.SQLPartType.WHERE);
        String sqlPart = FormatUtils.spaces(joinType.getOperation()) + "join " + table + FormatUtils.spaces("on") +
            FormatUtils.bracket(conditions.getSqlString());
        grammar.add(Grammar.SQLPartType.JOIN, sqlPart, conditions.getParameters());
        return this;
    }

    @Override
    public Builder<T, K> inRandomOrder() {
        return inRandomOrder(model.getPrimaryKeyColumnName());
    }

    @Override
    public Builder<T, K> inRandomOrder(String column) {
        Builder<T, K> sameSubBuilder1 = model.newQuery();
        Builder<T, K> sameSubBuilder2 = model.newQuery();
        String maxSql = sameSubBuilder1.selectFunction("max", column, null).toSql(SqlType.SELECT);
        String minSql = sameSubBuilder2.selectFunction("min", column, null).toSql(SqlType.SELECT);
        String floorSql = "rand()*((" + maxSql + ")-(" + minSql + "))+(" + minSql + ")";
        // select floor(rand()*((select max(`$key`) from $from)-(select min(`$key`) from $from))+(select min(`$key`) from $from))
        // select * from `student` where `id`in(select floor(rand()*((select max(`id`) from `student`)-(select min
        // (`id`) from `student`))+(select min(`id`) from `student`))) limit 5
        return whereSubQuery(column, "in", builder -> builder.selectFunction("floor", floorSql, null));
    }

    @Override
    public Builder<T, K> when(boolean condition, GenerateSqlPartFunctionalInterface<T, K> closure) {
        return when(condition, closure, builder -> builder);
    }

    @Override
    public Builder<T, K> when(boolean condition, GenerateSqlPartFunctionalInterface<T, K> closureIfTrue,
        GenerateSqlPartFunctionalInterface<T, K> closureIfFalse) {
        return condition ? closureIfTrue.execute(this) : closureIfFalse.execute(this);
    }


    protected Builder<T, K> columnGrammar(String sqlPart, @Nullable Collection<Object> parameters) {
        grammar.addSmartSeparator(Grammar.SQLPartType.COLUMN, sqlPart, parameters, ",");
        return this;
    }

    @Override
    public Builder<T, K> column(String column) {
        String sqlPart = backQuote(column);
        return columnRaw(sqlPart);
    }

    @Override
    public Builder<T, K> columnRaw(@Nullable String sqlPart) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            columnGrammar(sqlPart, null);
        }
        return this;
    }

    @Override
    public Builder<T, K> columnRaw(@Nullable String sqlPart, @Nullable Collection<?> parameters) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            columnGrammar(sqlPart, ObjectUtils.isEmpty(parameters) ? null : ObjectUtils.typeCast(parameters));
        }
        return this;
    }

    @Override
    public Builder<T, K> column(String... columns) {
        for (String column : columns) {
            column(column);
        }
        return this;
    }

    @Override
    public Builder<T, K> column(Collection<String> columnList) {
        for (String column : columnList) {
            column(column);
        }
        return this;
    }
}
