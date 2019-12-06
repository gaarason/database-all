package gaarason.database.test.parent;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.connections.ProxyDataSource;
import gaarason.database.eloquent.Model;
import gaarason.database.generator.Manager;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.runners.MethodSorters;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@FixMethodOrder(MethodSorters.JVM)
@Slf4j
public class BaseTests {

    @Resource
    List<DataSource> dataSourceSlaveSingleList;

    @Resource
    List<DataSource> dataSourceMasterSingleList;

    @Resource
    ProxyDataSource proxyDataSourceSingle;

    protected static String initSql = "";

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

    @Test
    public void test() {
        System.out.println("wwwwwwqqqqq test.");
    }

    protected List<DataSource> getDataSourceList() {
        return dataSourceMasterList();
    }

    // 初始化数据库连接列表
    protected void initDataSourceList(List<DataSource> dataSourceList) throws SQLException {
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

    protected static String readToString(String fileName) throws IOException {
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


    @Test
    public void run2() {
        ProxyDataSource proxyDataSource = proxyDataSource();

        ToolModel toolModel = new ToolModel(proxyDataSource);
    }

    private DataSource dataSourceMaster0() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(
            "jdbc:mysql://sakya.local/test_master_0?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai");
        druidDataSource.setDbType("com.alibaba.druid.pool.DruidDataSource");
        druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("root");
        return druidDataSource;
    }

    private List<DataSource> dataSourceMasterList() {
        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(dataSourceMaster0());
        return dataSources;
    }

    private ProxyDataSource proxyDataSource() {
        List<DataSource> dataSources = dataSourceMasterList();
        return new ProxyDataSource(dataSources);
    }

    public static class ToolModel extends Model<ToolModel.Inner> {
        private ProxyDataSource proxyDataSource;

        public ToolModel(ProxyDataSource dataSource) {
            proxyDataSource = dataSource;
        }

        public ProxyDataSource getProxyDataSource() {
            return proxyDataSource;
        }

        public static class Inner {

        }
    }

}
