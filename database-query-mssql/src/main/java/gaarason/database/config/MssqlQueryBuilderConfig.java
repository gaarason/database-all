package gaarason.database.config;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.query.Grammar;
import gaarason.database.provider.ContainerProvider;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.query.MsSqlBuilder;
import gaarason.database.query.grammars.MsSqlGrammar;

import java.io.Serializable;

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
    public <T extends Serializable, K extends Serializable> Builder<T, K> newBuilder(GaarasonDataSource gaarasonDataSource, Model<T, K> model) {
        return new MsSqlBuilder<>(gaarasonDataSource, model, newGrammar(model.getEntityClass()));
    }

    @Override
    public <T extends Serializable> Grammar newGrammar(Class<T> entityClass) {
        return new MsSqlGrammar(ModelShadowProvider.getByEntityClass(entityClass).getTableName());
    }
}
