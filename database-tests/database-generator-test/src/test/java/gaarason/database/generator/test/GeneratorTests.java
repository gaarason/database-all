package gaarason.database.generator.test;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.connection.GaarasonDataSourceBuilder;
import gaarason.database.connection.GaarasonDataSourceWrapper;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.eloquent.Model;
import gaarason.database.generator.Generator;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.*;

/**
 * 测试
 * @author xt
 */
@FixMethodOrder(MethodSorters.JVM)
public class GeneratorTests {

    @Test
    public void run有参构造() {
        String jdbcUrl = "jdbc:mysql://mysql.local/test_master_0?useUnicode=true&characterEncoding=utf-8" +
            "&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "root";
        Generator generator = new Generator(jdbcUrl, username, password);

        // set
//        generator.setOutputDir("./src/test/java/");     // 所有生成文件的路径
        generator.setOutputDir("./src/test/java1/");     // 所有生成文件的路径
        generator.setNamespace("gaarason.database.test.models.relation.pojo");                 // 所有生成文件的所属命名空间
        generator.setCorePoolSize(20);                  // 所用的线程数
        generator.setSpringBoot(true);                // 是否生成spring boot相关注解
        generator.setSwagger(false);                   // 是否生成swagger相关注解
        generator.setValidator(false);                 // 是否生成validator相关注解

        generator.setEntityStaticField(true);          // 是否在实体中生成静态字段
        generator.setBaseEntityDir("base");             // 实体父类的相对路径
        generator.setBaseEntityFields("id");            // 实体父类存在的字段
        generator.setBaseEntityName("BaseEntity");      // 实体父类的类名
        generator.setEntityDir("entity");               // 实体的相对路径
        generator.setEntityPrefix("");                  // 实体的类名前缀
        generator.setEntitySuffix("");                  // 实体的类名后缀

        generator.setDisInsertable("created_at", "updated_at");     // 新增时,不可通过ORM更改的字段
        generator.setDisUpdatable("created_at", "updated_at");      // 更新时,不可通过ORM更改的字段

        generator.setBaseModelDir("base");              // 模型父类的相对路径
        generator.setBaseModelName("BaseModel");        // 模型父类的类名
        generator.setModelDir("model");                 // 模型的相对路径
        generator.setModelPrefix("");                   // 模型的类名前缀
        generator.setModelSuffix("Model");              // 模型的类名后缀

        // 执行
        generator.run();
    }

    @Test
    public void run无参构造() {
        ToolModel.gaarasonDataSource = proxyDataSource();
        ToolModel toolModel = new ToolModel();
        AutoGenerator autoGenerator = new AutoGenerator(toolModel);
        // set
        autoGenerator.setEntityStaticField(true);
        autoGenerator.setSpringBoot(true);
        autoGenerator.setCorePoolSize(20);
//        autoGenerator.setOutputDir("./src/test/java/");
        autoGenerator.setOutputDir("./src/test/java1/");
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

    private GaarasonDataSource proxyDataSource() {
        List<DataSource> dataSources = dataSourceMasterList();
        return GaarasonDataSourceBuilder.build(dataSources);
    }

    public static class ToolModel extends Model<ToolModel.Inner, Serializable> {

        public static GaarasonDataSource gaarasonDataSource;

        @Override
        public GaarasonDataSource getGaarasonDataSource() {
            return gaarasonDataSource;
        }

        public static class Inner implements Serializable{

        }
    }

    public static class AutoGenerator extends Generator {

        private final Model<? extends Serializable, ? extends Serializable> toolModel;

        public AutoGenerator(Model<? extends Serializable, ? extends Serializable> model) {
            toolModel = model;
        }

        @Override
        public Model<? extends Serializable, ? extends Serializable> getModel() {
            return toolModel;
        }

    }

}
