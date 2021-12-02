package gaarason.database.spring.boot.starter.configurations;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.config.GaarasonDataSourceConfig;
import gaarason.database.eloquent.GeneralModel;
import gaarason.database.generator.GeneralGenerator;
import gaarason.database.provider.ContainerProvider;
import gaarason.database.spring.boot.starter.properties.GaarasonDatabaseProperties;
import gaarason.database.spring.boot.starter.provider.GaarasonTransactionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.util.Collections;

/**
 * 自动配置
 * @author xt
 */
@Slf4j
@Configuration
@AutoConfigureBefore(DruidDataSourceAutoConfigure.class)
@EnableConfigurationProperties({GaarasonDatabaseProperties.class})
@Import({GeneralModel.class, GeneralGenerator.class})
public class GaarasonDataSourceConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    @ConditionalOnMissingBean
    public DataSource dataSourceDruidConfig() {
        log.info("-------------------- dataSource druid config init ---------------------");
        return DruidDataSourceBuilder.create().build();
    }

    @Bean
    @ConditionalOnMissingBean
    public GaarasonDataSource gaarasonDataSource() {
        log.info("-------------------- gaarasonDataSource init --------------------------");
        return ContainerProvider.getBean(GaarasonDataSourceConfig.class).build(Collections.singletonList(dataSourceDruidConfig()));
    }

    @Bean
    @ConditionalOnMissingBean
    public GaarasonTransactionManager gaarasonTransactionManager() {
        log.info("-------------------- gaarasonTransactionManager init ------------------");
        return new GaarasonTransactionManager(gaarasonDataSource());
    }
}