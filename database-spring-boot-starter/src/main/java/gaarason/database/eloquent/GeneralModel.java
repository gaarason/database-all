package gaarason.database.eloquent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.logging.Log;
import gaarason.database.logging.LogFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Collection;

/**
 * 通用model
 * @author xt
 */
@Repository
public class GeneralModel extends Model<GeneralModel.Table, Serializable> {

    private static final Log log = LogFactory.getLog(GeneralModel.class);

    @Resource
    private GaarasonDataSource gaarasonDataSource;

    @Override
    public GaarasonDataSource getGaarasonDataSource() {
        return gaarasonDataSource;
    }

    public void log(String sql, Collection<?> parameterList) {
        log.debug("SQL complete : " + String.format(sql.replace(" ? ", "\"%s\""), parameterList.toArray()));
    }

    @gaarason.database.annotation.Table(name = "@@GeneralModel.Table@@")
    public static class Table implements Serializable {

    }

}
