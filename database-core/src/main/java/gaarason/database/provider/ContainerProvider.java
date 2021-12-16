package gaarason.database.provider;

import gaarason.database.config.ConversionConfig;
import gaarason.database.config.GaarasonDataSourceConfig;
import gaarason.database.config.QueryBuilderConfig;
import gaarason.database.config.QueryBuilderTypeConfig;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.function.InstanceCreatorFunctionalInterface;
import gaarason.database.contract.support.IdGenerator;
import gaarason.database.contract.support.ReflectionScan;
import gaarason.database.exception.InvalidConfigException;
import gaarason.database.exception.ModelNewInstanceException;
import gaarason.database.exception.ObjectNewInstanceException;
import gaarason.database.support.SnowFlakeIdGenerator;
import gaarason.database.util.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 容器化对象实例
 * @author xt
 */
@Slf4j
public final class ContainerProvider {

    /**
     * 实例化工厂 MAP
     */
    private static final ConcurrentHashMap<Class<?>, InstanceCreatorFunctionalInterface<?>> INSTANCE_CREATOR_MAP = new ConcurrentHashMap<>();

    /**
     * 实例对象 MAP
     */
    private static final ConcurrentHashMap<Class<?>, Object> INSTANCE_MAP = new ConcurrentHashMap<>();

    static {
        // ID生成 雪花算法
        register(IdGenerator.SnowFlakesID.class, (clazz -> new SnowFlakeIdGenerator(0, 0)));
        // ID生成 UUID 36
        register(IdGenerator.UUID36.class, clazz -> () -> UUID.randomUUID().toString());
        // ID生成 UUID 32
        register(IdGenerator.UUID32.class, clazz -> () -> UUID.randomUUID().toString().replace("-", ""));
        // ID生成 Never
        register(IdGenerator.Never.class, clazz -> () -> null);
        // 包扫描
        register(ReflectionScan.class, clazz -> new ReflectionScan() {
            public final Reflections reflections = new Reflections();

            @Override
            public Set<Class<? extends Model<?, ?>>> scanModels() {
                return ObjectUtils.typeCast(reflections.getSubTypesOf(Model.class));
            }
        });

        // 数据源
        register(GaarasonDataSourceConfig.class, clazz -> new GaarasonDataSourceConfig() {
        });
        // 类型转化
        register(ConversionConfig.class, clazz -> new ConversionConfig() {
        });
        // 数据库类型
        register(QueryBuilderTypeConfig.class, clazz -> () -> {
            List<Class<? extends QueryBuilderConfig>> list = new ArrayList<>();
            list.add(QueryBuilderConfig.Mysql.class);
            list.add(QueryBuilderConfig.Mssql.class);
            return list;
        });
        // 查询构造器 Mysql
        register(QueryBuilderConfig.Mysql.class, clazz -> new QueryBuilderConfig.Mysql() {
        });
        // 查询构造器 Mssql
        register(QueryBuilderConfig.Mssql.class, clazz -> new QueryBuilderConfig.Mssql() {
        });
    }

    private ContainerProvider() {

    }

    /**
     * 注册 实例化工厂
     * 只要没有实例化, 那么可以重复注册, 且后注册的覆盖先注册的
     * @param closure 实例化工厂
     */
    public static synchronized <T> void register(Class<T> interfaceClass, InstanceCreatorFunctionalInterface<T> closure) {
        if (INSTANCE_MAP.get(interfaceClass) != null) {
            throw new InvalidConfigException(interfaceClass + " should be registered before get bean.");
        }
        INSTANCE_CREATOR_MAP.put(interfaceClass, closure);
    }

    /**
     * 返回一个对象, 必然单例
     * @return 对象
     */
    public static <T> T getBean(Class<T> interfaceClass) {
        if (INSTANCE_MAP.get(interfaceClass) == null) {
            synchronized (interfaceClass) {
                if (INSTANCE_MAP.get(interfaceClass) == null) {
                    // 优先 使用注册的方式
                    InstanceCreatorFunctionalInterface<?> instanceCreatorFunctionalInterface = INSTANCE_CREATOR_MAP.get(interfaceClass);
                    if (null == instanceCreatorFunctionalInterface) {
                        // 保底 使用默认的方式
                        instanceCreatorFunctionalInterface = defaultNewInstance(interfaceClass);
                    }
                    try {
                        Object bean = instanceCreatorFunctionalInterface.execute(ObjectUtils.typeCast(interfaceClass));
                        INSTANCE_MAP.put(interfaceClass, bean);
                    } catch (Throwable e) {
                        throw new ModelNewInstanceException(interfaceClass, e.getMessage(), e);
                    }
                }
            }
        }
        return ObjectUtils.typeCast(INSTANCE_MAP.get(interfaceClass));
    }

    /**
     * 缺省实例化方式
     * @param clazz 类
     * @param <T>   类型
     */
    private static <T> InstanceCreatorFunctionalInterface<T> defaultNewInstance(Class<T> clazz) {
        return c -> {
            try {
                log.info("Instantiate unregistered objects[{}] by default.", clazz);
                return clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new ObjectNewInstanceException(clazz, e.getMessage(), e);
            }
        };
    }

}
