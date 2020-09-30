package gaarason.database.generator.test;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.connection.GaarasonDataSourceWrapper;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.eloquent.Model;
import gaarason.database.generator.Generator;
import gaarason.database.generator.util.DatabaseInfoUtil;
import gaarason.database.support.Column;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
//        generator.setIsSpringBoot(true);
//        generator.setIsSwagger(true);
//        generator.setIsValidator(true);
        generator.setCorePoolSize(20);
        generator.setOutputDir("./src/test/java/");
        generator.setNamespace("test.data");
        generator.setDisInsertable("created_at", "updated_at");
        generator.setDisUpdatable("created_at", "updated_at");

        generator.run();
    }

    @Test
    public void run无参构造() {
        ToolModel.gaarasonDataSourceWrapper = proxyDataSource();
        ToolModel     toolModel     = new ToolModel();
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

    private GaarasonDataSourceWrapper proxyDataSource() {
        List<DataSource> dataSources = dataSourceMasterList();
        return new GaarasonDataSourceWrapper(dataSources);
    }

    public static class ToolModel extends Model<ToolModel.Inner, Object> {
        public static GaarasonDataSourceWrapper gaarasonDataSourceWrapper;

        public GaarasonDataSourceWrapper getGaarasonDataSource() {
            return gaarasonDataSourceWrapper;
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

    @Test
    public void test() throws SQLException {

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

        GaarasonDataSource gaarasonDataSourceWrapper = new GaarasonDataSourceWrapper(Collections.singletonList(druidDataSource));

        Set<String> test = DatabaseInfoUtil.tableNames(gaarasonDataSourceWrapper, "test_master_0", null, null);
        System.out.println(test);

        List<Map<String, Column>> test1 = DatabaseInfoUtil.columns(gaarasonDataSourceWrapper, "test_master_0", null, null);

        for (Map<String, Column> stringObjectMap : test1) {
            System.out.println(stringObjectMap);
        }


    }
}
