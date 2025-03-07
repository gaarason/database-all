package gaarason.database.test.config;

import gaarason.database.config.MysqlQueryBuilderConfig;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.query.grammars.MySqlGrammar;
import gaarason.database.util.ObjectUtils;

public class MysqlQueryBuilderConfigV2 extends MysqlQueryBuilderConfig {

    @Override
    public <T, K> Builder<?, T, K> newBuilder(GaarasonDataSource gaarasonDataSource, Model<?, T, K> model) {
        return new MySqlBuilderV2<T, K>().initBuilder(gaarasonDataSource, ObjectUtils.typeCast(model), new MySqlGrammar(model.getTableName()));
    }
}