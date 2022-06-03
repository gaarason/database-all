package gaarason.database.config;

import gaarason.database.connection.GaarasonDataSourceBuilder;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.support.IdGenerator;
import gaarason.database.contract.support.ReflectionScan;
import gaarason.database.exception.TypeCastException;
import gaarason.database.lang.Nullable;
import gaarason.database.logging.Log;
import gaarason.database.logging.LogFactory;
import gaarason.database.provider.ContainerProvider;
import gaarason.database.provider.FieldInfo;
import gaarason.database.support.SnowFlakeIdGenerator;
import gaarason.database.util.ClassUtils;
import gaarason.database.util.ConverterUtils;
import gaarason.database.util.ObjectUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.FilterBuilder;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 默认配置初始化
 */
public class DefaultAutoConfiguration {

    private static final Log log = LogFactory.getLog(DefaultAutoConfiguration.class);

    /**
     * 是否已完成了自动配置类的配置(所有的GaarasonAutoconfiguration的实现类的init方法的调用)
     */
    private static volatile boolean initOnAllGaarasonAutoconfigurationDone = false;

    static {
        // GaarasonDatabaseProperties 配置
        ContainerProvider.register(GaarasonDatabaseProperties.class,
            (clazz -> GaarasonDatabaseProperties.buildFromSystemProperties()));

        // ID生成 雪花算法
        ContainerProvider.register(IdGenerator.SnowFlakesID.class, (clazz -> new SnowFlakeIdGenerator(
            ContainerProvider.getBean(GaarasonDatabaseProperties.class).getSnowFlake().getWorkerId(),
            ContainerProvider.getBean(GaarasonDatabaseProperties.class).getSnowFlake().getDataId())));

        // ID生成 UUID 36
        ContainerProvider.register(IdGenerator.UUID36.class, clazz -> () -> UUID.randomUUID().toString());

        // ID生成 UUID 32
        ContainerProvider.register(IdGenerator.UUID32.class,
            clazz -> () -> UUID.randomUUID().toString().replace("-", ""));

        // ID生成 Never
        ContainerProvider.register(IdGenerator.Never.class, clazz -> () -> null);

        // 包扫描
        ContainerProvider.register(ReflectionScan.class, clazz -> new DefaultReflectionScan());

        // 数据源
        ContainerProvider.register(GaarasonDataSourceConfig.class, clazz -> new GaarasonDataSourceConfig() {

            @Override
            public GaarasonDataSource build(DataSource masterDataSource) {
                return GaarasonDataSourceBuilder.build(masterDataSource);
            }

            @Override
            public GaarasonDataSource build(List<DataSource> masterDataSourceList) {
                return GaarasonDataSourceBuilder.build(masterDataSourceList);
            }

            @Override
            public GaarasonDataSource build(List<DataSource> masterDataSourceList,
                                            List<DataSource> slaveDataSourceList) {
                return GaarasonDataSourceBuilder.build(masterDataSourceList, slaveDataSourceList);
            }

        });

        // 类型转化
        ContainerProvider.register(ConversionConfig.class, clazz -> new ConversionConfig() {

            @Nullable
            @Override
            public <R> R castNullable(@Nullable final Object obj, final Class<R> clz) throws TypeCastException {
                return ConverterUtils.castNullable(obj, clz);
            }

            @Override
            public <R> R cast(final Object obj, final Class<R> clz) throws TypeCastException {
                return ConverterUtils.cast(obj, clz);
            }

            @Nullable
            @Override
            public <R> R getDefaultValueByJavaType(Class<R> clz) throws TypeCastException {
                return ConverterUtils.getDefaultValueByJavaType(clz);
            }

            @Nullable
            @Override
            public Object getValueFromJdbcResultSet(@Nullable FieldInfo fieldInfo, ResultSet resultSet, String column)
                throws SQLException {
                return ConverterUtils.getValueFromJdbcResultSet(fieldInfo, resultSet, column);
            }
        });


    }

    /**
     * 触发下类加载
     */
    public static void touch() {
        // 就是空的. 不用怀疑
    }

    /**
     * 找到所有的GaarasonAutoconfiguration自动配置类, 并执行其 init()
     */
    public synchronized static void initOnAllGaarasonAutoconfiguration() {
        // GaarasonAutoconfiguration 是否已经完成扫描
        if (initOnAllGaarasonAutoconfigurationDone) {
            return;
        }

        Set<Class<? extends GaarasonAutoconfiguration>> gaarasonAutoconfigurations = ContainerProvider.getBean(
            ReflectionScan.class).scanAutoconfiguration();

        for (Class<? extends GaarasonAutoconfiguration> gaarasonAutoconfiguration : gaarasonAutoconfigurations) {
            try {
                ClassUtils.newInstance(gaarasonAutoconfiguration).init();
                log.debug("Auto configuration [" + gaarasonAutoconfiguration.getName() + "] executed successfully .");
            } catch (Throwable e) {
                log.error(
                    "A problem was encountered during automatic configuration [" + gaarasonAutoconfiguration.getName() +
                        "].", e);
            }
        }
        initOnAllGaarasonAutoconfigurationDone = true;
        log.debug("All gaarasonAutoconfiguration has been init.");
    }


    /**
     * 默认反射扫描器
     * 当 scanModels 与 scanAutoconfiguration 均调用一次后, 自动执行 close(), 以便在gc时释放内存
     */
    public static class DefaultReflectionScan implements ReflectionScan {

        protected Reflections reflections;

        public DefaultReflectionScan() {
            reflections = getReflections();
        }

        @Override
        public Set<Class<? extends Model<?, ?>>> scanModels() {
            return ObjectUtils.typeCast(reflections.getSubTypesOf(Model.class));
        }

        @Override
        public Set<Class<? extends GaarasonAutoconfiguration>> scanAutoconfiguration() {
            return reflections.getSubTypesOf(GaarasonAutoconfiguration.class);
        }

        /**
         * 获取真实反射扫描器
         * @return Reflections
         */
        protected Reflections getReflections() {
            // 获取配置
            GaarasonDatabaseProperties properties = ContainerProvider.getBean(GaarasonDatabaseProperties.class);
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
}
