package gaarason.database.spring.boot.starter.test.data.model;

import gaarason.database.spring.boot.starter.test.data.entity.Student;
import gaarason.database.spring.boot.starter.test.data.model.base.BaseModel;
import gaarason.database.spring.boot.starter.test.data.repository.StudentQuery;
import org.springframework.stereotype.Repository;

@Repository
public class StudentModel extends BaseModel<Student, Long> implements StudentQuery {

//    @Override
//    protected Builder<Student, Long> apply(Builder<Student, Long> builder) {
//        // return builder->where("type", "2");
//        return builder;
//    }
}