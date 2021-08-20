package gaarason.database.contract.record;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.extra.Bind;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;

import java.io.Serializable;

/**
 * 关联关系
 * @param <T> 实体类
 * @param <K> 主键类型
 * @author xt
 */
public interface Relationship<T extends Serializable, K extends Serializable> {


    /**
     * 清空渴求式关联
     * @return 关联的Model的查询构造器
     */
    Record<T, K> withClear();

    /**
     * 渴求式关联
     * @param column 所关联的Model(当前模块的属性名)
     * @return 关联的Model的查询构造器
     */
    Record<T, K> with(String column);

    /**
     * 渴求式关联
     * @param column         所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @return 关联的Model的查询构造器
     */
    Record<T, K> with(String column, GenerateSqlPartFunctionalInterface<T, K> builderClosure);

    /**
     * 渴求式关联
     * @param column         所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure  所关联的Model的再一级关联
     * @return 关联的Model的查询构造器
     */
    Record<T, K> with(String column, GenerateSqlPartFunctionalInterface<T, K> builderClosure,
                      RelationshipRecordWithFunctionalInterface recordClosure);

    /**
     * 关系绑定操作对象
     * @param column 关系操作的字段(当前模块的属性名)
     * @return 关系绑定操作对象
     */
    Bind bind(String column);
}
