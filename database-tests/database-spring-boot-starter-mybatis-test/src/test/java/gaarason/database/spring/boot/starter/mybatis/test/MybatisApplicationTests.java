package gaarason.database.spring.boot.starter.mybatis.test;

import gaarason.database.eloquent.GeneralModel;
import gaarason.database.spring.boot.starter.mybatis.test.mybatis.mapper.StudentMapper;
import gaarason.database.spring.boot.starter.mybatis.test.service.TService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
@Slf4j
public class MybatisApplicationTests {

    @Resource
    private GeneralModel generalModel;

    @Resource
    private TService tService;

    @Resource
    private StudentMapper studentMapper;

    @Test
    void testRollback() throws TestException {
        int id = 1;

        generalModel.newQuery().from("student").where("id", id).data("age", 6).update();

        Object age = generalModel.newQuery().from("student").where("id", id).firstOrFail().toMap().get("age");

        System.out.println("原始, age : " + age);

        int newAge = Integer.parseInt(String.valueOf(age)) + 99;

        try {
            tService.doSomethingError(id, newAge);
        } catch (TestException e) {
            System.out.println("意料中的异常抛出");
        }
        Object ageBySelect = generalModel.newQuery().from("student").where("id", id).firstOrFail().toMap().get("age");

        System.out.println("事务回滚后, age : " + ageBySelect);

        Assertions.assertEquals(age, ageBySelect);
    }

    @Test
    void testCommit() throws TestException {
        int id = 1;

        generalModel.newQuery().from("student").where("id", id).data("age", 6).update();

        Object age = generalModel.newQuery().from("student").where("id", id).firstOrFail().toMap().get("age");

        System.out.println("原始, age : " + age);

        int newAge = Integer.parseInt(String.valueOf(age)) + 55;

        tService.doSomething(id, newAge);

        Object ageBySelectV2 = generalModel.newQuery().from("student").where("id", id).firstOrFail().toMap().get("age");

        System.out.println("事务提交后, age : " + ageBySelectV2);

        Assertions.assertEquals(newAge, ageBySelectV2);
    }

    @Test
    void testManyRun() throws TestException {
        int id = 1;
        generalModel.newQuery().from("student").where("id", id).data("age", 6).update();

        for (int i = 0; i < 99; i++) {
            Object age = generalModel.newQuery().from("student").where("id", id).firstOrFail().toMap().get("age");
            log.info("原始, age : " + age);
            int newAge = Integer.parseInt(String.valueOf(age)) + 1;
            tService.doSomething(id, newAge);
            // 事务外查询
            log.info("事务外 mybatis 查询次数: " + (i + 1));
            int selectById = studentMapper.selectById(id);
            Assertions.assertEquals(newAge, selectById);
        }
    }

}
