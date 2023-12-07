package gaarason.database.test;

import gaarason.database.test.parent.ScopeTests;
import gaarason.database.test.utils.DatabaseTypeUtil;
import org.junit.BeforeClass;

import java.io.IOException;

public class MysqlScopeTests extends ScopeTests {

    @BeforeClass
    public static void beforeClass() throws IOException {
        initSql = DatabaseTypeUtil.setDatabaseTypeToMysql();
    }
}
