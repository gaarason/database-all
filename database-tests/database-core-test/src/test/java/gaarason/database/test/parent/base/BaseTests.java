package gaarason.database.test.parent.base;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.exception.SQLRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

@FixMethodOrder(MethodSorters.JVM)
@Slf4j
abstract public class BaseTests {

    protected static String initSql = "";

    @BeforeClass
    public static void beforeClass() throws IOException {
        String sqlFilename = Thread.currentThread().getStackTrace()[1].getClass().getResource("/").toString().replace(
            "file:", "") + "../../src/test/java/gaarason/database/test/init/mysql.sql";
        initSql = readToString(sqlFilename);
    }
//    abstract protected List<DataSource> getDataSourceList();

//    abstract protected void setDatabaseType();
//
//    abstract protected String initSqlFileName();

//    abstract protected void initModel();

    protected static String readToString(String fileName) throws IOException {
        String encoding = "UTF-8";
        File file = new File(fileName);
        file.setReadable(true);
        Long fileLength = file.length();
        byte[] fileContent = new byte[fileLength.intValue()];
        FileInputStream in = new FileInputStream(file);
        in.read(fileContent);
        in.close();
        return new String(fileContent, encoding);
    }

    @AfterClass
    public static void afterClass() {
        log.debug("in after class");
    }

    abstract protected GaarasonDataSource getGaarasonDataSource();

    @Before
    public void before() throws SQLException {
        log.debug("数据库重新初始化开始");
        initDataSourceList(getGaarasonDataSource());
        log.debug("数据库重新初始化完成");
        otherAfter();
    }

    protected void otherAfter() {

    }

    // 初始化数据库连接列表
    protected void initDataSourceList(GaarasonDataSource gaarasonDataSource) throws SQLException {
        String[] split = initSql.split(";\n");
        String sqlTemp = "";
        for (DataSource dataSource : gaarasonDataSource.getMasterDataSourceList()) {

            try (Connection connection = dataSource.getConnection()) {
                for (String sql : split) {
                    sqlTemp = sql;
//                    System.out.println(sql);
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    int i = preparedStatement.executeUpdate();
//                    System.out.println(i);
                }
            } catch (Throwable e) {
                throw new SQLRuntimeException(sqlTemp, new ArrayList<>(), e.getMessage(),
                    gaarasonDataSource.getQueryBuilder().getValueSymbol(), e);
            }
        }
    }

    @After
    public void after() {
        log.debug("in after");
    }

}
