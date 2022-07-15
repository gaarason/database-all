package gaarason.database.config;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.query.MsSqlBuilder;
import gaarason.database.query.grammars.MsSqlGrammar;

public class MssqlQueryBuilderConfig implements QueryBuilderConfig {

    @Override
    public String getValueSymbol() {
        return "'";
    }

    @Override
    public boolean support(String databaseProductName) {
        return "microsoft sql server".equals(databaseProductName) || "mssql".equals(databaseProductName);
    }

    @Override
    public <T, K> Builder<T, K> newBuilder(
        GaarasonDataSource gaarasonDataSource, Model<T, K> model) {
        return new MsSqlBuilder<>(gaarasonDataSource, model, new MsSqlGrammar(model.getTableName()));
    }

}
