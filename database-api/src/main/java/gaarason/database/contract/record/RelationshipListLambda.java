package gaarason.database.contract.record;

import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
import gaarason.database.contract.support.LambdaStyle;

/**
 * 关联关系
 * @param <T> 实体类
 * @param <K> 主键类型
 * @author xt
 */
public interface RelationshipListLambda<T, K>
    extends RelationshipList<T, K>, LambdaStyle {

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名)表达式
     * @param <F> 属性类型
     * @return 关联的Model的查询构造器
     */
    default <F> RecordList<T, K> with(ColumnFunctionalInterface<T, F> fieldName) {
        return with(lambda2FieldName(fieldName));
    }

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名)表达式
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param <F> 属性类型
     * @return 关联的Model的查询构造器
     */
    default <F> RecordList<T, K> with(ColumnFunctionalInterface<T, F> fieldName,
        GenerateSqlPartFunctionalInterface<F, ?> builderClosure) {
        return with(lambda2FieldName(fieldName), builderClosure);
    }

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名)表达式
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure 所关联的Model的再一级关联
     * @param <F> 属性类型
     * @return 关联的Model的查询构造器
     */
    default <F> RecordList<T, K> with(ColumnFunctionalInterface<T, F> fieldName,
        GenerateSqlPartFunctionalInterface<F, ?> builderClosure,
        RelationshipRecordWithFunctionalInterface recordClosure) {
        return with(lambda2FieldName(fieldName), builderClosure, recordClosure);
    }
}
