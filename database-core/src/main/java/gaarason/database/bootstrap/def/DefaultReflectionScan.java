package gaarason.database.bootstrap.def;

import gaarason.database.config.GaarasonAutoconfiguration;
import gaarason.database.config.GaarasonDatabaseProperties;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.support.ReflectionScan;
import gaarason.database.util.ObjectUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.FilterBuilder;

import java.util.Set;

/**
 * 反射扫描器默认实现
 */
public class DefaultReflectionScan implements ReflectionScan {

    protected final Reflections reflections;

    public DefaultReflectionScan(GaarasonDatabaseProperties properties) {
        reflections = getReflections(properties);
    }

    @Override
    public Set<Class<? extends Model<?, ?, ?>>> scanModels() {
        return ObjectUtils.typeCast(reflections.getSubTypesOf(Model.class));
    }

    @Override
    public Set<Class<? extends GaarasonAutoconfiguration>> scanAutoconfiguration() {
        return reflections.getSubTypesOf(GaarasonAutoconfiguration.class);
    }

    /**
     * 获取真实反射扫描器
     * 配置扫描路径
     * @return Reflections
     */
    protected static Reflections getReflections(GaarasonDatabaseProperties properties) {
        // 获取配置
        GaarasonDatabaseProperties.Scan scan = properties.getScan();

        // 使用配置
        FilterBuilder filterBuilder = new FilterBuilder();
        for (String filterExcludePackage : scan.getFilterExcludePackages()) {
            filterBuilder.excludePackage(filterExcludePackage);
        }
        for (String filterIncludePattern : scan.getFilterIncludePatterns()) {
            filterBuilder.includePattern(filterIncludePattern);
        }
        for (String filterExcludePattern : scan.getFilterExcludePatterns()) {
            filterBuilder.excludePattern(filterExcludePattern);
        }

        return new Reflections(scan.getPackages(), filterBuilder, Scanners.SubTypes);
    }
}