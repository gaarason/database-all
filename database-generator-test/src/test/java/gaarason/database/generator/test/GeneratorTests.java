package gaarason.database.generator.test;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.connections.GaarasonDataSourceProvider;
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
    public void run有参构造() {
        String jdbcUrl = "jdbc:mysql://mysql.local/test_master_0?useUnicode=true&characterEncoding=utf-8" +
            "&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai";
        String    username  = "root";
        String    password  = "root";
        Generator generator = new Generator(jdbcUrl, username, password);

        // set
        generator.setStaticField(true);
        generator.setIsSpringBoot(true);
        generator.setIsSwagger(true);
        generator.setIsValidator(true);
        generator.setCorePoolSize(20);
        generator.setOutputDir("./src/test/java/");
        generator.setNamespace("test.data");
        generator.setDisInsertable("created_at", "updated_at");
        generator.setDisUpdatable("created_at", "updated_at");

        generator.run();
    }

    @Test
    public void run无参构造() {
        GaarasonDataSourceProvider gaarasonDataSourceProvider = proxyDataSource();
        ToolModel                  toolModel                  = new ToolModel(gaarasonDataSourceProvider);
        AutoGenerator              autoGenerator              = new AutoGenerator(toolModel);
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
            "jdbc:mysql://mysql.local/test_master_0?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("root");

        druidDataSource.setDbType("com.alibaba.druid.pool.DruidDataSource");
        druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        druidDataSource.setInitialSize(20);
        druidDataSource.setMaxActive(20);
        druidDataSource.setLoginTimeout(3);
        druidDataSource.setQueryTimeout(3);
        return druidDataSource;
    }

    private List<DataSource> dataSourceMasterList() {
        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(dataSourceMaster0());
        return dataSources;
    }

    private GaarasonDataSourceProvider proxyDataSource() {
        List<DataSource> dataSources = dataSourceMasterList();
        return new GaarasonDataSourceProvider(dataSources);
    }

    public static class ToolModel extends Model<ToolModel.Inner, Object> {
        private GaarasonDataSourceProvider gaarasonDataSourceProvider;

        public ToolModel(GaarasonDataSourceProvider dataSource) {
            gaarasonDataSourceProvider = dataSource;
        }

        public GaarasonDataSourceProvider getGaarasonDataSource() {
            return gaarasonDataSourceProvider;
        }

        public static class Inner {
        }
    }

    public static class AutoGenerator extends Generator {
        private Model<?, ?> toolModel;

        public AutoGenerator(Model<?, ?> model) {
            toolModel = model;
        }

        public Model<?, ?> getModel() {
            return toolModel;
        }

    }
}
