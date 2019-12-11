package gaarason.database.spring.boot.starter.test;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.connections.ProxyDataSource;
import gaarason.database.eloquent.Model;
import gaarason.database.generator.Manager;
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

        // 设置
        autoGenerator.setStaticField(true);
        autoGenerator.setIsSpringBoot(true);
        autoGenerator.setOutputDir("./src/main/java/");
        autoGenerator.setNamespace("gaarason.database.spring.boot.starter.test.data");
        String[] disableCreate = {"created_at", "updated_at"};
        autoGenerator.setDisInsertable(disableCreate);
        String[] disableUpdate = {"created_at", "updated_at"};
        autoGenerator.setDisUpdatable(disableUpdate);

        autoGenerator.run();
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

    public class AutoGenerator extends Manager {

        private Model toolModel;

        public AutoGenerator(Model model) {
            toolModel = model;
        }

        public Model getModel() {
            return toolModel;
        }

    }


}
