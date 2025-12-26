package gaarason.database.contract.record;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.extra.Bind;
import gaarason.database.contract.function.BuilderAnyWrapper;
import gaarason.database.contract.function.BuilderEntityWrapper;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.contract.function.RecordWrapper;
import gaarason.database.contract.support.LambdaStyle;

/**
 * 关联关系
 * @param <T> 实体类
 * @param <K> 主键类型
 * @author xt
 */
public interface RelationshipLambda<T, K>
    extends Relationship<T, K>, LambdaStyle {

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名)表达式
     * @param <F> 属性类型
     * @return 关联的Model的查询构造器
     * @deprecated 将 record.with() 使用 builder.with() 进行替换
     * @see gaarason.database.contract.eloquent.Builder#with(ColumnFunctionalInterface[])
     */
    @Deprecated
    default <F> Record<T, K> with(ColumnFunctionalInterface<T, F> fieldName) {
        return with(lambda2FieldName(fieldName));
    }

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名)表达式
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param <F> 属性类型
     * @return 关联的Model的查询构造器
     * @deprecated 将 record.with() 使用 builder.with() 进行替换
     * @see gaarason.database.contract.eloquent.Builder#with(ColumnFunctionalInterface, BuilderEntityWrapper)
     */
    @Deprecated
    default <F> Record<T, K> with(ColumnFunctionalInterface<T, F> fieldName, BuilderEntityWrapper<F> builderClosure) {
        return with(lambda2FieldName(fieldName), BuilderAnyWrapper.turn2(builderClosure));
    }

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名)表达式
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure 所关联的Model的再一级关联
     * @param <F> 属性类型
     * @return 关联的Model的查询构造器
     * @deprecated 将 record.with() 使用 builder.with() 进行替换
     * @see gaarason.database.contract.eloquent.Builder#with(ColumnFunctionalInterface, BuilderEntityWrapper)
     */
    @Deprecated
    default <F> Record<T, K> with(ColumnFunctionalInterface<T, F> fieldName, BuilderEntityWrapper<F> builderClosure,
        RecordWrapper recordClosure) {
        return with(lambda2FieldName(fieldName), BuilderAnyWrapper.turn2(builderClosure), recordClosure);
    }

    /**
     * 关系绑定操作对象
     * @param fieldName 关系操作的字段(当前模块的属性名)表达式
     * @param <F> 属性类型
     * @return 关系绑定操作对象
     */
    default <F> Bind bind(ColumnFunctionalInterface<T, F> fieldName) {
        return bind(lambda2FieldName(fieldName));
    }
}
