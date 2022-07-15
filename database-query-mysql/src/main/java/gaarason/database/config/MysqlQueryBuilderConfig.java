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

    @Override
    public <T, K> Builder<T, K> newBuilder(GaarasonDataSource gaarasonDataSource, Model<T, K> model) {
        return new MySqlBuilder<>(gaarasonDataSource, model, new MySqlGrammar(model.getTableName()));
    }

}
