package gaarason.database.spring.boot.starter.configurations;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import gaarason.database.config.GaarasonDataSourceConfig;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.support.IdGenerator;
import gaarason.database.eloquent.GeneralModel;
import gaarason.database.eloquent.annotation.Primary;
import gaarason.database.generator.GeneralGenerator;
import gaarason.database.provider.ContainerProvider;
import gaarason.database.provider.ModelInstanceProvider;
import gaarason.database.spring.boot.starter.properties.GaarasonDatabaseProperties;
import gaarason.database.spring.boot.starter.provider.GaarasonTransactionManager;
import gaarason.database.support.SnowFlakeIdGenerator;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.util.Collections;

/**
 * 自动配置
 * @author xt
 */
@Configuration
@AutoConfigureBefore(DruidDataSourceAutoConfigure.class)
@EnableConfigurationProperties({GaarasonDatabaseProperties.class})
@Import({GeneralModel.class, GeneralGenerator.class})
public class GaarasonDatabaseConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(GaarasonDatabaseConfiguration.class);

    /**
     * 指定 model 扫描范围
     */
    GaarasonDatabaseConfiguration(ApplicationContext applicationContext, GaarasonDatabaseProperties gaarasonDatabaseProperties) {
        // 注册 model实例获取方式
        ModelInstanceProvider.register(modelClass -> {
            try {
                return ObjectUtils.typeCast(applicationContext.getBean(modelClass));
            } catch (BeansException e) {
                return ObjectUtils.typeCast(applicationContext.getBean(StringUtils.lowerFirstChar(modelClass.getSimpleName())));
            }
        });
        LOGGER.info("Model instance provider has been registered success.");

        // 注册 雪花id实现
        final int workerId = gaarasonDatabaseProperties.getSnowFlake().getWorkerId();
        final int dataId = gaarasonDatabaseProperties.getSnowFlake().getDataId();
        ContainerProvider.register(IdGenerator.SnowFlakesID.class, clazz -> new SnowFlakeIdGenerator(workerId, dataId));

        LOGGER.info("SnowFlakesID[ workId: {}, dataId: {}] instance has been registered success.", workerId, dataId);

    }

    /**
     * 配置读取
     * @return 配置对象
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    @ConditionalOnMissingBean
    public DataSource dataSourceDruidConfig() {
        LOGGER.info("-------------------- dataSource druid config init ---------------------");
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * 数据源配置
     * @return 数据源
     */
    @Primary
    @Bean
    @ConditionalOnMissingBean
    public GaarasonDataSource gaarasonDataSource() {
        LOGGER.info("-------------------- gaarasonDataSource init --------------------------");
        return ContainerProvider.getBean(GaarasonDataSourceConfig.class).build(Collections.singletonList(dataSourceDruidConfig()));
    }

    /**
     * Spring 事物管理器
     * @return 事物管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public GaarasonTransactionManager gaarasonTransactionManager() {
        LOGGER.info("-------------------- gaarasonTransactionManager init ------------------");
        return new GaarasonTransactionManager(gaarasonDataSource());
    }
}