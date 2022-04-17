package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;

import java.io.Serializable;

/**
 * 限制
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface With<T extends Serializable, K extends Serializable> {

    /**
     * 渴求式关联
     * @param column 所关联的Model(当前模块的属性名)
     * @return 关联的Model的查询构造器
     */
    Builder<T, K> with(String column);

    /**
     * 渴求式关联
     * @param column         所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @return 关联的Model的查询构造器
     */
    Builder<T, K> with(String column, GenerateSqlPartFunctionalInterface<?, ?> builderClosure);

    /**
     * 渴求式关联
     * @param column         所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure  所关联的Model的再一级关联
     * @return 关联的Model的查询构造器
     */
    Builder<T, K> with(String column, GenerateSqlPartFunctionalInterface<?, ?> builderClosure,
        RelationshipRecordWithFunctionalInterface recordClosure);

}
