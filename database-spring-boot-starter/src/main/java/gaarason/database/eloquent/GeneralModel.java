package gaarason.database.eloquent;

import gaarason.database.contract.GaarasonDataSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class GeneralModel extends Model<GeneralModel.Table, Object> {

    @Resource
    private GaarasonDataSource gaarasonDataSource;

    @Override
    public GaarasonDataSource getGaarasonDataSource() {
        return gaarasonDataSource;
    }

    public static class Table {

    }

}
