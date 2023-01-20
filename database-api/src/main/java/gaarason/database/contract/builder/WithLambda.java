package gaarason.database.contract.builder;

import gaarason.database.appointment.AggregatesType;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
import gaarason.database.lang.Nullable;

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
    default <F> Builder<T, K> with(ColumnFunctionalInterface<T, F> fieldName) {
        return with(lambda2FieldName(fieldName));
    }

    /**
     * 渴求式关联
     * 对于复数关联关系, 可以正确的预测java类型
     * @param fieldName 所关联的Model(当前模块的属性名表达式)
     * @return 关联的Model的查询构造器
     */
    default <F> Builder<T, K> withMany(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName) {
        return withMany(lambda2FieldName(fieldName));
    }

    /**
     * 渴求式关联
     * 对于复数关联关系, 可以正确的预测java类型
     * @param fieldName 所关联的Model(当前模块的属性名表达式)
     * @return 关联的Model的查询构造器
     */
    default <F> Builder<T, K> withMany(ColumnFunctionalInterface.ColumnArray<T, F> fieldName) {
        return withMany(lambda2FieldName(fieldName));
    }

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名表达式)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @return 关联的Model的查询构造器
     */
    default <F> Builder<T, K> with(ColumnFunctionalInterface<T, F> fieldName,
        GenerateSqlPartFunctionalInterface<F, ?> builderClosure) {
        return with(lambda2FieldName(fieldName), builderClosure);
    }

    /**
     * 渴求式关联
     * 对于复数关联关系, 可以正确的预测java类型
     * @param fieldName 所关联的Model(当前模块的属性名表达式)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @return 关联的Model的查询构造器
     */
    default <F> Builder<T, K> withMany(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        GenerateSqlPartFunctionalInterface<F, ?> builderClosure) {
        return withMany(lambda2FieldName(fieldName), builderClosure);
    }

    /**
     * 渴求式关联
     * 对于复数关联关系, 可以正确的预测java类型
     * @param fieldName 所关联的Model(当前模块的属性名表达式)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @return 关联的Model的查询构造器
     */
    default <F> Builder<T, K> withMany(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        GenerateSqlPartFunctionalInterface<F, ?> builderClosure) {
        return withMany(lambda2FieldName(fieldName), builderClosure);
    }

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名表达式)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure 所关联的Model的再一级关联
     * @return 关联的Model的查询构造器
     */
    default <F> Builder<T, K> with(ColumnFunctionalInterface<T, F> fieldName,
        GenerateSqlPartFunctionalInterface<F, ?> builderClosure,
        RelationshipRecordWithFunctionalInterface recordClosure) {
        return with(lambda2FieldName(fieldName), builderClosure, recordClosure);
    }

    /**
     * 渴求式关联
     * 对于复数关联关系, 可以正确的预测java类型
     * @param fieldName 所关联的Model(当前模块的属性名表达式)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure 所关联的Model的再一级关联
     * @return 关联的Model的查询构造器
     */
    default <F> Builder<T, K> withMany(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        GenerateSqlPartFunctionalInterface<F, ?> builderClosure,
        RelationshipRecordWithFunctionalInterface recordClosure) {
        return withMany(lambda2FieldName(fieldName), builderClosure, recordClosure);
    }

    /**
     * 渴求式关联
     * 对于复数关联关系, 可以正确的预测java类型
     * @param fieldName 所关联的Model(当前模块的属性名表达式)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure 所关联的Model的再一级关联
     * @return 关联的Model的查询构造器
     */
    default <F> Builder<T, K> withMany(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        GenerateSqlPartFunctionalInterface<F, ?> builderClosure,
        RelationshipRecordWithFunctionalInterface recordClosure) {
        return withMany(lambda2FieldName(fieldName), builderClosure, recordClosure);
    }

    /**
     * 对关联关系进行操作
     * @param fieldName 关联关系属性名
     * @param builderClosure 自定义查询构造器
     * @param <F> 关联关系属性所对应的实体类型
     * @param <FK> 联关系属性所对应的数据表的主键类型
     * @return 查询构造器
     */
    default <F, FK> Builder<T, K> withOperation(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        GenerateSqlPartFunctionalInterface<F, FK> builderClosure) {
        return withOperation(lambda2FieldName(fieldName), builderClosure);
    }

    /**
     * 对关联关系进行操作
     * @param fieldName 关联关系属性名
     * @param builderClosure 自定义查询构造器
     * @param <F> 关联关系属性所对应的实体类型
     * @param <FK> 联关系属性所对应的数据表的主键类型
     * @return 查询构造器
     */
    default <F, FK> Builder<T, K> withOperation(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        GenerateSqlPartFunctionalInterface<F, FK> builderClosure) {
        return withOperation(lambda2FieldName(fieldName), builderClosure);
    }


    /**
     * @param op
     * @param fieldName
     * @param column
     * @param builderClosure
     * @param alisaFieldName
     * @param <F>
     * @param <FK>
     * @return
     */
    default <F, FK> Builder<T, K> withAggregate(AggregatesType op,
        ColumnFunctionalInterface.ColumnCollection<T, F> fieldName, ColumnFunctionalInterface<F, FK> column,
        GenerateSqlPartFunctionalInterface<F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(op, lambda2FieldName(fieldName), lambda2ColumnName(column), builderClosure,
            lambda2FieldNameNullable(alisaFieldName));
    }

    default <F, FK> Builder<T, K> withAggregate(AggregatesType op,
        ColumnFunctionalInterface.ColumnArray<T, F> fieldName, ColumnFunctionalInterface<F, FK> column,
        GenerateSqlPartFunctionalInterface<F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(op, lambda2FieldName(fieldName), lambda2ColumnName(column), builderClosure,
            lambda2FieldNameNullable(alisaFieldName));
    }

    // ------------ withCount ColumnCollection ------------ //

    default <F, FK> Builder<T, K> withCount(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column,
        GenerateSqlPartFunctionalInterface<F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(AggregatesType.count, lambda2FieldName(fieldName), lambda2ColumnName(column),
            builderClosure, lambda2FieldNameNullable(alisaFieldName));
    }

    default <F, FK> Builder<T, K> withCount(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName) {
        return withCount(lambda2FieldName(fieldName));
    }

    default <F, FK> Builder<T, K> withCount(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column) {
        return withCount(lambda2FieldName(fieldName), lambda2ColumnName(column),
            GenerateSqlPartFunctionalInterface.empty(), null);
    }

    default <F, FK> Builder<T, K> withCount(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column, ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withCount(lambda2FieldName(fieldName), lambda2ColumnName(column),
            GenerateSqlPartFunctionalInterface.empty(), lambda2FieldName(alisaFieldName));
    }

    // ------------ withCount ColumnArray ------------ //

    default <F, FK> Builder<T, K> withCount(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column,
        GenerateSqlPartFunctionalInterface<F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(AggregatesType.count, lambda2FieldName(fieldName), lambda2ColumnName(column),
            builderClosure, lambda2FieldNameNullable(alisaFieldName));
    }

    default <F, FK> Builder<T, K> withCount(ColumnFunctionalInterface.ColumnArray<T, F> fieldName) {
        return withCount(lambda2FieldName(fieldName), "*", GenerateSqlPartFunctionalInterface.empty(), null);
    }

    default <F, FK> Builder<T, K> withCount(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column) {
        return withCount(lambda2FieldName(fieldName), lambda2ColumnName(column),
            GenerateSqlPartFunctionalInterface.empty(), null);
    }

    default <F, FK> Builder<T, K> withCount(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column, ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withCount(lambda2FieldName(fieldName), lambda2ColumnName(column),
            GenerateSqlPartFunctionalInterface.empty(), lambda2FieldName(alisaFieldName));
    }

    // ------------ withMax ColumnCollection ------------ //

    default <F, FK> Builder<T, K> withMax(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column,
        GenerateSqlPartFunctionalInterface<F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(AggregatesType.max, fieldName, column, builderClosure, alisaFieldName);
    }

    default <F, FK> Builder<T, K> withMax(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column) {
        return withMax(fieldName, column, GenerateSqlPartFunctionalInterface.empty(), null);
    }

    default <F, FK> Builder<T, K> withMax(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column, ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withMax(fieldName, column, GenerateSqlPartFunctionalInterface.empty(), alisaFieldName);
    }

    // ------------ withMax ColumnArray ------------ //

    default <F, FK> Builder<T, K> withMax(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column,
        GenerateSqlPartFunctionalInterface<F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(AggregatesType.max, fieldName, column, builderClosure, alisaFieldName);
    }

    default <F, FK> Builder<T, K> withMax(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column) {
        return withMax(fieldName, column, GenerateSqlPartFunctionalInterface.empty(), null);
    }

    default <F, FK> Builder<T, K> withMax(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column, ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withMax(fieldName, column, GenerateSqlPartFunctionalInterface.empty(), alisaFieldName);
    }

    // ------------ withMin ColumnCollection ------------ //

    default <F, FK> Builder<T, K> withMin(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column,
        GenerateSqlPartFunctionalInterface<F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(AggregatesType.min, fieldName, column, builderClosure, alisaFieldName);
    }

    default <F, FK> Builder<T, K> withMin(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column) {
        return withMin(fieldName, column, GenerateSqlPartFunctionalInterface.empty(), null);
    }

    default <F, FK> Builder<T, K> withMin(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column, ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withMin(fieldName, column, GenerateSqlPartFunctionalInterface.empty(), alisaFieldName);
    }

    // ------------ withMin ColumnArray ------------ //

    default <F, FK> Builder<T, K> withMin(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column,
        GenerateSqlPartFunctionalInterface<F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(AggregatesType.min, fieldName, column, builderClosure, alisaFieldName);
    }

    default <F, FK> Builder<T, K> withMin(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column) {
        return withMin(fieldName, column, GenerateSqlPartFunctionalInterface.empty(), null);
    }

    default <F, FK> Builder<T, K> withMin(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column, ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withMin(fieldName, column, GenerateSqlPartFunctionalInterface.empty(), alisaFieldName);
    }

    // ------------ withMin ColumnCollection ------------ //

    default <F, FK> Builder<T, K> withAvg(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column,
        GenerateSqlPartFunctionalInterface<F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(AggregatesType.avg, fieldName, column, builderClosure, alisaFieldName);
    }

    default <F, FK> Builder<T, K> withAvg(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column) {
        return withAvg(fieldName, column, GenerateSqlPartFunctionalInterface.empty(), null);
    }

    default <F, FK> Builder<T, K> withAvg(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column, ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAvg(fieldName, column, GenerateSqlPartFunctionalInterface.empty(), alisaFieldName);
    }

    // ------------ withMin ColumnArray ------------ //

    default <F, FK> Builder<T, K> withAvg(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column,
        GenerateSqlPartFunctionalInterface<F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(AggregatesType.avg, fieldName, column, builderClosure, alisaFieldName);
    }

    default <F, FK> Builder<T, K> withAvg(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column) {
        return withAvg(fieldName, column, GenerateSqlPartFunctionalInterface.empty(), null);
    }

    default <F, FK> Builder<T, K> withAvg(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column, ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAvg(fieldName, column, GenerateSqlPartFunctionalInterface.empty(), alisaFieldName);
    }

    // ------------ withMin ColumnCollection ------------ //

    default <F, FK> Builder<T, K> withSum(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column,
        GenerateSqlPartFunctionalInterface<F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(AggregatesType.sum, fieldName, column, builderClosure, alisaFieldName);
    }

    default <F, FK> Builder<T, K> withSum(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column) {
        return withSum(fieldName, column, GenerateSqlPartFunctionalInterface.empty(), null);
    }

    default <F, FK> Builder<T, K> withSum(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column, @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withSum(fieldName, column, GenerateSqlPartFunctionalInterface.empty(), alisaFieldName);
    }

    // ------------ withMin ColumnArray ------------ //

    default <F, FK> Builder<T, K> withSum(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column,
        GenerateSqlPartFunctionalInterface<F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(AggregatesType.sum, fieldName, column, builderClosure, alisaFieldName);
    }

    default <F, FK> Builder<T, K> withSum(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column) {
        return withSum(fieldName, column, GenerateSqlPartFunctionalInterface.empty(), null);
    }

    default <F, FK> Builder<T, K> withSum(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column, @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withSum(fieldName, column, GenerateSqlPartFunctionalInterface.empty(), alisaFieldName);
    }

}
