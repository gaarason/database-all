package gaarason.database.autoconfiguration;

import gaarason.database.config.GaarasonAutoconfiguration;
import gaarason.database.config.MysqlQueryBuilderConfig;
import gaarason.database.config.QueryBuilderConfig;
import gaarason.database.contract.function.InstanceCreatorFunctionalInterface;
import gaarason.database.provider.ContainerProvider;

public class MysqlAutoconfiguration implements GaarasonAutoconfiguration {

    @Override
    public void init() {
        ContainerProvider.register(QueryBuilderConfig.class,
            new InstanceCreatorFunctionalInterface<QueryBuilderConfig>() {
                @Override
                public QueryBuilderConfig execute(Class<QueryBuilderConfig> clazz) throws Throwable {
                    return new MysqlQueryBuilderConfig();
                }

                @Override
                public Integer getOrder() {
                    return InstanceCreatorFunctionalInterface.super.getOrder();
                }
            });
    }
}
