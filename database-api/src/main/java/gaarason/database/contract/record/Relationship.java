package gaarason.database.contract.record;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.extra.Bind;
import gaarason.database.contract.function.BuilderAnyWrapper;
import gaarason.database.contract.function.RecordWrapper;

/**
 * 关联关系
 * @param <T> 实体类
 * @param <K> 主键类型
 * @author xt
 */
public interface Relationship<T, K> {


    /**
     * 清空渴求式关联
     * @return 关联的Model的查询构造器
     */
    Record<T, K> withClear();

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名)
     * @return 关联的Model的查询构造器
     */
    Record<T, K> with(String fieldName);

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @return 关联的Model的查询构造器
     */
    Record<T, K> with(String fieldName, BuilderAnyWrapper builderClosure);

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure 所关联的Model的再一级关联
     * @return 关联的Model的查询构造器
     */
    Record<T, K> with(String fieldName, BuilderAnyWrapper builderClosure,
        RecordWrapper recordClosure);

    /**
     * 关系绑定操作对象
     * @param fieldName 关系操作的字段(当前模块的属性名)
     * @return 关系绑定操作对象
     */
    Bind bind(String fieldName);
}
