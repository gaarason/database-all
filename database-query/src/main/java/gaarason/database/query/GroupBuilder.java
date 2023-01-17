package gaarason.database.query;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.query.Grammar;
import gaarason.database.lang.Nullable;
import gaarason.database.util.ObjectUtils;

import java.util.Collection;

/**
 * Group查询构造器
 * @param <T>
 * @param <K>
 * @author xt
 */
public abstract class GroupBuilder<T, K> extends DataBuilder<T, K> {

    protected Builder<T, K> groupGrammar(String sqlPart, @Nullable Collection<Object> parameters) {
        grammar.addSmartSeparator(Grammar.SQLPartType.GROUP, sqlPart, parameters, ",");
        return this;
    }

    @Override
    public Builder<T, K> groupRaw(@Nullable String sqlPart) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            groupGrammar(sqlPart, null);
        }
        return this;
    }

    @Override
    public Builder<T, K> group(String column) {
        String sqlPart = backQuote(column);
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
}
