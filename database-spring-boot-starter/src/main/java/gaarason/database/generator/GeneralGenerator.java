package gaarason.database.generator;

import gaarason.database.eloquent.GeneralModel;
import gaarason.database.eloquent.Model;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;

/**
 * 生成器
 * @author xt
 */
@Component
public class GeneralGenerator extends Generator {

    @Resource
    GeneralModel generalModel;

    @Override
    public Model<GeneralModel.Table, Serializable> getModel() {
        return generalModel;
    }
}
