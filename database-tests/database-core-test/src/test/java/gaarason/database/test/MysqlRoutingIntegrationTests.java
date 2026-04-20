package gaarason.database.test;

import gaarason.database.appointment.JoinType;
import gaarason.database.appointment.SqlType;
import gaarason.database.connection.GaarasonDataSourceContext;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.test.models.routing.RoutingIntegrationStudentModel;
import gaarason.database.test.parent.base.BaseTests;
import gaarason.database.test.utils.DatabaseTypeUtil;
import gaarason.database.util.ObjectUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 路由、同链接切库、动态切表真实数据库集成测试.
 * <p>
 * 依赖 {@code mysql.local} 与初始化脚本 {@code sql/mysql-routing_integration.sql} 等, 与现有用例一致.
 *
 * @author xt
 */
@FixMethodOrder(MethodSorters.JVM)
public class MysqlRoutingIntegrationTests extends BaseTests {

    private static final RoutingIntegrationStudentModel ROUTING_STUDENT_MODEL = new RoutingIntegrationStudentModel();

    @BeforeClass
    public static void beforeClass() throws IOException {
        DatabaseTypeUtil.setDatabaseTypeToMysql();
    }

    @Override
    protected GaarasonDataSource getGaarasonDataSource() {
        return ROUTING_STUDENT_MODEL.getGaarasonDataSource();
    }

    @Override
    protected List<TABLE> getInitTables() {
        return Arrays.asList(TABLE.student, TABLE.teacher, TABLE.routing_integration);
    }

    @Before
    public void beforeRoutingIntegration() {
        installIntegrationDynamicTableRouting();
    }

    @After
    public void afterRoutingIntegration() {
        clearAllRoutingContext();
        resetRoutingExtensionsToDefault();
    }

    private static void installIntegrationDynamicTableRouting() {
        GaarasonDataSourceContext.setDynamicTableRouting((logicalTableName, routeExpression) -> {
            if (ObjectUtils.isEmpty(routeExpression)) {
                return logicalTableName;
            }
            if (logicalTableName.startsWith("student_rt_")) {
                return logicalTableName;
            }
            if ("student".equals(logicalTableName)) {
                int shard = Integer.parseInt(routeExpression.trim());
                return "student_rt_" + String.format("%03d", shard);
            }
            return logicalTableName;
        });
    }

    private static void clearAllRoutingContext() {
        // execute* 与注解切面均采用 try/finally 自动恢复上下文，无需显式清理
    }

    private static void resetRoutingExtensionsToDefault() {
        GaarasonDataSourceContext.setDynamicDatabaseRouting(databaseKey -> databaseKey);
        GaarasonDataSourceContext.setDynamicDataSourceGroupRouting(groupKey -> groupKey);
        GaarasonDataSourceContext.setDynamicTableRouting((logicalTableName, routeExpression) -> {
            if (ObjectUtils.isEmpty(routeExpression)) {
                return logicalTableName;
            }
            return logicalTableName + "_" + routeExpression;
        });
        GaarasonDataSourceContext.setDynamicJdbcCatalogRouting((ds, connection, catalogKey) -> {
            try {
                connection.setCatalog(catalogKey);
            } catch (Throwable ignored) {
                connection.setSchema(catalogKey);
            }
        });
        GaarasonDataSourceContext.setDynamicExplicitTableRouting(() -> true);
    }

    @Test
    public void 数据源组切换_order组命中B库学生() {
        GaarasonDataSourceContext.executeDataSourceGroup("order", () -> {
            RoutingIntegrationStudentModel.Entity entity =
                ROUTING_STUDENT_MODEL.findOrFail(200).toObject();
            Assert.assertEquals("catalog_b", entity.getName());
        });
    }

    @Test
    public void 数据源组切换_master组命中主库学生() {
        GaarasonDataSourceContext.executeDataSourceGroup("master", () -> {
            RoutingIntegrationStudentModel.Entity entity =
                ROUTING_STUDENT_MODEL.findOrFail(3).toObject();
            Assert.assertEquals("小腾", entity.getName());
        });
    }

    @Test
    public void 同链接切库_executeDatabase命中A库() {
        GaarasonDataSourceContext.executeDatabase("gaarason_routing_a", () -> {
            RoutingIntegrationStudentModel.Entity entity =
                ROUTING_STUDENT_MODEL.findOrFail(100).toObject();
            Assert.assertEquals("catalog_a", entity.getName());
        });
    }

    @Test
    public void 动态切表_executeTable命中分表001() {
        GaarasonDataSourceContext.executeTable("1", () -> {
            RoutingIntegrationStudentModel.Entity entity =
                ROUTING_STUDENT_MODEL.findOrFail(9001).toObject();
            Assert.assertEquals("shard001", entity.getName());
        });
    }

    @Test
    public void 动态切表_from显式student仍按路由解析到分表002() {
        GaarasonDataSourceContext.executeTable("2", () -> {
            Record<RoutingIntegrationStudentModel.Entity, Integer> record = ROUTING_STUDENT_MODEL.newQuery()
                .from("student")
                .where("id", 9002)
                .firstOrFail();
            Assert.assertEquals("shard002", record.toObject().getName());
        });
    }

    @Test
    public void 动态切表_join的关联表名仍走解析器且teacher保持原名() {
        GaarasonDataSourceContext.executeTable("1", () -> {
            String sql = ROUTING_STUDENT_MODEL.newQuery()
                .join(JoinType.INNER, "teacher", "teacher_id", "=", "`teacher`.`id`")
                .where("id", 9001)
                .toSql(SqlType.SELECT);
            Assert.assertTrue("join 应包含 teacher 表", sql.contains("teacher"));
            Assert.assertTrue("主表应路由到分表", sql.contains("student_rt_001"));
        });
    }

    @Test
    public void 扩展点_dynamicDatabaseRouting_trim生效() {
        GaarasonDataSourceContext.setDynamicDatabaseRouting(
            databaseKey -> databaseKey == null ? null : databaseKey.trim());
        GaarasonDataSourceContext.executeDatabase("  gaarason_routing_a  ", () -> {
            RoutingIntegrationStudentModel.Entity entity =
                ROUTING_STUDENT_MODEL.findOrFail(100).toObject();
            Assert.assertEquals("catalog_a", entity.getName());
        });
    }

    @Test
    public void 扩展点_tableOverridePolicy为false时动态表不覆盖显式逻辑表() {
        GaarasonDataSourceContext.setDynamicExplicitTableRouting(() -> false);
        GaarasonDataSourceContext.executeTable("1", () -> {
            RoutingIntegrationStudentModel.Entity entity =
                ROUTING_STUDENT_MODEL.newQuery().from("student").where("id", 3).firstOrFail().toObject();
            Assert.assertEquals("小腾", entity.getName());
        });
    }

    @Test
    public void 嵌套_execute组与executeTable恢复() {
        GaarasonDataSourceContext.executeDataSourceGroup("master", () -> {
            GaarasonDataSourceContext.executeTable("1", () -> {
                Assert.assertEquals("shard001", ROUTING_STUDENT_MODEL.findOrFail(9001).toObject().getName());
            });
            Assert.assertEquals("小腾", ROUTING_STUDENT_MODEL.findOrFail(3).toObject().getName());
        });
    }

    @Test
    public void 事务内锁定数据源组_切换组上下文仍走开启事务时的组() {
        GaarasonDataSource ds = ROUTING_STUDENT_MODEL.getGaarasonDataSource();
        GaarasonDataSourceContext.executeDataSourceGroup("master", () -> {
            ds.begin();
            try {
                Assert.assertEquals("小腾", ROUTING_STUDENT_MODEL.findOrFail(3).toObject().getName());
                GaarasonDataSourceContext.executeDataSourceGroup("order", () -> {
                    Assert.assertEquals("小腾", ROUTING_STUDENT_MODEL.findOrFail(3).toObject().getName());
                    Assert.assertThrows(EntityNotFoundException.class, () -> ROUTING_STUDENT_MODEL.findOrFail(200));
                });
            } finally {
                ds.rollBack();
            }
        });
    }

    @Test
    public void 事务内锁定数据库键_切换库上下文仍走开启事务时的库() {
        GaarasonDataSource ds = ROUTING_STUDENT_MODEL.getGaarasonDataSource();
        GaarasonDataSourceContext.executeDatabase("gaarason_routing_a", () -> {
            ds.begin();
            try {
                Assert.assertEquals("catalog_a", ROUTING_STUDENT_MODEL.findOrFail(100).toObject().getName());
                GaarasonDataSourceContext.executeDatabase("gaarason_routing_b", () -> {
                    Assert.assertThrows(EntityNotFoundException.class, () -> ROUTING_STUDENT_MODEL.findOrFail(200));
                    Assert.assertEquals("catalog_a", ROUTING_STUDENT_MODEL.findOrFail(100).toObject().getName());
                });
            } finally {
                ds.rollBack();
            }
        });
    }
}
