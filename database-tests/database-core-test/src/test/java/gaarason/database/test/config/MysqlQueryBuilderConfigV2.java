package gaarason.database.test.config;

import gaarason.database.config.QueryBuilderConfig;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.query.Grammar;
import gaarason.database.query.grammars.BaseGrammar;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;

public class MysqlQueryBuilderConfigV2 implements QueryBuilderConfig, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public String getValueSymbol() {
        return "'";
    }

    @Override
    public boolean support(String databaseProductName) {
        return "mysql".equals(databaseProductName);
    }

    @Override
    public Grammar newGrammar(String tableName) {
        return new BaseGrammar(tableName, "`") {
            private static final long serialVersionUID = 1L;
        };
    }

    @Override
    public <T, K> Builder<?, T, K> newBuilder(GaarasonDataSource gaarasonDataSource, Model<?, T, K> model) {
        return new MySqlBuilderV2<T, K>().initBuilder(gaarasonDataSource, ObjectUtils.typeCast(model), newGrammar(model.getTableName()));
    }
}
