package gaarason.database.spring.boot.starter.configurations;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import gaarason.database.connections.GaarasonDataSourceBuilder;
import gaarason.database.connections.ProxyDataSource;
import gaarason.database.contracts.GaarasonDataSource;
import gaarason.database.eloquent.GeneralModel;
import gaarason.database.generator.GeneralGenerator;
import gaarason.database.spring.boot.starter.properties.DefaultProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@AutoConfigureBefore(DruidDataSourceAutoConfigure.class)
@EnableConfigurationProperties({DefaultProperties.class})
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
        List<DataSource> dataSourceList = new ArrayList<>();
        dataSourceList.add(dataSourceDruidConfig());
        log.info("-------------------- ProxyDataSource(GaarasonDataSource) init ---------------------");
        return GaarasonDataSourceBuilder.create().build(dataSourceList);
    }

}