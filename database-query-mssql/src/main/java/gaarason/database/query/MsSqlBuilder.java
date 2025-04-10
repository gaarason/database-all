package gaarason.database.query;

import gaarason.database.contract.query.Grammar;
import gaarason.database.util.FormatUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * mssql sql生成器
 * @param <T>
 * @param <K>
 * @author xt
 */
public final class MsSqlBuilder<T, K> extends AbstractBuilder<MsSqlBuilder<T, K>, T, K> {

    private static final long serialVersionUID = 1L;

    @Override
    public MsSqlBuilder<T, K> getSelf() {
        return this;
    }

    @Override
    public MsSqlBuilder<T, K> limit(Object offset, Object take) {
        Collection<Object> parameters = new ArrayList<>(2);
        String sqlPart = "offset " + grammar.replaceValueAndFillParameters(offset, parameters) + " rows fetch next " +
            grammar.replaceValueAndFillParameters(take, parameters) + " rows only";
        grammar.set(Grammar.SQLPartType.LIMIT, sqlPart, parameters);
        return this;
    }

    @Override
    public MsSqlBuilder<T, K> limit(Object take) {
        return limit(0, take);
    }

    /**
     * 给字段加上引号
     * @param something 字段 eg: sum(order.amount) AS sum_price
     * @return eg: sum(`order`.`amount`) AS `sum_price`
     */
    @Override
    public String supportBackQuote(String something) {
        return FormatUtils.backQuote(something, "\"");
    }
}
