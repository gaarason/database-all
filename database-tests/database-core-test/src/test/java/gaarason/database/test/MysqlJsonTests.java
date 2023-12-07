package gaarason.database.test;

import gaarason.database.test.parent.JsonTests;
import gaarason.database.test.utils.DatabaseTypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.io.IOException;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class MysqlJsonTests extends JsonTests {

    @BeforeClass
    public static void beforeClass() throws IOException {
        initSql = DatabaseTypeUtil.setDatabaseTypeToMysql();
    }
}
