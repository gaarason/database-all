package gaarason.database.spring.boot.starter.test.data.repository;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.model.Query;
import gaarason.database.spring.boot.starter.test.data.entity.Student;
import org.springframework.transaction.annotation.Transactional;

public interface StudentQuery<T, K> {


    @Transactional()
    Student updateName();

    @Transactional
    Student getInfoFromDB();

}
