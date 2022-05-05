package gaarason.database.query;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.query.Grammar;
import gaarason.database.lang.Nullable;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;
import java.util.*;

/**
 * 公用查询构造器
 * @param <T>
 * @param <K>
 * @author xt
 */
public abstract class GrammarBuilder<T extends Serializable, K extends Serializable> extends MiddleBuilder<T, K> {

    protected GrammarBuilder(GaarasonDataSource gaarasonDataSource, Model<T, K> model, Grammar grammar) {
        super(gaarasonDataSource, model, grammar);
    }

    protected Builder<T, K> whereGrammar(String sqlPart, @Nullable Collection<String> parameters, String separator) {
        grammar.addSmartSeparator(Grammar.SQLPartType.WHERE, sqlPart, parameters, separator);
        return this;
    }



    @Override
    public Builder<T, K> havingRaw(@Nullable String sqlPart, @Nullable Collection<?> parameters) {
        if (!ObjectUtils.isEmpty(sqlPart)) {
            grammar.addSmartSeparator(Grammar.SQLPartType.HAVING, sqlPart, conversionToStrings(parameters), " and ");
        }
        return this;
    }

}
