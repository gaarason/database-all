package gaarason.database.contract.record;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.contract.support.LambdaStyle;
import gaarason.database.lang.Nullable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 集合操作类 表达式风格
 * @param <T> 实体类型
 * @param <K> 主键类型
 */
public interface CollectionOperationLambda<T extends Serializable, K extends Serializable>
    extends CollectionOperation<Record<T, K>>, LambdaStyle<T, K> {

    /**
     * 根据元素中的属性名获取值
     * @param element 元素
     * @param column 列名表达式|属性名表达式
     * @return 值
     */
    @Nullable
    default <W> W elementGetValueByFieldName(Record<T, K> element, ColumnFunctionalInterface<T> column) {
        return elementGetValueByFieldName(element, lambda2FieldName(column));
    }

    /**
     * 返回集合中的所有元素的指定属性的值的平均值
     * @param fieldName 属性名表达式
     * @return 平均值
     */
    default BigDecimal avg(ColumnFunctionalInterface<T> fieldName) {
        return avg(lambda2FieldName(fieldName));
    }

    /**
     * 返回集合中所有元素的指定属性值的总和
     * @param fieldName 属性名表达式
     * @return 总和
     */
    default BigDecimal sum(ColumnFunctionalInterface<T> fieldName) {
        return sum(lambda2FieldName(fieldName));
    }

    /**
     * 返回集合中所有元素的指定属性值的最大值
     * @param fieldName 属性名表达式
     * @return 最大值
     */
    default BigDecimal max(ColumnFunctionalInterface<T> fieldName) {
        return max(lambda2FieldName(fieldName));

    }

    /**
     * 返回集合所有元素的指定属性的值的最小值
     * @param fieldName 属性名表达式
     * @return 最小值
     */
    default BigDecimal min(ColumnFunctionalInterface<T> fieldName) {
        return min(lambda2FieldName(fieldName));

    }

    /**
     * 返回集合所有元素的指定属性的值的众数
     * @param fieldName 属性名表达式
     * @return 众数列表
     */
    default <W> List<W> mode(ColumnFunctionalInterface<T> fieldName) {
        return mode(lambda2FieldName(fieldName));
    }

    /**
     * 返回集合所有元素的指定属性的值的中位数
     * @param fieldName 属性名表达式
     * @return 中位数
     */
    default BigDecimal median(ColumnFunctionalInterface<T> fieldName) {
        return median(lambda2FieldName(fieldName));

    }

    /**
     * 判断集合是否存在任何一个元素的属性的值等于给定值
     * @param fieldName 属性名表达式
     * @param value 给定值
     * @return bool
     */
    default boolean contains(ColumnFunctionalInterface<T> fieldName, @Nullable Object value) {
        return contains(lambda2FieldName(fieldName));
    }

    /**
     * 计算集合中每个元素的指定属性的值的出现次数
     * @param fieldName 属性名表达式
     * @return Map<属性的类型, 次数>
     */
    default <W> Map<W, Integer> countBy(ColumnFunctionalInterface<T> fieldName) {
        return countBy(lambda2FieldName(fieldName));
    }

    /**
     * 集合中的所有元素的指定属性的值为空的都会被移除
     * 改变自身
     * @param fieldName 属性名表达式
     * @return 移除的数量个数
     */
    default int filter(ColumnFunctionalInterface<T> fieldName) {
        return filter(lambda2FieldName(fieldName));
    }

    /**
     * 对集合中的元素按照通过给定属性的值进行分组
     * @param fieldName 属性名表达式
     * @return Map<Object, List < Record < T, K>>>
     */
    default <W> Map<W, List<Record<T, K>>> groupBy(ColumnFunctionalInterface<T> fieldName) {
        return groupBy(lambda2FieldName(fieldName));
    }

    /**
     * 方法将指定属性名的值作为集合的键，如果多个元素拥有同一个键，只有最后一个会出现在新集合里面
     * @param fieldName 属性名表达式
     * @return 全新的集合
     */
    default <W> Map<W, Record<T, K>> keyBy(ColumnFunctionalInterface<T> fieldName) {
        return keyBy(lambda2FieldName(fieldName));
    }


    /**
     * 将集合中的每个元素的指定属性的值, 组合成新的列表
     * @param fieldName 属性名表达式
     * @return 新的列表
     */
    default <W> List<W> pluck(ColumnFunctionalInterface<T> fieldName) {
        return pluck(lambda2FieldName(fieldName));
    }

    /**
     * 将集合中的每个元素的指定属性value的值, 使用给定的属性key的值进行索引, 如果存在重复索引，最后一个匹配的元素将会插入集合
     * @param fieldNameForValue 属性名表达式
     * @param fieldNameForKey 属性名表达式
     * @return 值的集合
     */
    default <W, Y> Map<W, Y> pluck(ColumnFunctionalInterface<T> fieldNameForValue,
                                   ColumnFunctionalInterface<T> fieldNameForKey) {
        return pluck(lambda2FieldName(fieldNameForValue), lambda2FieldName(fieldNameForKey));
    }

    /**
     * 通过元素中的指定属性的值，对集合进行正序排序
     * @param fieldName 属性名表达式
     * @return 新的集合
     */
    default List<Record<T, K>> sortBy(ColumnFunctionalInterface<T> fieldName) {
        return sortBy(lambda2FieldName(fieldName));
    }

    /**
     * 通过元素中的指定属性的值，对集合进行倒序排序
     * @param fieldName 属性名表达式
     * @return 新的集合
     */
    default List<Record<T, K>> sortByDesc(ColumnFunctionalInterface<T> fieldName) {
        return sortByDesc(lambda2FieldName(fieldName));
    }


    /**
     * 使用元素中的指定属性来剔除重复的元素，不影响自身
     * @param fieldName 属性名表达式
     * @return 去重后的集合
     */
    default List<Record<T, K>> unique(ColumnFunctionalInterface<T> fieldName) {
        return unique(lambda2FieldName(fieldName));
    }
}
