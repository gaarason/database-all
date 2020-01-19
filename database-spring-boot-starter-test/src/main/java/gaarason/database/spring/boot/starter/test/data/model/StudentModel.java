package gaarason.database.spring.boot.starter.test.data.model;

import gaarason.database.spring.boot.starter.test.data.entity.Student;
import gaarason.database.spring.boot.starter.test.data.model.base.BaseModel;
import gaarason.database.spring.boot.starter.test.data.repository.StudentRepository;
import org.springframework.stereotype.Repository;

@Repository
public class StudentModel extends BaseModel<Student> implements StudentRepository {

}