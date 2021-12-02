package gaarason.database.query;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.query.Grammar;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.query.grammars.MySqlGrammar;
import gaarason.database.util.FormatUtils;

import java.io.Serializable;

/**
 * Mysql sql生成器
 * @param <T>
 * @param <K>
 * @author xt
 */
public class MySqlBuilder<T extends Serializable, K extends Serializable> extends CommonBuilder<T, K> {

    public MySqlBuilder(GaarasonDataSource gaarasonDataSource, Model<T, K> model, Grammar grammar) {
        super(gaarasonDataSource, model, grammar);
    }

    @Override
    public Builder<T, K> limit(int offset, int take) {
        String sqlPart = String.valueOf(offset) + ',' + take;
        grammar.pushLimit(sqlPart);
        return this;
    }

    @Override
    public Builder<T, K> limit(int take) {
        String sqlPart = String.valueOf(take);
        grammar.pushLimit(sqlPart);
        return this;
    }

    /**
     * 给字段加上引号
     * @param something 字段 eg: sum(order.amount) AS sum_price
     * @return eg: sum(`order`.`amount`) AS `sum_price`
     */
    @Override
    protected String column(String something) {
        return FormatUtils.backQuote(something, "`");
    }
}
