package gaarason.database.eloquent;

import gaarason.database.contract.connection.GaarasonDataSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.io.Serializable;

/**
 * 通用model
 * @author xt
 */
@Repository
public class GeneralModel extends Model<GeneralModel.Table, Serializable> {

    @Resource
    private GaarasonDataSource gaarasonDataSource;

    @Override
    public GaarasonDataSource getGaarasonDataSource() {
        return gaarasonDataSource;
    }

    public static class Table implements Serializable {

    }

}
