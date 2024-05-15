package gaarason.database.bootstrap;

import gaarason.database.annotation.conversion.*;
import gaarason.database.bootstrap.def.DefaultReflectionScan;
import gaarason.database.config.ConversionConfig;
import gaarason.database.config.GaarasonAutoconfiguration;
import gaarason.database.config.GaarasonDatabaseProperties;
import gaarason.database.contract.support.*;
import gaarason.database.exception.TypeCastException;
import gaarason.database.lang.Nullable;
import gaarason.database.logging.Log;
import gaarason.database.logging.LogFactory;
import gaarason.database.provider.ContainerProvider;
import gaarason.database.provider.ModelInstanceProvider;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.NamedThreadFactory;
import gaarason.database.support.SnowFlakeIdGenerator;
import gaarason.database.util.ClassUtils;
import gaarason.database.util.ConverterUtils;
import gaarason.database.util.StringUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 初始化容器
 * @author xt
 */
public class ContainerBootstrap extends ContainerProvider {

    private static final Log LOGGER = LogFactory.getLog(ContainerBootstrap.class);

    /**
     * 配置文件
     */
    protected final GaarasonDatabaseProperties properties;

    /**
     * 是否已完成了初始化
     */
    protected volatile boolean bootstrapHasDone = false;

    /**
     * 初始化
     * @param databaseProperties 配置
     */
    protected ContainerBootstrap(GaarasonDatabaseProperties databaseProperties) {
        properties = databaseProperties;
    }

    /**
     * 自动使用默认的配置
     * @return ContainerProvider
     */
    public static ContainerBootstrap buildAndBootstrap() {
        return new ContainerBootstrap(
            GaarasonDatabaseProperties.buildFromSystemProperties().fillAndVerify()).autoBootstrap();
    }

    /**
     * 自动使用默认的配置
     * @return ContainerProvider
     */
    public static ContainerBootstrap build() {
        return new ContainerBootstrap(GaarasonDatabaseProperties.buildFromSystemProperties().fillAndVerify());
    }

    /**
     * 自动使用指定的配置
     * @param properties 配置
     * @return ContainerProvider
     */
    public static ContainerBootstrap build(GaarasonDatabaseProperties properties) {
        return new ContainerBootstrap(properties);
    }

    /**
     * 自动化启动
     * @return ContainerProvider
     */
    public synchronized ContainerBootstrap autoBootstrap() {
        if (bootstrapHasDone) {
            LOGGER.error("Bootstrap in " + getClass() + " can not bo done twice.");
            return this;
        }
        bootstrapHasDone = true;
        defaultRegister();
        bootstrapGaarasonAutoconfiguration();
        initialization();
        return this;
    }

    /**
     * 初始化 无依赖的对象, 以及仅依赖于配置的对象
     * @return ContainerProvider
     */
    public ContainerBootstrap defaultRegister() {
        // 初始化包扫描类
        register(ReflectionScan.class, clazz -> new DefaultReflectionScan(properties));
        // ID生成 雪花算法
        register(IdGenerator.SnowFlakesID.class, clazz -> initSnowFlakesID());
        // ID生成 UUID 36
        register(IdGenerator.UUID36.class, clazz -> () -> UUID.randomUUID().toString());
        // ID生成 UUID 32
        register(IdGenerator.UUID32.class, clazz -> () -> StringUtils.replace(UUID.randomUUID(), "-", ""));
        // ID生成 Never
        register(IdGenerator.Never.class, clazz -> () -> null);
        // 类型转化
        // todo better
        register(ConversionConfig.class, clazz -> initConversionConfig());
        //
        register(FieldFill.NotFill.class, clazz -> new FieldFill.NotFill());
        //
        register(FieldConversion.Default.class, clazz -> new DefaultConversion(this));
        register(FieldConversion.Json.class, clazz -> new JsonConversion());
        register(FieldConversion.EnumInteger.class, clazz -> new EnumIntegerConversion());
        register(FieldConversion.EnumString.class, clazz -> new EnumStringConversion());
        register(FieldConversion.Bit.class, clazz -> new BitConversion());
        //
        register(FieldStrategy.Default.class, clazz -> new FieldStrategy.Default());
        //
        register(FieldStrategy.Never.class, clazz -> new FieldStrategy.Never());
        register(FieldStrategy.Always.class, clazz -> new FieldStrategy.Always());
        register(FieldStrategy.NotNull.class, clazz -> new FieldStrategy.NotNull());
        register(FieldStrategy.NotEmpty.class, clazz -> new FieldStrategy.NotEmpty());
        // Model的实例化的工厂的提供者
        register(ModelInstanceProvider.class, clazz -> new ModelInstanceProvider(this));
        // Model信息大全
        register(ModelShadowProvider.class, clazz -> new ModelShadowProvider(this));
        // 异步线程池
        register(ExecutorService.class, clazz -> new ThreadPoolExecutor(properties.getAsyncPool().getCorePoolSize(),
            properties.getAsyncPool().getMaximumPoolSize(), properties.getAsyncPool().getKeepAliveTime(),
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(properties.getAsyncPool().getWorkQueueSize()),
            new NamedThreadFactory("gaarason-async")));
        return this;
    }

    /**
     * 找到所有的GaarasonAutoconfiguration自动配置类, 并执行其 init()
     * @return ContainerProvider
     */
    public ContainerBootstrap bootstrapGaarasonAutoconfiguration() {
        Set<Class<? extends GaarasonAutoconfiguration>> gaarasonAutoconfigurations = getBean(
            ReflectionScan.class).scanAutoconfiguration();

        for (Class<? extends GaarasonAutoconfiguration> gaarasonAutoconfiguration : gaarasonAutoconfigurations) {
            try {
                ClassUtils.newInstance(gaarasonAutoconfiguration).init(this);
                LOGGER.debug(
                    "Auto configuration [" + gaarasonAutoconfiguration.getName() + "] executed successfully .");
            } catch (Throwable e) {
                LOGGER.error(
                    "A problem was encountered during automatic configuration [" + gaarasonAutoconfiguration.getName() +
                        "].", e);
            }
        }
        LOGGER.debug("All gaarasonAutoconfiguration has been init.");
        return this;
    }

    /**
     * 初始化
     * @return ContainerProvider
     */
    public ContainerBootstrap initialization() {
        initModelShadow();
        return this;
    }

    /**
     * 雪花算法对象
     * @return SnowFlakesID
     */
    protected IdGenerator.SnowFlakesID initSnowFlakesID() {
        return new SnowFlakeIdGenerator(properties.getSnowFlake().getWorkerId(), properties.getSnowFlake().getDataId());
    }

    /**
     * 类型转化对象
     * @return ConversionConfig
     */
    protected static ConversionConfig initConversionConfig() {
        return new ConversionConfig() {
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
            public Object getValueFromJdbcResultSet(@Nullable Field field, ResultSet resultSet, String column)
                throws SQLException {
                return ConverterUtils.getValueFromJdbcResultSet(field, resultSet, column);
            }
        };
    }

    /**
     * 初始化ModelShadow
     * 找到所有的 Model 并解析
     * @return ModelShadow
     */
    protected ModelShadowProvider initModelShadow() {
        ModelShadowProvider modelShadowProvider = getBean(ModelShadowProvider.class);
        int i = modelShadowProvider.loadModels(getBean(ReflectionScan.class).scanModels());
        LOGGER.debug("All " + i + " Model has been load.");
        return modelShadowProvider;
    }

}
