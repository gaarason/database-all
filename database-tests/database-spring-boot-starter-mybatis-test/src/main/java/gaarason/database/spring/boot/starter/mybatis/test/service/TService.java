package gaarason.database.spring.boot.starter.mybatis.test.service;

import gaarason.database.eloquent.GeneralModel;
import gaarason.database.spring.boot.starter.mybatis.test.TestException;
import gaarason.database.spring.boot.starter.mybatis.test.mybatis.mapper.StudentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Slf4j
public class TService {

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private GeneralModel generalModel;

    @Transactional(rollbackFor = TestException.class)
    public void doSomethingError(int id, int age) throws TestException {

        studentMapper.updateAgeById(id, age);

        Object ageBySelect = generalModel.newQuery().from("student").where("id", id).firstOrFail().toMap().get("age");

        System.out.println("通过mybatis更新后, age : " + ageBySelect);

        throw new TestException();
    }

    @Transactional(rollbackFor = TestException.class)
    public void doSomething(int id, int age) throws TestException {

        studentMapper.updateAgeById(id, age);

        Object ageBySelect = generalModel.newQuery().from("student").where("id", id).firstOrFail().toMap().get("age");

        int newAge = studentMapper.selectById(id);

        log.info("通过mybatis更新后, age : " + ageBySelect + ", newAge : " + newAge);
    }
}