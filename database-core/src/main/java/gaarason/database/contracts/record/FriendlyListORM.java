package gaarason.database.contracts.record;

//import com.fasterxml.jackson.core.JsonProcessingException;
import gaarason.database.contracts.function.FilterRecordAttribute;

import java.util.List;
import java.util.Map;

/**
 * 结果友好转化
 * @param <T> 实体类
 * @param <K> 主键类型
 */
public interface FriendlyListORM<T, K> {

    /**
     * 转化为对象列表
     * @return 对象列表
     */
    List<T> toObjectList();

    /**
     * 转化为map list
     * @return mapList
     */
    List<Map<String, Object>> toMapList();

    /**
     * 过滤成list
     * @return 单个字段列表
     */
    List<Object> toList(FilterRecordAttribute<T, K> filterRecordAttribute);
}
