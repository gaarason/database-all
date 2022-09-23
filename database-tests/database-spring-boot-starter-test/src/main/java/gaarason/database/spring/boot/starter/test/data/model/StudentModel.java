package gaarason.database.spring.boot.starter.test.data.model;

import gaarason.database.exception.base.BaseException;
import gaarason.database.spring.boot.starter.test.data.entity.Student;
import gaarason.database.spring.boot.starter.test.data.model.base.BaseModel;
import gaarason.database.spring.boot.starter.test.data.repository.StudentQuery;
import org.springframework.stereotype.Repository;

@Repository
public class StudentModel extends BaseModel<Student, Long> implements StudentQuery<Student, Long> {

    @Override
    public Student updateName() {
        int update = newQuery().where("id", "1").data("name", "bgfd").update();

        if (true) {
            throw new BaseException("更新操作执行完, 抛出异常");
        }

        return findOrFail(1L).toObject();
    }

    @Override
    public Student getInfoFromDB() {
        Student student = findOrFail(1L).toObject();
        System.out.println("StudentModel : getInfoFromDB :" + student);
        return student;
    }

    @Override
    public Student father() {
        return null;
    }
}