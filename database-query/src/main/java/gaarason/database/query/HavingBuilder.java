package gaarason.database.query;

import gaarason.database.appointment.EntityUseType;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.query.Grammar;
import gaarason.database.lang.Nullable;
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
public abstract class HavingBuilder<T, K> extends GroupBuilder<T, K> {

    protected HavingBuilder(GaarasonDataSource gaarasonDataSource, Model<T, K> model, Grammar grammar) {
        super(gaarasonDataSource, model, grammar);
    }

    protected Builder<T, K> havingGrammar(String sqlPart, @Nullable Collection<Object> parameters, String separator) {
        grammar.addSmartSeparator(Grammar.SQLPartType.HAVING, sqlPart, parameters, separator);
        return this;
    }

    @Override
    public Builder<T, K> havingRaw(@Nullable String sqlPart, @Nullable Collection<?> parameters) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            havingGrammar(sqlPart, ObjectUtils.isEmpty(parameters) ? null : ObjectUtils.typeCast(parameters), " and ");
        }
        return this;
    }

    @Override
    public Builder<T, K> havingRaw(@Nullable String sqlPart) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            havingGrammar(sqlPart, null, " and ");
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
        ArrayList<Object> parameters = new ArrayList<>();
        String sqlPart = backQuote(column) + symbol + grammar.replaceValueAndFillParameters(value, parameters);
        havingGrammar(sqlPart, parameters, " and ");
        return this;
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
    public Builder<T, K> having(Object anyEntity) {
        final Map<String, Object> columnValueMap = modelShadowProvider.entityToMap(anyEntity,
            EntityUseType.CONDITION);
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
    public Builder<T, K> havingFind(@Nullable Map<String, Object> map) {
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
        return this;
    }

    @Override
    public Builder<T, K> havingNotFind(@Nullable Map<String, Object> map) {
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
    public Builder<T, K> havingKeywords(@Nullable Object value, Collection<String> columns) {
        andHaving(builder -> {
            for (String column : columns) {
                builder.orHaving(builderInner -> builderInner.havingMayLike(column, value));
            }
            return builder;
        });
        return this;
    }

    @Override
    public Builder<T, K> havingKeywords(@Nullable Object value, String... columns) {
        return havingKeywords(value, Arrays.asList(columns));
    }

    @Override
    public Builder<T, K> havingKeywordsIgnoreNull(@Nullable Object value, Collection<String> columns) {
        return andHavingIgnoreEmpty(builder -> {
            for (String column : columns) {
                builder.orHavingIgnoreEmpty(builderInner -> builderInner.havingMayLikeIgnoreNull(column, value));
            }
            return builder;
        });
    }

    @Override
    public Builder<T, K> havingKeywordsIgnoreNull(@Nullable Object value, String... columns) {
        return havingKeywordsIgnoreNull(value, Arrays.asList(columns));
    }

    @Override
    public Builder<T, K> havingLike(String column, @Nullable Object value) {
        if(ObjectUtils.isEmpty(value) || ObjectUtils.isEmpty(String.valueOf(value).replace("%", ""))){
            return this;
        }
        return havingIgnoreNull(column, "like", StringUtils.sqlPathLike(value));
    }

    @Override
    public Builder<T, K> havingLike(@Nullable Object anyEntity) {
        final Map<String, Object> columnValueMap = modelShadowProvider.entityToMap(anyEntity,
            EntityUseType.CONDITION);
        return havingLike(columnValueMap);
    }

    @Override
    public Builder<T, K> havingLike(@Nullable Map<String, Object> map) {
        if (ObjectUtils.isEmpty(map)) {
            return this;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            havingLike(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Builder<T, K> havingNotLike(String column, @Nullable Object value) {
        if(ObjectUtils.isEmpty(value) || ObjectUtils.isEmpty(String.valueOf(value).replace("%", ""))){
            return this;
        }
        return havingIgnoreNull(column, "not like", StringUtils.sqlPathLike(value));
    }

    @Override
    public Builder<T, K> havingNotLike(@Nullable Object anyEntity) {
        final Map<String, Object> columnValueMap = modelShadowProvider.entityToMap(anyEntity,
            EntityUseType.CONDITION);
        return havingNotLike(columnValueMap);
    }

    @Override
    public Builder<T, K> havingNotLike(@Nullable Map<String, Object> map) {
        if (ObjectUtils.isEmpty(map)) {
            return this;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            havingNotLike(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Builder<T, K> havingMayLike(String column, @Nullable Object value) {
        String s = conversion.castNullable(value, String.class);
        if (!ObjectUtils.isNull(s) && (s.endsWith("%") || s.startsWith("%"))) {
            return havingLike(column, value);
        } else {
            return having(column, value);
        }
    }

    @Override
    public Builder<T, K> havingMayNotLike(String column, @Nullable Object value) {
        String s = conversion.castNullable(value, String.class);
        if (!ObjectUtils.isNull(s) && (s.endsWith("%") || s.startsWith("%"))) {
            return havingNotLike(column, value);
        } else {
            return ObjectUtils.isNull(value) ? havingNotNull(column) : having(column, "<>", value);
        }
    }

    @Override
    public Builder<T, K> havingMayLikeIgnoreNull(String column, @Nullable Object value) {
        if (ObjectUtils.isNull(value)) {
            return this;
        }
        return havingMayLike(column, value);
    }

    @Override
    public Builder<T, K> havingMayNotLikeIgnoreNull(String column, @Nullable Object value) {
        if (ObjectUtils.isNull(value)) {
            return this;
        }
        return havingMayNotLike(column, value);
    }

    @Override
    public Builder<T, K> havingMayLike(@Nullable Object anyEntity) {
        final Map<String, Object> columnValueMap = modelShadowProvider.entityToMap(anyEntity,
            EntityUseType.CONDITION);
        return havingMayLike(columnValueMap);
    }

    @Override
    public Builder<T, K> havingMayNotLike(@Nullable Object anyEntity) {
        final Map<String, Object> columnValueMap = modelShadowProvider.entityToMap(anyEntity,
            EntityUseType.CONDITION);
        return havingMayNotLike(columnValueMap);
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
    public Builder<T, K> havingMayNotLike(@Nullable Map<String, Object> map) {
        if (ObjectUtils.isEmpty(map)) {
            return this;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            havingMayNotLike(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Builder<T, K> havingMayLikeIgnoreNull(@Nullable Map<String, Object> map) {
        if (ObjectUtils.isEmpty(map)) {
            return this;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            havingMayLikeIgnoreNull(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Builder<T, K> havingMayNotLikeIgnoreNull(@Nullable Map<String, Object> map) {
        if (ObjectUtils.isEmpty(map)) {
            return this;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            havingMayNotLikeIgnoreNull(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Builder<T, K> havingSubQuery(String column, String symbol, String completeSql) {
        String sqlPart = backQuote(column) + symbol + FormatUtils.bracket(completeSql);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingSubQuery(String column, String symbol,
        GenerateSqlPartFunctionalInterface<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String completeSql = FormatUtils.bracket(sqlPartInfo.getSqlString());
        String sqlPart = backQuote(column) + symbol + completeSql;
        return havingGrammar(sqlPart, sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public Builder<T, K> havingIn(String column, Collection<?> valueList) {
        Collection<Object> parameters = new ArrayList<>();
        String valueStr = grammar.replaceValuesAndFillParameters(ObjectUtils.typeCast(valueList), parameters, ",");
        String sqlPart = backQuote(column) + "in" + FormatUtils.bracket(valueStr);
        return havingGrammar(sqlPart, parameters, " and ");
    }

    @Override
    public Builder<T, K> havingNotIn(String column, Collection<?> valueList) {
        Collection<Object> parameters = new ArrayList<>();
        String valueStr = grammar.replaceValuesAndFillParameters(ObjectUtils.typeCast(valueList), parameters, ",");
        String sqlPart = backQuote(column) + "not in" + FormatUtils.bracket(valueStr);
        return havingGrammar(sqlPart, parameters, " and ");
    }

    @Override
    public Builder<T, K> havingInRaw(String column, String sql) {
        String sqlPart = backQuote(column) + "in" + FormatUtils.bracket(sql);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingNotInRaw(String column, String sql) {
        String sqlPart = backQuote(column) + "not in" + FormatUtils.bracket(sql);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingInIgnoreEmpty(String column, @Nullable Collection<?> valueList) {
        return ObjectUtils.isEmpty(valueList) ? this : havingIn(column, valueList);
    }

    @Override
    public Builder<T, K> havingNotInIgnoreEmpty(String column, @Nullable Collection<?> valueList) {
        return ObjectUtils.isEmpty(valueList) ? this : havingNotIn(column, valueList);
    }

    @Override
    public Builder<T, K> havingIn(String column, Object... valueArray) {
        return havingIn(column, Arrays.asList(valueArray));
    }

    @Override
    public Builder<T, K> havingNotIn(String column, Object... valueArray) {
        return havingNotIn(column, Arrays.asList(valueArray));
    }

    @Override
    public Builder<T, K> havingInIgnoreEmpty(String column, @Nullable Object... valueArray) {
        return ObjectUtils.isEmpty(valueArray) ? this : havingIn(column, valueArray);
    }

    @Override
    public Builder<T, K> havingNotInIgnoreEmpty(String column, @Nullable Object... valueArray) {
        return ObjectUtils.isEmpty(valueArray) ? this : havingNotIn(column, valueArray);
    }

    @Override
    public Builder<T, K> havingIn(String column, GenerateSqlPartFunctionalInterface<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sqlPart = backQuote(column) + "in" + FormatUtils.bracket(sqlPartInfo.getSqlString());
        return havingGrammar(sqlPart, sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public Builder<T, K> havingNotIn(String column, GenerateSqlPartFunctionalInterface<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sqlPart = backQuote(column) + "not in" + FormatUtils.bracket(sqlPartInfo.getSqlString());
        return havingGrammar(sqlPart, sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public Builder<T, K> havingBetween(String column, Object min, Object max) {
        Collection<Object> parameters = new ArrayList<>();
        return havingBetweenRaw(backQuote(column), grammar.replaceValueAndFillParameters(min, parameters),
            grammar.replaceValueAndFillParameters(max, parameters), parameters);
    }

    @Override
    public Builder<T, K> havingBetweenRaw(String column, Object min, Object max, @Nullable Collection<?> parameters) {
        String sqlPart = column + " between " + min + " and " + max;
        return havingGrammar(sqlPart, ObjectUtils.isEmpty(parameters) ? null : ObjectUtils.typeCast(parameters),
            " and ");
    }

    @Override
    public Builder<T, K> havingBetweenRaw(String column, Object min, Object max) {
        return havingBetweenRaw(column, min, max, null);
    }

    @Override
    public Builder<T, K> havingNotBetween(String column, Object min, Object max) {
        Collection<Object> parameters = new ArrayList<>();
        return havingNotBetweenRaw(backQuote(column), grammar.replaceValueAndFillParameters(min, parameters),
            grammar.replaceValueAndFillParameters(max, parameters), parameters);
    }

    @Override
    public Builder<T, K> havingNotBetweenRaw(String column, Object min, Object max,
        @Nullable Collection<?> parameters) {
        String sqlPart = column + " not between " + min + " and " + max;
        return havingGrammar(sqlPart, ObjectUtils.isEmpty(parameters) ? null : ObjectUtils.typeCast(parameters),
            " and ");
    }

    @Override
    public Builder<T, K> havingNotBetweenRaw(String column, Object min, Object max) {
        return havingNotBetweenRaw(column, min, max, null);
    }

    @Override
    public Builder<T, K> havingNull(String column) {
        String sqlPart = backQuote(column) + "is null";
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingNotNull(String column) {
        String sqlPart = backQuote(column) + "is not null";
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingExistsRaw(String sql) {
        String sqlPart = "exists " + FormatUtils.bracket(sql);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingNotExistsRaw(String sql) {
        String sqlPart = "not exists " + FormatUtils.bracket(sql);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingExists(GenerateSqlPartFunctionalInterface<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sql = "exists " + FormatUtils.bracket(sqlPartInfo.getSqlString());
        return havingGrammar(sql, sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public Builder<T, K> havingNotExists(GenerateSqlPartFunctionalInterface<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sql = "not exists " + FormatUtils.bracket(sqlPartInfo.getSqlString());
        return havingGrammar(sql, sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public Builder<T, K> havingColumn(String column1, String symbol, String column2) {
        String sqlPart = backQuote(column1) + symbol + backQuote(column2);
        return havingRaw(sqlPart);
    }

    @Override
    public Builder<T, K> havingColumn(String column1, String column2) {
        return havingColumn(column1, "=", column2);
    }

    @Override
    public Builder<T, K> andHaving(GenerateSqlPartFunctionalInterface<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure, Grammar.SQLPartType.HAVING);
        return havingGrammar(FormatUtils.bracket(sqlPartInfo.getSqlString()), sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public Builder<T, K> orHaving(GenerateSqlPartFunctionalInterface<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure, Grammar.SQLPartType.HAVING);
        return havingGrammar(FormatUtils.bracket(sqlPartInfo.getSqlString()), sqlPartInfo.getParameters(), " or ");
    }

    @Override
    public Builder<T, K> andHavingIgnoreEmpty(GenerateSqlPartFunctionalInterface<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure, Grammar.SQLPartType.HAVING);
        if (!ObjectUtils.isEmpty(sqlPartInfo.getSqlString())) {
            havingGrammar(FormatUtils.bracket(sqlPartInfo.getSqlString()), sqlPartInfo.getParameters(), " and ");
        }
        return this;
    }

    @Override
    public Builder<T, K> orHavingIgnoreEmpty(GenerateSqlPartFunctionalInterface<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure, Grammar.SQLPartType.HAVING);
        if (!ObjectUtils.isEmpty(sqlPartInfo.getSqlString())) {
            havingGrammar(FormatUtils.bracket(sqlPartInfo.getSqlString()), sqlPartInfo.getParameters(), " or ");
        }
        return this;
    }
}
