package gaarason.database.test.parent;

import gaarason.database.connections.ProxyDataSource;
import gaarason.database.test.models.StudentModel;
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
import java.util.List;

@FixMethodOrder(MethodSorters.JVM)
@Slf4j
public class BaseTests {

    private static String initSql = "";

    protected static StudentModel studentModel = new StudentModel();

    @BeforeClass
    public static void beforeClass() throws IOException {
        String sqlFilename = Thread.currentThread().getStackTrace()[1].getClass().getResource("/").toString().replace(
            "file:", "") + "../../src/test/java/gaarason/database/test/init/Init.sql";
        initSql = readToString(sqlFilename);
    }

    @Before
    public void before() throws SQLException {
        log.debug("数据库重新初始化开始");
        List<DataSource> dataSourceList = getDataSourceList();
        initDataSourceList(dataSourceList);
        log.debug("数据库重新初始化完成");
    }

    private List<DataSource> getDataSourceList() {
        ProxyDataSource proxyDataSource = studentModel.getProxyDataSource();
        return proxyDataSource.getMasterDataSourceList();
    }

    // 初始化数据库连接列表
    private void initDataSourceList(List<DataSource> dataSourceList) throws SQLException {
        String[] split = initSql.split(";\n");
        for (DataSource dataSource : dataSourceList) {
            Connection connection = dataSource.getConnection();
            for (String sql : split) {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                int               i                 = preparedStatement.executeUpdate();
            }
            connection.close();
        }
    }

    private static String readToString(String fileName) throws IOException {
        String encoding = "UTF-8";
        File   file     = new File(fileName);
        file.setReadable(true);
        Long            fileLength  = file.length();
        byte[]          fileContent = new byte[fileLength.intValue()];
        FileInputStream in          = new FileInputStream(file);
        in.read(fileContent);
        in.close();
        return new String(fileContent, encoding);
    }

    @After
    public void after() {
        log.debug("in after");
    }

    @AfterClass
    public static void afterClass() {
        log.debug("in after class");
    }

}
