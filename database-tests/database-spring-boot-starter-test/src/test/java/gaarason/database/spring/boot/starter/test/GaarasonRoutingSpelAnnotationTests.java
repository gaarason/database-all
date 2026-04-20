package gaarason.database.spring.boot.starter.test;

import gaarason.database.connection.GaarasonDataSourceContext;
import gaarason.database.spring.boot.starter.annotation.GaarasonTable;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * 路由注解 {@code spel=true} 与 SpEL 求值集成验证.
 *
 * @author xt
 */
@RunWith(SpringRunner.class)
@org.springframework.boot.test.context.SpringBootTest(classes = TestApplication.class)
@Import(GaarasonRoutingSpelAnnotationTests.RoutingSpelTestBeans.class)
public class GaarasonRoutingSpelAnnotationTests {

    @Resource
    private RoutingSpelSubject routingSpelSubject;

    @Test
    public void gaarasonTable_spel引用Bean求值后写入表路由上下文() {
        String table = routingSpelSubject.readTableKeyViaBean();
        Assert.assertEquals("route_probe_fromBean", table);
    }

    @Test
    public void gaarasonTable_spel使用方法参数() {
        String table = routingSpelSubject.readTableKeyViaArg(42);
        Assert.assertEquals("route_probe_42", table);
    }

    @Configuration
    static class RoutingSpelTestBeans {

        @Bean("routingSpelTestHelper")
        public RoutingSpelTestHelper routingSpelTestHelper() {
            return new RoutingSpelTestHelper();
        }

        @Bean
        public RoutingSpelSubject routingSpelSubject() {
            return new RoutingSpelSubject();
        }
    }

    public static class RoutingSpelTestHelper {

        public String key() {
            return "fromBean";
        }
    }

    public static class RoutingSpelSubject {

        @GaarasonTable(spel = true, value = "@routingSpelTestHelper.key()")
        public String readTableKeyViaBean() {
            return GaarasonDataSourceContext.resolvePhysicalTableName("route_probe");
        }

        @GaarasonTable(spel = true, value = "#p0")
        public String readTableKeyViaArg(int shard) {
            return GaarasonDataSourceContext.resolvePhysicalTableName("route_probe");
        }
    }
}
