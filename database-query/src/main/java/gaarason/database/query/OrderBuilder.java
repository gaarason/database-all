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
public abstract class OrderBuilder<T, K> extends HavingBuilder<T, K> {

    protected Builder<T, K> orderGrammar(String sqlPart, @Nullable Collection<Object> parameters) {
        grammar.addSmartSeparator(Grammar.SQLPartType.ORDER, sqlPart, parameters, ",");
        return this;
    }

    protected Builder<T, K> orderFirstGrammar(String sqlPart, @Nullable Collection<Object> parameters) {
        grammar.addFirstSmartSeparator(Grammar.SQLPartType.ORDER, sqlPart, parameters, ",");
        return this;
    }

    @Override
    public Builder<T, K> orderBy(@Nullable String column, OrderBy type) {
        if (null != column) {
            String sqlPart = backQuote(column) + " " + type.getOperation();
            return orderByRaw(sqlPart);
        }
        return this;
    }

    @Override
    public Builder<T, K> orderBy(@Nullable String column) {
        return orderBy(column, OrderBy.ASC);
    }

    @Override
    public Builder<T, K> orderByRaw(@Nullable String sqlPart) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            orderGrammar(sqlPart, null);
        }
        return this;
    }

    @Override
    public Builder<T, K> firstOrderBy(BuilderWrapper<T, K> closure) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure, Grammar.SQLPartType.ORDER);
        return orderFirstGrammar(sqlPartInfo.getSqlString(), sqlPartInfo.getParameters());
    }
}
