package gaarason.database.spring.boot.starter.test;

import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.eloquent.GeneralModel;
import gaarason.database.generator.GeneralGenerator;
import gaarason.database.generator.Generator;
import gaarason.database.spring.boot.starter.test.data.entity.Student;
import gaarason.database.spring.boot.starter.test.data.model.StudentModel;
import gaarason.database.spring.boot.starter.test.data.model.TeacherModel;
import gaarason.database.spring.boot.starter.test.data.repository.StudentQuery;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.JVM)
@Slf4j
public class TestApplicationTests {

    @Resource
    GeneralGenerator generalGenerator;

    @Resource
    GeneralModel generalModel;

    @Resource
    StudentQuery<Student, Long> studentQuery;

    @Resource
    Model<Student, Long> studentModel;

    @Resource
    TeacherModel teacherModel;

    @Test
    public void 生成代码() {
        // 设置
        generalGenerator.setStaticField(true);
        generalGenerator.setIsSpringBoot(false);
        generalGenerator.setOutputDir("./src/main/java/");
        generalGenerator.setNamespace("gaarason.database.test.relation.data");
        String[] disableCreate = {"created_at", "updated_at"};
        generalGenerator.setDisInsertable(disableCreate);
        String[] disableUpdate = {"created_at", "updated_at"};
        generalGenerator.setDisUpdatable(disableUpdate);

        generalGenerator.run();
    }

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
    public void 简单查询_通用() {
        Record<GeneralModel.Table, Object> first = generalModel.newQuery().from("student").where("id", "3").first();
        Assert.assertNotNull(first);
        Map<String, Object> stringObjectMap = first.toMap();
        Assert.assertEquals(stringObjectMap.get("id"), 3);
        System.out.println(stringObjectMap);
    }

    @Test
    public void 简单插入_返回自增id() {
        List<String> vList = new ArrayList<>();
        vList.add("aaaccc");
        Object id = generalModel.newQuery().from("student").select("name").value(vList).insertGetId();
        Assert.assertNotNull(id);

        Object studentId = generalModel.newQuery().from("student").insertGetId();
        Assert.assertNotNull(studentId);

        Assert.assertEquals(Integer.parseInt(studentId.toString()) - 1, Integer.parseInt(id.toString()));
    }

    @Test
    public void 使用接口调用(){
        try{
            Student student = studentQuery.updateName();
            System.out.println(student);

        }catch (Throwable ignored){

        }

        Student student = studentQuery.getInfoFromDB();
        System.out.println(student);
    }

//    @Test
//    public void 模型在spring中是容器获取的单例(){
//        Record<Student, Long> infoFromDB = studentQuery.getInfoFromDB();
//        Student               student    = infoFromDB.toObject();
//        System.out.println(student);
//
//        Model<Student, Long> model1 = ModelShadowProvider.getByModelClass(StudentModel.class).getModel();
//        Model<Student, Object> model2 = ModelShadowProvider.getByEntityClass(Student.class).getModel();
//
//        System.out.println(model1.getClass());
//        System.out.println(model2.getClass());
//        System.out.println(studentModel.getClass());
//        System.out.println(studentQuery.getClass());
//        Assert.assertSame(model1, model2);
//        Assert.assertSame(model2, studentModel);
//        Assert.assertSame(model2, studentQuery);
//
//    }

}