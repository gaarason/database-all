package gaarason.database.query;

import gaarason.database.appointment.AggregatesType;
import gaarason.database.appointment.JoinType;
import gaarason.database.appointment.SqlType;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.relation.RelationSubQuery;
import gaarason.database.contract.function.BuilderAnyWrapper;
import gaarason.database.contract.function.BuilderWrapper;
import gaarason.database.contract.function.ToSqlFunctionalInterface;
import gaarason.database.contract.query.Grammar;
import gaarason.database.lang.Nullable;
import gaarason.database.support.EntityMember;
import gaarason.database.support.FieldRelationMember;
import gaarason.database.util.FormatUtils;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * 公用查询构造器
 * @param <T>
 * @param <K>
 * @author xt
 */
public abstract class AbstractBuilder<B extends Builder<B, T, K>, T, K> extends WhereBuilder<B, T, K> {

    @Override
    public <R> R aggregate(AggregatesType op, String column) {
        String alias = StringUtils.getRandomString(6);
        B builder = getSelf();

        // 存在 group
        if (!grammar.isEmpty(Grammar.SQLPartType.GROUP)) {
            // 不存在 select
            if (grammar.isEmpty(Grammar.SQLPartType.SELECT)) {
                Grammar.SQLPartInfo groupInfo = grammar.get(Grammar.SQLPartType.GROUP);
                selectRaw(groupInfo.getSqlString(), groupInfo.getParameters());
            }

            builder = (B) model.withTrashed().from(alias + "sub", subBuilder -> getSelf());
        }
        // 不存在 group, 但存在 select
        else if (!grammar.isEmpty(Grammar.SQLPartType.SELECT)) {
            builder = (B) model.newQuery().setAnyBuilder(builder).clear(Grammar.SQLPartType.SELECT);
        }

        Map<String, Object> resMap = builder.selectFunction(op.toString(), column, alias).firstOrFail().toMap();
        return ObjectUtils.typeCast(resMap.get(alias));
    }

    @Override
    public B forceIndex(String indexName) {
        grammar.addSmartSeparator(Grammar.SQLPartType.FORCE_INDEX, FormatUtils.column(indexName), null);
        return getSelf();
    }

    @Override
    public B ignoreIndex(String indexName) {
        grammar.addSmartSeparator(Grammar.SQLPartType.IGNORE_INDEX, FormatUtils.column(indexName), null);
        return getSelf();
    }

    @Override
    public B fromRaw(@Nullable String sqlPart) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            grammar.set(Grammar.SQLPartType.FROM, sqlPart, null);
            grammar.set(Grammar.SQLPartType.TABLE, sqlPart, null);
        }
        return getSelf();
    }

    @Override
    public B fromRaw(@Nullable String sqlPart, Collection<?> parameters) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            grammar.set(Grammar.SQLPartType.FROM, sqlPart, ObjectUtils.typeCast(parameters));
            grammar.set(Grammar.SQLPartType.TABLE, sqlPart, ObjectUtils.typeCast(parameters));
        }
        return getSelf();
    }

    @Override
    public B from(String table) {
        return fromRaw(backQuote(table));
    }

    @Override
    public B from(Object anyEntity) {
        String tableName = modelShadowProvider.parseAnyEntityWithCache(anyEntity.getClass()).getTableName();
        return from(tableName);
    }

    @Override
    public B from(String alias, BuilderAnyWrapper closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sqlPart = FormatUtils.bracket(sqlPartInfo.getSqlString()) + alias;
        grammar.set(Grammar.SQLPartType.FROM, sqlPart, sqlPartInfo.getParameters());
        grammar.set(Grammar.SQLPartType.TABLE, sqlPart, sqlPartInfo.getParameters());
        return getSelf();
    }

    @Override
    public B from(String alias, String sql) {
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
    public B value(@Nullable Collection<?> values) {
        if (ObjectUtils.isEmpty(values)) {
            grammar.addSmartSeparator(Grammar.SQLPartType.VALUE, "()", null);
            return getSelf();
        }
        Collection<Object> parameters = new ArrayList<>();
        String sqlPart = FormatUtils.bracket(grammar.replaceValuesAndFillParameters(values, parameters, ","));
        grammar.addSmartSeparator(Grammar.SQLPartType.VALUE, sqlPart, parameters);
        return getSelf();
    }

    @Override
    public B valueList(Collection<? extends Collection<?>> valuesList) {
        for (Collection<?> values : valuesList) {
            value(values);
        }
        return getSelf();
    }

    @Override
    public B sharedLock() {
        grammar.set(Grammar.SQLPartType.LOCK, "lock in share mode", null);
        return getSelf();
    }

    @Override
    public B lockForUpdate() {
        grammar.set(Grammar.SQLPartType.LOCK, "for update", null);
        return getSelf();
    }

    @Override
    public B union(BuilderWrapper<B, T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        grammar.add(Grammar.SQLPartType.UNION, "union" + FormatUtils.bracket(sqlPartInfo.getSqlString()),
            sqlPartInfo.getParameters());
        return getSelf();
    }

    @Override
    public B unionAll(BuilderWrapper<B, T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        grammar.add(Grammar.SQLPartType.UNION, "union all" + FormatUtils.bracket(sqlPartInfo.getSqlString()),
            sqlPartInfo.getParameters());
        return getSelf();
    }

    private boolean unionEachFirstActionMark = true;

    @Override
    public B union(Builder<?, ?, ?> builder) {
        if (unionEachFirstActionMark) {
            setAnyBuilder(builder);
            unionEachFirstActionMark = false;
        } else {
            union(subBuilder -> subBuilder.setAnyBuilder(builder));
        }
        return getSelf();
    }

    @Override
    public B unionAll(Builder<?, ?, ?> builder) {
        if (unionEachFirstActionMark) {
            setAnyBuilder(builder);
            unionEachFirstActionMark = false;
        } else {
            unionAll(subBuilder -> subBuilder.setAnyBuilder(builder));
        }
        return getSelf();
    }

    @Override
    public B joinRaw(@Nullable String sqlPart) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            grammar.add(Grammar.SQLPartType.JOIN, sqlPart, null);
        }
        return getSelf();
    }

    @Override
    public B joinRaw(@Nullable String sqlPart, @Nullable Collection<?> parameters) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            grammar.add(Grammar.SQLPartType.JOIN, sqlPart,
                ObjectUtils.isEmpty(parameters) ? null : ObjectUtils.typeCast(parameters));
        }
        return getSelf();
    }

    @Override
    public B join(String table, String column1, String symbol, String column2) {
        return join(JoinType.INNER, table, builder -> builder.whereColumn(column1, symbol, column2));
    }

    @Override
    public B join(JoinType joinType, String table, String column1, String symbol, String column2) {
        return join(joinType, table, builder -> builder.whereColumn(column1, symbol, column2));
    }

    @Override
    public B join(JoinType joinType, BuilderWrapper<B, T, K> tempTable, String alias,
        BuilderWrapper<B, T, K> joinConditions) {
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

        return getSelf();
    }

    @Override
    public B join(JoinType joinType, String table,
        BuilderWrapper<B, T, K> joinConditions) {
        Grammar.SQLPartInfo conditions = generateSql(joinConditions, Grammar.SQLPartType.WHERE);
        String sqlPart = FormatUtils.spaces(joinType.getOperation()) + "join " + table + FormatUtils.spaces("on") +
            FormatUtils.bracket(conditions.getSqlString());
        grammar.add(Grammar.SQLPartType.JOIN, sqlPart, conditions.getParameters());
        return getSelf();
    }

    @Override
    public B inRandomOrder() {
        return inRandomOrder(model.getPrimaryKeyColumnName());
    }

    @Override
    public B inRandomOrder(String column) {
        B sameSubBuilder1 = model.newQuery();
        B sameSubBuilder2 = model.newQuery();
        String maxSql = sameSubBuilder1.selectFunction("max", column, null).toSql(SqlType.SELECT);
        String minSql = sameSubBuilder2.selectFunction("min", column, null).toSql(SqlType.SELECT);
        String floorSql = "rand()*((" + maxSql + ")-(" + minSql + "))+(" + minSql + ")";
        // select floor(rand()*((select max(`$key`) from $from)-(select min(`$key`) from $from))+(select min(`$key`) from $from))
        // select * from `student` where `id`in(select floor(rand()*((select max(`id`) from `student`)-(select min
        // (`id`) from `student`))+(select min(`id`) from `student`))) limit 5
        return whereSubQuery(column, "in", builder -> builder.selectFunction("floor", floorSql, null));
    }

    @Override
    public B when(boolean condition, BuilderWrapper<B, T, K> closure) {
        return when(condition, closure, builder -> builder);
    }

    @Override
    public B when(boolean condition, BuilderWrapper<B, T, K> closureIfTrue,
        BuilderWrapper<B, T, K> closureIfFalse) {
        return condition ? closureIfTrue.execute(getSelf()) : closureIfFalse.execute(getSelf());
    }


    protected B columnGrammar(String sqlPart, @Nullable Collection<Object> parameters) {
        grammar.addSmartSeparator(Grammar.SQLPartType.COLUMN, sqlPart, parameters, ",");
        return getSelf();
    }

    @Override
    public B column(String column) {
        String sqlPart = backQuote(column);
        return columnRaw(sqlPart);
    }

    @Override
    public B columnRaw(@Nullable String sqlPart) {
        return columnRaw(sqlPart, null);
    }

    @Override
    public B columnRaw(@Nullable String sqlPart, @Nullable Collection<?> parameters) {
        return ObjectUtils.isEmpty(sqlPart) ? getSelf() :
            columnGrammar(sqlPart, ObjectUtils.isEmpty(parameters) ? null : ObjectUtils.typeCast(parameters));
    }

    @Override
    public B column(String... columns) {
        for (String column : columns) {
            column(column);
        }
        return getSelf();
    }

    @Override
    public B column(Collection<String> columnList) {
        for (String column : columnList) {
            column(column);
        }
        return getSelf();
    }

    @Override
    public B whereHas(String relationFieldName, BuilderAnyWrapper closure) {
        // 获取关联关系
        EntityMember<T, K> entityMember = modelShadowProvider.parseAnyEntityWithCache(entityClass);
        FieldRelationMember relationMember = entityMember.getFieldRelationMemberByFieldName(
                relationFieldName);

        return whereAnyExists(builder -> (relationMember.getRelationSubQuery().prepareForWhereHas(closure)));
    }

    @Override
    public B whereNotHas(String relationFieldName, BuilderAnyWrapper closure) {
        // 获取关联关系
        EntityMember<T, K> entityMember = modelShadowProvider.parseAnyEntityWithCache(entityClass);
        FieldRelationMember relationMember = entityMember.getFieldRelationMemberByFieldName(
                relationFieldName);

        return whereAnyNotExists(builder -> (relationMember.getRelationSubQuery().prepareForWhereHas(closure)));
    }

    @Override
    public B whereHasIn(String relationFieldName, BuilderAnyWrapper closure) {
        // 获取关联关系
        EntityMember<T, K> entityMember = modelShadowProvider.parseAnyEntityWithCache(entityClass);
        FieldRelationMember relationMember = entityMember.getFieldRelationMemberByFieldName(
                relationFieldName);
        RelationSubQuery relationSubQuery = relationMember.getRelationSubQuery();
        return whereIn(relationSubQuery.localKeyForWhereHasIn(),
                builder -> ObjectUtils.typeCast(relationSubQuery.prepareForWhereHasIn(closure)));
    }

    @Override
    public B whereNotHasIn(String relationFieldName, BuilderAnyWrapper closure) {
        // 获取关联关系
        EntityMember<T, K> entityMember = modelShadowProvider.parseAnyEntityWithCache(entityClass);
        FieldRelationMember relationMember = entityMember.getFieldRelationMemberByFieldName(
                relationFieldName);
        RelationSubQuery relationSubQuery = relationMember.getRelationSubQuery();
        return whereNotIn(relationSubQuery.localKeyForWhereHasIn(),
                builder -> ObjectUtils.typeCast(relationSubQuery.prepareForWhereHasIn(closure)));
    }

    @Override
    public B limit(Object offset, Object take) {
        Collection<Object> parameters = new ArrayList<>(2);
        String sqlPart = grammar.replaceValueAndFillParameters(offset, parameters) + "," +
                grammar.replaceValueAndFillParameters(take, parameters);
        grammar.set(Grammar.SQLPartType.LIMIT, sqlPart, parameters);
        return getSelf();
    }

    @Override
    public B limit(Object take) {
        Collection<Object> parameters = new ArrayList<>(1);
        String sqlPart = grammar.replaceValueAndFillParameters(take, parameters);
        grammar.set(Grammar.SQLPartType.LIMIT, sqlPart, parameters);
        return getSelf();
    }

}
