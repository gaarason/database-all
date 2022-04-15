package gaarason.database.spring.boot.starter.test.data.model;

import gaarason.database.spring.boot.starter.test.data.entity.Student;
import gaarason.database.spring.boot.starter.test.data.entity.Teacher;
import gaarason.database.spring.boot.starter.test.data.model.base.BaseModel;
import gaarason.database.spring.boot.starter.test.data.repository.StudentQuery;
import gaarason.database.spring.boot.starter.test.data.repository.TeacherQuery;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class TeacherModel extends BaseModel<Teacher, Integer> implements TeacherQuery<Teacher, Integer> {


    @Resource
    StudentQuery<Teacher, Integer> studentQuery;

    @Override
    public Student updateName() {

        studentQuery.getInfoFromDB();
        try {

            studentQuery.updateName();
        }catch (Exception e) {

        }

        Student student = studentQuery.getInfoFromDB();


        return student;
    }

    @Override
    public Student getInfoFromDB() {
        return null;
    }

    @Override
    public Student father() {
        return null;
    }
}