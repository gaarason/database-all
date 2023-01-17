package gaarason.database.query;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.query.Grammar;
import gaarason.database.util.FormatUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Mysql sql生成器
 * @param <T>
 * @param <K>
 * @author xt
 */
public class MySqlBuilder<T, K> extends OtherBuilder<T, K> {

    private static final long serialVersionUID = 1L;

    @Override
    public Builder<T, K> limit(Object offset, Object take) {
        Collection<Object> parameters = new ArrayList<>(2);
        String sqlPart = grammar.replaceValueAndFillParameters(offset, parameters) + "," +
            grammar.replaceValueAndFillParameters(take, parameters);
        grammar.set(Grammar.SQLPartType.LIMIT, sqlPart, parameters);
        return this;
    }

    @Override
    public Builder<T, K> limit(Object take) {
        Collection<Object> parameters = new ArrayList<>(1);
        String sqlPart = grammar.replaceValueAndFillParameters(take, parameters);
        grammar.set(Grammar.SQLPartType.LIMIT, sqlPart, parameters);
        return this;
    }

    /**
     * 给字段加上引号
     * @param something 字段 eg: sum(order.amount) AS sum_price
     * @return eg: sum(`order`.`amount`) AS `sum_price`
     */
    @Override
    protected String backQuote(String something) {
        return FormatUtils.backQuote(something, "`");
    }
}
