package gaarason.database.eloquent.relations;

import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.annotations.BelongsToMany;
import gaarason.database.eloquent.annotations.HasMany;
import gaarason.database.eloquent.annotations.HasOne;
import gaarason.database.exception.ModelNewInstanceException;
import gaarason.database.support.Column;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class SubQuery {

    /**
     * 一对一关系
     * @param field
     * @param stringColumnMap
     * @return
     */
    @Nullable
    public static Object dealHasOne(Field field, Map<String, Column> stringColumnMap) {
        HasOne        hasOne      = field.getAnnotation(HasOne.class);
        Model<Object> targetModel = getModelInstance(hasOne.targetModel());
        String        foreignKey  = hasOne.foreignKey();
        String        localKey    = hasOne.localKey();
        localKey = "".equals(localKey) ? targetModel.getPrimaryKeyName() : localKey;
        Record record = targetModel.newQuery()
            .where(localKey, String.valueOf(stringColumnMap.get(foreignKey).getValue()))
            .first();
        return record == null ? null : record.getEntity();
    }

    /**
     * 一对多关系
     * @param field
     * @param stringColumnMap
     * @return
     */
    public static List<Object> dealHasMany(Field field, Map<String, Column> stringColumnMap) {
        HasMany       hasMany     = field.getAnnotation(HasMany.class);
        Model<Object> targetModel = getModelInstance(hasMany.targetModel());
        String        foreignKey  = hasMany.foreignKey();
        String        localKey    = hasMany.localKey();
        localKey = "".equals(localKey) ? targetModel.getPrimaryKeyName() : localKey;
        return targetModel.newQuery()
            .where(foreignKey, String.valueOf(stringColumnMap.get(localKey).getValue()))
            .get().toObjectList();
    }

    /**
     * 多对多关系
     * @param field
     * @param stringColumnMap
     * @return
     */
    public static List<Object> dealBelongsToMany(Field field, Map<String, Column> stringColumnMap) {
        BelongsToMany belongsToMany         = field.getAnnotation(BelongsToMany.class);
        Model<Object> relationModel         = getModelInstance(belongsToMany.relationModel()); // user_teacher
        final String  modelForeignKey       = belongsToMany.modelForeignKey(); // user_id
        final String  modelLocalKey         = belongsToMany.modelLocalKey(); // user.id
        Model<Object> targetModel           = getModelInstance(belongsToMany.targetModel()); // teacher
        final String  targetModelForeignKey = belongsToMany.targetModelForeignKey(); // teacher_id
        final String  targetModelLocalKey   = belongsToMany.targetModelLocalKey();  // teacher.id
        List<Object> targetModelForeignKeyList = relationModel.newQuery()
            .where(modelForeignKey, String.valueOf(stringColumnMap.get(modelLocalKey).getValue()))
            .get()
            .toList(record -> record.toMap().get(targetModelForeignKey));
        return targetModel.newQuery().whereIn(targetModelLocalKey, targetModelForeignKeyList).get().toObjectList();
    }

    /**
     * 获取 model 实例
     * @param modelClass
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T> Model<T> getModelInstance(Class<? extends Model> modelClass) {
        try {
            return modelClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ModelNewInstanceException(e.getMessage());
        }
    }
}
