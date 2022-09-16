package gaarason.database.spring.boot.starter.configurations;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import gaarason.database.autoconfiguration.MssqlAutoconfiguration;
import gaarason.database.autoconfiguration.MysqlAutoconfiguration;
import gaarason.database.bootstrap.ContainerBootstrap;
import gaarason.database.config.GaarasonDatabaseProperties;
import gaarason.database.connection.GaarasonDataSourceBuilder;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.core.Container;
import gaarason.database.eloquent.GeneralModel;
import gaarason.database.generator.GeneralGenerator;
import gaarason.database.logging.Log;
import gaarason.database.logging.LogFactory;
import gaarason.database.provider.ModelInstanceProvider;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.spring.boot.starter.annotation.GaarasonDatabaseScanRegistrar;
import gaarason.database.spring.boot.starter.provider.GaarasonTransactionManager;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

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
     * 容器初始化
     * @param applicationContext 应用上下文
     * @param gaarasonDatabaseProperties 配置
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
         * 认定 GaarasonDatabaseScan 的解析一定在此之前完成了.
         * 默认使用 pring boot 的包扫描路径
         */
        gaarasonDatabaseProperties.mergeScan(GaarasonDatabaseScanRegistrar.getScan())
            .fillPackageWhenIsEmpty(packageOnSpringBoot)
            .fillAndVerify();

        // 从配置创建全新容器
        ContainerBootstrap container = ContainerBootstrap.build(gaarasonDatabaseProperties);

        container.defaultRegister();

        // 注册 model实例获取方式
        container.getBean(ModelInstanceProvider.class).register(modelClass -> {
            try {
                return ObjectUtils.typeCast(applicationContext.getBean(modelClass));
            } catch (BeansException e) {
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
            new MysqlAutoconfiguration().init(container);
            new MssqlAutoconfiguration().init(container);
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
     * @see org.springframework.nativex.NativeListener
     */
    protected static boolean isNative() {
        return "true".equals(System.getProperty("springAot")) ||
            (System.getProperty("org.graalvm.nativeimage.imagecode") != null);
    }

    @Configuration(proxyBeanMethods = false)
    public static class GaarasonDataSourceAutoconfigure {

        /**
         * 数据源配置
         * @return 数据源
         */
        @Primary
        @Bean()
        @ConditionalOnMissingBean(GaarasonDataSource.class)
        public GaarasonDataSource gaarasonDataSource(DataSource dataSource, Container container) {
            LOGGER.info("GaarasonDataSource init with " + dataSource.getClass().getName());
            // 创建 GaarasonDataSource
            return GaarasonDataSourceBuilder.build(dataSource, container);
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
    }

}