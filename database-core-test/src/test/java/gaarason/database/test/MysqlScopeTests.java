package gaarason.database.test;

import gaarason.database.test.parent.ScopeTests;
import gaarason.database.test.utils.DatabaseTypeUtil;
import org.junit.BeforeClass;

import java.io.IOException;

public class MysqlScopeTests extends ScopeTests {

    @BeforeClass
    public static void beforeClass() throws IOException {
        DatabaseTypeUtil.setDatabaseTypeToMysql();
        String sqlFilename = Thread.currentThread().getStackTrace()[1].getClass().getResource("/").toString().replace(
            "file:", "") + "../../src/test/java/gaarason/database/test/init/mysql.sql";
        initSql = readToString(sqlFilename);
    }
}
