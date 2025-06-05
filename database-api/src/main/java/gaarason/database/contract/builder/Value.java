package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.BuilderWrapper;
import gaarason.database.lang.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * 值
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Value<B extends Builder<B, T, K>, T, K> {

    /**
     * 加入sql片段
     * @param sqlPart sql片段
     * @param parameters 参数绑定列表
     * @return 查询构造器
     */
    B valueRaw(@Nullable String sqlPart, @Nullable Collection<?> parameters);

    /**
     * 加入sql片段
     * @param sqlPart sql片段
     * @return 查询构造器
     */
    B valueRaw(@Nullable String sqlPart);

    /**
     * 加入sql片段集合
     * @param sqlParts sql片段集合
     * @return 查询构造器
     */
    B valueRaw(@Nullable Collection<String> sqlParts);

    /**
     * 插入数据使用
     * 需要手动调用 column() 方法
     * @param values 值列表
     * @return 查询构造器
     */
    B value(@Nullable Collection<?> values);

    /**
     * 插入数据使用
     * 已自动调用 column() 方法
     * @param entityMap 值列表map, 建议使用顺序稳定的map, eg:LinkedHashMap
     * @return 查询构造器
     */
    B value(@Nullable Map<String, Object> entityMap);

    /**
     * 插入数据使用
     * 已自动调用 column() 方法
     * @param anyEntity 实体
     * @return 查询构造器
     */
    B value(Object anyEntity);

    /**
     * 批量插入数据使用
     * 性能而言, `值列表类型(list<list<String>>)` 要好于 `map类型(list<map<String, Object>>)` 要好于 `实体`,
     * 因为`实体`先转化为`map`类型再转化为`值列表`,
     * 且在上述处理的过程中, 会根据`实体`上的注解, 进行策略判断,数值填充,序列化等操作, 均会有所消耗.
     * 注 : `map类型`时, 建议使用顺序稳定的map, eg:LinkedHashMap
     * @param entitiesOrMapsOrLists 多个实体 or 多个值MAP or多个值列表
     * @return 查询构造器
     */
    B values(@Nullable Collection<?> entitiesOrMapsOrLists);

    /**
     * 查询后批量插入
     * 完整 sql eg : insert into `student`(`name`)(select `name` from `student` where `sex`="1" limit "3")
     * @param closure 查询语句
     * @return 查询构造器
     */
    B values(BuilderWrapper<B, T, K> closure);

}
