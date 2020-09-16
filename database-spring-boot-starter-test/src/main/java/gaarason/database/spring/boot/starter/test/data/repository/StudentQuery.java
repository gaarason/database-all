package gaarason.database.spring.boot.starter.test.data.repository;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.model.Query;
import gaarason.database.spring.boot.starter.test.data.entity.Student;

public interface StudentQuery<T, K> {


    Record<T, K> getInfoFromDB();

}
