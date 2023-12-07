package gaarason.database.spring.boot.starter.test.data.repository;

import gaarason.database.spring.boot.starter.test.data.entity.Student;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface StudentQuery<T, K> {


//    @Transactional(rollbackFor = Throwable.class)

    @Transactional(propagation = Propagation.NESTED, rollbackFor = Throwable.class)
    Student updateName();

    @Transactional(propagation = Propagation.NESTED, rollbackFor = Throwable.class)
    Student getInfoFromDB();

    @Transactional(propagation = Propagation.NESTED, rollbackFor = Throwable.class)
    Student father();

}
