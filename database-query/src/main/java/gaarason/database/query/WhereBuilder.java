package gaarason.database.query;

import gaarason.database.appointment.EntityUseType;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.BuilderAnyWrapper;
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
 * Where查询构造器
 * @param <T>
 * @param <K>
 * @author xt
 */
public abstract class WhereBuilder<T, K> extends SelectBuilder<T, K> {

    protected Builder<T, K> whereGrammar(String sqlPart, @Nullable Collection<Object> parameters, String separator) {
        grammar.addSmartSeparator(Grammar.SQLPartType.WHERE, sqlPart, parameters, separator);
        return this;
    }

    @Override
    public Builder<T, K> whereRaw(@Nullable String sqlPart, @Nullable Collection<?> parameters) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            whereGrammar(sqlPart, ObjectUtils.isEmpty(parameters) ? null : ObjectUtils.typeCast(parameters), " and ");
        }
        return this;
    }

    @Override
    public Builder<T, K> whereRaw(@Nullable String sqlPart) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            whereGrammar(sqlPart, null, " and ");
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
        ArrayList<Object> parameters = new ArrayList<>();
        String sqlPart = backQuote(column) + symbol + grammar.replaceValueAndFillParameters(value, parameters);
        whereGrammar(sqlPart, parameters, " and ");
        return this;
    }

    @Override
    public Builder<T, K> whereBit(String column, Object value) {
        long packed = BitUtils.pack(value);
        // column & 1
        String sqlPart1 = backQuote(column) + "&" + packed;
        // ( column & 1 ) > 0
        String sqlPart2 = FormatUtils.bracket(sqlPart1) + ">0";
        return whereRaw(sqlPart2);
    }

    @Override
    public Builder<T, K> whereBitNot(String column, Object value) {
        long packed = BitUtils.pack(value);
        // column & 1
        String sqlPart1 = backQuote(column) + "&" + packed;
        // ( column & 1 ) > 0
        String sqlPart2 = FormatUtils.bracket(sqlPart1) + "=0";
        return whereRaw(sqlPart2);
    }

    @Override
    public Builder<T, K> whereBitIn(String column, Collection<?> values) {
        long packed = BitUtils.packs(ObjectUtils.typeCast(values));
        // column & 1
        String sqlPart1 = backQuote(column) + "&" + packed;
        // ( column & 1 ) != 0
        String sqlPart2 = FormatUtils.bracket(sqlPart1) + "!=0";
        return whereRaw(sqlPart2);
//        return andWhere(builder -> {
//            for (Object value : values) {
//                builder.orWhere(builder1 -> builder1.whereBit(column, value));
//            }
//            return builder;
//        });
    }

    @Override
    public Builder<T, K> whereBitNotIn(String column, Collection<?> values) {
        long packed = BitUtils.packs(ObjectUtils.typeCast(values));
        // column ^ 1
        String sqlPart1 = backQuote(column) + "^" + packed;
        // ( column ^ 1 ) != 0
        String sqlPart2 = FormatUtils.bracket(sqlPart1) + "!=0";
        return whereRaw(sqlPart2);
//        return andWhere(builder -> {
//            for (Object value : values) {
//                builder.orWhere(builder1 -> builder1.whereBitNot(column, value));
//            }
//            return builder;
//        });
    }

    @Override
    public Builder<T, K> whereBitStrictIn(String column, Collection<?> values) {
        long packed = BitUtils.packs(ObjectUtils.typeCast(values));
        // column & 1
        String sqlPart1 = backQuote(column) + "&" + packed;
        // ( column & 1 ) = 1
        String sqlPart2 = FormatUtils.bracket(sqlPart1) + "=" + packed;
        return whereRaw(sqlPart2);
//        return andWhere(builder -> {
//            for (Object value : values) {
//                builder.whereBit(column, value);
//            }
//            return builder;
//        });
    }

    @Override
    public Builder<T, K> whereBitStrictNotIn(String column, Collection<?> values) {
        long packed = BitUtils.packs(ObjectUtils.typeCast(values));
        // column & 1
        String sqlPart1 = backQuote(column) + "&" + packed;
        // ( column & 1 ) = 0
        String sqlPart2 = FormatUtils.bracket(sqlPart1) + "=" + 0;
        return whereRaw(sqlPart2);
//        return andWhere(builder -> {
//            for (Object value : values) {
//                builder.whereBitNot(column, value);
//            }
//            return builder;
//        });
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
    public Builder<T, K> where(Object anyEntity) {
        final Map<String, Object> columnValueMap = modelShadowProvider.entityToMap(anyEntity, EntityUseType.CONDITION);
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
    public Builder<T, K> whereFind(@Nullable Map<String, Object> map) {
        if (!ObjectUtils.isEmpty(map)) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String column = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Collection) {
                    whereInIgnoreEmpty(column, (Collection<?>) value);
                } else if (value instanceof Map) {
                    Map<?, ?> betweenMap = (Map<?, ?>) value;
                    if (betweenMap.containsKey("begin") && betweenMap.containsKey("end")) {
                        whereBetween(column, betweenMap.get("begin"), betweenMap.get("end"));
                    }
                } else {
                    whereMayLike(column, value);
                }
            }
        }
        return this;
    }

    @Override
    public Builder<T, K> whereNotFind(@Nullable Map<String, Object> map) {
        if (!ObjectUtils.isEmpty(map)) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String column = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Collection) {
                    whereNotInIgnoreEmpty(column, (Collection<?>) value);
                } else if (value instanceof Map) {
                    Map<?, ?> betweenMap = (Map<?, ?>) value;
                    if (betweenMap.containsKey("begin") && betweenMap.containsKey("end")) {
                        whereNotBetween(column, betweenMap.get("begin"), betweenMap.get("end"));
                    }
                } else {
                    whereMayNotLike(column, value);
                }
            }
        }
        return this;
    }

    @Override
    public Builder<T, K> whereIgnoreNull(@Nullable Map<String, Object> map) {
        if (!ObjectUtils.isEmpty(map)) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                whereIgnoreNull(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Override
    public Builder<T, K> whereAnyLike(@Nullable Object value, Collection<String> columns) {
        return andWhereIgnoreEmpty(builder -> {
            for (String column : columns) {
                builder.orWhereIgnoreEmpty(builderInner -> builderInner.whereLike(column, value));
            }
            return builder;
        });
    }

    @Override
    public Builder<T, K> whereAnyLike(@Nullable Object value, String... columns) {
        return whereAnyLike(value, Arrays.asList(columns));
    }

    @Override
    public Builder<T, K> whereAllLike(@Nullable Object value, Collection<String> columns) {
        return andWhereIgnoreEmpty(builder -> {
            for (String column : columns) {
                builder.andWhereIgnoreEmpty(builderInner -> builderInner.whereLike(column, value));
            }
            return builder;
        });
    }

    @Override
    public Builder<T, K> whereAllLike(@Nullable Object value, String... columns) {
        return whereAllLike(value, Arrays.asList(columns));
    }

    @Override
    public Builder<T, K> whereLike(String column, @Nullable Object value) {
        if (ObjectUtils.isEmpty(value) || ObjectUtils.isEmpty(StringUtils.replace(value, "%", ""))) {
            return this;
        }
        return whereIgnoreNull(column, "like", StringUtils.sqlPathLike(value));
    }

    @Override
    public Builder<T, K> whereLike(@Nullable Object anyEntity) {
        final Map<String, Object> columnValueMap = modelShadowProvider.entityToMap(anyEntity, EntityUseType.CONDITION);
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
    public Builder<T, K> whereNotLike(String column, @Nullable Object value) {
        if (ObjectUtils.isEmpty(value) || ObjectUtils.isEmpty(StringUtils.replace(value, "%", ""))) {
            return this;
        }
        return whereIgnoreNull(column, "not like", StringUtils.sqlPathLike(value));
    }

    @Override
    public Builder<T, K> whereNotLike(@Nullable Object anyEntity) {
        final Map<String, Object> columnValueMap = modelShadowProvider.entityToMap(anyEntity, EntityUseType.CONDITION);
        return whereNotLike(columnValueMap);
    }

    @Override
    public Builder<T, K> whereNotLike(@Nullable Map<String, Object> map) {
        if (ObjectUtils.isEmpty(map)) {
            return this;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            whereNotLike(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Builder<T, K> whereMayLike(String column, @Nullable Object value) {
        String s = conversion.castNullable(value, String.class);
        if (!ObjectUtils.isNull(s) && (s.endsWith("%") || s.startsWith("%"))) {
            return whereLike(column, value);
        } else {
            return where(column, value);
        }
    }

    @Override
    public Builder<T, K> whereMayNotLike(String column, @Nullable Object value) {
        String s = conversion.castNullable(value, String.class);
        if (!ObjectUtils.isNull(s) && (s.endsWith("%") || s.startsWith("%"))) {
            return whereNotLike(column, value);
        } else {
            return ObjectUtils.isNull(value) ? whereNotNull(column) : where(column, "<>", value);
        }
    }

    @Override
    public Builder<T, K> whereMayLikeIgnoreNull(String column, @Nullable Object value) {
        if (ObjectUtils.isNull(value)) {
            return this;
        }
        return whereMayLike(column, value);
    }

    @Override
    public Builder<T, K> whereMayNotLikeIgnoreNull(String column, @Nullable Object value) {
        if (ObjectUtils.isNull(value)) {
            return this;
        }
        return whereMayNotLike(column, value);
    }

    @Override
    public Builder<T, K> whereMayLike(@Nullable Object anyEntity) {
        final Map<String, Object> columnValueMap = modelShadowProvider.entityToMap(anyEntity, EntityUseType.CONDITION);
        return whereMayLike(columnValueMap);
    }

    @Override
    public Builder<T, K> whereMayNotLike(@Nullable Object anyEntity) {
        final Map<String, Object> columnValueMap = modelShadowProvider.entityToMap(anyEntity, EntityUseType.CONDITION);
        return whereMayNotLike(columnValueMap);
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
    public Builder<T, K> whereMayNotLike(@Nullable Map<String, Object> map) {
        if (ObjectUtils.isEmpty(map)) {
            return this;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            whereMayNotLike(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Builder<T, K> whereMayLikeIgnoreNull(@Nullable Map<String, Object> map) {
        if (ObjectUtils.isEmpty(map)) {
            return this;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            whereMayLikeIgnoreNull(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Builder<T, K> whereMayNotLikeIgnoreNull(@Nullable Map<String, Object> map) {
        if (ObjectUtils.isEmpty(map)) {
            return this;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            whereMayNotLikeIgnoreNull(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Builder<T, K> whereSubQuery(String column, String symbol, String completeSql) {
        String sqlPart = backQuote(column) + symbol + FormatUtils.bracket(completeSql);
        return whereRaw(sqlPart);
    }


    @Override
    public Builder<T, K> whereSubQuery(String column, String symbol, BuilderWrapper<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String completeSql = FormatUtils.bracket(sqlPartInfo.getSqlString());
        String sqlPart = backQuote(column) + symbol + completeSql;
        return whereGrammar(sqlPart, sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public Builder<T, K> whereIn(String column, Collection<?> valueList) {
        Collection<Object> parameters = new ArrayList<>();
        String valueStr = grammar.replaceValuesAndFillParameters(ObjectUtils.typeCast(valueList), parameters, ",");
        String sqlPart = backQuote(column) + "in" + FormatUtils.bracket(valueStr);
        return whereGrammar(sqlPart, parameters, " and ");
    }

    @Override
    public Builder<T, K> whereNotIn(String column, Collection<?> valueList) {
        Collection<Object> parameters = new ArrayList<>();
        String valueStr = grammar.replaceValuesAndFillParameters(ObjectUtils.typeCast(valueList), parameters, ",");
        String sqlPart = backQuote(column) + "not in" + FormatUtils.bracket(valueStr);
        return whereGrammar(sqlPart, parameters, " and ");
    }

    @Override
    public Builder<T, K> whereInRaw(String column, String sql) {
        String sqlPart = backQuote(column) + "in" + FormatUtils.bracket(sql);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereNotInRaw(String column, String sql) {
        String sqlPart = backQuote(column) + "not in" + FormatUtils.bracket(sql);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereInIgnoreEmpty(String column, @Nullable Collection<?> valueList) {
        return ObjectUtils.isEmpty(valueList) ? this : whereIn(column, valueList);
    }

    @Override
    public Builder<T, K> whereNotInIgnoreEmpty(String column, @Nullable Collection<?> valueList) {
        return ObjectUtils.isEmpty(valueList) ? this : whereNotIn(column, valueList);
    }

    @Override
    public Builder<T, K> whereIn(String column, Object... valueArray) {
        return whereIn(column, Arrays.asList(valueArray));
    }

    @Override
    public Builder<T, K> whereNotIn(String column, Object... valueArray) {
        return whereNotIn(column, Arrays.asList(valueArray));
    }

    @Override
    public Builder<T, K> whereInIgnoreEmpty(String column, @Nullable Object... valueArray) {
        return ObjectUtils.isEmpty(valueArray) ? this : whereIn(column, valueArray);
    }

    @Override
    public Builder<T, K> whereNotInIgnoreEmpty(String column, @Nullable Object... valueArray) {
        return ObjectUtils.isEmpty(valueArray) ? this : whereNotIn(column, valueArray);
    }

    @Override
    public Builder<T, K> whereIn(String column, BuilderWrapper<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sqlPart = backQuote(column) + "in" + FormatUtils.bracket(sqlPartInfo.getSqlString());
        return whereGrammar(sqlPart, sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public Builder<T, K> whereNotIn(String column, BuilderWrapper<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sqlPart = backQuote(column) + "not in" + FormatUtils.bracket(sqlPartInfo.getSqlString());
        return whereGrammar(sqlPart, sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public Builder<T, K> whereBetween(String column, Object min, Object max) {
        Collection<Object> parameters = new ArrayList<>();
        return whereBetweenRaw(backQuote(column), grammar.replaceValueAndFillParameters(min, parameters),
                grammar.replaceValueAndFillParameters(max, parameters), parameters);
    }

    @Override
    public Builder<T, K> whereBetweenRaw(String column, Object min, Object max, @Nullable Collection<?> parameters) {
        String sqlPart = column + " between " + min + " and " + max;
        return whereGrammar(sqlPart, ObjectUtils.isEmpty(parameters) ? null : ObjectUtils.typeCast(parameters),
                " and ");
    }

    @Override
    public Builder<T, K> whereBetweenRaw(String column, Object min, Object max) {
        return whereBetweenRaw(column, min, max, null);
    }

    @Override
    public Builder<T, K> whereNotBetween(String column, Object min, Object max) {
        Collection<Object> parameters = new ArrayList<>();
        return whereNotBetweenRaw(backQuote(column), grammar.replaceValueAndFillParameters(min, parameters),
                grammar.replaceValueAndFillParameters(max, parameters), parameters);
    }

    @Override
    public Builder<T, K> whereNotBetweenRaw(String column, Object min, Object max, @Nullable Collection<?> parameters) {
        String sqlPart = column + " not between " + min + " and " + max;
        return whereGrammar(sqlPart, ObjectUtils.isEmpty(parameters) ? null : ObjectUtils.typeCast(parameters),
                " and ");
    }

    @Override
    public Builder<T, K> whereNotBetweenRaw(String column, Object min, Object max) {
        return whereNotBetweenRaw(column, min, max, null);
    }

    @Override
    public Builder<T, K> whereNull(String column) {
        String sqlPart = backQuote(column) + "is null";
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereNotNull(String column) {
        String sqlPart = backQuote(column) + "is not null";
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereExistsRaw(String sql) {
        String sqlPart = "exists " + FormatUtils.bracket(sql);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereNotExistsRaw(String sql) {
        String sqlPart = "not exists " + FormatUtils.bracket(sql);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereExists(BuilderWrapper<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sql = "exists " + FormatUtils.bracket(sqlPartInfo.getSqlString());
        return whereGrammar(sql, sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public Builder<T, K> whereAnyExists(BuilderAnyWrapper closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sql = "exists " + FormatUtils.bracket(sqlPartInfo.getSqlString());
        return whereGrammar(sql, sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public Builder<T, K> whereNotExists(BuilderWrapper<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sql = "not exists " + FormatUtils.bracket(sqlPartInfo.getSqlString());
        return whereGrammar(sql, sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public Builder<T, K> whereAnyNotExists(BuilderAnyWrapper closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sql = "not exists " + FormatUtils.bracket(sqlPartInfo.getSqlString());
        return whereGrammar(sql, sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public Builder<T, K> whereColumn(String column1, String symbol, String column2) {
        String sqlPart = backQuote(column1) + symbol + backQuote(column2);
        return whereRaw(sqlPart);
    }

    @Override
    public Builder<T, K> whereColumn(String column1, String column2) {
        return whereColumn(column1, "=", column2);
    }

    @Override
    public Builder<T, K> whereNot(BuilderWrapper<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure, Grammar.SQLPartType.WHERE);
        return whereGrammar("!" + FormatUtils.bracket(sqlPartInfo.getSqlString()), sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public Builder<T, K> andWhere(BuilderWrapper<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure, Grammar.SQLPartType.WHERE);
        return whereGrammar(FormatUtils.bracket(sqlPartInfo.getSqlString()), sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public Builder<T, K> orWhere(BuilderWrapper<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure, Grammar.SQLPartType.WHERE);
        return whereGrammar(FormatUtils.bracket(sqlPartInfo.getSqlString()), sqlPartInfo.getParameters(), " or ");
    }

    @Override
    public Builder<T, K> andWhereIgnoreEmpty(BuilderWrapper<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure, Grammar.SQLPartType.WHERE);
        if (!ObjectUtils.isEmpty(sqlPartInfo.getSqlString())) {
            whereGrammar(FormatUtils.bracket(sqlPartInfo.getSqlString()), sqlPartInfo.getParameters(), " and ");
        }
        return this;
    }

    @Override
    public Builder<T, K> orWhereIgnoreEmpty(BuilderWrapper<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure, Grammar.SQLPartType.WHERE);
        if (!ObjectUtils.isEmpty(sqlPartInfo.getSqlString())) {
            whereGrammar(FormatUtils.bracket(sqlPartInfo.getSqlString()), sqlPartInfo.getParameters(), " or ");
        }
        return this;
    }
}
