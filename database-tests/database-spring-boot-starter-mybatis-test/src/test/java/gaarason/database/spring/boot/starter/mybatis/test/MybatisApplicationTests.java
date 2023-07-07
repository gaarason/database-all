package gaarason.database.spring.boot.starter.mybatis.test;

import gaarason.database.eloquent.GeneralModel;
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

    @Test
    void test1() {
        int id = 1;

        generalModel.newQuery().from("student").where("id", id).data("age", 6).update();

        Object age = generalModel.newQuery().from("student").where("id", id).firstOrFail().toMap().get("age");

        System.out.println("原始, age : " + age);


        int newAge = Integer.parseInt(String.valueOf(age)) + 99;

        try {
            tService.doSomething(id, newAge);
        } catch (TestException e) {
            System.out.println("意料中的异常抛出");
        }
        Object ageBySelect = generalModel.newQuery().from("student").where("id", id).firstOrFail().toMap().get("age");

        System.out.println("事务回滚后, age : " + ageBySelect);

        Assertions.assertEquals(age, ageBySelect);

    }

}
