package gaarason.database.spring.boot.starter.configurations;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import gaarason.database.connections.ProxyDataSource;
import gaarason.database.spring.boot.starter.properties.DefaultProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@AutoConfigureBefore(DruidDataSourceAutoConfigure.class)
@EnableConfigurationProperties({DefaultProperties.class})
public class ProxyDataSourceConfiguration {

    @Bean(name = "dataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    public DataSource dataSource() {
        log.info("-------------------- dataSource primary init ---------------------");
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = "proxyDataSource")
    public ProxyDataSource proxyDataSource() {
        List<DataSource> dataSourceList = new ArrayList<>();
        dataSourceList.add(dataSource());
        log.info("-------------------- proxyDataSource primary init ---------------------");
        return new ProxyDataSource(dataSourceList);
    }

}