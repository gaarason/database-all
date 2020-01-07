package gaarason.database.spring.boot.starter.test;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;

@Configuration
@AutoConfigureAfter(DataSource.class)
@Import(GeneralGenerator.class)
public class GeneralGeneratorConfiguration {

}