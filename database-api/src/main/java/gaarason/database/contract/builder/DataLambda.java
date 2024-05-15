package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.lang.Nullable;

import java.util.Collection;

/**
 * 数据
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface DataLambda<T, K> extends Data<T, K>, Support<T, K> {

    /**
     * 数据更新
     * @param column 列名表达式
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> data(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return data(lambda2ColumnName(column), value);
    }

    /**
     * 数据更新(忽略值为null的情况)
     * @param column 列名表达式
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> dataIgnoreNull(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return dataIgnoreNull(lambda2ColumnName(column), value);
    }

    /**
     * 字段自增
     * @param column 列名表达式
     * @param steps 步长
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> dataIncrement(ColumnFunctionalInterface<T, F> column, Object steps) {
        return dataIncrement(lambda2ColumnName(column), steps);
    }

    /**
     * 字段自减
     * @param column 列名表达式
     * @param steps 步长
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> dataDecrement(ColumnFunctionalInterface<T, F> column, Object steps) {
        return dataDecrement(lambda2ColumnName(column), steps);
    }

    /**
     * 字段设定选项值
     * @param column 列名表达式(位存储)
     * @param values 选项值集合(eg: 0,1,2,3)
     * @return 查询构造器
     */
    default <F> Builder<T, K> dataBit(ColumnFunctionalInterface<T, F> column, Collection<Object> values) {
        return dataBit(lambda2ColumnName(column), values);
    }

    /**
     * 字段增定选项值
     * @param column 列名表达式(位存储)
     * @param values 选项值集合(eg: 0,1,2,3)
     * @return 查询构造器
     */
    default <F> Builder<T, K> dataBitIncrement(ColumnFunctionalInterface<T, F> column, Collection<Object> values) {
        return dataBitIncrement(lambda2ColumnName(column), values);
    }

    /**
     * 字段移除选项值
     * @param column 列名表达式(位存储)
     * @param values 选项值集合(eg: 0,1,2,3)
     * @return 查询构造器
     */
    default <F> Builder<T, K> dataBitDecrement(ColumnFunctionalInterface<T, F> column, Collection<Object> values) {
        return dataBitDecrement(lambda2ColumnName(column), values);
    }

}
