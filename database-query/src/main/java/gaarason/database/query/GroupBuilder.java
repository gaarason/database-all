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
abstract class GroupBuilder<B extends Builder<B, T, K>, T, K>  extends DataBuilder<B, T, K> {

    protected B groupGrammar(String sqlPart, @Nullable Collection<Object> parameters) {
        grammar.addSmartSeparator(Grammar.SQLPartType.GROUP, sqlPart, parameters, ",");
        return getSelf();
    }

    @Override
    public B groupRaw(@Nullable String sqlPart) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            groupGrammar(sqlPart, null);
        }
        return getSelf();
    }

    @Override
    public B group(String column) {
        String sqlPart = columnAlias(column);
        return groupRaw(sqlPart);
    }

    @Override
    public B group(String... columnArray) {
        for (String column : columnArray) {
            group(column);
        }
        return getSelf();
    }

    @Override
    public B group(Collection<String> columnList) {
        for (String column : columnList) {
            group(column);
        }
        return getSelf();
    }
}
