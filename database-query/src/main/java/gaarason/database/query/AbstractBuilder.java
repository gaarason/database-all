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
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        grammar.addSmartSeparator(Grammar.SQLPartType.FORCE_INDEX, supportBackQuote(indexName), null, ",");
        return getSelf();
    }

    @Override
    public B ignoreIndex(String indexName) {
        grammar.addSmartSeparator(Grammar.SQLPartType.IGNORE_INDEX, supportBackQuote(indexName), null, ",");
        return getSelf();
    }

    protected B tableGrammar(String sqlPart, @Nullable Collection<Object> parameters) {
        grammar.set(Grammar.SQLPartType.TABLE, sqlPart, parameters);
        return getSelf();
    }

    protected B fromGrammar(String sqlPart, @Nullable Collection<Object> parameters, @Nullable String alise) {
        grammar.set(Grammar.SQLPartType.FROM, sqlPart, parameters);
        return setAlias(alise);
    }

    @Override
    public B tableRaw(@Nullable String sqlPart) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            tableGrammar(sqlPart, null);
        }
        return getSelf();
    }

    @Override
    public B table(String table) {
        return tableRaw(supportBackQuote(table));
    }

    @Override
    public B fromRaw(@Nullable String sqlPart) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            return fromGrammar(sqlPart, null, null);
        }
        return getSelf();
    }

    @Override
    public B fromRaw(@Nullable String sqlPart, Collection<?> parameters) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            return fromGrammar(sqlPart, ObjectUtils.typeCast(parameters), null);
        }
        return getSelf();
    }

    @Override
    public B from(String table) {
        table(table);
        return fromRaw(tableAlias(table));
    }

    @Override
    public B from(Object anyEntity) {
        String tableName = modelShadowProvider.parseAnyEntityWithCache(anyEntity.getClass()).getTableName();
        return from(tableName);
    }

    @Override
    public B from(String alias, BuilderAnyWrapper closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sqlPart = supportBracket(sqlPartInfo.getSqlString()) + alias;
        return fromGrammar(sqlPart, sqlPartInfo.getParameters(), alias);
    }

    @Override
    public B from(String alias, String sql) {
        return fromGrammar(supportBracket(sql) + alias, null, alias);
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

// --------------------------- value ---------------------------- //

    protected B valueGrammar(String sqlPart, @Nullable Collection<Object> parameters) {
        /*
         * 插入语句 : insert into table (column1, column2) values (v1, v2)
         * 插入语句 : insert into table (column1, column2) values (v1, v2),(v3, v4),(v5, v6)
         * 插入语句 : insert into table (column1, column2) (select c1, c2 from tableOther where id>9 limit 3)
         */
        grammar.addSmartSeparator(Grammar.SQLPartType.VALUE, sqlPart, parameters, ",");
        return getSelf();
    }

    @Override
    public B valueRaw(@Nullable String sqlPart, @Nullable Collection<?> parameters) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            sqlPart = grammar.isEmpty(Grammar.SQLPartType.VALUE) ? " values " + sqlPart : sqlPart;
            return valueGrammar(sqlPart, ObjectUtils.isEmpty(parameters) ? null : ObjectUtils.typeCast(parameters));
        }
        return getSelf();
    }

    @Override
    public B valueRaw(@Nullable String sqlPart) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            return valueRaw(sqlPart, null);
        }
        return getSelf();
    }

    @Override
    public B valueRaw(@Nullable Collection<String> sqlParts) {
        if (!ObjectUtils.isEmpty(sqlParts)) {
            for (String sqlPart : sqlParts) {
                valueRaw(sqlPart);
            }
        }
        return getSelf();
    }

    @Override
    public B value(@Nullable Collection<?> values) {
        if (ObjectUtils.isEmpty(values)) {
            return valueRaw("()", null);
        }
        Collection<Object> parameters = new ArrayList<>();
        String sqlPart = supportBracket(grammar.replaceValuesAndFillParameters(values, parameters, ","));
        return valueRaw(sqlPart, parameters);
    }

    @Override
    public B value(@Nullable Map<String, Object> entityMap) {
        if (ObjectUtils.isEmpty(entityMap)) {
            return value((Collection<?>) null);
        }
        beforeBatchInsertMapStyle(Collections.singleton(entityMap));
        return getSelf();
    }

    @Override
    public B value(Object anyEntity) {
        return values(Collections.singleton(anyEntity));
    }

    @Override
    public B values(@Nullable Collection<?> entitiesOrMapsOrLists) {
        if (ObjectUtils.isEmpty(entitiesOrMapsOrLists)) {
            return value(Collections.emptyList());
        }
        // 用第一个元素来判断类型
        Object ele = entitiesOrMapsOrLists.stream().findFirst().orElseThrow(IllegalArgumentException::new);
        // 列表
        if (ele instanceof Collection) {
            for (Object values : entitiesOrMapsOrLists) {
                value((Collection<?>) values);
            }
            return getSelf();
        }
        // map
        else if (ele instanceof Map) {
            return beforeBatchInsertMapStyle(ObjectUtils.typeCast(entitiesOrMapsOrLists));
        }
        // 实体
        else {
            return beforeBatchInsertEntityStyle(ele, entitiesOrMapsOrLists);
        }
    }

    @Override
    public B values(BuilderWrapper<B, T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sql = supportBracket(sqlPartInfo.getSqlString());
        return valueGrammar(sql, sqlPartInfo.getParameters());
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
        grammar.addSmartSeparator(Grammar.SQLPartType.UNION, "union" + supportBracket(sqlPartInfo.getSqlString()),
            sqlPartInfo.getParameters(), " ");
        return getSelf();
    }

    @Override
    public B unionAll(BuilderWrapper<B, T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        grammar.addSmartSeparator(Grammar.SQLPartType.UNION, "union all" + supportBracket(sqlPartInfo.getSqlString()),
            sqlPartInfo.getParameters(), " ");
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
        return joinRaw(sqlPart, null);
    }

    @Override
    public B joinRaw(@Nullable String sqlPart, @Nullable Collection<?> parameters) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            grammar.addSmartSeparator(Grammar.SQLPartType.JOIN, sqlPart,
                ObjectUtils.isEmpty(parameters) ? null : ObjectUtils.typeCast(parameters), " ");
        }
        return getSelf();
    }

    @Override
    public B join(String joinTable, String localColumn, String symbol, String joinTableColumn) {
        return join(JoinType.INNER, joinTable, localColumn, symbol, joinTableColumn);
    }

    @Override
    public B join(JoinType joinType, String joinTable, String localColumn, String symbol, String joinTableColumn) {
        return join(joinType, joinTable, builder -> builder.whereRaw(builder.columnAlias(localColumn) + symbol + joinTableColumn));
    }

    @Override
    public B join(JoinType joinType, BuilderWrapper<B, T, K> tempTable, String alias,
        BuilderWrapper<B, T, K> joinConditions) {
        Grammar.SQLPartInfo tableInfo = generateSql(tempTable);
        String table = supportBracket(tableInfo.getSqlString()) + alias;

        Grammar.SQLPartInfo conditions = generateSqlPart(joinConditions, Grammar.SQLPartType.WHERE);
        String sqlPart = supportSpaces(joinType.getOperation()) + "join " + table + supportSpaces("on") +
            supportBracket(conditions.getSqlString());

        Collection<Object> parameters = tableInfo.getParameters();
        assert parameters != null;
        assert conditions.getParameters() != null;
        parameters.addAll(conditions.getParameters());

        return joinRaw(sqlPart, parameters);
    }

    @Override
    public B join(JoinType joinType, String joinTable, BuilderWrapper<B, T, K> joinConditions) {
        Grammar.SQLPartInfo conditions = generateSqlPart(joinConditions, Grammar.SQLPartType.WHERE);
        String sqlPart = supportSpaces(joinType.getOperation()) + "join " + joinTable + supportSpaces("on") +
            supportBracket(conditions.getSqlString());
        return joinRaw(sqlPart, conditions.getParameters());
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
        if (condition) {
            closureIfTrue.execute(getSelf());
        } else {
            closureIfFalse.execute(getSelf());
        }
        return getSelf();
    }


    protected B columnGrammar(String sqlPart, @Nullable Collection<Object> parameters) {
        grammar.addSmartSeparator(Grammar.SQLPartType.COLUMN, sqlPart, parameters, ",");
        return getSelf();
    }

    @Override
    public B column(String column) {
        String sqlPart = columnAlias(column);
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

        return whereAnyExists(builder -> (relationMember.getRelationSubQuery().prepareForWhereHas(builder, closure)));
    }

    @Override
    public B whereNotHas(String relationFieldName, BuilderAnyWrapper closure) {
        // 获取关联关系
        EntityMember<T, K> entityMember = modelShadowProvider.parseAnyEntityWithCache(entityClass);
        FieldRelationMember relationMember = entityMember.getFieldRelationMemberByFieldName(
                relationFieldName);

        return whereAnyNotExists(builder -> (relationMember.getRelationSubQuery().prepareForWhereHas(builder, closure)));
    }

    @Override
    public B whereHasIn(String relationFieldName, BuilderAnyWrapper closure) {
        // 获取关联关系
        EntityMember<T, K> entityMember = modelShadowProvider.parseAnyEntityWithCache(entityClass);
        FieldRelationMember relationMember = entityMember.getFieldRelationMemberByFieldName(
                relationFieldName);
        RelationSubQuery relationSubQuery = relationMember.getRelationSubQuery();
        return whereIn(relationSubQuery.localKeyForWhereHasIn(),
                builder -> ObjectUtils.typeCast(relationSubQuery.prepareForWhereHasIn(builder, closure)));
    }

    @Override
    public B whereNotHasIn(String relationFieldName, BuilderAnyWrapper closure) {
        // 获取关联关系
        EntityMember<T, K> entityMember = modelShadowProvider.parseAnyEntityWithCache(entityClass);
        FieldRelationMember relationMember = entityMember.getFieldRelationMemberByFieldName(
                relationFieldName);
        RelationSubQuery relationSubQuery = relationMember.getRelationSubQuery();
        return whereNotIn(relationSubQuery.localKeyForWhereHasIn(),
                builder -> ObjectUtils.typeCast(relationSubQuery.prepareForWhereHasIn(builder, closure)));
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
