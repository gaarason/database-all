package gaarason.database.autoconfiguration;

import gaarason.database.config.GaarasonAutoconfiguration;
import gaarason.database.config.MssqlQueryBuilderConfig;
import gaarason.database.config.QueryBuilderConfig;
import gaarason.database.contract.function.InstanceCreatorFunctionalInterface;
import gaarason.database.provider.ContainerProvider;

public class MssqlAutoconfiguration implements GaarasonAutoconfiguration {

    @Override
    public void init() {
        ContainerProvider.register(QueryBuilderConfig.class,
            new InstanceCreatorFunctionalInterface<QueryBuilderConfig>() {
                @Override
                public QueryBuilderConfig execute(Class<QueryBuilderConfig> clazz) throws Throwable {
                    return new MssqlQueryBuilderConfig();
                }

                @Override
                public Integer getOrder() {
                    return InstanceCreatorFunctionalInterface.super.getOrder();
                }
            });
    }
}
