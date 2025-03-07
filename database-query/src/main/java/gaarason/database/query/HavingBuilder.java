package gaarason.database.query;

import gaarason.database.appointment.EntityUseType;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.BuilderWrapper;
import gaarason.database.contract.query.Grammar;
import gaarason.database.lang.Nullable;
import gaarason.database.util.BitUtils;
import gaarason.database.util.FormatUtils;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Having查询构造器
 * @param <T>
 * @param <K>
 * @author xt
 */
abstract class HavingBuilder<B extends Builder<B, T, K>, T, K> extends GroupBuilder<B, T, K> {

    protected B havingGrammar(String sqlPart, @Nullable Collection<Object> parameters, String separator) {
        grammar.addSmartSeparator(Grammar.SQLPartType.HAVING, sqlPart, parameters, separator);
        return getSelf();
    }

    @Override
    public B havingRaw(@Nullable String sqlPart, @Nullable Collection<?> parameters) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            havingGrammar(sqlPart, ObjectUtils.isEmpty(parameters) ? null : ObjectUtils.typeCast(parameters), " and ");
        }
        return getSelf();
    }

    @Override
    public B havingRaw(@Nullable String sqlPart) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            havingGrammar(sqlPart, null, " and ");
        }
        return getSelf();
    }

    @Override
    public B havingRaw(@Nullable Collection<String> sqlParts) {
        if (!ObjectUtils.isEmpty(sqlParts)) {
            for (String sqlPart : sqlParts) {
                havingRaw(sqlPart);
            }
        }
        return getSelf();
    }

    @Override
    public B having(String column, String symbol, Object value) {
        ArrayList<Object> parameters = new ArrayList<>();
        String sqlPart = backQuote(column) + symbol + grammar.replaceValueAndFillParameters(value, parameters);
        havingGrammar(sqlPart, parameters, " and ");
        return getSelf();
    }

    @Override
    public B havingBit(String column, Object value) {
        long packed = BitUtils.pack(value);
        // column & 1
        String sqlPart1 = backQuote(column) + "&" + packed;
        // ( column & 1 ) > 0
        String sqlPart2 = FormatUtils.bracket(sqlPart1) + ">0";
        return havingRaw(sqlPart2);
    }

    @Override
    public B havingBitNot(String column, Object value) {
        long packed = BitUtils.pack(value);
        // column & 1
        String sqlPart1 = backQuote(column) + "&" + packed;
        // ( column & 1 ) > 0
        String sqlPart2 = FormatUtils.bracket(sqlPart1) + "=0";
        return havingRaw(sqlPart2);
    }

    @Override
    public B havingBitIn(String column, Collection<?> values) {
        return andHaving(builder -> {
            for (Object value : values) {
                builder.orHaving(builder1 -> builder1.havingBit(column, value));
            }
            return builder;
        });
    }

    @Override
    public B havingBitNotIn(String column, Collection<?> values) {
        return andHaving(builder -> {
            for (Object value : values) {
                builder.orHaving(builder1 -> builder1.havingBitNot(column, value));
            }
            return builder;
        });
    }

    @Override
    public B havingBitStrictIn(String column, Collection<?> values) {
        return andHaving(builder -> {
            for (Object value : values) {
                builder.havingBit(column, value);
            }
            return builder;
        });
    }

    @Override
    public B havingBitStrictNotIn(String column, Collection<?> values) {
        return andHaving(builder -> {
            for (Object value : values) {
                builder.havingBitNot(column, value);
            }
            return builder;
        });
    }

    @Override
    public B havingIgnoreNull(String column, String symbol, @Nullable Object value) {
        return ObjectUtils.isNull(value) ? getSelf() : having(column, symbol, value);
    }

    @Override
    public B having(String column, @Nullable Object value) {
        return ObjectUtils.isNull(value) ? havingNull(column) : having(column, "=", value);
    }

    @Override
    public B havingIgnoreNull(String column, @Nullable Object value) {
        return ObjectUtils.isNull(value) ? getSelf() : having(column, value);
    }

    @Override
    public B having(Object anyEntity) {
        final Map<String, Object> columnValueMap = modelShadowProvider.entityToMap(anyEntity,
            EntityUseType.CONDITION);
        return having(columnValueMap);
    }

    @Override
    public B having(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            having(entry.getKey(), entry.getValue());
        }
        return getSelf();
    }

    @Override
    public B havingFind(@Nullable Map<String, Object> map) {
        if (!ObjectUtils.isEmpty(map)) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String column = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Collection) {
                    havingInIgnoreEmpty(column, (Collection<?>) value);
                } else if (value instanceof Map) {
                    Map<?, ?> betweenMap = (Map<?, ?>) value;
                    if (betweenMap.containsKey("begin") && betweenMap.containsKey("end")) {
                        havingBetween(column, betweenMap.get("begin"), betweenMap.get("end"));
                    }
                } else {
                    havingMayLike(column, value);
                }
            }
        }
        return getSelf();
    }

    @Override
    public B havingNotFind(@Nullable Map<String, Object> map) {
        if (!ObjectUtils.isEmpty(map)) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String column = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Collection) {
                    havingNotInIgnoreEmpty(column, (Collection<?>) value);
                } else if (value instanceof Map) {
                    Map<?, ?> betweenMap = (Map<?, ?>) value;
                    if (betweenMap.containsKey("begin") && betweenMap.containsKey("end")) {
                        havingNotBetween(column, betweenMap.get("begin"), betweenMap.get("end"));
                    }
                } else {
                    havingMayNotLike(column, value);
                }
            }
        }
        return getSelf();
    }

    @Override
    public B havingIgnoreNull(@Nullable Map<String, Object> map) {
        if (!ObjectUtils.isNull(map)) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                havingIgnoreNull(entry.getKey(), entry.getValue());
            }
        }
        return getSelf();
    }

    @Override
    public B havingAnyLike(@Nullable Object value, Collection<String> columns) {
        return andHavingIgnoreEmpty(builder -> {
            for (String column : columns) {
                builder.orHavingIgnoreEmpty(builderInner -> builderInner.havingLike(column, value));
            }
            return builder;
        });
    }

    @Override
    public B havingAnyLike(@Nullable Object value, String... columns) {
        return havingAnyLike(value, Arrays.asList(columns));
    }

    @Override
    public B havingAllLike(@Nullable Object value, Collection<String> columns) {
        return andHavingIgnoreEmpty(builder -> {
            for (String column : columns) {
                builder.andHavingIgnoreEmpty(builderInner -> builderInner.havingLike(column, value));
            }
            return builder;
        });
    }

    @Override
    public B havingAllLike(@Nullable Object value, String... columns) {
        return havingAllLike(value, Arrays.asList(columns));
    }

    @Override
    public B havingLike(String column, @Nullable Object value) {
        if (ObjectUtils.isEmpty(value) || ObjectUtils.isEmpty(StringUtils.replace(value, "%", ""))) {
            return getSelf();
        }
        return havingIgnoreNull(column, "like", StringUtils.sqlPathLike(value));
    }

    @Override
    public B havingLike(@Nullable Object anyEntity) {
        final Map<String, Object> columnValueMap = modelShadowProvider.entityToMap(anyEntity,
            EntityUseType.CONDITION);
        return havingLike(columnValueMap);
    }

    @Override
    public B havingLike(@Nullable Map<String, Object> map) {
        if (ObjectUtils.isEmpty(map)) {
            return getSelf();
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            havingLike(entry.getKey(), entry.getValue());
        }
        return getSelf();
    }

    @Override
    public B havingNotLike(String column, @Nullable Object value) {
        if (ObjectUtils.isEmpty(value) || ObjectUtils.isEmpty(StringUtils.replace(value, "%", ""))) {
            return getSelf();
        }
        return havingIgnoreNull(column, "not like", StringUtils.sqlPathLike(value));
    }

    @Override
    public B havingNotLike(@Nullable Object anyEntity) {
        final Map<String, Object> columnValueMap = modelShadowProvider.entityToMap(anyEntity,
            EntityUseType.CONDITION);
        return havingNotLike(columnValueMap);
    }

    @Override
    public B havingNotLike(@Nullable Map<String, Object> map) {
        if (ObjectUtils.isEmpty(map)) {
            return getSelf();
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            havingNotLike(entry.getKey(), entry.getValue());
        }
        return getSelf();
    }

    @Override
    public B havingMayLike(String column, @Nullable Object value) {
        String s = conversion.castNullable(value, String.class);
        if (!ObjectUtils.isNull(s) && (s.endsWith("%") || s.startsWith("%"))) {
            return havingLike(column, value);
        } else {
            return having(column, value);
        }
    }

    @Override
    public B havingMayNotLike(String column, @Nullable Object value) {
        String s = conversion.castNullable(value, String.class);
        if (!ObjectUtils.isNull(s) && (s.endsWith("%") || s.startsWith("%"))) {
            return havingNotLike(column, value);
        } else {
            return ObjectUtils.isNull(value) ? havingNotNull(column) : having(column, "<>", value);
        }
    }

    @Override
    public B havingMayLikeIgnoreNull(String column, @Nullable Object value) {
        if (ObjectUtils.isNull(value)) {
            return getSelf();
        }
        return havingMayLike(column, value);
    }

    @Override
    public B havingMayNotLikeIgnoreNull(String column, @Nullable Object value) {
        if (ObjectUtils.isNull(value)) {
            return getSelf();
        }
        return havingMayNotLike(column, value);
    }

    @Override
    public B havingMayLike(@Nullable Object anyEntity) {
        final Map<String, Object> columnValueMap = modelShadowProvider.entityToMap(anyEntity,
            EntityUseType.CONDITION);
        return havingMayLike(columnValueMap);
    }

    @Override
    public B havingMayNotLike(@Nullable Object anyEntity) {
        final Map<String, Object> columnValueMap = modelShadowProvider.entityToMap(anyEntity,
            EntityUseType.CONDITION);
        return havingMayNotLike(columnValueMap);
    }

    @Override
    public B havingMayLike(@Nullable Map<String, Object> map) {
        if (ObjectUtils.isEmpty(map)) {
            return getSelf();
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            havingMayLike(entry.getKey(), entry.getValue());
        }
        return getSelf();
    }

    @Override
    public B havingMayNotLike(@Nullable Map<String, Object> map) {
        if (ObjectUtils.isEmpty(map)) {
            return getSelf();
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            havingMayNotLike(entry.getKey(), entry.getValue());
        }
        return getSelf();
    }

    @Override
    public B havingMayLikeIgnoreNull(@Nullable Map<String, Object> map) {
        if (ObjectUtils.isEmpty(map)) {
            return getSelf();
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            havingMayLikeIgnoreNull(entry.getKey(), entry.getValue());
        }
        return getSelf();
    }

    @Override
    public B havingMayNotLikeIgnoreNull(@Nullable Map<String, Object> map) {
        if (ObjectUtils.isEmpty(map)) {
            return getSelf();
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            havingMayNotLikeIgnoreNull(entry.getKey(), entry.getValue());
        }
        return getSelf();
    }

    @Override
    public B havingSubQuery(String column, String symbol, String completeSql) {
        String sqlPart = backQuote(column) + symbol + FormatUtils.bracket(completeSql);
        return havingRaw(sqlPart);
    }

    @Override
    public B havingSubQuery(String column, String symbol,
        BuilderWrapper<B, T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String completeSql = FormatUtils.bracket(sqlPartInfo.getSqlString());
        String sqlPart = backQuote(column) + symbol + completeSql;
        return havingGrammar(sqlPart, sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public B havingIn(String column, Collection<?> valueList) {
        Collection<Object> parameters = new ArrayList<>();
        String valueStr = grammar.replaceValuesAndFillParameters(ObjectUtils.typeCast(valueList), parameters, ",");
        String sqlPart = backQuote(column) + "in" + FormatUtils.bracket(valueStr);
        return havingGrammar(sqlPart, parameters, " and ");
    }

    @Override
    public B havingNotIn(String column, Collection<?> valueList) {
        Collection<Object> parameters = new ArrayList<>();
        String valueStr = grammar.replaceValuesAndFillParameters(ObjectUtils.typeCast(valueList), parameters, ",");
        String sqlPart = backQuote(column) + "not in" + FormatUtils.bracket(valueStr);
        return havingGrammar(sqlPart, parameters, " and ");
    }

    @Override
    public B havingInRaw(String column, String sql) {
        String sqlPart = backQuote(column) + "in" + FormatUtils.bracket(sql);
        return havingRaw(sqlPart);
    }

    @Override
    public B havingNotInRaw(String column, String sql) {
        String sqlPart = backQuote(column) + "not in" + FormatUtils.bracket(sql);
        return havingRaw(sqlPart);
    }

    @Override
    public B havingInIgnoreEmpty(String column, @Nullable Collection<?> valueList) {
        return ObjectUtils.isEmpty(valueList) ? getSelf() : havingIn(column, valueList);
    }

    @Override
    public B havingNotInIgnoreEmpty(String column, @Nullable Collection<?> valueList) {
        return ObjectUtils.isEmpty(valueList) ? getSelf() : havingNotIn(column, valueList);
    }

    @Override
    public B havingIn(String column, Object... valueArray) {
        return havingIn(column, Arrays.asList(valueArray));
    }

    @Override
    public B havingNotIn(String column, Object... valueArray) {
        return havingNotIn(column, Arrays.asList(valueArray));
    }

    @Override
    public B havingInIgnoreEmpty(String column, @Nullable Object... valueArray) {
        return ObjectUtils.isEmpty(valueArray) ? getSelf() : havingIn(column, valueArray);
    }

    @Override
    public B havingNotInIgnoreEmpty(String column, @Nullable Object... valueArray) {
        return ObjectUtils.isEmpty(valueArray) ? getSelf() : havingNotIn(column, valueArray);
    }

    @Override
    public B havingIn(String column, BuilderWrapper<B, T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sqlPart = backQuote(column) + "in" + FormatUtils.bracket(sqlPartInfo.getSqlString());
        return havingGrammar(sqlPart, sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public B havingNotIn(String column, BuilderWrapper<B, T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sqlPart = backQuote(column) + "not in" + FormatUtils.bracket(sqlPartInfo.getSqlString());
        return havingGrammar(sqlPart, sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public B havingBetween(String column, Object min, Object max) {
        Collection<Object> parameters = new ArrayList<>();
        return havingBetweenRaw(backQuote(column), grammar.replaceValueAndFillParameters(min, parameters),
            grammar.replaceValueAndFillParameters(max, parameters), parameters);
    }

    @Override
    public B havingBetweenRaw(String column, Object min, Object max, @Nullable Collection<?> parameters) {
        String sqlPart = column + " between " + min + " and " + max;
        return havingGrammar(sqlPart, ObjectUtils.isEmpty(parameters) ? null : ObjectUtils.typeCast(parameters),
            " and ");
    }

    @Override
    public B havingBetweenRaw(String column, Object min, Object max) {
        return havingBetweenRaw(column, min, max, null);
    }

    @Override
    public B havingNotBetween(String column, Object min, Object max) {
        Collection<Object> parameters = new ArrayList<>();
        return havingNotBetweenRaw(backQuote(column), grammar.replaceValueAndFillParameters(min, parameters),
            grammar.replaceValueAndFillParameters(max, parameters), parameters);
    }

    @Override
    public B havingNotBetweenRaw(String column, Object min, Object max,
        @Nullable Collection<?> parameters) {
        String sqlPart = column + " not between " + min + " and " + max;
        return havingGrammar(sqlPart, ObjectUtils.isEmpty(parameters) ? null : ObjectUtils.typeCast(parameters),
            " and ");
    }

    @Override
    public B havingNotBetweenRaw(String column, Object min, Object max) {
        return havingNotBetweenRaw(column, min, max, null);
    }

    @Override
    public B havingNull(String column) {
        String sqlPart = backQuote(column) + "is null";
        return havingRaw(sqlPart);
    }

    @Override
    public B havingNotNull(String column) {
        String sqlPart = backQuote(column) + "is not null";
        return havingRaw(sqlPart);
    }

    @Override
    public B havingExistsRaw(String sql) {
        String sqlPart = "exists " + FormatUtils.bracket(sql);
        return havingRaw(sqlPart);
    }

    @Override
    public B havingNotExistsRaw(String sql) {
        String sqlPart = "not exists " + FormatUtils.bracket(sql);
        return havingRaw(sqlPart);
    }

    @Override
    public B havingExists(BuilderWrapper<B, T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sql = "exists " + FormatUtils.bracket(sqlPartInfo.getSqlString());
        return havingGrammar(sql, sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public B havingNotExists(BuilderWrapper<B, T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sql = "not exists " + FormatUtils.bracket(sqlPartInfo.getSqlString());
        return havingGrammar(sql, sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public B havingColumn(String column1, String symbol, String column2) {
        String sqlPart = backQuote(column1) + symbol + backQuote(column2);
        return havingRaw(sqlPart);
    }

    @Override
    public B havingColumn(String column1, String column2) {
        return havingColumn(column1, "=", column2);
    }

    @Override
    public B havingNot(BuilderWrapper<B, T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure, Grammar.SQLPartType.HAVING);
        return havingGrammar("!" + FormatUtils.bracket(sqlPartInfo.getSqlString()), sqlPartInfo.getParameters(), " and ");
    }
    @Override
    public B andHaving(BuilderWrapper<B, T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure, Grammar.SQLPartType.HAVING);
        return havingGrammar(FormatUtils.bracket(sqlPartInfo.getSqlString()), sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public B orHaving(BuilderWrapper<B, T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure, Grammar.SQLPartType.HAVING);
        return havingGrammar(FormatUtils.bracket(sqlPartInfo.getSqlString()), sqlPartInfo.getParameters(), " or ");
    }

    @Override
    public B andHavingIgnoreEmpty(BuilderWrapper<B, T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure, Grammar.SQLPartType.HAVING);
        if (!ObjectUtils.isEmpty(sqlPartInfo.getSqlString())) {
            havingGrammar(FormatUtils.bracket(sqlPartInfo.getSqlString()), sqlPartInfo.getParameters(), " and ");
        }
        return getSelf();
    }

    @Override
    public B orHavingIgnoreEmpty(BuilderWrapper<B, T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure, Grammar.SQLPartType.HAVING);
        if (!ObjectUtils.isEmpty(sqlPartInfo.getSqlString())) {
            havingGrammar(FormatUtils.bracket(sqlPartInfo.getSqlString()), sqlPartInfo.getParameters(), " or ");
        }
        return getSelf();
    }
}
