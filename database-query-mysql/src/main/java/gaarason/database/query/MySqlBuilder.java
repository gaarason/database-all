package gaarason.database.query;

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
public class MySqlBuilder<T, K> extends OtherBuilder<MySqlBuilder<T, K>, T, K> {

    private static final long serialVersionUID = 1L;

    @Override
    public MySqlBuilder<T, K> getSelf() {
        return this;
    }

    @Override
    public MySqlBuilder<T, K> limit(Object offset, Object take) {
        Collection<Object> parameters = new ArrayList<>(2);
        String sqlPart = grammar.replaceValueAndFillParameters(offset, parameters) + "," +
            grammar.replaceValueAndFillParameters(take, parameters);
        grammar.set(Grammar.SQLPartType.LIMIT, sqlPart, parameters);
        return getSelf();
    }

    @Override
    public MySqlBuilder<T, K> limit(Object take) {
        Collection<Object> parameters = new ArrayList<>(1);
        String sqlPart = grammar.replaceValueAndFillParameters(take, parameters);
        grammar.set(Grammar.SQLPartType.LIMIT, sqlPart, parameters);
        return getSelf();
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

    public MySqlBuilder<T, K> adddddd() {
        return getSelf();
    }
}
