package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;

/**
 * 限制
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface WithLambda<T, K> extends With<T, K>, Support<T, K> {

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名表达式)
     * @return 关联的Model的查询构造器
     */
    default Builder<T, K> with(ColumnFunctionalInterface<T> fieldName) {
        return with(lambda2FieldName(fieldName));
    }

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名表达式)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @return 关联的Model的查询构造器
     */
    default Builder<T, K> with(ColumnFunctionalInterface<T> fieldName,
        GenerateSqlPartFunctionalInterface<?, ?> builderClosure) {
        return with(lambda2FieldName(fieldName), builderClosure);
    }

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名表达式)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure 所关联的Model的再一级关联
     * @return 关联的Model的查询构造器
     */
    default Builder<T, K> with(ColumnFunctionalInterface<T> fieldName,
        GenerateSqlPartFunctionalInterface<?, ?> builderClosure,
        RelationshipRecordWithFunctionalInterface recordClosure) {
        return with(lambda2FieldName(fieldName), builderClosure, recordClosure);
    }

}
