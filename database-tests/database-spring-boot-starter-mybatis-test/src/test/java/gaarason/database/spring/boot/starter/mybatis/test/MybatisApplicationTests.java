package gaarason.database.spring.boot.starter.mybatis.test;

import gaarason.database.eloquent.GeneralModel;
import gaarason.database.spring.boot.starter.mybatis.test.mybatis.mapper.StudentMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@SpringBootTest
@Slf4j
public class MybatisApplicationTests {

    @Resource
    private GeneralModel generalModel;

    @Resource
    @Lazy
    MybatisApplicationTests mybatisApplicationTests;

    @Resource
    private StudentMapper studentMapper;

    @Transactional
    public void doSomething(int id, int age) {

        studentMapper.updateAgeById(id, age);

        Object ageBySelect = generalModel.newQuery().from("student").findOrFail(id).toMap().get("age");

        System.out.println("通过mybatis更新后, age : " + ageBySelect);

        Assertions.assertEquals(age, ageBySelect);

        throw new RuntimeException();
    }


    @Test
    void test1() {
        int id = 1;

        generalModel.newQuery().from("student").where("id", id).data("age",6).update();

        Object age = generalModel.newQuery().from("student").where("id", id).firstOrFail().toMap().get("age");

        System.out.println("原始, age : " + age);


        int newAge = Integer.parseInt(String.valueOf(age)) + 99;

        try {
            mybatisApplicationTests.doSomething(id, newAge);
        } catch (Throwable ignore) {
            System.out.println("异常抛出");

        }
        Object ageBySelect = generalModel.newQuery().from("student").where("id", id).firstOrFail().toMap().get("age");

        System.out.println("事务回滚后, age : " + ageBySelect);

        Assertions.assertEquals(age, ageBySelect);

    }

}
