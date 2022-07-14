package gaarason.database.bootstrap;

import gaarason.database.bootstrap.def.DefaultReflectionScan;
import gaarason.database.config.ConversionConfig;
import gaarason.database.config.GaarasonAutoconfiguration;
import gaarason.database.config.GaarasonDatabaseProperties;
import gaarason.database.contract.support.IdGenerator;
import gaarason.database.contract.support.ReflectionScan;
import gaarason.database.exception.TypeCastException;
import gaarason.database.lang.Nullable;
import gaarason.database.logging.Log;
import gaarason.database.logging.LogFactory;
import gaarason.database.provider.ContainerProvider;
import gaarason.database.provider.FieldInfo;
import gaarason.database.provider.ModelInstanceProvider;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.SnowFlakeIdGenerator;
import gaarason.database.util.ClassUtils;
import gaarason.database.util.ConverterUtils;
import gaarason.database.util.ObjectUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

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
     * 自动使用默认的配置
     * @return ContainerProvider
     */
    public static ContainerBootstrap buildAndBootstrap() {
        return new ContainerBootstrap(GaarasonDatabaseProperties.buildFromSystemProperties().fillAndVerify()).bootstrap();
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
     * 启动
     */
    public synchronized ContainerBootstrap bootstrap() {
        if (bootstrapHasDone) {
            LOGGER.error("Bootstrap in " + getClass() + " can not bo done twice.");
            return this;
        }
        bootstrapHasDone = true;
        bootstrapStepFirst();
        bootstrapStepSecond();
        bootstrapStepThird();
        return this;
    }

    /**
     * 初始化第一步
     * 初始化 无依赖的对象, 以及仅依赖于配置的对象
     */
    protected void bootstrapStepFirst() {
        // 初始化包扫描类
        register(ReflectionScan.class, clazz -> new DefaultReflectionScan(properties));
        // ID生成 雪花算法
        register(IdGenerator.SnowFlakesID.class, clazz -> initSnowFlakesID());
        // ID生成 UUID 36
        register(IdGenerator.UUID36.class, clazz -> () -> UUID.randomUUID().toString());
        // ID生成 UUID 32
        register(IdGenerator.UUID32.class, clazz -> () -> UUID.randomUUID().toString().replace("-", ""));
        // ID生成 Never
        // todo check
        register(IdGenerator.Never.class, clazz -> () -> null);
        // 类型转化
        // todo better
        register(ConversionConfig.class, clazz -> initConversionConfig());
        // Model的实例化的工厂的提供者
        register(ModelInstanceProvider.class, clazz -> new ModelInstanceProvider(this));
    }

    /**
     * 初始化第二步
     * 初始化 依赖于bootstrapStepFirst()结果的对象
     */
    protected void bootstrapStepSecond() {
        register(ModelShadowProvider.class, clazz -> initModelShadow());
    }

    /**
     * 初始化第三步
     * 初始化 其他对象 以及自动配置
     */
    protected void bootstrapStepThird() {
        // 扫描 GaarasonAutoconfiguration 并执行其init()
        bootstrapGaarasonAutoconfiguration();
    }

    /**
     * 初始化
     * @param databaseProperties 配置
     */
    protected ContainerBootstrap(GaarasonDatabaseProperties databaseProperties) {
        properties = databaseProperties;
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
    protected ConversionConfig initConversionConfig() {
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
        ModelShadowProvider modelShadowProvider = new ModelShadowProvider(this);
        modelShadowProvider.loadModels(ObjectUtils.typeCast(getBean(ReflectionScan.class).scanModels()));
        LOGGER.debug("All Model has been load.");
        return modelShadowProvider;
    }

    /**
     * 找到所有的GaarasonAutoconfiguration自动配置类, 并执行其 init()
     */
    protected void bootstrapGaarasonAutoconfiguration() {
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
    }
}
