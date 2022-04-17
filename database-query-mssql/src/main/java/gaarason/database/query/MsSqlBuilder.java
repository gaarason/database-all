package gaarason.database.query;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.query.Grammar;
import gaarason.database.util.FormatUtils;

import java.io.Serializable;

/**
 * mssql sql生成器
 * @param <T>
 * @param <K>
 * @author xt
 */
public class MsSqlBuilder<T extends Serializable, K extends Serializable> extends CommonBuilder<T, K> {

    public MsSqlBuilder(GaarasonDataSource gaarasonDataSource, Model<T, K> model,  Grammar grammar) {
        super(gaarasonDataSource, model, grammar);
    }

    @Override
    public Builder<T, K> limit(int offset, int take) {
        String sqlPart = "offset " + offset + " rows fetch next " + take + " rows only";
        grammar.pushLimit(sqlPart);
        return this;
    }

    @Override
    public Builder<T, K> limit(int take) {
        return limit(0, take);
    }

    /**
     * 给字段加上引号
     * @param something 字段 eg: sum(order.amount) AS sum_price
     * @return eg: sum(`order`.`amount`) AS `sum_price`
     */
    @Override
    protected String column(String something) {
        return FormatUtils.backQuote(something, "\"");
    }
}
