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
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@FixMethodOrder(MethodSorters.JVM)
@Slf4j
abstract public class BaseTests {

    protected static Map<String, Map<TABLE, String[]>>  initSqlMap = new HashMap<>();

    protected static String initSql = "";

    protected static ThreadPoolExecutor pool = new ThreadPoolExecutor(8, 8, 1L, TimeUnit.MINUTES,
        new LinkedBlockingDeque<>(100));

    static {
        // 包扫描
        System.setProperty("gaarason.database.scan.packages", "gaarason.database");

        Map<TABLE, String[]> tableSql = new HashMap<>();
        for (TABLE table : TABLE.values()) {
            String sqlFilename = Thread.currentThread().getStackTrace()[1].getClass().getResource("/").toString().replace(
                "file:", "") + "../../src/test/java/gaarason/database/test/init/mysql-"+table.name()+".sql";
            try {
                tableSql.put(table, readToString(sqlFilename).split(";\n"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        initSqlMap.put("mysql", tableSql);
    }
    @BeforeClass
    public static void beforeClass() throws IOException {

//        String sqlFilename = Thread.currentThread().getStackTrace()[1].getClass().getResource("/").toString().replace(
//            "file:", "") + "../../src/test/java/gaarason/database/test/init/mysql.sql";
//        initSql = readToString(sqlFilename);
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

    abstract protected List<TABLE> getInitTables();

    @Before
    public void before() throws SQLException, InterruptedException {
        log.debug("数据库重新初始化开始");
        initDataSourceList(getGaarasonDataSource(), getInitTables());
        log.debug("数据库重新初始化完成");
        otherAfter();
    }

    protected void otherAfter() {

    }

    // 初始化数据库连接列表
    protected void initDataSourceList(GaarasonDataSource gaarasonDataSource, List<TABLE> tables) throws SQLException, InterruptedException {
        for (DataSource dataSource : gaarasonDataSource.getMasterDataSourceList()) {
                String sqlTemp = "";
                try (Connection connection = dataSource.getConnection()) {
                    connection.setAutoCommit(false);

                    String databaseProductName = connection.getMetaData().getDatabaseProductName().toLowerCase(
                        Locale.ENGLISH);
                    Map<TABLE, String[]> stringMap = initSqlMap.get(databaseProductName);
                    log.info(" 数据库重新初始化 : {}", tables);
                    CountDownLatch countDownLatch = new CountDownLatch(tables.size());

                    for (TABLE table : tables) {
                        pool.execute(() -> {
                            String[] sqls = stringMap.get(table);
                            try{
                                for (String sql : sqls) {
                                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                                    int i = preparedStatement.executeUpdate();
                                }
                            }catch (Throwable e){
                                log.error("sql init error", e);
                            }finally {
                                countDownLatch.countDown();
                            }
                        });
                    }
                    connection.commit();
                    connection.setAutoCommit(true);
                    countDownLatch.await();
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

    public enum TABLE {
        data_type,datetime_test,null_test,people,relationship_student_teacher,student,teacher,test,comment,image,post,super_relation
    }

}
