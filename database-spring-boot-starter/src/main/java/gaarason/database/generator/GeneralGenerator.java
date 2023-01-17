package gaarason.database.generator;

import gaarason.database.eloquent.GeneralModel;
import gaarason.database.eloquent.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 生成器
 * @author xt
 */
@Component
public class GeneralGenerator extends Generator {

    @Autowired
    GeneralModel generalModel;

    @Override
    public Model<GeneralModel.Table, Serializable> getModel() {
        return generalModel;
    }
}
