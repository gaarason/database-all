package gaarason.database.test;

import gaarason.database.annotation.BelongsTo;
import gaarason.database.annotation.BelongsToMany;
import gaarason.database.annotation.Column;
import gaarason.database.annotation.HasOneOrMany;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import gaarason.database.appointment.SqlType;
import gaarason.database.bootstrap.ContainerBootstrap;
import gaarason.database.connection.GaarasonDataSourceBuilder;
import gaarason.database.connection.GaarasonDataSourceContext;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.BuilderAnyWrapper;
import gaarason.database.contract.function.RecordWrapper;
import gaarason.database.contract.query.Grammar;
import gaarason.database.core.Container;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.RecordBean;
import gaarason.database.eloquent.relation.BelongsToManyQueryRelation;
import gaarason.database.eloquent.relation.HasOneOrManyQueryRelation;
import gaarason.database.exception.CloneNotSupportedRuntimeException;
import gaarason.database.provider.ContainerProvider;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.FieldRelationMember;
import gaarason.database.test.config.MySqlBuilderV2;
import gaarason.database.test.stub.JdbcStub;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 构造器/关系层/执行层缺陷回归测试(纯单测, 使用 JDBC 桩, 不依赖数据库).
 * <p>
 * 覆盖: 空集合 in()、HasOneOrMany 本地键缺省取值、BelongsTo 主键绑定与空外键、多对多关系操作键值拼接、空构造器合并、深度复制关系映射、容器实例列表可变性与并发、预处理语句未关闭、异步执行的跨线程路由上下文、异步线程池与连接池的生命周期.
 */
@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class CoreFixRegressionTests {

    private static final JdbcStub JDBC_STUB;

    private static final GaarasonDataSource GAARASON_DATA_SOURCE;

    static {
        System.setProperty("gaarason.database.scan.packages", "gaarason.database");

        JDBC_STUB = new JdbcStub();
        GAARASON_DATA_SOURCE = GaarasonDataSourceBuilder.build(JDBC_STUB);
        GAARASON_DATA_SOURCE.getContainer().signUpIdentification("core-fix-regression");
    }

    @Before
    public void resetStub() {
        JDBC_STUB.reset();
    }

    /**
     * 统计占位符数量
     */
    private static int placeholderCount(String sql) {
        int count = 0;
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?') {
                count++;
            }
        }
        return count;
    }

    // ----------------------------- 空集合 in() ----------------------------- //

    /**
     * 空集合的 whereIn 不能生成 `in ()`(语法错误), 应退化为恒假条件
     */
    @Test
    public void 空集合whereIn不应生成空的in列表() {
        String sql = new SonEntity.SonModel().newQuery().whereIn("fix_teacher_id", Collections.emptyList())
            .toSql(SqlType.SELECT);
        log.info("empty whereIn sql: {}", sql);

        Assert.assertFalse("不应生成空的 in 列表, sql: " + sql, sql.replace(" ", "").contains("in()"));
    }

    /**
     * 空集合的 whereNotIn 不能生成 `not in ()`(语法错误), 应退化为恒真条件
     */
    @Test
    public void 空集合whereNotIn不应生成空的notin列表() {
        String sql = new SonEntity.SonModel().newQuery().whereNotIn("fix_teacher_id", new ArrayList<>())
            .toSql(SqlType.SELECT);
        log.info("empty whereNotIn sql: {}", sql);

        Assert.assertFalse("不应生成空的 not in 列表, sql: " + sql, sql.replace(" ", "").contains("notin()"));
    }

    /**
     * 空集合的 whereIn 依然可以下发, 且占位符与绑定参数一一对应
     */
    @Test
    public void 空集合whereIn可以正常执行() {
        SonEntity.SonModel sonModel = new SonEntity.SonModel();
        RecordList<SonEntity, Integer> sons = sonModel.newQuery().whereIn("fix_teacher_id", Collections.emptyList())
            .get();

        List<JdbcStub.StubStatement> selects = JDBC_STUB.statementsStartingWith("select");
        Assert.assertEquals(1, selects.size());
        JdbcStub.StubStatement statement = selects.get(0);
        Assert.assertEquals("占位符与绑定参数不一致, sql: " + statement.sql, placeholderCount(statement.sql),
            statement.parameterList().size());
        Assert.assertTrue("空集合应查不到数据, 实际: " + statement, sons.isEmpty());
    }

    /**
     * 非空集合的 whereIn 保持原有语义
     */
    @Test
    public void 非空集合whereIn照常绑定参数() {
        JDBC_STUB.enqueueRows(JdbcStub.row("sid", 1, "fix_teacher_id", 1, "name", "son-1"));
        SonEntity.SonModel sonModel = new SonEntity.SonModel();
        sonModel.newQuery().whereIn("fix_teacher_id", Arrays.asList(1, 2)).get();

        List<JdbcStub.StubStatement> selects = JDBC_STUB.statementsStartingWith("select");
        Assert.assertEquals(1, selects.size());
        Assert.assertTrue("sql: " + selects.get(0).sql, selects.get(0).sql.replace(" ", "").contains("in(?,?)"));
        Assert.assertEquals(Arrays.asList(1, 2), selects.get(0).parameterList());
    }

    // ----------------------------- HasOneOrMany 本地键 ----------------------------- //

    /**
     * 一对多关系未显式声明 localModelLocalKey 时, 缺省键必须取本表(父表)主键, 而不是子表主键
     */
    @Test
    public void 一对多本地键缺省取本表主键() {
        HasOneOrManyQueryRelation relation = sonsRelation();

        Assert.assertEquals("本表主键列名为 tid, 缺省本地键应取它", "tid",
            relation.hasOneOrManyTemplate.localModelLocalKey);
        Assert.assertEquals("tid", relation.localKeyForWhereHasIn());
    }

    /**
     * 缺省本地键取值错误时, 急切加载会把关系结果全部丢弃
     */
    @Test
    public void 一对多按本表主键筛选关系记录() {
        HasOneOrManyQueryRelation relation = sonsRelation();

        JDBC_STUB.enqueueRows(
            JdbcStub.row("sid", 1, "fix_teacher_id", 1, "name", "son-1"),
            JdbcStub.row("sid", 2, "fix_teacher_id", 1, "name", "son-2"),
            JdbcStub.row("sid", 3, "fix_teacher_id", 2, "name", "son-3"));
        RecordList<SonEntity, Integer> sons = new SonEntity.SonModel().newQuery().get();

        Record<TeacherEntity, Integer> teacher = new RecordBean<>(new TeacherEntity.TeacherModel(),
            JdbcStub.row("tid", 1, "name", "teacher-1"), "");

        Map<String, Object> matched = relation.filterBatchRecordByRelationOperation(teacher, sons);
        Assert.assertEquals("应匹配到 sid=1 的子记录, 实际: " + matched, 1, matched.get("sid"));
    }

    /**
     * 关系子查询的本地键取值集合, 来自本表主键列
     */
    @Test
    public void 一对多关系条件使用本表主键列() {
        HasOneOrManyQueryRelation relation = sonsRelation();

        List<Map<String, Object>> metadata = new ArrayList<>();
        metadata.add(JdbcStub.row("tid", 1, "name", "teacher-1"));
        metadata.add(JdbcStub.row("tid", 2, "name", "teacher-2"));

        List<String> rawSql = new ArrayList<>();
        List<Object> boundParameters = new ArrayList<>();
        relation.prepareTargetBuilder(metadata, null, BuilderAnyWrapper.empty(), BuilderAnyWrapper.empty())
            .toSql(SqlType.SELECT, (sql, parameters) -> {
                rawSql.add(sql);
                boundParameters.addAll(parameters);
                return sql;
            });
        String sql = rawSql.get(0);
        log.info("relation target sql: {} {}", sql, boundParameters);

        Assert.assertTrue("关系条件应绑定本表主键值, sql: " + sql + " " + boundParameters,
            sql.replace(" ", "").contains("in(?,?)"));
        Assert.assertTrue("应绑定本表主键值 1、2, 实际: " + boundParameters, boundParameters.containsAll(
            Arrays.asList(1, 2)));
    }

    // ----------------------------- BelongsTo 主键绑定 ----------------------------- //

    /**
     * 解除关系时, 主键必须以原始类型下发, 而不是字符串化后的值
     */
    @Test
    public void 解除关系绑定主键原始值() {
        SonEntity.SonModel sonModel = new SonEntity.SonModel();
        Record<SonEntity, Integer> son = new RecordBean<>(sonModel,
            JdbcStub.row("sid", 1, "fix_teacher_id", 1, "name", "son-1"), "");

        int affected = son.bind("teacher").detach();
        Assert.assertEquals(1, affected);

        List<JdbcStub.StubStatement> updates = JDBC_STUB.statementsStartingWith("update");
        Assert.assertEquals(1, updates.size());
        JdbcStub.StubStatement update = updates.get(0);
        Assert.assertFalse("主键不应被字符串化下发, 参数: " + update.parameterList() + ", sql: " + update.sql,
            update.parameterList().contains("1"));
        Assert.assertTrue("主键应以原始类型下发, 参数: " + update.parameterList() + ", sql: " + update.sql,
            update.parameterList().contains(1));
    }

    /**
     * 外键为空时解除关系属于无事可做, 应返回 0 而不是抛异常
     */
    @Test
    public void 外键为空时解除关系返回零() {
        SonEntity.SonModel sonModel = new SonEntity.SonModel();
        Record<SonEntity, Integer> son = new RecordBean<>(sonModel,
            JdbcStub.row("sid", 4, "fix_teacher_id", null, "name", "son-4"), "");

        Assert.assertEquals(0, son.bind("teacher").detach());
        Assert.assertTrue("不应下发写语句: " + JDBC_STUB.statements,
            JDBC_STUB.statementsStartingWith("update").isEmpty());
    }

    /**
     * 建立关系时, 主键同样必须以原始类型下发
     */
    @Test
    public void 建立关系绑定主键原始值() {
        SonEntity.SonModel sonModel = new SonEntity.SonModel();
        Record<SonEntity, Integer> son = new RecordBean<>(sonModel,
            JdbcStub.row("sid", 1, "fix_teacher_id", 2, "name", "son-1"), "");

        JDBC_STUB.enqueueRows(JdbcStub.row("tid", 1, "name", "teacher-1"));
        int affected = son.bind("teacher").attach(1);
        Assert.assertEquals(1, affected);

        List<JdbcStub.StubStatement> updates = JDBC_STUB.statementsStartingWith("update");
        Assert.assertEquals(1, updates.size());
        JdbcStub.StubStatement update = updates.get(0);
        Assert.assertFalse("主键不应被字符串化下发, 参数: " + update.parameterList() + ", sql: " + update.sql,
            update.parameterList().contains("1"));
    }

    // ----------------------------- 合并空构造器 ----------------------------- //

    /**
     * 合并一个空构造器不应把空片段写入, 否则会污染 isEmpty 判定, 进而生成非法 SQL
     */
    @Test
    public void 合并空构造器不应生成非法空片段() {
        Builder<?, ?, ?> target = new SonEntity.SonModel().newQuery();
        target.mergerAnyBuilder(new SonEntity.SonModel().newQuery());

        Grammar grammar = target.getGrammar();
        for (Grammar.SQLPartType type : Grammar.SQLPartType.values()) {
            Assert.assertTrue("合并空构造器后片段应仍为空: " + type, grammar.isEmpty(type));
        }

        String sql = target.toSql(SqlType.SELECT);
        log.info("merge empty sql: {}", sql);
        Assert.assertFalse("不应出现空的 force index 片段, sql: " + sql, sql.contains("force index ()"));
        Assert.assertFalse("不应出现空的 ignore index 片段, sql: " + sql, sql.contains("ignore index ()"));
        Assert.assertFalse("不应因空 UNION 片段而整体加括号, sql: " + sql, sql.trim().startsWith("("));
        Assert.assertTrue("应保留缺省查询列, sql: " + sql, sql.replace(" ", "").startsWith("select*from"));

        String deleteSql = target.toSql(SqlType.DELETE);
        log.info("merge empty delete sql: {}", deleteSql);
        Assert.assertFalse("删除语句不应出现空片段, sql: " + deleteSql, deleteSql.contains("force index ()"));
    }

    /**
     * 合并空构造器后继续追加片段, 不应被空片段带出多余分隔符
     */
    @Test
    public void 合并空构造器后追加列不应带前导分隔符() {
        Builder<?, ?, ?> target = new SonEntity.SonModel().newQuery();
        target.mergerAnyBuilder(new SonEntity.SonModel().newQuery());

        String sql = target.select("name").toSql(SqlType.SELECT);
        log.info("merge empty then select sql: {}", sql);
        Assert.assertFalse("不应出现前导逗号, sql: " + sql, sql.contains("select ,"));
        Assert.assertTrue("应以列名开头, sql: " + sql, sql.contains("`.`name`"));
    }

    // ----------------------------- 深度复制关系映射 ----------------------------- //

    /**
     * 深度复制成功时, 副本与原本共享关系映射
     */
    @Test
    public void 深度复制应保留关系映射() {
        Grammar grammar = newSonGrammarWithRelation();

        Grammar copy = grammar.deepCopy();
        Assert.assertTrue("副本应含关系映射, 实际: " + copy.pullRelation(), copy.pullRelation().containsKey("teacher"));
        Assert.assertTrue("原本应保留关系映射, 实际: " + grammar.pullRelation(),
            grammar.pullRelation().containsKey("teacher"));
    }

    /**
     * 深度复制失败时, 不得把当前的关系映射清空(否则关系查询会静默丢失)
     */
    @Test
    public void 深度复制失败不应丢失关系映射() {
        Builder<?, ?, ?> builder = new SonEntity.SonModel().newQuery();
        Grammar grammar = builder.getGrammar();
        grammar.pushRelation("teacher",
            new Record.Relation("teacher", BuilderAnyWrapper.empty(), RecordWrapper.EMPTY));

        // 不可序列化的绑定参数, 使深度复制失败
        builder.where("name", new Object());
        try {
            grammar.deepCopy();
            Assert.fail("深度复制应当失败");
        } catch (CloneNotSupportedRuntimeException e) {
            log.info("expected deep copy failure: {}", e.getMessage());
        }

        Assert.assertTrue("关系映射不应丢失, 实际: " + grammar.pullRelation(),
            grammar.pullRelation().containsKey("teacher"));
    }

    /**
     * 获取带关系映射的语法分析器
     */
    private static Grammar newSonGrammarWithRelation() {
        Grammar grammar = new SonEntity.SonModel().newQuery().getGrammar();
        grammar.pushRelation("teacher",
            new Record.Relation("teacher", BuilderAnyWrapper.empty(), RecordWrapper.EMPTY));
        return grammar;
    }

    // ----------------------------- 容器实例列表 ----------------------------- //

    /**
     * 容器不得把内部可变列表直接返回给调用方, 否则外部可以污染容器状态
     */
    @Test
    public void 容器返回的实例列表不应可被外部修改() {
        Container container = new ContainerProvider();
        container.register(Extension.class, clazz -> new ExtensionA());
        container.register(Extension.class, clazz -> new ExtensionB());

        List<Extension> beans = container.getBeans(Extension.class);
        Assert.assertEquals(2, beans.size());

        try {
            beans.add(new ExtensionA());
            Assert.fail("容器返回了内部可变列表, 外部可以污染容器状态");
        } catch (UnsupportedOperationException e) {
            // 期望: 返回不可变快照
        }
        try {
            beans.clear();
            Assert.fail("容器返回了内部可变列表, 外部可以清空容器");
        } catch (UnsupportedOperationException e) {
            // 期望: 返回不可变快照
        }

        Assert.assertEquals("容器实例数量不应变化, 实际: " + container.getBeans(Extension.class), 2,
            container.getBeans(Extension.class).size());
    }

    /**
     * 并发获取实例: 不得抛出异常, 每个工厂只实例化一次, 已发布的实例不得丢失
     */
    @Test
    public void 并发获取容器实例应保持一致() throws InterruptedException {
        Container container = new ContainerProvider();
        container.register(Extension.class, clazz -> new ExtensionA());
        container.register(Extension.class, clazz -> new ExtensionB());

        int threads = 8;
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
        List<Extension> firstBeans = Collections.synchronizedList(new ArrayList<>());
        Thread[] workers = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < 200; j++) {
                        List<Extension> beans = container.getBeans(Extension.class);
                        // 边读边遇到内部列表被其它线程结构性修改时, 会抛 ConcurrentModificationException
                        for (Extension bean : beans) {
                            if (bean == null) {
                                throw new IllegalStateException("实例不应为 null");
                            }
                        }
                        if (beans.size() != 2) {
                            throw new IllegalStateException("实例数量不应为 2, 实际: " + beans.size());
                        }
                        firstBeans.add(container.getBean(Extension.class));
                    }
                } catch (Throwable e) {
                    errors.add(e);
                }
            });
            workers[i].start();
        }
        for (Thread worker : workers) {
            worker.join();
        }

        Assert.assertTrue("并发获取容器实例出错: " + errors, errors.isEmpty());
        Assert.assertEquals("getBean 应始终返回同一个单例, 实际: " + new HashSet<>(firstBeans), 1,
            new HashSet<>(firstBeans).size());
        Assert.assertEquals(2, container.getBeans(Extension.class).size());
    }

    // ----------------------------- 语句泄漏 ----------------------------- //

    /**
     * 查询结束后, 预处理语句应当被关闭
     */
    @Test
    public void 查询结束后语句应被关闭() {
        JDBC_STUB.enqueueRows(JdbcStub.row("sid", 1, "fix_teacher_id", 1, "name", "son-1"));
        new SonEntity.SonModel().newQuery().get();

        Assert.assertTrue("存在未关闭的语句: " + JDBC_STUB.openStatements(), JDBC_STUB.openStatements().isEmpty());
    }

    /**
     * 写操作结束后, 预处理语句应当被关闭
     */
    @Test
    public void 写操作结束后语句应被关闭() {
        SonEntity.SonModel sonModel = new SonEntity.SonModel();
        sonModel.newQuery().where("sid", 1).data("name", "renamed").update();

        Assert.assertTrue("存在未关闭的语句: " + JDBC_STUB.openStatements(), JDBC_STUB.openStatements().isEmpty());
    }

    /**
     * 聚合查询结束后, 预处理语句应当被关闭
     */
    @Test
    public void 聚合查询结束后语句应被关闭() {
        JDBC_STUB.enqueueRows(JdbcStub.row("aggregate", 3L));
        new SonEntity.SonModel().newQuery().count();

        Assert.assertTrue("存在未关闭的语句: " + JDBC_STUB.openStatements(), JDBC_STUB.openStatements().isEmpty());
    }

    // ----------------------------- 多对多关系操作键值 ----------------------------- //

    /**
     * 多对多关系操作中, 人造关系键的值必须参数绑定, 不能把键值 toString 后拼进 SQL
     */
    @Test
    public void 多对多关系操作字符串键不应拼进SQL() {
        BelongsToManyQueryRelation relation = rolesRelation();

        JDBC_STUB.enqueueRows(
            JdbcStub.row("user_id", "u-1", "role_id", 1),
            JdbcStub.row("user_id", "u-1", "role_id", 2));
        RecordList<RelationshipUserRoleEntity, Integer> relations = new RelationshipUserRoleEntity.RelationshipUserRoleModel().newQuery().get();

        List<Map<String, Object>> metadata = new ArrayList<>();
        metadata.add(JdbcStub.row("uid", "u-1", "name", "user-1"));

        List<String> rawSql = new ArrayList<>();
        List<Object> boundParameters = new ArrayList<>();
        relation.prepareTargetBuilderByRelationOperation(metadata, relations, BuilderAnyWrapper.empty(),
            BuilderAnyWrapper.empty()).toSql(SqlType.SELECT, (sql, parameters) -> {
            rawSql.add(sql);
            boundParameters.addAll(parameters);
            return sql;
        });
        String sql = rawSql.get(0);
        log.info("belongsToMany relation operation sql: {} {}", sql, boundParameters);

        Assert.assertFalse("字符串键值被拼进了 SQL, sql: " + sql, sql.contains("\"u-1\"") || sql.contains("'u-1'")
            || sql.replace(" ", "").contains("u-1as"));
        Assert.assertTrue("关系键值应以占位符下发, sql: " + sql, sql.replace(" ", "").contains("?as`@R@`"));
        Assert.assertTrue("应绑定关系键值 u-1, 实际: " + boundParameters, boundParameters.contains("u-1"));
    }

    /**
     * 键值来自数据, 含引号时也不能破坏 SQL 结构
     */
    @Test
    public void 多对多关系操作键值含引号不应破坏SQL() {
        BelongsToManyQueryRelation relation = rolesRelation();

        JDBC_STUB.enqueueRows(JdbcStub.row("user_id", "u'1", "role_id", 1));
        RecordList<RelationshipUserRoleEntity, Integer> relations = new RelationshipUserRoleEntity.RelationshipUserRoleModel().newQuery().get();

        List<String> rawSql = new ArrayList<>();
        List<Object> boundParameters = new ArrayList<>();
        relation.prepareTargetBuilderByRelationOperation(new ArrayList<>(), relations, BuilderAnyWrapper.empty(),
            BuilderAnyWrapper.empty()).toSql(SqlType.SELECT, (sql, parameters) -> {
            rawSql.add(sql);
            boundParameters.addAll(parameters);
            return sql;
        });
        String sql = rawSql.get(0);
        log.info("belongsToMany quoted key sql: {} {}", sql, boundParameters);

        Assert.assertFalse("键值不应出现在 SQL 文本中, sql: " + sql, sql.contains("u'1"));
        Assert.assertTrue("键值应原样绑定, 实际: " + boundParameters, boundParameters.contains("u'1"));
    }

    /**
     * 获取 sons 关系处理器
     */
    private static HasOneOrManyQueryRelation sonsRelation() {
        ModelShadowProvider modelShadowProvider = GAARASON_DATA_SOURCE.getContainer()
            .getBean(ModelShadowProvider.class);
        TeacherEntity.TeacherModel teacherModel = new TeacherEntity.TeacherModel();
        Map<String, FieldRelationMember> relationFieldMap = modelShadowProvider.get(teacherModel).getEntityMember()
            .getRelationFieldMap();
        return (HasOneOrManyQueryRelation) relationFieldMap.get("sons").getRelationSubQuery();
    }

    /**
     * 获取 roles 多对多关系处理器
     */
    private static BelongsToManyQueryRelation rolesRelation() {
        ModelShadowProvider modelShadowProvider = GAARASON_DATA_SOURCE.getContainer()
            .getBean(ModelShadowProvider.class);
        UserEntity.UserModel userModel = new UserEntity.UserModel();
        Map<String, FieldRelationMember> relationFieldMap = modelShadowProvider.get(userModel).getEntityMember()
            .getRelationFieldMap();
        return (BelongsToManyQueryRelation) relationFieldMap.get("roles").getRelationSubQuery();
    }

    // ----------------------------- 异步执行的路由上下文 ----------------------------- //

    /**
     * 异步查询取连接发生在线程池线程, 必须继承调用线程的库路由上下文, 否则连接会落到错误的库
     */
    @Test
    public void 异步查询应继承调用线程的库路由上下文() throws Exception {
        List<String> resolvedKeys = observedDatabaseKeys();
        try {
            JDBC_STUB.enqueueRows(JdbcStub.row("sid", 1, "fix_teacher_id", 1, "name", "son-1"));
            GaarasonDataSourceContext.executeDatabase("db-sync",
                () -> new SonEntity.SonModel().newQuery().get());
            Assert.assertTrue("同步查询应看到库路由上下文, 实际: " + resolvedKeys, resolvedKeys.contains("db-sync"));

            resolvedKeys.clear();
            JDBC_STUB.enqueueRows(JdbcStub.row("sid", 1, "fix_teacher_id", 1, "name", "son-1"));
            GaarasonDataSourceContext.executeDatabase("db-async", () -> new SonEntity.SonModel()
                .nativeQueryListAsync("select * from gaarason_fix_son", null)).get(10, TimeUnit.SECONDS);

            Assert.assertTrue("异步查询应看到调用线程的库路由上下文, 实际: " + resolvedKeys,
                resolvedKeys.contains("db-async"));
        } finally {
            restoreDatabaseRouting();
        }
    }

    /**
     * 异步事务的闭包在线程池线程执行, 其中创建的查询同样不能丢表路由上下文
     */
    @Test
    public void 异步事务应继承调用线程的表路由上下文() throws Exception {
        SonEntity.SonModel sonModel = new SonEntity.SonModel();
        String syncSql = GaarasonDataSourceContext.executeTable("0024",
            () -> sonModel.newQuery().toSql(SqlType.SELECT));
        log.info("table routing sync sql: {}", syncSql);
        Assert.assertTrue("同步查询应解析出物理表, sql: " + syncSql, syncSql.contains("gaarason_fix_son_0024"));

        String asyncSql = GaarasonDataSourceContext.executeTable("0024",
            () -> sonModel.newQuery().transactionAsync(() -> sonModel.newQuery().toSql(SqlType.SELECT)))
            .get(10, TimeUnit.SECONDS);
        log.info("table routing async sql: {}", asyncSql);
        Assert.assertTrue("异步事务内的查询应解析出物理表, sql: " + asyncSql, asyncSql.contains("gaarason_fix_son_0024"));
    }

    /**
     * 异步任务结束必须复原线程池线程的上下文, 不能残留给下一个任务
     */
    @Test
    public void 异步任务不应在线程池线程残留路由上下文() throws Exception {
        List<String> resolvedKeys = observedDatabaseKeys();
        try {
            JDBC_STUB.enqueueRows(JdbcStub.row("sid", 1, "fix_teacher_id", 1, "name", "son-1"));
            GaarasonDataSourceContext.executeDatabase("db-leak", () -> new SonEntity.SonModel()
                .nativeQueryListAsync("select * from gaarason_fix_son", null)).get(10, TimeUnit.SECONDS);

            resolvedKeys.clear();
            JDBC_STUB.enqueueRows(JdbcStub.row("sid", 1, "fix_teacher_id", 1, "name", "son-1"));
            // 不带上下文执行, 复用的线程池线程不应看到上一个任务的库上下文
            new SonEntity.SonModel().nativeQueryListAsync("select * from gaarason_fix_son", null)
                .get(10, TimeUnit.SECONDS);

            Assert.assertFalse("线程池线程残留了上一次任务的库路由上下文, 实际: " + resolvedKeys,
                resolvedKeys.contains("db-leak"));
        } finally {
            restoreDatabaseRouting();
        }
    }

    /**
     * 记录每次取连接时解析到的库键, 用于观测路由上下文对哪个线程可见
     */
    private static List<String> observedDatabaseKeys() {
        List<String> resolvedKeys = Collections.synchronizedList(new ArrayList<>());
        GaarasonDataSourceContext.setDynamicDatabaseRouting(databaseKey -> {
            resolvedKeys.add(databaseKey == null ? "<none>" : databaseKey);
            return databaseKey;
        });
        return resolvedKeys;
    }

    /**
     * 恢复缺省的库键解析策略
     */
    private static void restoreDatabaseRouting() {
        GaarasonDataSourceContext.setDynamicDatabaseRouting(databaseKey -> databaseKey);
    }

    // ----------------------------- 容器与连接池的生命周期 ----------------------------- //

    /**
     * 容器关闭必须能停掉它创建的异步线程池, 否则线程与任务队列会一直残留
     */
    @Test
    public void 容器关闭应停止异步线程池() throws Exception {
        ContainerBootstrap container = ContainerBootstrap.build();
        container.signUpIdentification("core-fix-shutdown-" + System.nanoTime());
        container.defaultRegister();

        ExecutorService executor = container.getBean(ExecutorService.class);
        executor.submit(() -> null).get();
        Assert.assertFalse("关闭前线程池不应处于 shutdown 状态", executor.isShutdown());

        container.shutdown();

        Assert.assertTrue("容器关闭后异步线程池应已 shutdown", executor.isShutdown());
        Assert.assertTrue("容器关闭后异步线程池应能终止", executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    // ----------------------------- 实体与模型 ----------------------------- //

    /**
     * 本地表主键列名为 tid, 与子表主键列名 sid 不同
     */
    @Data
    @Table(name = "gaarason_fix_teacher")
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
        }
    }

    @Data
    @Table(name = "gaarason_fix_son")
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
        }
    }

    /**
     * 本表主键是字符串列, 多对多关系操作时不能把键值当数字拼进 SQL
     */
    @Data
    @Table(name = "gaarason_fix_user")
    public static class UserEntity implements Serializable {

        @Primary
        @Column(name = "uid")
        private String uid;

        @Column(name = "name")
        private String name;

        /**
         * 故意不声明 localModelLocalKey/targetModelLocalKey, 走缺省分支
         */
        @BelongsToMany(relationModel = RelationshipUserRoleEntity.RelationshipUserRoleModel.class,
            foreignKeyForLocalModel = "user_id", foreignKeyForTargetModel = "role_id")
        private List<RoleEntity> roles;

        public static class UserModel extends Model<MySqlBuilderV2<UserEntity, String>, UserEntity, String> {

            @Override
            public GaarasonDataSource getGaarasonDataSource() {
                return GAARASON_DATA_SOURCE;
            }
        }
    }

    @Data
    @Table(name = "gaarason_fix_role")
    public static class RoleEntity implements Serializable {

        @Primary
        @Column(name = "rid")
        private Integer rid;

        @Column(name = "name")
        private String name;

        public static class RoleModel extends Model<MySqlBuilderV2<RoleEntity, Integer>, RoleEntity, Integer> {

            @Override
            public GaarasonDataSource getGaarasonDataSource() {
                return GAARASON_DATA_SOURCE;
            }
        }
    }

    /**
     * 中间表
     */
    @Data
    @Table(name = "gaarason_fix_relationship_user_role")
    public static class RelationshipUserRoleEntity implements Serializable {

        @Primary
        @Column(name = "id")
        private Integer id;

        @Column(name = "user_id")
        private String userId;

        @Column(name = "role_id")
        private Integer roleId;

        public static class RelationshipUserRoleModel extends
            Model<MySqlBuilderV2<RelationshipUserRoleEntity, Integer>, RelationshipUserRoleEntity, Integer> {

            @Override
            public GaarasonDataSource getGaarasonDataSource() {
                return GAARASON_DATA_SOURCE;
            }
        }
    }

    /**
     * 容器实例, 用于验证实例列表的可变性与并发
     */
    public interface Extension {
    }

    public static class ExtensionA implements Extension {
    }

    public static class ExtensionB implements Extension {
    }
}
