package gaarason.database.spring.boot.starter.annotation;

import gaarason.database.config.GaarasonDatabaseProperties;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;

/**
 * 包扫描注解解析, 结果合并
 * @see GaarasonDatabaseScan
 */
public class GaarasonDatabaseScanRegistrar implements ImportBeanDefinitionRegistrar {

    private static final GaarasonDatabaseProperties.Scan SCAN = new GaarasonDatabaseProperties.Scan();

    public static GaarasonDatabaseProperties.Scan getScan() {
        return SCAN;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        synchronized (GaarasonDatabaseScanRegistrar.class) {
            AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(GaarasonDatabaseScan.class.getName()));
            if (annoAttrs == null) {
                return;
            }

            SCAN.getPackages().addAll(Arrays.asList(annoAttrs.getStringArray("packages")));
            SCAN.getFilterExcludePackages().addAll(Arrays.asList(annoAttrs.getStringArray("filterExcludePackages")));
            SCAN.getFilterIncludePatterns().addAll(Arrays.asList(annoAttrs.getStringArray("filterIncludePatterns")));
            SCAN.getFilterExcludePatterns().addAll(Arrays.asList(annoAttrs.getStringArray("filterExcludePatterns")));
        }
    }

}
