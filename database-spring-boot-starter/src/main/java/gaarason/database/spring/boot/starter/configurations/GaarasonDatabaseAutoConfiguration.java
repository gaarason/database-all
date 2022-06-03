package gaarason.database.spring.boot.starter.configurations;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import gaarason.database.config.GaarasonDataSourceConfig;
import gaarason.database.config.GaarasonDatabaseProperties;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.eloquent.GeneralModel;
import gaarason.database.generator.GeneralGenerator;
import gaarason.database.logging.Log;
import gaarason.database.logging.LogFactory;
import gaarason.database.provider.ContainerProvider;
import gaarason.database.provider.ModelInstanceProvider;
import gaarason.database.spring.boot.starter.annotation.GaarasonDatabaseScan;
import gaarason.database.spring.boot.starter.annotation.GaarasonDatabaseScanRegistrar;
import gaarason.database.spring.boot.starter.provider.GaarasonTransactionManager;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Collections;

/**
 * 自动配置
 * @author xt
 */
@Configuration
@AutoConfigureAfter({DruidDataSourceAutoConfigure.class, DataSourceAutoConfiguration.class})
@Import({GeneralModel.class, GeneralGenerator.class})
public class GaarasonDatabaseAutoConfiguration {

    private static final Log LOGGER = LogFactory.getLog(GaarasonDatabaseAutoConfiguration.class);

    /**
     * Spring配置GaarasonDatabaseProperties
     */
    @Bean
    @ConfigurationProperties(prefix = GaarasonDatabaseProperties.PREFIX)
    public GaarasonDatabaseProperties gaarasonDatabaseProperties() {
        return new GaarasonDatabaseProperties();
    }


    @Configuration
    public static class GaarasonConfigAutoconfigure {
        /**
         * 指定 model 扫描范围
         */
        GaarasonConfigAutoconfigure(ApplicationContext applicationContext,
                                    GaarasonDatabaseProperties gaarasonDatabaseProperties) {

            final String springBootApplicationPackage = applicationContext.getBeansWithAnnotation(
                    SpringBootApplication.class)
                .entrySet()
                .iterator()
                .next()
                .getValue()
                .getClass()
                .getPackage().getName();

            /*
             * GaarasonDatabaseProperties 配置注册到 ContainerProvider
             * 认定 GaarasonDatabaseScan 的解析一定在此之前完成了.
             * 默认使用 @SpringBootApplication 所在的包路径
             */
            ContainerProvider.register(GaarasonDatabaseProperties.class,
                (clazz -> gaarasonDatabaseProperties.mergeScan(GaarasonDatabaseScanRegistrar.getScan())
                    .fillPackageWhenIsEmpty(springBootApplicationPackage)
                    .fillAndVerify()));

            // 注册 model实例获取方式
            ModelInstanceProvider.register(modelClass -> {
                try {
                    return ObjectUtils.typeCast(applicationContext.getBean(modelClass));
                } catch (BeansException e) {
                    return ObjectUtils.typeCast(
                        applicationContext.getBean(StringUtils.lowerFirstChar(modelClass.getSimpleName())));
                }
            });
            LOGGER.info("Model instance provider has been registered success.");
        }
    }

    @Configuration
    public static class GaarasonDataSourceAutoconfigure {

        @Resource
        DataSource dataSource;

        /**
         * 数据源配置
         * @return 数据源
         */
        @Primary
        @Bean(autowireCandidate = false)
        @ConditionalOnMissingBean(GaarasonDataSource.class)
        public GaarasonDataSource gaarasonDataSource() {
            LOGGER.info("GaarasonDataSource init with " + dataSource.getClass().getName());
            return ContainerProvider.getBean(GaarasonDataSourceConfig.class)
                .build(Collections.singletonList(dataSource));
        }

        /**
         * Spring 事物管理器
         * @return 事物管理器
         */
        @Primary
        @Bean
        @ConditionalOnMissingBean(GaarasonTransactionManager.class)
        public GaarasonTransactionManager gaarasonTransactionManager() {
            LOGGER.info("GaarasonTransactionManager init");
            return new GaarasonTransactionManager(gaarasonDataSource());
        }
    }

}