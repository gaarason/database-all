package gaarason.database.query;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.query.Grammar;
import gaarason.database.lang.Nullable;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Data查询构造器
 * @param <T>
 * @param <K>
 * @author xt
 */
public abstract class DataBuilder<T extends Serializable, K extends Serializable> extends MiddleBuilder<T, K> {

    protected DataBuilder(GaarasonDataSource gaarasonDataSource, Model<T, K> model, Grammar grammar) {
        super(gaarasonDataSource, model, grammar);
    }

    protected Builder<T, K> dataGrammar(String sqlPart, @Nullable Collection<Object> parameters) {
        grammar.addSmartSeparator(Grammar.SQLPartType.DATA, sqlPart, parameters, ",");
        return this;
    }

    @Override
    public Builder<T, K> dataRaw(@Nullable String sqlPart) {
        if(!ObjectUtils.isEmpty(sqlPart)){
            dataGrammar(sqlPart, null);
        }
        return this;
    }

    @Override
    public Builder<T, K> data(String column, @Nullable Object value) {
        ArrayList<Object> parameters = new ArrayList<>();
        String sqlPart = backQuote(column) + '=' + grammar.replaceValueAndFillParameters(value, parameters);
        return dataGrammar(sqlPart, parameters);
    }

    @Override
    public Builder<T, K> dataIgnoreNull(String column, @Nullable Object value) {
        return ObjectUtils.isNull(value) ? this : data(column, value);
    }

    @Override
    public Builder<T, K> data(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            data(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Builder<T, K> dataIgnoreNull(@Nullable Map<String, Object> map) {
        if (!ObjectUtils.isEmpty(map)) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                dataIgnoreNull(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Override
    public Builder<T, K> dataIncrement(String column, Object steps) {
        ArrayList<Object> parameters = new ArrayList<>();
        String sqlPart = backQuote(column) + '=' + backQuote(column) + '+' + grammar.replaceValueAndFillParameters(steps, parameters);
        return dataGrammar(sqlPart, parameters);
    }

    @Override
    public Builder<T, K> dataDecrement(String column, Object steps) {
        ArrayList<Object> parameters = new ArrayList<>();
        String sqlPart = backQuote(column) + '=' + backQuote(column) + '-' + grammar.replaceValueAndFillParameters(steps, parameters);
        return dataGrammar(sqlPart, parameters);
    }
}
