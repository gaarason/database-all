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
import gaarason.database.util.ConverterUtils;
import gaarason.database.util.ObjectUtils;
import org.reflections.Reflections;

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

    private static final Reflections reflections = new Reflections();

    static {

        // ID生成 雪花算法
        ContainerProvider.register(IdGenerator.SnowFlakesID.class, (clazz -> new SnowFlakeIdGenerator(0, 0)));
        // ID生成 UUID 36
        ContainerProvider.register(IdGenerator.UUID36.class, clazz -> () -> UUID.randomUUID().toString());
        // ID生成 UUID 32
        ContainerProvider.register(IdGenerator.UUID32.class,
            clazz -> () -> UUID.randomUUID().toString().replace("-", ""));
        // ID生成 Never
        ContainerProvider.register(IdGenerator.Never.class, clazz -> () -> null);

        /*
         * 包扫描 - model
         */
        ContainerProvider.register(ReflectionScan.class,
            clazz -> () -> ObjectUtils.typeCast(reflections.getSubTypesOf(Model.class)));

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
     * 自动配置类, 执行其 init()
     */
    public static void init() {
        // GaarasonAutoconfiguration 扫描
        Set<Class<? extends GaarasonAutoconfiguration>> gaarasonAutoconfigurations = reflections.getSubTypesOf(
            GaarasonAutoconfiguration.class);
        for (Class<? extends GaarasonAutoconfiguration> gaarasonAutoconfiguration : gaarasonAutoconfigurations) {
            try {
                gaarasonAutoconfiguration.newInstance().init();
                log.debug("Auto configuration [" + gaarasonAutoconfiguration.getName() + "] executed successfully .");
            } catch (Throwable e) {
                log.error(
                    "A problem was encountered during automatic configuration [" + gaarasonAutoconfiguration.getName() +
                        "].", e);
            }
        }
        log.debug("All gaarasonAutoconfiguration has been init.");
    }
}
