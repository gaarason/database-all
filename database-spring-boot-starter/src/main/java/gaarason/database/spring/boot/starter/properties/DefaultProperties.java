package gaarason.database.spring.boot.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.sql.DataSource;

@Data
@ConfigurationProperties(prefix = "spring.gaarason.database.default")
public class DefaultProperties {

    private String driverClassName;

    private Class<? extends DataSource> type;


}