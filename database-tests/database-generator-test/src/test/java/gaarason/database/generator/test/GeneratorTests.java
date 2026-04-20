package gaarason.database.generator.test;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.connection.GaarasonDataSourceBuilder;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.support.FieldConversion;
import gaarason.database.contract.support.FieldFill;
import gaarason.database.contract.support.FieldStrategy;
import gaarason.database.eloquent.Model;
import gaarason.database.generator.Generator;
import gaarason.database.generator.appointment.Style;
import gaarason.database.query.QueryBuilder;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试
 * @author xt
 */
@FixMethodOrder(MethodSorters.JVM)
public class GeneratorTests {

    /**
     * 通过系统属性补齐包扫描，确保可发现默认方言自动配置，避免回退实例化 QueryBuilderConfig 接口。
     */
    private static void ensureScanPackagesForGenerator() {
        String key = "gaarason.database.scan.packages";
        String current = System.getProperty(key);
        if (current == null || current.trim().isEmpty()) {
            System.setProperty(key, "gaarason.database");
        } else if (!current.contains("gaarason.database")) {
            System.setProperty(key, current + ",gaarason.database");
        }
    }

    /**
     * 生成供其它项目（如 Spring Boot 3 / Jakarta）拷贝的样例代码，勿写入 {@code src/test/java}，否则会参与本模块编译并导致依赖冲突。
     */
    private static String codegenExportDir() {
        String basedir = System.getProperty("basedir", ".");
        return new File(basedir, "target/generated-code-export").getAbsolutePath();
    }

    @Test
    public void run有参构造() {
        ensureScanPackagesForGenerator();
        String jdbcUrl = "jdbc:mysql://mysql.local/test_master_0?useUnicode=true&characterEncoding=utf-8" +
            "&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "root";
        Generator generator = new Generator(jdbcUrl, username, password);

        generator.setStyle(Style.ENTITY);
        // set
        generator.setOutputDir(codegenExportDir());     // 所有生成文件的路径
        generator.setNamespace("gaarason.database.test.models.morph");                 // 所有生成文件的所属命名空间
        generator.setCorePoolSize(20);                  // 所用的线程数
        generator.setSpringBoot(Generator.SpringBootVersion.THREE);                // 是否生成spring boot相关注解
        generator.setSwagger(false);                   // 是否生成swagger相关注解
        generator.setValidator(false);                 // 是否生成validator相关注解

        generator.setEntityStaticField(false);          // 是否在实体中生成静态字段
        generator.setBaseEntityDir("base");             // 实体父类的相对路径
        generator.setBaseEntityFields("id");            // 实体父类存在的字段
        generator.setBaseEntityName("BaseEntity");      // 实体父类的类名
        generator.setEntityDir("entity");               // 实体的相对路径
        generator.setEntityPrefix("");                  // 实体的类名前缀
        generator.setEntitySuffix("");                  // 实体的类名后缀

        generator.setColumnDisSelectable("created_at", "updated_at");             // 字段, 不可查询

        generator.setColumnFill(FieldFill.NotFill.class, "created_at", "updated_at");  // 字段, 填充方式

        generator.setColumnStrategy(FieldStrategy.Default.class, "created_at", "updated_at");   // 字段, 使用策略
        generator.setColumnInsertStrategy(FieldStrategy.Never.class, "created_at", "updated_at");   // 字段, 新增使用策略
        generator.setColumnUpdateStrategy(FieldStrategy.Never.class, "created_at", "updated_at");   // 字段, 更新使用策略
        generator.setColumnConditionStrategy(FieldStrategy.Default.class, "created_at", "updated_at");   // 字段, 条件使用策略

        generator.setColumnConversion(FieldConversion.SimpleValue.class, "created_at", "updated_at");   // 字段, 序列化与反序列化方式

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
        ensureScanPackagesForGenerator();
        ToolModel.gaarasonDataSource = proxyDataSource();
        ToolModel toolModel = new ToolModel();
        AutoGenerator autoGenerator = new AutoGenerator(toolModel);
        // set
        autoGenerator.setEntityStaticField(true);
        autoGenerator.setSpringBoot(Generator.SpringBootVersion.THREE);
        autoGenerator.setCorePoolSize(20);
        autoGenerator.setOutputDir(codegenExportDir());
        autoGenerator.setNamespace("test.data");

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

    public static class ToolModel extends Model<QueryBuilder<ToolModel.Inner, Serializable>, ToolModel.Inner, Serializable> {

        public static GaarasonDataSource gaarasonDataSource;

        @Override
        public GaarasonDataSource getGaarasonDataSource() {
            return gaarasonDataSource;
        }

        public static class Inner implements Serializable {

        }
    }

    public static class AutoGenerator extends Generator {

        private final Model<?, ?, ?> toolModel;

        public AutoGenerator(Model<?, ?, ?> model) {
            toolModel = model;
        }

        @Override
        public Model<?, ?, ?> getModel() {
            return toolModel;
        }

    }

}
