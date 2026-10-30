package gaarason.database.test;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.annotation.BelongsTo;
import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import gaarason.database.appointment.SqlType;
import gaarason.database.connection.GaarasonDataSourceBuilder;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.eloquent.Model;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.test.config.MySqlBuilderV2;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 疑似缺陷登记测试(未修复, 与 {@link MysqlFixRegressionTests} 区分).
 * <p>
 * 每个用例都固化"当前真实行为", 因此本类可以常跑: 一旦有人改动导致行为漂移, 这里会立刻变红, 届时应当同时更新
 * 断言与注释里的期望行为(而不是简单把断言改回旧值)。注释中标注的 `期望` 为修复方向。
 * <p>
 * 依赖本机 MySQL(3306, 库 test_master_0)。
 */
@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class MysqlPendingDefectTests {

    private static final DruidDataSource DRUID;

    private static final GaarasonDataSource GAARASON_DATA_SOURCE;

    static {
        // 包扫描, 与其他测试基类保持一致, 避免回退实例化 QueryBuilderConfig 接口
        System.setProperty("gaarason.database.scan.packages", "gaarason.database");

        DRUID = new DruidDataSource();
        DRUID.setUrl("jdbc:mysql://127.0.0.1:3306/test_master_0?useUnicode=true&characterEncoding=utf-8"
            + "&useSSL=false&allowPublicKeyRetrieval=true&zeroDateTimeBehavior=convertToNull"
            + "&serverTimezone=Asia/Shanghai");
        DRUID.setDriverClassName("com.mysql.cj.jdbc.Driver");
        DRUID.setUsername("root");
        DRUID.setPassword("root");
        DRUID.setInitialSize(2);
        DRUID.setMinIdle(2);
        DRUID.setMaxActive(5);
        DRUID.setTestOnBorrow(false);
        DRUID.setTestOnReturn(false);
        DRUID.setPoolPreparedStatements(false);
        DRUID.setMaxPoolPreparedStatementPerConnectionSize(-1);
        GAARASON_DATA_SOURCE = GaarasonDataSourceBuilder.build(DRUID);
        GAARASON_DATA_SOURCE.getContainer().signUpIdentification("mysql-pending-defect");
    }

    @BeforeClass
    public static void createTables() throws SQLException {
        execute(Arrays.asList(
            "drop table if exists gaarason_mysql_pending_son",
            "drop table if exists gaarason_mysql_pending_teacher",
            "create table gaarason_mysql_pending_teacher (tid int not null auto_increment primary key, "
                + "name varchar(20)) engine InnoDB default charset utf8mb4",
            "create table gaarason_mysql_pending_son (sid int not null auto_increment primary key, "
                + "fix_teacher_id int null, name varchar(20)) engine InnoDB default charset utf8mb4"));
    }

    @Before
    public void seedData() throws SQLException {
        execute(Arrays.asList(
            "truncate table gaarason_mysql_pending_son",
            "truncate table gaarason_mysql_pending_teacher",
            "insert into gaarason_mysql_pending_teacher (tid, name) values (1, 'teacher-1'), (2, 'teacher-2')",
            "insert into gaarason_mysql_pending_son (sid, fix_teacher_id, name) values (1, 1, 'son-1'), "
                + "(2, 1, 'son-2'), (3, 2, 'son-3'), (4, null, 'son-4')"));
    }

    private static void execute(List<String> statements) throws SQLException {
        try (Connection connection = DRUID.getConnection()) {
            for (String statement : statements) {
                try (Statement handler = connection.createStatement()) {
                    handler.execute(statement);
                }
            }
        }
    }

    // ----------------------------- INSERT 的列名来自 column, select 被丢弃 ----------------------------- //

    /**
     * INSERT 只编排 TABLE/COLUMN/VALUE/LAST 片段, `select()` 落入的 SELECT 片段被静默丢弃, 于是生成缺列名的语句.
     * <p>
     * 期望: 要么 select 参与 INSERT 列名, 要么在构造阶段就拒绝 "有值无列" 的写法.
     */
    @Test
    public void 插入时select片段被静默丢弃() {
        SonEntity.SonModel sonModel = new SonEntity.SonModel();
        List<Object> values = new ArrayList<>();
        values.add("son-x");

        // toSql(SqlType) 会把参数内联展示, 这里用闭包捕获带占位符的原始 SQL
        final String[] rawSql = new String[1];
        sonModel.newQuery().select("name").value(values).toSql(SqlType.INSERT, (sql, parameterList) -> {
            rawSql[0] = sql;
            return sql;
        });
        log.info("select+value insert sql: {}", rawSql[0]);

        // 当前行为: 列名缺失, 只剩 values
        Assert.assertTrue("当前行为: 只生成了值片段, sql: " + rawSql[0],
            rawSql[0].replace(" ", "").contains("values(?)"));
        Assert.assertFalse("当前行为: select 的列没有出现在 INSERT 中, sql: " + rawSql[0],
            rawSql[0].replace(" ", "").contains("`name`"));

        // 当前行为: 该语句发到数据库才报 "Column count doesn't match value count at row 1"
        try {
            sonModel.newQuery().select("name").value(values).insertGetId();
            Assert.fail("当前行为: 缺列名的语句本应被数据库拒绝");
        } catch (SQLRuntimeException e) {
            log.info("database rejected it: {}", e.getMessage());
            Assert.assertTrue("应为列数不匹配的错误: " + e.getMessage(),
                e.getMessage().contains("Column count doesn't match value count"));
        }
    }

    /**
     * 对照: 用 `column()` 指定列名才是 INSERT 的正确写法(Druid 用例即如此), 生成的 SQL 带列名且能插入.
     */
    @Test
    public void 插入时column片段正常生成列名() {
        SonEntity.SonModel sonModel = new SonEntity.SonModel();
        List<Object> values = new ArrayList<>();
        values.add("son-x");

        String sql = sonModel.newQuery().column("name").value(values).toSql(SqlType.INSERT);
        log.info("column+value insert sql: {}", sql);
        Assert.assertTrue("应含列名片段, sql: " + sql, sql.contains("`name`"));

        Assert.assertNotNull(sonModel.newQuery().column("name").value(values).insertGetId());
        Assert.assertEquals(1, sonModel.newQuery().where("name", "son-x").get().size());
    }

    /**
     * 列数与值数不匹配时框架不做任何校验, 错误延迟到数据库.
     * <p>
     * 期望: 构造阶段即抛出参数不匹配的框架异常.
     */
    @Test
    public void 列数与值数不匹配不在构造阶段校验() {
        SonEntity.SonModel sonModel = new SonEntity.SonModel();

        final String[] rawSql = new String[1];
        final Collection<?>[] capturedParameters = new Collection<?>[1];
        sonModel.newQuery().column("name").value(Arrays.asList("a", "b")).toSql(SqlType.INSERT,
            (sql, parameterList) -> {
                rawSql[0] = sql;
                capturedParameters[0] = parameterList;
                return sql;
            });
        log.info("mismatch sql: {}, parameters: {}", rawSql[0], capturedParameters[0]);
        // 当前行为: 1 列 2 值的语句被原样生成, 框架不拦截
        Assert.assertTrue("当前行为: 列数与值数不一致仍生成 SQL, sql: " + rawSql[0],
            rawSql[0].replace(" ", "").contains("(`name`)") && rawSql[0].replace(" ", "").contains("values(?,?)"));
        Assert.assertEquals("当前行为: 两个值照常绑定", 2, capturedParameters[0].size());

        try {
            sonModel.newQuery().column("name").value(Arrays.asList("a", "b")).insertGetId();
            Assert.fail("当前行为: 数据库才会发现列数与值数不匹配");
        } catch (SQLRuntimeException e) {
            log.info("database rejected it: {}", e.getMessage());
            Assert.assertTrue("应为列数不匹配的错误: " + e.getMessage(),
                e.getMessage().contains("Column count doesn't match value count"));
        }
    }

    // ----------------------------- BelongsTo attach 外键为 NULL ----------------------------- //

    /**
     * attach 的更新条件含 `fk != ?`, 而 `NULL != ?` 恒为 UNKNOWN, 导致 "从无关系建立关系" 这条路径不生效.
     * <p>
     * 期望: 外键为 NULL 时也应建立关系(条件应为 `fk is null or fk != ?`).
     */
    @Test
    public void 外键为空时attach不生效() {
        SonEntity.SonModel sonModel = new SonEntity.SonModel();
        Record<SonEntity, Integer> son = sonModel.newQuery().findOrFail(4);
        Assert.assertNull(son.toObject().getFixTeacherId());

        // 当前行为: 返回 0, 关系没建立
        Assert.assertEquals("当前行为: NULL 外键无法 attach", 0, son.bind("teacher").attach(2));
        Assert.assertNull(sonModel.newQuery().with("teacher").findOrFail(4).toObject().getTeacher());

        // 对照: 已有关系(非 NULL)时改绑正常
        Record<SonEntity, Integer> bound = sonModel.newQuery().findOrFail(3);
        Assert.assertEquals(1, bound.bind("teacher").attach(1));
        Assert.assertEquals(Integer.valueOf(1),
            sonModel.newQuery().with("teacher").findOrFail(3).toObject().getTeacher().getTid());
    }

    // ----------------------------- BelongsTo detach 的解除值 ----------------------------- //

    /**
     * detach 把外键写成 "字段缺省值"(Integer → 0), 而不是 NULL, 于是留下悬挂外键 0.
     * <p>
     * 期望: 可空列应写 NULL; 若确认保留当前语义, 应在文档中明确 "解除即回落到字段缺省值".
     */
    @Test
    public void 解除关系把外键写为字段缺省值而非NULL() {
        SonEntity.SonModel sonModel = new SonEntity.SonModel();
        Record<SonEntity, Integer> son = sonModel.newQuery().findOrFail(1);
        Assert.assertEquals(Integer.valueOf(1), son.toObject().getFixTeacherId());

        Assert.assertEquals(1, son.bind("teacher").detach());

        Integer foreignKeyValue = sonModel.newQuery().findOrFail(1).toObject().getFixTeacherId();
        log.info("detach left fix_teacher_id = {}", foreignKeyValue);
        // 当前行为: 0(字段缺省值), 而非 NULL
        Assert.assertEquals("当前行为: 外键被写为字段缺省值 0", Integer.valueOf(0), foreignKeyValue);
        // 关系不再指向 tid=1
        Assert.assertNull(sonModel.newQuery().with("teacher").findOrFail(1).toObject().getTeacher());
    }

    // ----------------------------- 实体与模型 ----------------------------- //

    @Data
    @Table(name = "gaarason_mysql_pending_teacher")
    public static class TeacherEntity implements Serializable {

        @Primary
        @Column(name = "tid")
        private Integer tid;

        @Column(name = "name")
        private String name;

        public static class TeacherModel extends Model<MySqlBuilderV2<TeacherEntity, Integer>, TeacherEntity,
            Integer> {

            @Override
            public GaarasonDataSource getGaarasonDataSource() {
                return GAARASON_DATA_SOURCE;
            }
        }
    }

    @Data
    @Table(name = "gaarason_mysql_pending_son")
    public static class SonEntity implements Serializable {

        @Primary
        @Column(name = "sid")
        private Integer sid;

        @Column(name = "fix_teacher_id")
        private Integer fixTeacherId;

        @Column(name = "name")
        private String name;

        @BelongsTo(localModelForeignKey = "fix_teacher_id")
        private TeacherEntity teacher;

        public static class SonModel extends Model<MySqlBuilderV2<SonEntity, Integer>, SonEntity, Integer> {

            @Override
            public GaarasonDataSource getGaarasonDataSource() {
                return GAARASON_DATA_SOURCE;
            }

            @Override
            public void log(String sql, Collection<?> parameterList) {
                log.info("sql: {} {}", sql, parameterList);
            }
        }
    }
}
