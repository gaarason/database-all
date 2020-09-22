package gaarason.database.spring.boot.starter.configurations;

import gaarason.database.core.lang.NonNull;
import gaarason.database.provider.ModelInstanceProvider;
import gaarason.database.util.ObjectUtil;
import gaarason.database.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.annotation.Resource;

@Slf4j
public class GaarasonModelInstanceConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    @Resource
    ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ModelInstanceProvider.register((modelClass) -> {
            try {
                return ObjectUtil.typeCast(applicationContext.getBean(modelClass));
            } catch (BeansException e) {
                return ObjectUtil.typeCast(applicationContext.getBean(StringUtil.lowerFirstChar(modelClass.getSimpleName())));
            }
        });
        log.info("Model instance provider has been registered success.");
    }
}