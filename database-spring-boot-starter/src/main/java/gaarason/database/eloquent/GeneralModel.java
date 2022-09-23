package gaarason.database.eloquent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.logging.Log;
import gaarason.database.logging.LogFactory;
import gaarason.database.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;

/**
 * 通用model
 * @author xt
 */
@Component
public class GeneralModel extends Model<GeneralModel.Table, Serializable> {

    private static final Log log = LogFactory.getLog(GeneralModel.class);

    @Autowired
    private GaarasonDataSource gaarasonDataSource;

    @Override
    public GaarasonDataSource getGaarasonDataSource() {
        return gaarasonDataSource;
    }

    public void log(String sql, Collection<?> parameterList) {
        if (log.isDebugEnabled()) {
            log.debug(
                "SQL complete : " + String.format(StringUtils.replace(sql, " ? ", "\"%s\""), parameterList.toArray()));
        }
    }

    @gaarason.database.annotation.Table(name = "@@GeneralModel.Table@@")
    public static class Table implements Serializable {

    }

}
