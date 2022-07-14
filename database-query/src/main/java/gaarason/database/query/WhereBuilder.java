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

import java.io.Serializable;
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
public abstract class WhereBuilder<T extends Serializable, K extends Serializable> extends SelectBuilder<T, K> {

    protected WhereBuilder(GaarasonDataSource gaarasonDataSource, Model<T, K> model, Grammar grammar) {
        super(gaarasonDataSource, model, grammar);
    }

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
        final Map<String, Object> columnValueMap = modelShadowProvider.columnValueMap(entity, EntityUseType.CONDITION);
        return where(columnValueMap);
    }

    @Override
    public Builder<T, K> where(Object entity) {
        // todo
//        final Map<String, Object> columnValueMap = modelShadowProvider.columnValueMap(entity);
//        return where(columnValueMap);
        return null;
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
    public Builder<T, K> whereKeywords(@Nullable Object value, Collection<String> columns) {
        andWhere(builder -> {
            for (String column : columns) {
                builder.orWhere(builderInner -> builderInner.whereMayLike(column, value));
            }
            return builder;
        });
        return this;
    }

    @Override
    public Builder<T, K> whereKeywords(@Nullable Object value, String... columns) {
        return whereKeywords(value, Arrays.asList(columns));
    }

    @Override
    public Builder<T, K> whereKeywordsIgnoreNull(@Nullable Object value, Collection<String> columns) {
        andWhereIgnoreEmpty(builder -> {
            for (String column : columns) {
                builder.orWhereIgnoreEmpty(builderInner -> builderInner.whereMayLikeIgnoreNull(column, value));
            }
            return builder;
        });
        return this;
    }

    @Override
    public Builder<T, K> whereKeywordsIgnoreNull(@Nullable Object value, String... columns) {
        return whereKeywordsIgnoreNull(value, Arrays.asList(columns));
    }

    @Override
    public Builder<T, K> whereLike(String column, @Nullable Object value) {
        return whereIgnoreNull(column, "like", value);
    }

    @Override
    public Builder<T, K> whereLike(@Nullable T entity) {
        final Map<String, Object> columnValueMap = modelShadowProvider.columnValueMap(entity, EntityUseType.CONDITION);
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
        String s = conversion.castNullable(value, String.class);
        if (!ObjectUtils.isNull(s) && (s.endsWith("%") || s.startsWith("%"))) {
            return whereLike(column, value);
        } else {
            return where(column, value);
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
    public Builder<T, K> whereMayLike(@Nullable T entity) {
        final Map<String, Object> columnValueMap = modelShadowProvider.columnValueMap(entity, EntityUseType.CONDITION);
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
    public Builder<T, K> whereSubQuery(String column, String symbol, String completeSql) {
        String sqlPart = backQuote(column) + symbol + FormatUtils.bracket(completeSql);
        return whereRaw(sqlPart);
    }


    @Override
    public Builder<T, K> whereSubQuery(String column, String symbol, GenerateSqlPartFunctionalInterface<T, K> closure) {
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
    public Builder<T, K> whereIn(String column, GenerateSqlPartFunctionalInterface<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sqlPart = backQuote(column) + "in" + FormatUtils.bracket(sqlPartInfo.getSqlString());
        return whereGrammar(sqlPart, sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public Builder<T, K> whereNotIn(String column, GenerateSqlPartFunctionalInterface<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sqlPart = backQuote(column) + "not in" + FormatUtils.bracket(sqlPartInfo.getSqlString());
        return whereGrammar(sqlPart, sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public Builder<T, K> whereBetween(String column, Object min, Object max) {
        Collection<Object> parameters = new ArrayList<>();
        String sqlPart = backQuote(column) + "between" +
            grammar.replaceValueAndFillParameters(min, parameters) + "and" +
            grammar.replaceValueAndFillParameters(max, parameters);
        return whereGrammar(sqlPart, parameters, " and ");
    }

    @Override
    public Builder<T, K> whereNotBetween(String column, Object min, Object max) {
        Collection<Object> parameters = new ArrayList<>();
        String sqlPart = backQuote(column) + "not between" +
            grammar.replaceValueAndFillParameters(min, parameters) + "and" +
            grammar.replaceValueAndFillParameters(max, parameters);
        return whereGrammar(sqlPart, parameters, " and ");
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
    public Builder<T, K> whereExists(GenerateSqlPartFunctionalInterface<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String sql = "exists " + FormatUtils.bracket(sqlPartInfo.getSqlString());
        return whereGrammar(sql, sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public Builder<T, K> whereNotExists(GenerateSqlPartFunctionalInterface<T, K> closure) {
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
    public Builder<T, K> andWhere(GenerateSqlPartFunctionalInterface<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure, Grammar.SQLPartType.WHERE);
        return whereGrammar(FormatUtils.bracket(sqlPartInfo.getSqlString()), sqlPartInfo.getParameters(), " and ");
    }

    @Override
    public Builder<T, K> orWhere(GenerateSqlPartFunctionalInterface<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure, Grammar.SQLPartType.WHERE);
        return whereGrammar(FormatUtils.bracket(sqlPartInfo.getSqlString()), sqlPartInfo.getParameters(), " or ");
    }

    @Override
    public Builder<T, K> andWhereIgnoreEmpty(GenerateSqlPartFunctionalInterface<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure, Grammar.SQLPartType.WHERE);
        if (!ObjectUtils.isEmpty(sqlPartInfo.getSqlString())) {
            whereGrammar(FormatUtils.bracket(sqlPartInfo.getSqlString()), sqlPartInfo.getParameters(), " and ");
        }
        return this;
    }

    @Override
    public Builder<T, K> orWhereIgnoreEmpty(GenerateSqlPartFunctionalInterface<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure, Grammar.SQLPartType.WHERE);
        if (!ObjectUtils.isEmpty(sqlPartInfo.getSqlString())) {
            whereGrammar(FormatUtils.bracket(sqlPartInfo.getSqlString()), sqlPartInfo.getParameters(), " or ");
        }
        return this;
    }
}
