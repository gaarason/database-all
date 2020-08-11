package gaarason.database.eloquent.relations;

import gaarason.database.contracts.eloquent.relations.SubQuery;
import gaarason.database.eloquent.Model;
import gaarason.database.exception.ModelNewInstanceException;
import gaarason.database.support.Column;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            throw new ModelNewInstanceException(e.getMessage());
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
}
