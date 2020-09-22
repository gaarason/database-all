package gaarason.database.spring.boot.starter.test.data.model;

import gaarason.database.spring.boot.starter.test.data.entity.Student;
import gaarason.database.spring.boot.starter.test.data.model.base.BaseModel;
import gaarason.database.spring.boot.starter.test.data.repository.StudentQuery;
import org.springframework.stereotype.Repository;

@Repository
public class StudentModel extends BaseModel<Student, Long> implements StudentQuery<Student, Long> {

    @Override
    public Student updateName() {
        int update = newQuery().where("id", "1").data("name", "222").update();

        return findOrFail(1L).toObject();
    }

    @Override
    public Student getInfoFromDB() {

        return findOrFail(1L).toObject();
    }


//    @Override
//    protected Builder<Student, Long> apply(Builder<Student, Long> builder) {
//        // return builder->where("type", "2");
//        return builder;
//    }
}