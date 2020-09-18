package gaarason.database.eloquent.relation;

import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.eloquent.relation.RelationSubQuery;
import gaarason.database.eloquent.RecordListBean;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.Column;
import gaarason.database.util.ObjectUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * 关联关系
 */
abstract public class BaseRelationSubQuery implements RelationSubQuery {

    @Override
    public RecordList<?, ?> dealBatchPrepare(String sql1) {
        return new RecordListBean<>();
    }

    /**
     * 获取 model 实例
     * @param field 属性信息
     * @return Model实例
     */
    protected static Model<?, ?> getModelInstance(Field field) {
        Class<?> clazz = ObjectUtil.isCollection(field.getType()) ?
            ObjectUtil.getGenerics((ParameterizedType) field.getGenericType(), 0) :
            field.getType();
        return ModelShadowProvider.getByEntity(clazz).getModel();
    }

    /**
     * 获取 model 实例
     * @param modelClass Model类
     * @return Model实例
     */
    protected static Model<?, ?> getModelInstance(Class<? extends Model<?, ?>> modelClass) {
        return ModelShadowProvider.getByModel(ObjectUtil.typeCast(modelClass)).getModel();
    }

    /**
     * 将数据源中的某一列,转化为可以使用 where in 查询的 set
     * @param stringColumnMapList 数据源
     * @param column              目标列
     * @return 目标列的集合
     */
    protected static Set<Object> getColumnInMapList(List<Map<String, Column>> stringColumnMapList, String column) {
        Set<Object> result = new HashSet<>();
        for (Map<String, Column> stringColumnMap : stringColumnMapList) {
            result.add(stringColumnMap.get(column).getValue());
        }
        return result;
    }

    /**
     * 将满足条件的对象筛选并返回
     * @param relationshipObjectList 待筛选的对象列表
     * @param columnName             对象的属性的名
     * @param fieldTargetValue       对象的属性的目标值
     * @return 对象列表
     */
    protected static List<?> findObjList(List<?> relationshipObjectList, String columnName, String fieldTargetValue) {
        List<Object> objectList = new ArrayList<>();
        if (relationshipObjectList.size() > 0) {
            // 模型信息
            ModelShadowProvider.ModelInfo<?, Object> modelInfo = ModelShadowProvider.getByEntity(
                relationshipObjectList.get(0).getClass());
            // 字段信息
            ModelShadowProvider.FieldInfo fieldInfo = modelInfo.getColumnFieldMap().get(columnName);

            for (Object o : relationshipObjectList) {
                // 值
                Object fieldValue = ModelShadowProvider.fieldGet(fieldInfo, o);
                // 满足则加入
                if (fieldTargetValue.equals(String.valueOf(fieldValue))) {
                    objectList.add(o);
                }
            }

        }
        return objectList;
    }
}
