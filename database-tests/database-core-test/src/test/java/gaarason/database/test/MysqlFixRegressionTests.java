package gaarason.database.test;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.annotation.BelongsTo;
import gaarason.database.annotation.Column;
import gaarason.database.annotation.HasOneOrMany;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import gaarason.database.appointment.SqlType;
import gaarason.database.connection.GaarasonDataSourceBuilder;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.eloquent.Model;
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
import java.util.Collections;
import java.util.List;

/**
 * 修复项的 MySQL 真实库回归测试.
 * <p>
 * 与 {@link CoreFixRegressionTests}(JDBC 桩, 只断言 SQL 形状与绑定参数) 互补: 这里验证生成的 SQL 真的被
 * MySQL 接受, 且数据真的按预期加载/更新. 依赖本机 MySQL(3306, root/root, 库 test_master_0).
 */
@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class MysqlFixRegressionTests {

    /**
     * 捕获每次下发 SQL 的绑定参数
     */
    private static final List<List<Object>> CAPTURED_PARAMETERS = Collections.synchronizedList(new ArrayList<>());

    private static final DruidDataSource DRUID;

    private static final GaarasonDataSource GAARASON_DATA_SOURCE;

    static {
        // 包扫描, 与其他测试基类保持一致, 避免回退实例化 QueryBuilderConfig 接口
        System.setProperty("gaarason.database.scan.packages", "gaarason.database");

        DRUID = new DruidDataSource();
        DRUID.setUrl("jdbc:mysql://127.0.0.1:3306/test_master_0?useUnicode=true&characterEncoding=utf-8"
            + "&useSSL=false&allowPublicKeyRetrieval=true&zeroDateTimeBehavior=convertToNull"
            + "&serverTimezone=Asia/Shanghai&useServerPrepStmts=true");
        DRUID.setDriverClassName("com.mysql.cj.jdbc.Driver");
        DRUID.setUsername("root");
        DRUID.setPassword("root");
        DRUID.setInitialSize(2);
        DRUID.setMinIdle(2);
        DRUID.setMaxActive(5);
        DRUID.setTestOnBorrow(false);
        DRUID.setTestOnReturn(false);
        // 语句由框架自身负责关闭, 池不参与缓存, 便于观测服务端语句数量
        DRUID.setPoolPreparedStatements(false);
        DRUID.setMaxPoolPreparedStatementPerConnectionSize(-1);
        GAARASON_DATA_SOURCE = GaarasonDataSourceBuilder.build(DRUID);
        GAARASON_DATA_SOURCE.getContainer().signUpIdentification("mysql-fix-regression");
    }

    @BeforeClass
    public static void createTables() throws SQLException {
        execute(Arrays.asList(
            "drop table if exists gaarason_mysql_fix_teacher",
            "drop table if exists gaarason_mysql_fix_son",
            "create table gaarason_mysql_fix_teacher (tid int not null auto_increment primary key, name varchar(20)) "
                + "engine InnoDB default charset utf8mb4",
            "create table gaarason_mysql_fix_son (sid int not null auto_increment primary key, fix_teacher_id int, "
                + "name varchar(20)) engine InnoDB default charset utf8mb4"));
    }

    @Before
    public void seedData() throws SQLException {
        CAPTURED_PARAMETERS.clear();
        execute(Arrays.asList(
            "truncate table gaarason_mysql_fix_son",
            "truncate table gaarason_mysql_fix_teacher",
            "insert into gaarason_mysql_fix_teacher (tid, name) values (1, 'teacher-1'), (2, 'teacher-2')",
            "insert into gaarason_mysql_fix_son (sid, fix_teacher_id, name) values (1, 1, 'son-1'), (2, 1, 'son-2'), "
                + "(3, 2, 'son-3'), (4, null, 'son-4')"));
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

    // ----------------------------- 空集合 in() ----------------------------- //

    /**
     * 空集合的 whereIn 必须能被 MySQL 接受, 且查不到任何数据
     */
    @Test
    public void 空集合whereIn在真实库可执行() {
        SonEntity.SonModel sonModel = new SonEntity.SonModel();
        String sql = sonModel.newQuery().whereIn("fix_teacher_id", Collections.emptyList()).toSql(SqlType.SELECT);
        log.info("empty whereIn sql: {}", sql);

        Assert.assertFalse("不应生成空的 in 列表, sql: " + sql, sql.replace(" ", "").contains("in()"));
        Assert.assertEquals(0, sonModel.newQuery().whereIn("fix_teacher_id", Collections.emptyList()).get().size());
    }

    /**
     * 空集合的 whereNotIn 必须能被 MySQL 接受, 且不排除任何数据
     */
    @Test
    public void 空集合whereNotIn在真实库可执行() {
        SonEntity.SonModel sonModel = new SonEntity.SonModel();
        String sql = sonModel.newQuery().whereNotIn("fix_teacher_id", new ArrayList<>()).toSql(SqlType.SELECT);
        log.info("empty whereNotIn sql: {}", sql);

        Assert.assertFalse("不应生成空的 not in 列表, sql: " + sql, sql.replace(" ", "").contains("notin()"));
        // 外键为 null 的行同样不被 `not in` 排除, 但 MySQL 中 null 不满足任何比较, 因此这里以非空外键行数断言
        Assert.assertEquals(3, sonModel.newQuery().whereNotNull("fix_teacher_id")
            .whereNotIn("fix_teacher_id", new ArrayList<>()).get().size());
    }

    // ----------------------------- HasOneOrMany 本地键 ----------------------------- //

    /**
     * 父子表主键列名不同且未声明 localModelLocalKey 时, 急切加载必须真的取到子记录
     */
    @Test
    public void 一对多急切加载按本表主键取数() {
        TeacherEntity.TeacherModel teacherModel = new TeacherEntity.TeacherModel();
        RecordList<TeacherEntity, Integer> teachers = teacherModel.newQuery().with("sons").orderBy("tid").get();

        Assert.assertEquals(2, teachers.size());
        List<Integer> firstSonIds = new ArrayList<>();
        for (SonEntity sonEntity : teachers.get(0).toObject().getSons()) {
            firstSonIds.add(sonEntity.getSid());
        }
        Collections.sort(firstSonIds);
        Assert.assertEquals(Arrays.asList(1, 2), firstSonIds);

        List<Integer> secondSonIds = new ArrayList<>();
        for (SonEntity sonEntity : teachers.get(1).toObject().getSons()) {
            secondSonIds.add(sonEntity.getSid());
        }
        Assert.assertEquals(Collections.singletonList(3), secondSonIds);
    }

    // ----------------------------- BelongsTo 主键绑定 ----------------------------- //

    /**
     * 解除关系: 主键以原始类型下发, 且数据真的被解除
     */
    @Test
    public void 解除关系绑定主键原始值并生效() {
        SonEntity.SonModel sonModel = new SonEntity.SonModel();
        Record<SonEntity, Integer> son = sonModel.newQuery().findOrFail(1);

        CAPTURED_PARAMETERS.clear();
        Assert.assertEquals(1, son.bind("teacher").detach());

        boolean boundAsNumber = false;
        boolean boundAsString = false;
        for (List<Object> parameters : CAPTURED_PARAMETERS) {
            for (Object parameter : parameters) {
                if (Integer.valueOf(1).equals(parameter)) {
                    boundAsNumber = true;
                }
                if ("1".equals(parameter)) {
                    boundAsString = true;
                }
            }
        }
        Assert.assertTrue("主键应以数字下发, 捕获参数: " + CAPTURED_PARAMETERS, boundAsNumber);
        Assert.assertFalse("主键不应被字符串化下发, 捕获参数: " + CAPTURED_PARAMETERS, boundAsString);

        // 解除后外键写为字段缺省值(现有框架语义), 关系不再指向 tid=1
        Record<SonEntity, Integer> reloaded = sonModel.newQuery().with("teacher").findOrFail(1);
        Assert.assertNull("关系未解除: " + reloaded.toObject(), reloaded.toObject().getTeacher());
    }

    /**
     * 外键为空时解除关系属于无事可做, 返回 0 而不是抛异常
     */
    @Test
    public void 外键为空时解除关系返回零() {
        SonEntity.SonModel sonModel = new SonEntity.SonModel();
        Record<SonEntity, Integer> son = sonModel.newQuery().findOrFail(4);
        Assert.assertNull(son.toObject().getFixTeacherId());

        Assert.assertEquals(0, son.bind("teacher").detach());
    }

    /**
     * 建立关系: 本表主键以原始类型下发, 且关系真的被改绑
     */
    @Test
    public void 建立关系绑定主键原始值并生效() {
        SonEntity.SonModel sonModel = new SonEntity.SonModel();
        // sid=3 当前绑定 tid=2, 改绑到 tid=1
        Record<SonEntity, Integer> son = sonModel.newQuery().findOrFail(3);
        Assert.assertEquals(Integer.valueOf(2), son.toObject().getFixTeacherId());

        CAPTURED_PARAMETERS.clear();
        Assert.assertEquals(1, son.bind("teacher").attach(1));

        boolean boundAsString = false;
        for (List<Object> parameters : CAPTURED_PARAMETERS) {
            if (parameters.contains("3")) {
                boundAsString = true;
            }
        }
        Assert.assertFalse("本表主键不应被字符串化下发, 捕获参数: " + CAPTURED_PARAMETERS, boundAsString);

        Record<SonEntity, Integer> reloaded = sonModel.newQuery().with("teacher").findOrFail(3);
        Assert.assertNotNull("关系未建立", reloaded.toObject().getTeacher());
        Assert.assertEquals(Integer.valueOf(1), reloaded.toObject().getTeacher().getTid());
    }

    // ----------------------------- 实体与模型 ----------------------------- //

    /**
     * 本地表主键列名为 tid, 与子表主键列名 sid 不同
     */
    @Data
    @Table(name = "gaarason_mysql_fix_teacher")
    public static class TeacherEntity implements Serializable {

        @Primary
        @Column(name = "tid")
        private Integer tid;

        @Column(name = "name")
        private String name;

        /**
         * 故意不声明 localModelLocalKey, 走缺省分支
         */
        @HasOneOrMany(sonModelForeignKey = "fix_teacher_id")
        private List<SonEntity> sons;

        public static class TeacherModel extends Model<MySqlBuilderV2<TeacherEntity, Integer>, TeacherEntity,
            Integer> {

            @Override
            public GaarasonDataSource getGaarasonDataSource() {
                return GAARASON_DATA_SOURCE;
            }

            @Override
            public void log(String sql, Collection<?> parameterList) {
                CAPTURED_PARAMETERS.add(new ArrayList<>(parameterList));
            }
        }
    }

    @Data
    @Table(name = "gaarason_mysql_fix_son")
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
                CAPTURED_PARAMETERS.add(new ArrayList<>(parameterList));
            }
        }
    }
}
