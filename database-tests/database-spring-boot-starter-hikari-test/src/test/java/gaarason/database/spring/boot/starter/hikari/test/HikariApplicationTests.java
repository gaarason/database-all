package gaarason.database.spring.boot.starter.hikari.test;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.eloquent.GeneralModel;
import gaarason.database.generator.GeneralGenerator;
import gaarason.database.generator.Generator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spring Boot 集成样例: GeneralModel/Generator 与 Hikari 数据源联调.
 * <p>
 * {@link #生成代码()}、{@link #run有参构造()} 仅用于向 {@code target/generated-code-export} 导出供他项拷贝的生成代码,非本模块编译源.
 */
@SpringBootTest(classes = HikariApplication.class)
@Slf4j
public class HikariApplicationTests {

    /**
     * 有参构造的 Generator 走系统属性构建配置，需保证包扫描覆盖默认自动配置包。
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
     * 生成供其它项目拷贝的样例代码，勿写入 {@code src/test/java}，否则会参与本模块编译。
     */
    private static String codegenExportDir() {
        String basedir = System.getProperty("basedir", ".");
        return new File(basedir, "target/generated-code-export").getAbsolutePath();
    }

    @Resource
    GeneralGenerator generator;

    @Resource
    GeneralModel generalModel;


    @Test
    public void 生成代码() {
        // set
        generator.setOutputDir(codegenExportDir());     // 所有生成文件的路径
        generator.setNamespace("data");                 // 所有生成文件的所属命名空间
        generator.setCorePoolSize(20);                  // 所用的线程数
        generator.setSpringBoot(Generator.SpringBootVersion.THREE);                  // 是否生成spring boot相关注解
        generator.setSwagger(true);                     // 是否生成swagger相关注解
        generator.setValidator(true);                   // 是否生成validator相关注解

        generator.setEntityStaticField(true);           // 是否在实体中生成静态字段
        generator.setBaseEntityDir("base");             // 实体父类的相对路径
        generator.setBaseEntityFields("id");            // 实体父类存在的字段
        generator.setBaseEntityName("BaseEntity");      // 实体父类的类名
        generator.setEntityDir("entity");               // 实体的相对路径
        generator.setEntityPrefix("");                  // 实体的类名前缀
        generator.setEntitySuffix("");                  // 实体的类名后缀


        generator.setBaseModelDir("base");              // 模型父类的相对路径
        generator.setBaseModelName("BaseModel");        // 模型父类的类名
        generator.setModelDir("model");                 // 模型的相对路径
        generator.setModelPrefix("");                   // 模型的类名前缀
        generator.setModelSuffix("Model");              // 模型的类名后缀

        // 执行
        generator.run();
    }

    @Test
    public void run有参构造() {
        ensureScanPackagesForGenerator();
        String jdbcUrl = "jdbc:mysql://mysql.local/test_master_0?useUnicode=true&characterEncoding=utf-8" +
            "&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "root";
        Generator generator = new Generator(jdbcUrl, username, password);

        // set
        generator.setEntityStaticField(true);
        generator.setSpringBoot(Generator.SpringBootVersion.THREE);                // 是否生成spring boot相关注解
        generator.setSwagger(true);                   // 是否生成swagger相关注解
        generator.setValidator(true);                 // 是否生成validator相关注解
        generator.setCorePoolSize(20);
        generator.setOutputDir(codegenExportDir());
        generator.setNamespace("test.data");

        generator.run();
    }

    @Test
    public void 简单查询_通用() {
        Record<GeneralModel.Table, Serializable> first = generalModel.newQuery()
            .from("student")
            .where("id", "3")
            .first();
        Assertions.assertNotNull(first);
        Map<String, Object> stringObjectMap = first.toMap();
        Assertions.assertEquals(stringObjectMap.get("id").toString(), "3");
        System.out.println(stringObjectMap);
    }

    @Test
    public void 简单插入_返回自增id() {
        List<Object> vList = new ArrayList<>();
        vList.add("aaaccc");
        // INSERT 的列名来自 column 片段, select 只参与 SELECT 片段
        Object id = generalModel.newQuery().from("student").column("name").value(vList).insertGetId();
        Assertions.assertNotNull(id);

        Object studentId = generalModel.newQuery().from("student").insertGetId();
        Assertions.assertNotNull(studentId);

        Assertions.assertEquals(Integer.parseInt(studentId.toString()) - 1, Integer.parseInt(id.toString()));
    }

}