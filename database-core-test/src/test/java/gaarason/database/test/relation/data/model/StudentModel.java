package gaarason.database.test.relation.data.model;

import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.test.relation.data.model.base.BaseModel;
import gaarason.database.test.relation.data.pojo.Student;
import gaarason.database.test.relation.data.pojo.Teacher;

public class StudentModel extends BaseModel<Student, Long> {

//    public Teacher teacher(){
//        return null;
//    }
//
//    protected <T, K> T hasOne(Model<T, K> model, String idName, String idName2){
//        Record<T, K> first = model.newQuery().where(model.getPrimaryKeyName(), "").first();
//        return first == null ? null : first.getEntity();
//    }

}

