package gaarason.database.spring.boot.starter.configurations;

import gaarason.database.contract.support.IdGenerator;
import gaarason.database.provider.ContainerProvider;
import gaarason.database.provider.ModelInstanceProvider;
import gaarason.database.spring.boot.starter.properties.GaarasonDatabaseProperties;
import gaarason.database.support.SnowFlakeIdGenerator;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.annotation.Resource;

/**
 * 自动配置
 * @author xt
 */
public class GaarasonInstanceConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GaarasonInstanceConfiguration.class);

    @Resource
    ApplicationContext applicationContext;

    @Resource
    GaarasonDatabaseProperties gaarasonDatabaseProperties;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ModelInstanceProvider.register(modelClass -> {
            try {
                return ObjectUtils.typeCast(applicationContext.getBean(modelClass));
            } catch (BeansException e) {
                return ObjectUtils.typeCast(applicationContext.getBean(StringUtils.lowerFirstChar(modelClass.getSimpleName())));
            }
        });
        LOGGER.info("Model instance provider has been registered success.");

        final int workerId = gaarasonDatabaseProperties.getSnowFlake().getWorkerId();
        final int dataId = gaarasonDatabaseProperties.getSnowFlake().getDataId();
        ContainerProvider.register(IdGenerator.SnowFlakesID.class,
            (clazz -> new SnowFlakeIdGenerator(workerId, dataId)));

        LOGGER.info("SnowFlakesID[ workId: {}, dataId: {}] instance has been registered success.", workerId, dataId);
    }
}