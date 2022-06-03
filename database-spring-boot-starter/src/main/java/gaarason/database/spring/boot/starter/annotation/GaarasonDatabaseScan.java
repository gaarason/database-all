package gaarason.database.spring.boot.starter.annotation;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 包扫描注解配置
 * 哟绿玉扫描所有的Model以及
 * @see gaarason.database.config.GaarasonDatabaseProperties.Scan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(GaarasonDatabaseScanRegistrar.class)
public @interface GaarasonDatabaseScan {

    @AliasFor("packages")
    String[] value() default {};

    String[] packages() default {};

    String[] filterExcludePackages() default {};

    String[] filterIncludePatterns() default {};

    String[] filterExcludePatterns() default {};

}

