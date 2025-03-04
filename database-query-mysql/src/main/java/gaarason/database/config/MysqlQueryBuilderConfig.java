package gaarason.database.config;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.query.MySqlBuilder;
import gaarason.database.query.grammars.MySqlGrammar;

public class MysqlQueryBuilderConfig implements QueryBuilderConfig {

    @Override
    public String getValueSymbol() {
        return "'";
    }

    @Override
    public boolean support(String databaseProductName) {
        return "mysql".equals(databaseProductName);
    }

//    @Override
    public Builder<?, ?, ?> newBuilder11(GaarasonDataSource gaarasonDataSource, Model<?, ?, ?> model) {
        return new MySqlBuilder<>().initBuilder(gaarasonDataSource, model, new MySqlGrammar(model.getTableName()));
    }

//    @Override
    public <B extends Builder<B, T, K>, T, K> B newBuilder(GaarasonDataSource gaarasonDataSource, Model<B, T, K> model) {
        return new MySqlBuilder<T, K>().initBuilder(gaarasonDataSource, model, new MySqlGrammar(model.getTableName()));
    }

}
