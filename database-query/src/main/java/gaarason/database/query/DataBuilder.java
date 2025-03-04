package gaarason.database.query;

import gaarason.database.appointment.EntityUseType;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.query.Grammar;
import gaarason.database.lang.Nullable;
import gaarason.database.util.BitUtils;
import gaarason.database.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Data查询构造器
 * @param <T>
 * @param <K>
 * @author xt
 */
public abstract class DataBuilder<B extends Builder<B, T, K>, T, K>  extends ExecuteLevel3Builder<B, T, K> {

    protected B dataGrammar(String sqlPart, @Nullable Collection<Object> parameters) {
        grammar.addSmartSeparator(Grammar.SQLPartType.DATA, sqlPart, parameters, ",");
        return getSelf();
    }

    @Override
    public B dataRaw(@Nullable String sqlPart) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            dataGrammar(sqlPart, null);
        }
        return getSelf();
    }

    @Override
    public B data(String column, @Nullable Object value) {
        ArrayList<Object> parameters = new ArrayList<>();
        String sqlPart = backQuote(column) + '=' + grammar.replaceValueAndFillParameters(value, parameters);
        return dataGrammar(sqlPart, parameters);
    }

    @Override
    public B dataIgnoreNull(String column, @Nullable Object value) {
        return ObjectUtils.isNull(value) ? getSelf() : data(column, value);
    }

    @Override
    public B data(Object anyEntity) {
        final Map<String, Object> columnValueMap = modelShadowProvider.entityToMap(anyEntity, EntityUseType.UPDATE);
        return data(columnValueMap);
    }

    @Override
    public B data(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            data(entry.getKey(), entry.getValue());
        }
        return getSelf();
    }

    @Override
    public B dataIgnoreNull(@Nullable Map<String, Object> map) {
        if (!ObjectUtils.isEmpty(map)) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                dataIgnoreNull(entry.getKey(), entry.getValue());
            }
        }
        return getSelf();
    }

    @Override
    public B dataIncrement(String column, Object steps) {
        ArrayList<Object> parameters = new ArrayList<>();
        String sqlPart = backQuote(column) + '=' + backQuote(column) + '+' +
            grammar.replaceValueAndFillParameters(steps, parameters);
        return dataGrammar(sqlPart, parameters);
    }

    @Override
    public B dataDecrement(String column, Object steps) {
        ArrayList<Object> parameters = new ArrayList<>();
        String sqlPart = backQuote(column) + '=' + backQuote(column) + '-' +
            grammar.replaceValueAndFillParameters(steps, parameters);
        return dataGrammar(sqlPart, parameters);
    }

    @Override
    public B dataBit(String column, Collection<Object> values) {
        long packed = BitUtils.packs(values);
        return data(column, packed);
    }

    @Override
    public B dataBitIncrement(String column, Collection<Object> values) {
        long packed = BitUtils.packs(values);
        ArrayList<Object> parameters = new ArrayList<>();
        String sqlPart = backQuote(column) + '=' + backQuote(column) + '|' +
                grammar.replaceValueAndFillParameters(packed, parameters);
        return dataGrammar(sqlPart, parameters);
    }

    @Override
    public  B dataBitDecrement(String column, Collection<Object> values) {
        long packed = BitUtils.packs(values);
        ArrayList<Object> parameters = new ArrayList<>();
        String sqlPart = backQuote(column) + '=' + backQuote(column) + "& ~" +
                grammar.replaceValueAndFillParameters(packed, parameters);
        return dataGrammar(sqlPart, parameters);
    }
}
