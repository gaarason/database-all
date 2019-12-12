package gaarason.database.spring.boot.starter.test;

import gaarason.database.eloquent.GeneralModel;
import gaarason.database.eloquent.Record;
import gaarason.database.spring.boot.starter.test.data.model.StudentModel;
import gaarason.database.spring.boot.starter.test.data.pojo.Student;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.JVM)
@Slf4j
public class TestApplicationTests {

    @Resource
    GeneralModel generalModel;

    @Resource
    StudentModel studentModel;

    @Test
    public void 简单查询() {
        Record<GeneralModel.Table> first = generalModel.newQuery().from("student").where(Student.ID, "3").first();
//        Record<Student> first = studentModel.newQuery().where(Student.ID, "3").first();
        Assert.assertNotNull(first);
        Map<String, Object> stringObjectMap = first.toMap();
        Assert.assertEquals((long) stringObjectMap.get("id"), 3);
        System.out.println(stringObjectMap);
    }

}