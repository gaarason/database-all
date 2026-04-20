package gaarason.database.spring.boot.starter.configurations;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import gaarason.database.autoconfiguration.DefaultAutoconfiguration;
import gaarason.database.bootstrap.ContainerBootstrap;
import gaarason.database.config.GaarasonDatabaseProperties;
import gaarason.database.connection.DataSourceGroup;
import gaarason.database.connection.GaarasonDataSourceContext;
import gaarason.database.connection.GaarasonRoutingDataSourceWrapper;
import gaarason.database.contract.routing.DynamicDatabaseRouting;
import gaarason.database.contract.routing.DynamicDataSourceGroupRouting;
import gaarason.database.contract.routing.DynamicExplicitTableRouting;
import gaarason.database.contract.routing.DynamicJdbcCatalogRouting;
import gaarason.database.contract.routing.DynamicTableRouting;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.core.Container;
import gaarason.database.lang.Nullable;
import gaarason.database.eloquent.GeneralModel;
import gaarason.database.generator.GeneralGenerator;
import gaarason.database.logging.Log;
import gaarason.database.logging.LogFactory;
import gaarason.database.provider.GodProvider;
import gaarason.database.provider.ModelInstanceProvider;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.spring.boot.starter.annotation.GaarasonDatabaseScanRegistrar;
import gaarason.database.spring.boot.starter.aop.GaarasonDataSourceAspect;
import gaarason.database.spring.boot.starter.properties.GaarasonDataSourceProperties;
import gaarason.database.spring.boot.starter.properties.GaarasonDataSourceProperties.DataSourceGroupProperties;
import gaarason.database.spring.boot.starter.properties.GaarasonDataSourceProperties.DataSourceNodeProperties;
import gaarason.database.spring.boot.starter.provider.GaarasonTransactionManager;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 自动配置
 * @author xt
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter({DruidDataSourceAutoConfigure.class, DataSourceAutoConfiguration.class})
@Import({GeneralModel.class, GeneralGenerator.class})
public class GaarasonDatabaseAutoConfiguration {

    private static final Log LOGGER = LogFactory.getLog(GaarasonDatabaseAutoConfiguration.class);

    /**
     * Spring配置GaarasonDatabaseProperties
     */
    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = GaarasonDatabaseProperties.PREFIX)
    public GaarasonDatabaseProperties gaarasonDatabaseProperties() {
        return new GaarasonDatabaseProperties();
    }

    /**
     * 多数据源分组配置属性
     */
    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = GaarasonDataSourceProperties.PREFIX)
    public GaarasonDataSourceProperties gaarasonDataSourceProperties() {
        return new GaarasonDataSourceProperties();
    }

    /**
     * 容器初始化
     * @param applicationContext 应用上下文
     * @param gaarasonDatabaseProperties 配置
     * @see gaarason.database.spring.boot.starter.annotation.GaarasonDatabaseScan
     * @return 容器
     */
    @Bean
    @ConditionalOnMissingBean
    public Container container(ApplicationContext applicationContext,
        GaarasonDatabaseProperties gaarasonDatabaseProperties) {

        // 获取 spring boot 的包扫描路径
        List<String> packageOnSpringBoot = AutoConfigurationPackages.get(applicationContext);

        /*
         * 将配置合并
         * 认定 @GaarasonDatabaseScan 的解析一定在此之前完成了.
         * 缺省时, 使用 spring boot 的包扫描路径
         */
        gaarasonDatabaseProperties.mergeScan(GaarasonDatabaseScanRegistrar.getScan())
            .fillPackageWhenIsEmpty(packageOnSpringBoot)
            .fillAndVerify();

        // 从配置创建全新容器
        ContainerBootstrap container = ContainerBootstrap.build(gaarasonDatabaseProperties);

        // 简单兼容下`spring-boot-devtools`等等会重新加载Bean的技术方案
        GodProvider.remove("primary-container");

        // 查询构造器序列化的必要步骤
        container.signUpIdentification("primary-container");

        container.defaultRegister();

        // 注册 model实例获取方式
        container.getBean(ModelInstanceProvider.class).register(modelClass -> {
            try {
                LOGGER.debug("尝试通过 Spring 实例化 model 类 ["+ modelClass +"]");
                return ObjectUtils.typeCast(applicationContext.getBean(modelClass));
            } catch (BeansException e) {
                LOGGER.debug("尝试通过 Spring 实例化 model 名 ["+ StringUtils.lowerFirstChar(modelClass.getSimpleName()) +"]");
                return ObjectUtils.typeCast(
                    applicationContext.getBean(StringUtils.lowerFirstChar(modelClass.getSimpleName())));
            }
        });
        LOGGER.info("Model instance provider has been registered success.");

        bootstrapGaarasonAutoconfiguration(container);

        bootstrapInitialization(container);

        LOGGER.info("Container has completed initialization.");
        return container;
    }

    /**
     * 自动配置, 在native时, 手动进行
     * 在native时, 更多的用户自定的配置, 也需要手动执行, 需要且在任意sql执行前完成
     * @param container 容器
     */
    protected void bootstrapGaarasonAutoconfiguration(ContainerBootstrap container) {
        if (isNative()) {
            new DefaultAutoconfiguration().init(container);
        } else {
            container.bootstrapGaarasonAutoconfiguration();
        }
    }

    /**
     * 自动配置, 在native时, 手动进行
     * 在native时, 更多的用户自定的model, 也需要手动执行, 需要且在对应的model使用前完成
     * 同时也增加了一定的动态的能力 详见 ModelShadowProvider
     * @param container 容器
     */
    protected void bootstrapInitialization(ContainerBootstrap container) {
        if (isNative()) {
            container.getBean(ModelShadowProvider.class).loadModels((Collections.singleton(GeneralModel.class)));
            // 其他均需要手动执行
        } else {
            container.initialization();
        }
    }

    /**
     * 是否是 Spring native 环境
     * @return 是否是 Spring native 环境
     */
    protected static boolean isNative() {
        return "true".equals(System.getProperty("springAot")) ||
            (System.getProperty("org.graalvm.nativeimage.imagecode") != null);
    }

    @Configuration(proxyBeanMethods = false)
    public static class GaarasonDataSourceAutoconfigure {

        /**
         * 数据源配置
         * <p>
         * 当配置了 gaarason.database.datasource.groups 时, 自动创建多组路由数据源;
         * 否则使用传统的单 Spring DataSource 包装.
         * <p>
         * 各 {@link ObjectProvider} 路由 Bean 若存在则注册到 {@link GaarasonDataSourceContext},供组/库/表与同连接切库使用.
         *
         * @param dataSourceProvider                    Spring 默认数据源
         * @param container                             Gaarason 容器
         * @param dsProperties                          多组 YAML 配置
         * @param dynamicDatabaseRoutingProvider        可选:库键路由
         * @param dynamicDataSourceGroupRoutingProvider 可选:组键路由
         * @param dynamicTableRoutingProvider           可选:表名路由
         * @param dynamicJdbcCatalogRoutingProvider     可选:JDBC catalog 切换
         * @param dynamicExplicitTableRoutingProvider   可选:是否覆盖显式表名
         * @return 主 {@link GaarasonDataSource} Bean
         */
        @Primary
        @Bean()
        @ConditionalOnMissingBean(GaarasonDataSource.class)
        public GaarasonDataSource gaarasonDataSource(ObjectProvider<DataSource> dataSourceProvider,
            Container container, GaarasonDataSourceProperties dsProperties,
            ObjectProvider<DynamicDatabaseRouting> dynamicDatabaseRoutingProvider,
            ObjectProvider<DynamicDataSourceGroupRouting> dynamicDataSourceGroupRoutingProvider,
            ObjectProvider<DynamicTableRouting> dynamicTableRoutingProvider,
            ObjectProvider<DynamicJdbcCatalogRouting> dynamicJdbcCatalogRoutingProvider,
            ObjectProvider<DynamicExplicitTableRouting> dynamicExplicitTableRoutingProvider) {

            applyRoutingExtensions(dynamicDatabaseRoutingProvider, dynamicDataSourceGroupRoutingProvider,
                dynamicTableRoutingProvider, dynamicJdbcCatalogRoutingProvider, dynamicExplicitTableRoutingProvider);

            String defaultGroup = ObjectUtils.isEmpty(dsProperties.getDefaultGroup())
                ? "master"
                : dsProperties.getDefaultGroup();
            Map<String, DataSourceGroup> groupMap;
            if (dsProperties.getGroups().isEmpty()) {
                groupMap = buildSingleGroupMap(dataSourceProvider, defaultGroup);
                LOGGER.info("GaarasonDataSource init with single-group routing mode, groups: " + groupMap.keySet()
                    + ", default: " + defaultGroup);
            } else {
                groupMap = buildGroupMap(dsProperties, defaultGroup);
                LOGGER.info("GaarasonDataSource init with multi-group routing mode, groups: " + groupMap.keySet()
                    + ", default: " + defaultGroup);
            }
            return new GaarasonRoutingDataSourceWrapper(groupMap, defaultGroup, container);
        }

        /**
         * 将 Spring 容器中可选的路由策略 Bean 同步到 {@link GaarasonDataSourceContext}(仅注册非空实例).
         */
        private static void applyRoutingExtensions(
            ObjectProvider<DynamicDatabaseRouting> dynamicDatabaseRoutingProvider,
            ObjectProvider<DynamicDataSourceGroupRouting> dynamicDataSourceGroupRoutingProvider,
            ObjectProvider<DynamicTableRouting> dynamicTableRoutingProvider,
            ObjectProvider<DynamicJdbcCatalogRouting> dynamicJdbcCatalogRoutingProvider,
            ObjectProvider<DynamicExplicitTableRouting> dynamicExplicitTableRoutingProvider) {

            DynamicDatabaseRouting dynamicDatabaseRouting = dynamicDatabaseRoutingProvider.getIfAvailable();
            if (dynamicDatabaseRouting != null) {
                GaarasonDataSourceContext.setDynamicDatabaseRouting(dynamicDatabaseRouting);
            }
            DynamicDataSourceGroupRouting dynamicDataSourceGroupRouting =
                dynamicDataSourceGroupRoutingProvider.getIfAvailable();
            if (dynamicDataSourceGroupRouting != null) {
                GaarasonDataSourceContext.setDynamicDataSourceGroupRouting(dynamicDataSourceGroupRouting);
            }
            DynamicTableRouting dynamicTableRouting = dynamicTableRoutingProvider.getIfAvailable();
            if (dynamicTableRouting != null) {
                GaarasonDataSourceContext.setDynamicTableRouting(dynamicTableRouting);
            }
            DynamicJdbcCatalogRouting dynamicJdbcCatalogRouting = dynamicJdbcCatalogRoutingProvider.getIfAvailable();
            if (dynamicJdbcCatalogRouting != null) {
                GaarasonDataSourceContext.setDynamicJdbcCatalogRouting(dynamicJdbcCatalogRouting);
            }
            DynamicExplicitTableRouting dynamicExplicitTableRouting =
                dynamicExplicitTableRoutingProvider.getIfAvailable();
            if (dynamicExplicitTableRouting != null) {
                GaarasonDataSourceContext.setDynamicExplicitTableRouting(dynamicExplicitTableRouting);
            }
        }

        /**
         * Spring 事物管理器
         * @return 事物管理器
         */
        @Primary
        @Bean
        @ConditionalOnMissingBean(GaarasonTransactionManager.class)
        public GaarasonTransactionManager gaarasonTransactionManager(GaarasonDataSource gaarasonDataSource) {
            LOGGER.info("GaarasonTransactionManager init");
            return new GaarasonTransactionManager(gaarasonDataSource);
        }

        /**
         * 数据源组切换 AOP 切面
         */
        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
        public GaarasonDataSourceAspect gaarasonDataSourceAspect(ConfigurableListableBeanFactory beanFactory) {
            LOGGER.info("GaarasonDataSourceAspect init");
            return new GaarasonDataSourceAspect(beanFactory);
        }

        /**
         * 根据 YAML 配置构建数据源组映射
         * @param dataSourceProvider 数据源提供者
         * @param defaultGroup 默认组名
         * @return 数据源组映射
         */
        private static Map<String, DataSourceGroup> buildSingleGroupMap(ObjectProvider<DataSource> dataSourceProvider,
            String defaultGroup) {
            DataSource dataSource = dataSourceProvider.getIfAvailable();
            if (dataSource == null) {
                throw new IllegalStateException(
                    "No DataSource bean found. Either configure a Spring DataSource or set gaarason.database.datasource.groups.");
            }
            Map<String, DataSourceGroup> groupMap = new LinkedHashMap<>();
            groupMap.put(defaultGroup, new DataSourceGroup(Collections.singletonList(dataSource)));
            return groupMap;
        }

        /**
         * 根据 YAML 配置构建数据源组映射
         * @param dsProperties 数据源配置
         * @param defaultGroup 默认组名
         * @return 数据源组映射
         */
        private static Map<String, DataSourceGroup> buildGroupMap(GaarasonDataSourceProperties dsProperties,
            String defaultGroup) {
            Map<String, DataSourceGroup> groupMap = new LinkedHashMap<>();
            for (Map.Entry<String, DataSourceGroupProperties> entry : dsProperties.getGroups().entrySet()) {
                String groupKey = entry.getKey();
                DataSourceGroupProperties groupProps = entry.getValue();

                List<DataSource> masters = createDataSources(groupProps.getMaster(), groupProps.getType());
                List<DataSource> slaves = createDataSources(groupProps.getSlave(), groupProps.getType());

                if (masters.isEmpty()) {
                    throw new IllegalArgumentException(
                        "Datasource group [" + groupKey + "] must have at least one master datasource.");
                }

                groupMap.put(groupKey, slaves.isEmpty()
                    ? new DataSourceGroup(masters)
                    : new DataSourceGroup(masters, slaves));
            }
            if (!groupMap.containsKey(defaultGroup)) {
                throw new IllegalArgumentException("Default datasource group [" + defaultGroup + "] not found in configured groups.");
            }
            return groupMap;
        }

        /**
         * 从节点配置列表创建 DataSource 实例
         * @param nodes 节点配置列表
         * @param dsType 数据源类型
         * @return DataSource 实例列表
         */
        @SuppressWarnings("unchecked")
        private static List<DataSource> createDataSources(List<DataSourceNodeProperties> nodes,
            @Nullable String dsType) {
            if (ObjectUtils.isEmpty(nodes)) {
                return Collections.emptyList();
            }
            List<DataSource> result = new ArrayList<>(nodes.size());
            for (DataSourceNodeProperties node : nodes) {
                DataSourceBuilder<?> builder = DataSourceBuilder.create();
                if (node.getUrl() != null) {
                    builder.url(node.getUrl());
                }
                if (node.getUsername() != null) {
                    builder.username(node.getUsername());
                }
                if (node.getPassword() != null) {
                    builder.password(node.getPassword());
                }
                if (node.getDriverClassName() != null) {
                    builder.driverClassName(node.getDriverClassName());
                }
                if (!ObjectUtils.isEmpty(dsType)) {
                    try {
                        builder.type((Class<? extends DataSource>) Class.forName(dsType));
                    } catch (ClassNotFoundException e) {
                        throw new IllegalArgumentException("DataSource type class not found: " + dsType, e);
                    }
                }
                result.add(builder.build());
            }
            return result;
        }
    }

}