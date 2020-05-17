package gaarason.database.eloquent.relations;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.annotations.BelongsTo;
import gaarason.database.eloquent.annotations.BelongsToMany;
import gaarason.database.eloquent.annotations.HasMany;
import gaarason.database.exception.ModelNewInstanceException;
import gaarason.database.support.Column;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 关联关系
 */
public class SubQuery {

    /**
     * 获取 model 实例
     * @param modelClass Model类
     * @return Model实例
     */
    @SuppressWarnings("unchecked")
    protected static <T, K> Model<T, K> getModelInstance(Class<? extends Model> modelClass) {
        try {
            return modelClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ModelNewInstanceException(e.getMessage());
        }
    }

    /**
     * 将数据源中的某一列,转化为可以使用 where in 查询的 set
     * @param stringColumnMapList 数据源
     * @param column 目标列
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
