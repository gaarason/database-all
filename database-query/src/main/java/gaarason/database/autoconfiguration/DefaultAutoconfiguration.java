package gaarason.database.autoconfiguration;

import gaarason.database.config.DefaultQueryBuilderConfig;
import gaarason.database.config.GaarasonAutoconfiguration;
import gaarason.database.config.QueryBuilderConfig;
import gaarason.database.contract.function.InstanceCreatorFunctionalInterface;
import gaarason.database.core.Container;

/**
 * 通用数据库方言自动配置
 * <p>
 * 以最低优先级注册, 仅在没有更具体的方言配置匹配时生效
 * @author xt
 */
public class DefaultAutoconfiguration implements GaarasonAutoconfiguration {

    @Override
    public void init(Container container) {
        container.register(QueryBuilderConfig.class,
            new InstanceCreatorFunctionalInterface<QueryBuilderConfig>() {
                @Override
                public QueryBuilderConfig execute(Class<QueryBuilderConfig> clazz) {
                    return new DefaultQueryBuilderConfig();
                }

                @Override
                public Integer getOrder() {
                    return Integer.MAX_VALUE;
                }
            });
    }
}
