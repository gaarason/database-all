package gaarason.database.spring.boot.starter.test;

import gaarason.database.core.lang.NonNull;
import gaarason.database.eloquent.GeneralModel;
import gaarason.database.eloquent.Model;
import gaarason.database.generator.Manager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class GeneralGenerator extends Manager {

    @Resource
    GeneralModel generalModel;

    @Override
    @NonNull
    public Model getModel() {
        return generalModel;
    }
}
