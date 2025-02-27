package gaarason.database.query;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.BuilderWrapper;
import gaarason.database.contract.query.Grammar;
import gaarason.database.lang.Nullable;
import gaarason.database.support.EntityMember;
import gaarason.database.util.FormatUtils;
import gaarason.database.util.ObjectUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Select查询构造器
 * @param <T>
 * @param <K>
 * @author xt
 */
public abstract class SelectBuilder<T, K> extends OrderBuilder<T, K> {

    protected Builder<T, K> selectGrammar(String sqlPart, @Nullable Collection<Object> parameters) {
        grammar.addSmartSeparator(Grammar.SQLPartType.SELECT, sqlPart, parameters, ",");
        return this;
    }

    @Override
    public Builder<T, K> select(String column) {
        String sqlPart = backQuote(column);
        return selectRaw(sqlPart);
    }

    @Override
    public Builder<T, K> selectRaw(@Nullable String sqlPart) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            selectGrammar(sqlPart, null);
        }
        return this;
    }

    @Override
    public Builder<T, K> selectRaw(@Nullable String sqlPart, @Nullable Collection<?> parameters) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            selectGrammar(sqlPart, ObjectUtils.isEmpty(parameters) ? null : ObjectUtils.typeCast(parameters));
        }
        return this;
    }

    @Override
    public Builder<T, K> select(String... columnArray) {
        for (String column : columnArray) {
            select(column);
        }
        return this;
    }

    @Override
    public Builder<T, K> select(Object anyEntity) {
        return select(anyEntity.getClass());
    }

    @Override
    public Builder<T, K> select(Class<?> anyEntityClass) {
        EntityMember<?, Object> entityMember = modelShadowProvider.parseAnyEntityWithCache(anyEntityClass);
        List<String> columnList = entityMember.getSelectColumnList();
        // 缓存存取
        String columnString = entityMember.getSelectColumnString(
                gaarasonDataSource.getQueryBuilder().getValueSymbol(),
                () -> columnList.stream().map(this::backQuote).collect(
                        Collectors.joining(", ")));

        return selectRaw(columnString);
    }

    @Override
    public Builder<T, K> select(Collection<String> columnList) {
        for (String column : columnList) {
            select(column);
        }
        return this;
    }

    @Override
    public Builder<T, K> selectFunction(String function, String parameter, @Nullable String alias) {
        String sqlPart =
            function + FormatUtils.bracket(parameter) + (alias == null ? "" : " as " + FormatUtils.quotes(alias));
        return selectRaw(sqlPart);
    }

    @Override
    public Builder<T, K> selectFunction(String function, String parameter) {
        return selectFunction(function, parameter, null);
    }

    // todo test
    @Override
    public Builder<T, K> selectFunction(String function, BuilderWrapper<T, K> closure,
        @Nullable String alias) {
        Grammar.SQLPartInfo sqlPartInfo = generateSql(closure);
        String completeSql = sqlPartInfo.getSqlString();
        String sqlPart =
            function + FormatUtils.bracket(completeSql) + (alias == null ? "" : " as " + FormatUtils.quotes(alias));
        return selectGrammar(sqlPart, sqlPartInfo.getParameters());
    }

    // todo test
    @Override
    public Builder<T, K> selectFunction(String function, BuilderWrapper<T, K> closure) {
        return selectFunction(function, closure, null);

    }

    @Override
    public Builder<T, K> selectCustom(String columnName, String value) {
        return selectRaw(value + " as " + backQuote(columnName));
    }
}
