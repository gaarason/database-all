package gaarason.database.contract.builder;

import gaarason.database.appointment.AggregatesType;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.BuilderAnyWrapper;
import gaarason.database.contract.function.BuilderWrapper;
import gaarason.database.contract.function.RecordWrapper;
import gaarason.database.lang.Nullable;

/**
 * 限制
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface With<B extends Builder<B, T, K>, T, K> {

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名)
     * @return 关联的Model的查询构造器
     */
    B with(String fieldName);

    /**
     * 渴求式关联
     * 为了api统一, 减少理解记忆的难度
     * @param fieldName 所关联的Model(当前模块的属性名)
     * @return 关联的Model的查询构造器
     * @see WithLambda
     */
    default B withMany(String fieldName) {
        return with(fieldName);
    }

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @return 关联的Model的查询构造器
     */
    <F> B with(String fieldName, BuilderWrapper<?, F, ?> builderClosure);

    /**
     * 渴求式关联
     * 为了api统一, 减少理解记忆的难度
     * @param fieldName 所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @return 关联的Model的查询构造器
     * @see WithLambda
     */
    default <F> B withMany(String fieldName, BuilderWrapper<?, F, ?> builderClosure) {
        return with(fieldName, builderClosure);
    }

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure 所关联的Model的再一级关联
     * @return 关联的Model的查询构造器
     */
    <F> B with(String fieldName, BuilderWrapper<?, F, ?> builderClosure,
        RecordWrapper recordClosure);

    /**
     * 渴求式关联
     * 为了api统一, 减少理解记忆的难度
     * @param fieldName 所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure 所关联的Model的再一级关联
     * @return 关联的Model的查询构造器
     * @see WithLambda
     */
    default <F> B withMany(String fieldName, BuilderWrapper<?, F, ?> builderClosure,
        RecordWrapper recordClosure) {
        return with(fieldName, builderClosure, recordClosure);
    }

    /**
     * 对关联关系进行操作
     * @param fieldName 关联关系属性名
     * @param operationBuilder 操作查询构造器
     * @param customBuilder 自定义查询构造器
     * @param alisaFieldName 查询结果的别名
     * @return 查询构造器
     */
    B withOperation(String fieldName, BuilderAnyWrapper operationBuilder, BuilderAnyWrapper customBuilder, String alisaFieldName);

    /**
     * 对关联关系进行统计操作
     * @param op 操作类型
     * @param fieldName 关联关系属性名
     * @param column 对的的关联关系表中的数据列名
     * @param customBuilder 自定义查询构造器
     * @param alisaFieldName 查询结果的别名
     * @return 查询构造器
     */
    B withAggregate(AggregatesType op, String fieldName, String column,
            BuilderAnyWrapper customBuilder, @Nullable String alisaFieldName);

    default B withCount(String fieldName, String column,
            BuilderAnyWrapper builderClosure, @Nullable String alisaFieldName) {
        return withAggregate(AggregatesType.count, fieldName, column, builderClosure, alisaFieldName);
    }

    default B withCount(String fieldName) {
        String alisaFieldName = fieldName + "Count";
        return withCount(fieldName, "*", BuilderAnyWrapper.empty(), alisaFieldName);
    }

    default B withCount(String fieldName, String column) {
        return withCount(fieldName, column, BuilderAnyWrapper.empty(), null);
    }

    default B withCount(String fieldName, String column, String alisaFieldName) {
        return withCount(fieldName, column, BuilderAnyWrapper.empty(), alisaFieldName);
    }

    default B withMax(String fieldName, String column,
            BuilderAnyWrapper builderClosure, @Nullable String alisaFieldName) {
        return withAggregate(AggregatesType.max, fieldName, column, builderClosure, alisaFieldName);
    }

    default B withMax(String fieldName, String column) {
        return withMax(fieldName, column, BuilderAnyWrapper.empty(), null);
    }

    default B withMax(String fieldName, String column, String alisaFieldName) {
        return withMax(fieldName, column, BuilderAnyWrapper.empty(), alisaFieldName);
    }

    default B withMin(String fieldName, String column,
            BuilderAnyWrapper builderClosure, @Nullable String alisaFieldName) {
        return withAggregate(AggregatesType.min, fieldName, column, builderClosure, alisaFieldName);
    }

    default B withMin(String fieldName, String column) {
        return withMin(fieldName, column, BuilderAnyWrapper.empty(), null);
    }

    default B withMin(String fieldName, String column, String alisaFieldName) {
        return withMin(fieldName, column, BuilderAnyWrapper.empty(), alisaFieldName);
    }

    default B withAvg(String fieldName, String column,
            BuilderAnyWrapper builderClosure, @Nullable String alisaFieldName) {
        return withAggregate(AggregatesType.avg, fieldName, column, builderClosure, alisaFieldName);
    }

    default B withAvg(String fieldName, String column) {
        return withAvg(fieldName, column, BuilderAnyWrapper.empty(), null);
    }

    default B withAvg(String fieldName, String column, String alisaFieldName) {
        return withAvg(fieldName, column, BuilderAnyWrapper.empty(), alisaFieldName);
    }

    default B withSum(String fieldName, String column,
            BuilderAnyWrapper builderClosure, @Nullable String alisaFieldName) {
        return withAggregate(AggregatesType.sum, fieldName, column, builderClosure, alisaFieldName);
    }

    default B withSum(String fieldName, String column) {
        return withSum(fieldName, column, BuilderAnyWrapper.empty(), null);
    }

    default B withSum(String fieldName, String column, String alisaFieldName) {
        return withSum(fieldName, column, BuilderAnyWrapper.empty(), alisaFieldName);
    }
}
