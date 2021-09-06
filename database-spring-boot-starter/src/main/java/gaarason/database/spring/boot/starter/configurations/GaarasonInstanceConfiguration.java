package gaarason.database.spring.boot.starter.configurations;

import gaarason.database.contract.support.IdGenerator;
import gaarason.database.provider.ContainerProvider;
import gaarason.database.provider.ModelInstanceProvider;
import gaarason.database.spring.boot.starter.properties.GaarasonDatabaseProperties;
import gaarason.database.support.SnowFlakeIdGenerator;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.annotation.Resource;

/**
 * 自动配置
 * @author xt
 */
@Slf4j
public class GaarasonInstanceConfiguration implements ApplicationListener<ContextRefreshedEvent> {

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
        log.info("Model instance provider has been registered success.");

        int workerId = gaarasonDatabaseProperties.getSnowFlake().getWorkerId();
        ContainerProvider.register(IdGenerator.SnowFlakesID.class,
            (clazz -> new SnowFlakeIdGenerator(workerId, 0)));

        log.info("SnowFlakesID[{}] instance has been registered success.", workerId);

    }
}