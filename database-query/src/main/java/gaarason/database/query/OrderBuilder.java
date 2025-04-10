package gaarason.database.query;

import gaarason.database.appointment.OrderBy;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.BuilderWrapper;
import gaarason.database.contract.query.Grammar;
import gaarason.database.lang.Nullable;
import gaarason.database.util.ObjectUtils;

import java.util.Collection;

/**
 * Order查询构造器
 * @param <T>
 * @param <K>
 * @author xt
 */
abstract class OrderBuilder<B extends Builder<B, T, K>, T, K> extends HavingBuilder<B, T, K> {

    protected B orderGrammar(String sqlPart, @Nullable Collection<Object> parameters) {
        grammar.addSmartSeparator(Grammar.SQLPartType.ORDER, sqlPart, parameters, ",");
        return getSelf();
    }

    protected B orderFirstGrammar(String sqlPart, @Nullable Collection<Object> parameters) {
        grammar.addFirstSmartSeparator(Grammar.SQLPartType.ORDER, sqlPart, parameters, ",");
        return getSelf();
    }

    @Override
    public B orderBy(@Nullable String column, OrderBy type) {
        if (null != column) {
            String sqlPart = columnAlias(column) + " " + type.getOperation();
            return orderByRaw(sqlPart);
        }
        return getSelf();
    }

    @Override
    public B orderBy(@Nullable String column) {
        return orderBy(column, OrderBy.ASC);
    }

    @Override
    public B orderByRaw(@Nullable String sqlPart) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            orderGrammar(sqlPart, null);
        }
        return getSelf();
    }

    @Override
    public B firstOrderBy(BuilderWrapper<B, T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSqlPart(closure, Grammar.SQLPartType.ORDER);
        return orderFirstGrammar(sqlPartInfo.getSqlString(), sqlPartInfo.getParameters());
    }
}
