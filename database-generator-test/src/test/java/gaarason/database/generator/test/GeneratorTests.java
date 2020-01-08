package gaarason.database.generator.test;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.connections.ProxyDataSource;
import gaarason.database.eloquent.Model;
import gaarason.database.generator.Generator;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class GeneratorTests {

    @Test
    public void run() {
        ProxyDataSource proxyDataSource = proxyDataSource();
        ToolModel toolModel = new ToolModel(proxyDataSource);
        AutoGenerator autoGenerator = new AutoGenerator(toolModel);
        // set
        autoGenerator.setStaticField(true);
        autoGenerator.setIsSpringBoot(true);
        autoGenerator.setCorePoolSize(20);
        autoGenerator.setOutputDir("./src/test/java/");
        autoGenerator.setNamespace("test.data");
        autoGenerator.setDisInsertable("created_at", "updated_at");
        autoGenerator.setDisUpdatable("created_at", "updated_at");

        autoGenerator.run();
    }

    private DataSource dataSourceMaster0() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(
            "jdbc:mysql://sakya.local/test_master_0?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("root");

//        druidDataSource.setUrl(
//            "jdbc:mysql://116.62.120.228:5588/rental_jq?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai");
//        druidDataSource.setUsername("ykj-im-test");
//        druidDataSource.setPassword("lTkgD91ZAz3egmwv");

        druidDataSource.setDbType("com.alibaba.druid.pool.DruidDataSource");
        druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        druidDataSource.setInitialSize(20);
        druidDataSource.setMaxActive(20);
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

    public class AutoGenerator extends Generator {
        private Model toolModel;
        public AutoGenerator(Model model) {
            toolModel = model;
        }
        public Model getModel() {
            return toolModel;
        }

    }
}
