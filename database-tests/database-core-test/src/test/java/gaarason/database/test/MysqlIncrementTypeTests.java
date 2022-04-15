package gaarason.database.test;

import gaarason.database.test.parent.IncrementTypeTests;
import gaarason.database.test.utils.DatabaseTypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class MysqlIncrementTypeTests extends IncrementTypeTests {

    @BeforeClass
    public static void beforeClass() throws IOException {
        DatabaseTypeUtil.setDatabaseTypeToMysql();
        String sqlFilename = Thread.currentThread().getStackTrace()[1].getClass().getResource("/").toString().replace(
            "file:", "") + "../../src/test/java/gaarason/database/test/init/mysql.sql";
        initSql = readToString(sqlFilename);
    }

    @Test
    public void insertEmpty(){
        Long aLong = peopleModel.newQuery().executeGetId("insert into people values ()", new ArrayList<>());
        Assert.assertNotNull(aLong);
        Assert.assertEquals(20, aLong.intValue());

    }
}
