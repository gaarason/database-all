package gaarason.database.spring.boot.starter.test;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.support.ShowType;
import gaarason.database.eloquent.GeneralModel;
import gaarason.database.generator.GeneralGenerator;
import gaarason.database.generator.Generator;
import gaarason.database.query.MySqlBuilder;
import gaarason.database.spring.boot.starter.test.data.entity.Student;
import gaarason.database.spring.boot.starter.test.data.entity.Teacher;
import gaarason.database.spring.boot.starter.test.data.entity.TestEntity;
import gaarason.database.spring.boot.starter.test.data.model.StudentModel;
import gaarason.database.spring.boot.starter.test.data.repository.StudentQuery;
import gaarason.database.spring.boot.starter.test.data.repository.TeacherQuery;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.JVM)
@Slf4j
public class DruidApplicationTests {

    @Resource
    GeneralGenerator generator;

    @Resource
    GeneralModel generalModel;

    @Resource
    TestEntity.Model testEntityModel;

    @Resource
    StudentQuery<Student, Long> studentQuery;

    @Resource
    TeacherQuery<Teacher, Integer> teacherQuery;

    @Resource
    StudentModel studentModel;

    @Test
    public void 生成代码() {
        // set
        generator.setOutputDir("./src/test/java/");     // 所有生成文件的路径
        generator.setNamespace("data");                 // 所有生成文件的所属命名空间
        generator.setCorePoolSize(20);                  // 所用的线程数
        generator.setSpringBoot(Generator.SpringBootVersion.THREE);    // 是否生成spring boot3相关注解
        generator.setSwagger(true);                     // 是否生成swagger相关注解
        generator.setValidator(true);                   // 是否生成validator相关注解
        generator.setJdkDependVersion(Generator.JdkDependVersion.JAKARTA);  // jdk依赖使用的包是 javax 还是 jakarta ?

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
//        generator.setOutputDir("./src/test/java/");
        generator.setOutputDir("./src/test/java1/");
        generator.setNamespace("test.data");

        generator.run();
    }

    @Test
    public void 简单查询_通用_generalModel() {
        Record<GeneralModel.Table, Serializable> first = generalModel.newQuery()
            .from("student")
            .where("id", "3")
            .firstOrFail();
        Assert.assertNotNull(first);
        Map<String, Object> stringObjectMap = first.toMap();
        Assert.assertEquals(stringObjectMap.get("id").toString(), "3");
        System.out.println(stringObjectMap);
    }

    @Test
    public void 简单插入_返回自增id() {
        List<Object> vList = new ArrayList<>();
        vList.add("aaaccc");
        Object id = generalModel.newQuery().from("student").column("name").value(vList).insertGetId();
        Assert.assertNotNull(id);

        Object studentId = generalModel.newQuery().from("student").insertGetId();
        Assert.assertNotNull(studentId);

        Assert.assertEquals(Integer.parseInt(studentId.toString()) - 1, Integer.parseInt(id.toString()));
    }

    @Test
    public void 分页_关系() {
        GaarasonDataSource gaarasonDataSource = studentModel.getGaarasonDataSource();
        studentModel.newQuery().with("relationshipStudentTeachers", builder -> builder.showType(
                new ShowType<MySqlBuilder<Student, Long>>() {}).limit(3)).paginate(1, 7);
    }

    @Test
    public void 使用接口调用() {
        try {
            Student student = studentQuery.updateName();
            System.out.println(student);

        } catch (Throwable e) {
            e.printStackTrace();
        }

        Student student = studentQuery.getInfoFromDB();
        System.out.println(student);
    }

    @Test
    public void sss() {
        Student student = teacherQuery.updateName();
        System.out.println("end : " + student);
    }

}