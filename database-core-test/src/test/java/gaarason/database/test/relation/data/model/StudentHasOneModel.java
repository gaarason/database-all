package gaarason.database.test.relation.data.model;

import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.exception.ModelNewInstanceException;
import gaarason.database.test.relation.data.model.base.BaseModel;
import gaarason.database.test.relation.data.pojo.StudentHasOne;
import gaarason.database.test.relation.data.pojo.Teacher;

public class StudentHasOneModel extends BaseModel<StudentHasOne, Long> {

//    public Teacher teacher(){
//        return hasOne(TeacherModel.class, "teacher_id", "id");
//    }
//
//    protected <T, K> T hasOne(Class<? extends Model> modelClass, String idName, String idName2){
//
//        Model model = getModelInstance(modelClass);
//
//        Record<T, K> first = model.newQuery().where(model.getPrimaryKeyName(), "").first();
//        return first == null ? null : first.getEntity();
//    }
//
//
//    /**
//     * 获取 model 实例
//     * @param relationshipModelClass
//     * @return
//     */
//    private static Model getModelInstance(Class<? extends Model> relationshipModelClass) {
//        try {
//            return relationshipModelClass.newInstance();
//        } catch (InstantiationException | IllegalAccessException e) {
//            throw new ModelNewInstanceException(e.getMessage());
//        }
//    }
}

