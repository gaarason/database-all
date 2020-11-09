package gaarason.database.test.parent.base;

import gaarason.database.test.utils.DatabaseTypeUtil;
import org.junit.BeforeClass;

import java.io.IOException;

abstract public class MysqlBaseTests extends BaseTests {

//    @BeforeClass
//    public void beforeClass() throws IOException {
//        DatabaseTypeUtil.setDatabaseTypeToMysql();
//        String sqlFilename = Thread.currentThread().getStackTrace()[1].getClass().getResource("/").toString().replace(
//            "file:", "") + "../../src/test/java/gaarason/database/test/init/mysql.sql";
//        initSql = readToString(sqlFilename);
//    }
}
