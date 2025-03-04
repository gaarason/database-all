package gaarason.database.contract.builder;

import gaarason.database.appointment.AggregatesType;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.BuilderWrapper;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.contract.function.RecordWrapper;
import gaarason.database.lang.Nullable;

/**
 * 限制
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface WithLambda<B extends Builder<B, T, K>, T, K> extends With<B, T, K>, Support<B, T, K> {

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名表达式)
     * @return 关联的Model的查询构造器
     */
    default <F> B with(ColumnFunctionalInterface<T, F> fieldName) {
        return with(lambda2FieldName(fieldName));
    }

    /**
     * 渴求式关联
     * 对于复数关联关系, 可以正确的预测java类型
     * @param fieldName 所关联的Model(当前模块的属性名表达式)
     * @return 关联的Model的查询构造器
     */
    default <F> B withMany(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName) {
        return withMany(lambda2FieldName(fieldName));
    }

    /**
     * 渴求式关联
     * 对于复数关联关系, 可以正确的预测java类型
     * @param fieldName 所关联的Model(当前模块的属性名表达式)
     * @return 关联的Model的查询构造器
     */
    default <F> B withMany(ColumnFunctionalInterface.ColumnArray<T, F> fieldName) {
        return withMany(lambda2FieldName(fieldName));
    }

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名表达式)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @return 关联的Model的查询构造器
     */
    default <F> B with(ColumnFunctionalInterface<T, F> fieldName,
        BuilderWrapper<?, F, ?> builderClosure) {
        return with(lambda2FieldName(fieldName), builderClosure);
    }

    /**
     * 渴求式关联
     * 对于复数关联关系, 可以正确的预测java类型
     * @param fieldName 所关联的Model(当前模块的属性名表达式)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @return 关联的Model的查询构造器
     */
    default <F> B withMany(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        BuilderWrapper<?, F, ?> builderClosure) {
        return withMany(lambda2FieldName(fieldName), builderClosure);
    }

    /**
     * 渴求式关联
     * 对于复数关联关系, 可以正确的预测java类型
     * @param fieldName 所关联的Model(当前模块的属性名表达式)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @return 关联的Model的查询构造器
     */
    default <F> B withMany(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        BuilderWrapper<?, F, ?> builderClosure) {
        return withMany(lambda2FieldName(fieldName), builderClosure);
    }

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名表达式)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure 所关联的Model的再一级关联
     * @return 关联的Model的查询构造器
     */
    default <F> B with(ColumnFunctionalInterface<T, F> fieldName,
        BuilderWrapper<?, F, ?> builderClosure,
        RecordWrapper recordClosure) {
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
    default <F> B withMany(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        BuilderWrapper<?, F, ?> builderClosure,
        RecordWrapper recordClosure) {
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
    default <F> B withMany(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        BuilderWrapper<?, F, ?> builderClosure,
        RecordWrapper recordClosure) {
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
    default <F, FK> B withOperation(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        BuilderWrapper<?, ?, ?> operationBuilder, BuilderWrapper<?, F, FK> builderClosure, ColumnFunctionalInterface.ColumnCollection<T, F> alisaFieldName) {
        return withOperation(lambda2FieldName(fieldName), operationBuilder, builderClosure, lambda2FieldName(alisaFieldName));
    }

    /**
     * 对关联关系进行操作
     * @param fieldName 关联关系属性名
     * @param builderClosure 自定义查询构造器
     * @param <F> 关联关系属性所对应的实体类型
     * @param <FK> 联关系属性所对应的数据表的主键类型
     * @return 查询构造器
     */
    default <F, FK> B withOperation(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        BuilderWrapper<?, ?, ?> operationBuilder, BuilderWrapper<?, F, FK> builderClosure, ColumnFunctionalInterface.ColumnArray<T, F> alisaFieldName) {
        return withOperation(lambda2FieldName(fieldName), operationBuilder, builderClosure, lambda2FieldName(alisaFieldName));
    }

    /**
     * 统计操作
     * @param op 操作类型
     * @param fieldName 关联关系属性名
     * @param column 列名
     * @param builderClosure 自定义查询构造器
     * @param alisaFieldName 别名
     * @param <F> 关联关系属性所对应的实体类型
     * @param <FK> 联关系属性所对应的数据表的主键类型
     * @return 查询构造器
     */
    default <F, FK> B withAggregate(AggregatesType op,
        ColumnFunctionalInterface.ColumnCollection<T, F> fieldName, ColumnFunctionalInterface<F, FK> column,
        BuilderWrapper<?, F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(op, lambda2FieldName(fieldName), lambda2ColumnName(column), builderClosure,
            lambda2FieldNameNullable(alisaFieldName));
    }

    default <F, FK> B withAggregate(AggregatesType op,
        ColumnFunctionalInterface.ColumnArray<T, F> fieldName, ColumnFunctionalInterface<F, FK> column,
        BuilderWrapper<?, F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(op, lambda2FieldName(fieldName), lambda2ColumnName(column), builderClosure,
            lambda2FieldNameNullable(alisaFieldName));
    }

    // ------------ withCount ColumnCollection ------------ //

    default <F, FK> B withCount(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column,
        BuilderWrapper<?, F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(AggregatesType.count, lambda2FieldName(fieldName), lambda2ColumnName(column),
            builderClosure, lambda2FieldNameNullable(alisaFieldName));
    }

    default <F, FK> B withCount(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName) {
        return withCount(lambda2FieldName(fieldName));
    }

    default <F, FK> B withCount(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column) {
        return withCount(lambda2FieldName(fieldName), lambda2ColumnName(column),
            BuilderWrapper.empty(), null);
    }

    default <F, FK> B withCount(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column, ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withCount(lambda2FieldName(fieldName), lambda2ColumnName(column),
            BuilderWrapper.empty(), lambda2FieldName(alisaFieldName));
    }

    // ------------ withCount ColumnArray ------------ //

    default <F, FK> B withCount(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column,
        BuilderWrapper<?, F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(AggregatesType.count, lambda2FieldName(fieldName), lambda2ColumnName(column),
            builderClosure, lambda2FieldNameNullable(alisaFieldName));
    }

    default <F, FK> B withCount(ColumnFunctionalInterface.ColumnArray<T, F> fieldName) {
        return withCount(lambda2FieldName(fieldName), "*", BuilderWrapper.empty(), null);
    }

    default <F, FK> B withCount(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column) {
        return withCount(lambda2FieldName(fieldName), lambda2ColumnName(column),
            BuilderWrapper.empty(), null);
    }

    default <F, FK> B withCount(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column, ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withCount(lambda2FieldName(fieldName), lambda2ColumnName(column),
            BuilderWrapper.empty(), lambda2FieldName(alisaFieldName));
    }

    // ------------ withMax ColumnCollection ------------ //

    default <F, FK> B withMax(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column,
        BuilderWrapper<?, F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(AggregatesType.max, fieldName, column, builderClosure, alisaFieldName);
    }

    default <F, FK> B withMax(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column) {
        return withMax(fieldName, column, BuilderWrapper.empty(), null);
    }

    default <F, FK> B withMax(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column, ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withMax(fieldName, column, BuilderWrapper.empty(), alisaFieldName);
    }

    // ------------ withMax ColumnArray ------------ //

    default <F, FK> B withMax(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column,
        BuilderWrapper<?, F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(AggregatesType.max, fieldName, column, builderClosure, alisaFieldName);
    }

    default <F, FK> B withMax(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column) {
        return withMax(fieldName, column, BuilderWrapper.empty(), null);
    }

    default <F, FK> B withMax(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column, ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withMax(fieldName, column, BuilderWrapper.empty(), alisaFieldName);
    }

    // ------------ withMin ColumnCollection ------------ //

    default <F, FK> B withMin(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column,
        BuilderWrapper<?, F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(AggregatesType.min, fieldName, column, builderClosure, alisaFieldName);
    }

    default <F, FK> B withMin(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column) {
        return withMin(fieldName, column, BuilderWrapper.empty(), null);
    }

    default <F, FK> B withMin(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column, ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withMin(fieldName, column, BuilderWrapper.empty(), alisaFieldName);
    }

    // ------------ withMin ColumnArray ------------ //

    default <F, FK> B withMin(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column,
        BuilderWrapper<?, F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(AggregatesType.min, fieldName, column, builderClosure, alisaFieldName);
    }

    default <F, FK> B withMin(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column) {
        return withMin(fieldName, column, BuilderWrapper.empty(), null);
    }

    default <F, FK> B withMin(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column, ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withMin(fieldName, column, BuilderWrapper.empty(), alisaFieldName);
    }

    // ------------ withMin ColumnCollection ------------ //

    default <F, FK> B withAvg(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column,
        BuilderWrapper<?, F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(AggregatesType.avg, fieldName, column, builderClosure, alisaFieldName);
    }

    default <F, FK> B withAvg(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column) {
        return withAvg(fieldName, column, BuilderWrapper.empty(), null);
    }

    default <F, FK> B withAvg(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column, ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAvg(fieldName, column, BuilderWrapper.empty(), alisaFieldName);
    }

    // ------------ withMin ColumnArray ------------ //

    default <F, FK> B withAvg(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column,
        BuilderWrapper<?, F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(AggregatesType.avg, fieldName, column, builderClosure, alisaFieldName);
    }

    default <F, FK> B withAvg(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column) {
        return withAvg(fieldName, column, BuilderWrapper.empty(), null);
    }

    default <F, FK> B withAvg(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column, ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAvg(fieldName, column, BuilderWrapper.empty(), alisaFieldName);
    }

    // ------------ withMin ColumnCollection ------------ //

    default <F, FK> B withSum(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column,
        BuilderWrapper<?, F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(AggregatesType.sum, fieldName, column, builderClosure, alisaFieldName);
    }

    default <F, FK> B withSum(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column) {
        return withSum(fieldName, column, BuilderWrapper.empty(), null);
    }

    default <F, FK> B withSum(ColumnFunctionalInterface.ColumnCollection<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column, @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withSum(fieldName, column, BuilderWrapper.empty(), alisaFieldName);
    }

    // ------------ withMin ColumnArray ------------ //

    default <F, FK> B withSum(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column,
        BuilderWrapper<?, F, FK> builderClosure,
        @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withAggregate(AggregatesType.sum, fieldName, column, builderClosure, alisaFieldName);
    }

    default <F, FK> B withSum(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column) {
        return withSum(fieldName, column, BuilderWrapper.empty(), null);
    }

    default <F, FK> B withSum(ColumnFunctionalInterface.ColumnArray<T, F> fieldName,
        ColumnFunctionalInterface<F, FK> column, @Nullable ColumnFunctionalInterface<T, ?> alisaFieldName) {
        return withSum(fieldName, column, BuilderWrapper.empty(), alisaFieldName);
    }

}
