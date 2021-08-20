package gaarason.database.spring.boot.starter.test.data.model;

import gaarason.database.spring.boot.starter.test.data.entity.Test;
import gaarason.database.spring.boot.starter.test.data.model.base.BaseModel;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

/**
 * @author xt
 */
@Repository
public class TestModel extends BaseModel<Test, Serializable> {

}