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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = HikariApplication.class)
@Slf4j
public class HikariApplicationTests {

    @Resource
    GeneralGenerator generator;

    @Resource
    GeneralModel generalModel;



    @Test
    public void 生成代码() {
        // set
//        generator.setOutputDir("./src/test/java/");     // 所有生成文件的路径
        generator.setOutputDir("./src/test/java1/");     // 所有生成文件的路径
        generator.setNamespace("data");                 // 所有生成文件的所属命名空间
        generator.setCorePoolSize(20);                  // 所用的线程数
        generator.setSpringBoot(true);                  // 是否生成spring boot相关注解
        generator.setSwagger(true);                     // 是否生成swagger相关注解
        generator.setValidator(true);                   // 是否生成validator相关注解

        generator.setEntityStaticField(true);           // 是否在实体中生成静态字段
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
    public void run有参构造() {
        String jdbcUrl = "jdbc:mysql://mysql.local/test_master_0?useUnicode=true&characterEncoding=utf-8" +
            "&zeroDateTimeBehavior=convertToNull&useSSL=true&autoReconnect=true&serverTimezone=Asia/Shanghai";
        String    username  = "root";
        String    password  = "root";
        Generator generator = new Generator(jdbcUrl, username, password);

        // set
        generator.setEntityStaticField(true);
        generator.setSpringBoot(true);                // 是否生成spring boot相关注解
        generator.setSwagger(true);                   // 是否生成swagger相关注解
        generator.setValidator(true);                 // 是否生成validator相关注解
        generator.setCorePoolSize(20);
//        generator.setOutputDir("./src/test/java/");
        generator.setOutputDir("./src/test/java1/");
        generator.setNamespace("test.data");
        generator.setDisInsertable("created_at", "updated_at");
        generator.setDisUpdatable("created_at", "updated_at");

        generator.run();
    }

    @Test
    public void 简单查询_通用() {
        Record<GeneralModel.Table, Serializable> first = generalModel.newQuery().from("student").where("id", "3").first();
        Assertions.assertNotNull(first);
        Map<String, Object> stringObjectMap = first.toMap();
        Assertions.assertEquals(stringObjectMap.get("id").toString(), "3");
        System.out.println(stringObjectMap);
    }

    @Test
    public void 简单插入_返回自增id() {
        List<Object> vList = new ArrayList<>();
        vList.add("aaaccc");
        Object id = generalModel.newQuery().from("student").select("name").value(vList).insertGetId();
        Assertions.assertNotNull(id);

        Object studentId = generalModel.newQuery().from("student").insertGetId();
        Assertions.assertNotNull(studentId);

        Assertions.assertEquals(Integer.parseInt(studentId.toString()) - 1, Integer.parseInt(id.toString()));
    }

}