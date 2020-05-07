package gaarason.database.eloquent.relations;

import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.annotations.BelongsTo;
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
    public static <T, K> T dealHasOne(Field field, Map<String, Column> stringColumnMap) {
        HasOne   hasOne      = field.getAnnotation(HasOne.class);
        Model<T, K> targetModel = getModelInstance(hasOne.targetModel());
        String   foreignKey  = hasOne.foreignKey();
        String   localKey    = hasOne.localKey();
        localKey = "".equals(localKey) ? targetModel.getPrimaryKeyColumnName() : localKey;
        Record<T, K> record = targetModel.newQuery()
            .where(localKey, String.valueOf(stringColumnMap.get(foreignKey).getValue()))
            .first();
        return record == null ? null : record.toObjectWithoutRelationship();
    }

    /**
     * 一对多关系
     * @param field
     * @param stringColumnMap
     * @return
     */
    public static <T, K> List<T> dealHasMany(Field field, Map<String, Column> stringColumnMap) {
        HasMany  hasMany     = field.getAnnotation(HasMany.class);
        Model<T, K> targetModel = getModelInstance(hasMany.targetModel());
        String   foreignKey  = hasMany.foreignKey();
        String   localKey    = hasMany.localKey();
        localKey = "".equals(localKey) ? targetModel.getPrimaryKeyColumnName() : localKey;
        return targetModel.newQuery()
            .where(foreignKey, String.valueOf(stringColumnMap.get(localKey).getValue()))
            .get().toObjectWithoutRelationship();
    }

    /**
     * 多对多关系
     * @param field
     * @param stringColumnMap
     * @return
     */
    public static <T, K> List<T> dealBelongsToMany(Field field, Map<String, Column> stringColumnMap) {
        BelongsToMany belongsToMany         = field.getAnnotation(BelongsToMany.class);
        Model<T, K>      relationModel         = getModelInstance(belongsToMany.relationModel()); // user_teacher
        final String  modelForeignKey       = belongsToMany.modelForeignKey(); // user_id
        final String  modelLocalKey         = belongsToMany.modelLocalKey(); // user.id
        Model<T, K>      targetModel           = getModelInstance(belongsToMany.targetModel()); // teacher
        final String  targetModelForeignKey = belongsToMany.targetModelForeignKey(); // teacher_id
        final String  targetModelLocalKey   = belongsToMany.targetModelLocalKey();  // teacher.id
        List<Object> targetModelForeignKeyList = relationModel.newQuery()
            .where(modelForeignKey, String.valueOf(stringColumnMap.get(modelLocalKey).getValue()))
            .get()
            .toList(record -> record.toMap().get(targetModelForeignKey));
        return targetModel.newQuery().whereIn(targetModelLocalKey, targetModelForeignKeyList).get().toObjectWithoutRelationship();
    }

    /**
     * 多对多关系
     * @param field
     * @param stringColumnMap
     * @return
     */
    public static <T, K> T dealBelongsTo(Field field, Map<String, Column> stringColumnMap) {
        BelongsTo belongsTo   = field.getAnnotation(BelongsTo.class);
        Model<T, K>  parentModel = getModelInstance(belongsTo.parentModel()); // user_teacher
        String    foreignKey  = belongsTo.foreignKey();
        String    localKey    = belongsTo.localKey();
        localKey = "".equals(localKey) ? parentModel.getPrimaryKeyColumnName() : localKey;
        Record<T, K> record = parentModel.newQuery()
            .where(localKey, String.valueOf(stringColumnMap.get(foreignKey).getValue()))
            .first();
        return record == null ? null : record.toObjectWithoutRelationship();
    }

    /**
     * 获取 model 实例
     * @param modelClass
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T, K> Model<T, K> getModelInstance(Class<? extends Model> modelClass) {
        try {
            return modelClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ModelNewInstanceException(e.getMessage());
        }
    }
}
