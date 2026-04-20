package gaarason.database.test.parent.base;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.test.utils.DatabaseTypeUtil;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 测试基类: 负责 Druid 数据源、按表并发执行初始化 SQL、以及各 {@link TABLE} 枚举对应的建表/清数脚本加载.
 *
 * @author xt
 */
@FixMethodOrder(MethodSorters.JVM)
@Slf4j
abstract public class BaseTests {

    protected static Map<String, Map<TABLE, String[]>>  initSqlMap = new HashMap<>();

    protected static String initSql = "";

    /**
     * 按表并发执行初始化脚本；每个任务单独从池取连接，禁止多表共用一个 Connection（会交错执行语句）.
     */
    protected static final ThreadPoolExecutor INIT_SQL_POOL = new ThreadPoolExecutor(8, 8, 1L, TimeUnit.MINUTES,
        new LinkedBlockingDeque<>(100));

    static {
        // 包扫描
        System.setProperty("gaarason.database.scan.packages", "gaarason.database");

        Map<TABLE, String[]> tableSql = new HashMap<>();
        for (TABLE table : TABLE.values()) {
//            String sqlFilename = Thread.currentThread().getStackTrace()[1].getClass().getResource("/").toString().replace(

//            String sqlFilename = Thread.currentThread().getStackTrace()[1].getClassName().replace(".","/") + "/../../src/test/java/gaarason/database/test/init/mysql-"+table.name()+".sql";
            try {
                String sqlFilename = BaseTests.class.getClassLoader().getResource("sql/mysql-"+table.name()+".sql").getFile();
                // Windows 下资源常为 CRLF，仅用 ";\n" 会导致整文件当成一条语句执行而初始化失败
                String[] segments = DatabaseTypeUtil.readToString(sqlFilename).split(";\\r?\\n");
                List<String> statements = new ArrayList<>(segments.length);
                for (String segment : segments) {
                    String trimmed = segment.trim();
                    if (!trimmed.isEmpty()) {
                        statements.add(trimmed);
                    }
                }
                tableSql.put(table, statements.toArray(new String[0]));
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

    /**
     * 初始化脚本多为非限定表名；池化连接可能仍停留在其它 catalog（业务测试里 executeDatabase 等）,
     * 与 {@link gaarason.database.connection.GaarasonDataSourceWrapper} 行为对齐, 先切回 URL 默认库.
     */
    private static void alignConnectionCatalogToUrlDefault(Connection connection, DataSource dataSource)
        throws SQLException {
        if (!(dataSource instanceof DruidDataSource)) {
            return;
        }
        String catalog = extractDefaultCatalogFromJdbcUrl(((DruidDataSource) dataSource).getUrl());
        if (catalog != null && !catalog.isEmpty()) {
            connection.setCatalog(catalog);
        }
    }

    private static String extractDefaultCatalogFromJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null) {
            return null;
        }
        int question = jdbcUrl.indexOf('?');
        String withoutParams = question >= 0 ? jdbcUrl.substring(0, question) : jdbcUrl;
        int schemeSlashes = withoutParams.indexOf("//");
        if (schemeSlashes < 0) {
            return null;
        }
        String authorityAndPath = withoutParams.substring(schemeSlashes + 2);
        int slash = authorityAndPath.indexOf('/');
        if (slash < 0 || slash >= authorityAndPath.length() - 1) {
            return null;
        }
        String catalog = authorityAndPath.substring(slash + 1).trim();
        return catalog.isEmpty() ? null : catalog;
    }

    // 初始化数据库连接列表（每个表对应 resources/sql/mysql-{table}.sql，由子类 getInitTables 选择；多表并行、每表独占连接）
    protected void initDataSourceList(GaarasonDataSource gaarasonDataSource, List<TABLE> tables)
        throws SQLException, InterruptedException {
        for (DataSource dataSource : gaarasonDataSource.getMasterDataSourceList()) {
            String sqlTemp = "";
            Map<TABLE, String[]> stringMap;
            try (Connection metaConnection = dataSource.getConnection()) {
                String databaseProductName = metaConnection.getMetaData().getDatabaseProductName().toLowerCase(
                    Locale.ENGLISH);
                stringMap = initSqlMap.get(databaseProductName);
                if (stringMap == null) {
                    throw new SQLRuntimeException(sqlTemp, new ArrayList<>(),
                        "unsupported database product for init sql: " + databaseProductName,
                        gaarasonDataSource.getQueryBuilder().getValueSymbol(), null);
                }
            } catch (Throwable e) {
                if (e instanceof SQLRuntimeException) {
                    throw e;
                }
                throw new SQLRuntimeException(sqlTemp, new ArrayList<>(), e.getMessage(),
                    gaarasonDataSource.getQueryBuilder().getValueSymbol(), e);
            }
            log.info(" 数据库重新初始化 : {}", tables);
            CountDownLatch latch = new CountDownLatch(tables.size());
            AtomicReference<Throwable> failure = new AtomicReference<>();
            for (TABLE table : tables) {
                String[] sqls = stringMap.get(table);
                if (sqls == null) {
                    throw new SQLRuntimeException(sqlTemp, new ArrayList<>(), "missing init sql for table " + table,
                        gaarasonDataSource.getQueryBuilder().getValueSymbol(), null);
                }
                INIT_SQL_POOL.execute(() -> {
                    try (Connection connection = dataSource.getConnection()) {
                        alignConnectionCatalogToUrlDefault(connection, dataSource);
                        connection.setAutoCommit(false);
                        try {
                            for (String sql : sqls) {
                                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                                    preparedStatement.executeUpdate();
                                }
                            }
                            connection.commit();
                        } catch (Throwable e) {
                            try {
                                connection.rollback();
                            } catch (SQLException rollbackEx) {
                                log.warn("sql init rollback failed, table={}", table, rollbackEx);
                            }
                            log.error("sql init error, table={}", table, e);
                            failure.compareAndSet(null, e);
                        } finally {
                            connection.setAutoCommit(true);
                        }
                    } catch (SQLException e) {
                        log.error("sql init open connection failed, table={}", table, e);
                        failure.compareAndSet(null, e);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
            Throwable first = failure.get();
            if (first != null) {
                throw new SQLRuntimeException(sqlTemp, new ArrayList<>(), first.getMessage(),
                    gaarasonDataSource.getQueryBuilder().getValueSymbol(), first);
            }
        }
    }

    @After
    public void after() {
        log.debug("in after");
    }

    public enum TABLE {
        data_type,datetime_test,null_test,people,relationship_student_teacher,student,teacher,test,comment,image,post,super_relation,routing_integration
    }

}
