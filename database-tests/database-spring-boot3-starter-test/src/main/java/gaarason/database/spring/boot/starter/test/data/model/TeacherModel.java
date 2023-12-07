package gaarason.database.spring.boot.starter.test.data.model;

import gaarason.database.spring.boot.starter.test.data.entity.Student;
import gaarason.database.spring.boot.starter.test.data.entity.Teacher;
import gaarason.database.spring.boot.starter.test.data.model.base.BaseModel;
import gaarason.database.spring.boot.starter.test.data.repository.StudentQuery;
import gaarason.database.spring.boot.starter.test.data.repository.TeacherQuery;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class TeacherModel extends BaseModel<Teacher, Integer> implements TeacherQuery<Teacher, Integer> {


    @Resource
    StudentQuery<Student, Integer> studentModel;

    @Override
    public Student updateName() {

        studentModel.getInfoFromDB();
        try {

            studentModel.updateName();
        } catch (Exception ignore) {

        }


        return studentModel.getInfoFromDB();
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