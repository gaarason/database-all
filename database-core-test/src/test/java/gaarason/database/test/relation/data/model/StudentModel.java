package gaarason.database.test.relation.data.model;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.model.Query;
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

    public <RT, RK> Builder<RT, RK> hasMany(Class<? extends Query<RT, RK>> repository, String foreignKey,
                                            String localKey) {
        return null;
    }


    public <RT, RK> Builder<RT, RK> hasMany(Query<RT, RK> query, String foreignKey,
                                            String localKey) {
//        repository.newQuery().where(foreignKey, localKey)

        return null;
    }

    public Teacher teacher() {
        hasMany(TeacherModel.class, "teacher_id", "id");
        return null;
    }

}

