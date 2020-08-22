package gaarason.database.eloquent.relations;

import gaarason.database.contracts.eloquent.relations.SubQuery;
import gaarason.database.eloquent.Model;
import gaarason.database.exception.ModelNewInstanceException;
import gaarason.database.support.Column;
import gaarason.database.utils.EntityUtil;

import java.util.*;

/**
 * 关联关系
 */
abstract public class BaseSubQuery implements SubQuery {

    /**
     * 获取 model 实例
     * @param modelClass Model类
     * @return Model实例
     */
    protected static Model<?, ?> getModelInstance(Class<? extends Model<?, ?>> modelClass) {
        try {
            // todo 判断是spring项目, 则从容器中查找实例
            return modelClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ModelNewInstanceException(e.getMessage(), e);
        }
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
     * @param fieldName              对象的属性的名
     * @param fieldTargetValue       对象的属性的目标值
     * @return 对象列表
     */
    protected static List<?> findObjList(List<?> relationshipObjectList, String fieldName, String fieldTargetValue) {
        List<Object> objectList = new ArrayList<>();
        for (Object o : relationshipObjectList) {
            Object fieldByColumn = EntityUtil.getFieldValueByColumn(o, fieldName);
            // 满足则加入
            if (fieldTargetValue.equals(fieldByColumn.toString())) {
                objectList.add(o);
            }
        }
        return objectList;
    }
}
