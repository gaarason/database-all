package gaarason.database.contract.record;

import gaarason.database.core.lang.Nullable;
import gaarason.database.util.ConverterUtils;
import gaarason.database.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 集合操作类
 * @author xt
 * @since 2021/11/8 7:04 下午
 */
public interface CollectionOperation<E> extends List<E> {

    /**
     * 根据属性名获取值
     * @param element   对象
     * @param fieldName 属性名
     * @return 值
     */
    @Nullable
    Object getValueByFieldName(E element, String fieldName);

    /**
     * 返回集合表示的底层数数据
     * @return 列表
     */
    default List<E> all() {
        return new ArrayList<>(this);
    }

    /**
     * 方法返回所有集合项的平均值
     * @return 平均值
     */
    default BigDecimal avg() {
        BigDecimal bigDecimal = BigDecimal.ZERO;
        for (E e : this) {
            bigDecimal = bigDecimal.add(ObjectUtils.isEmpty(e) ? BigDecimal.ZERO : ConverterUtils.castNullable(e, BigDecimal.class));
        }
        return bigDecimal.divide(new BigDecimal(size()), 8, RoundingMode.HALF_UP);
    }

    /**
     * 方法返回所有集合指定项的平均值
     * @param fieldName 属性名
     * @return 平均值
     */
    default BigDecimal avg(String fieldName) {
        BigDecimal bigDecimal = BigDecimal.ZERO;
        for (E e : this) {
            Object value = getValueByFieldName(e, fieldName);
            bigDecimal = bigDecimal.add(ObjectUtils.isEmpty(value) ? BigDecimal.ZERO : ConverterUtils.castNullable(value, BigDecimal.class));
        }
        return bigDecimal.divide(new BigDecimal(size()), 8, RoundingMode.HALF_UP);
    }

    /**
     * 返回集合中给定键的最大值
     * @param fieldName 属性名
     * @return 最大值
     */
    default BigDecimal max(String fieldName) {
        BigDecimal maxValue = null;
        for (E e : this) {
            Object valueObj = getValueByFieldName(e, fieldName);
            BigDecimal value = ObjectUtils.isEmpty(valueObj) ? BigDecimal.ZERO : ConverterUtils.castNullable(valueObj, BigDecimal.class);
            maxValue = maxValue == null ? value : maxValue.max(value);
        }
        return maxValue == null ? BigDecimal.ZERO : maxValue;
    }

    /**
     * 返回集合中给定键的中位数
     * @param fieldName 属性名
     * @return 中位数
     */
    default BigDecimal median(String fieldName) {
        if (isEmpty()) {
            return BigDecimal.ZERO;
        }

        int count = 0;
        PriorityQueue<BigDecimal> minHeap = new PriorityQueue<>(size() / 2, BigDecimal::compareTo);
        PriorityQueue<BigDecimal> maxHeap = new PriorityQueue<>(size() / 2, Comparator.reverseOrder());

        for (E e : this) {
            Object valueObj = getValueByFieldName(e, fieldName);
            BigDecimal value = ObjectUtils.isEmpty(valueObj) ? BigDecimal.ZERO : ConverterUtils.castNullable(valueObj, BigDecimal.class);
            if (count % 2 == 0) {
                minHeap.add(value);
                BigDecimal theMin = minHeap.poll();
                maxHeap.add(theMin);
            } else {
                maxHeap.add(value);
                BigDecimal theMax = maxHeap.poll();
                minHeap.add(theMax);
            }
            count++;
        }
        // 绝不可能为null, 第一个元素必进maxHeap
        assert maxHeap.peek() != null;
        if (count % 2 == 0) {
            return maxHeap.peek().add(minHeap.peek()).divide(new BigDecimal("2"), 8, RoundingMode.HALF_UP);
        } else {
            return maxHeap.peek();
        }
    }


    /**
     * 方法将一个集合分割成多个小尺寸的小集合
     * @param newSize 小集合的尺寸
     * @return 包含多个小集合的集合
     */
    default List<List<E>> chunk(int newSize) {
        List<List<E>> outsideList = new ArrayList<>(size() / newSize);
        ArrayList<E> innerList = new ArrayList<>(newSize);
        for (E e : this) {
            if (innerList.size() == newSize) {
                outsideList.add(innerList);
                innerList = new ArrayList<>(newSize);
            }
            innerList.add(e);
        }
        return outsideList;
    }

    /**
     * 判断集合是否包含一个给定值
     * @param value 给定值
     * @return bool
     */
    default boolean contains(Object value) {
        for (E e : this) {
            return ObjectUtils.nullSafeEquals(e, value);
        }
        return false;
    }

    /**
     * 判断集合是否包含一个给定值
     * @param fieldName 属性名
     * @param value     给定值
     * @return bool
     */
    default boolean contains(String fieldName, Object value) {
        for (E e : this) {
            return ObjectUtils.nullSafeEquals(getValueByFieldName(e, fieldName), value);
        }
        return false;
    }

    /**
     * 判断集合是否包含一个给定值
     * @param closure 闭包
     * @return bool
     */
    default boolean contains(DecideTwo<Integer, E> closure) {
        int index = 0;
        for (E e : this) {
            if (closure.judge(index++, e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回集合中所有项的总数
     * @return 总数
     */
    default int count() {
        return size();
    }

    /**
     * 计算每个元素的出现次数
     * @return Map<E, 次数>
     */
    default Map<E, Integer> countBy() {
        Map<E, Integer> theMap = new HashMap<>(16);
        for (E e : this) {
            Integer count = theMap.computeIfAbsent(e, k -> 0);
            theMap.put(e, ++count);
        }
        return theMap;
    }

    /**
     * 计算每个元素的出现次数
     * @param closure 闭包
     * @return Map<E, 次数>
     */
    default Map<E, Integer> countBy(DecideOne<E> closure) {
        Map<E, Integer> theMap = new HashMap<>(16);
        for (E e : this) {
            if (closure.judge(e)) {
                Integer count = theMap.computeIfAbsent(e, k -> 0);
                theMap.put(e, ++count);
            }
        }
        return theMap;
    }

    /**
     * 集合的所有元素能够通过给定的真理测试
     * 如果集合为空，every 方法将返回 true
     * @param closure 闭包
     * @return bool
     */
    default boolean every(DecideTwo<Integer, E> closure) {
        int index = 0;
        for (E e : this) {
            if (!closure.judge(index++, e)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 通过给定回调过滤集合，只有通过给定真理测试的数据项才会保留下来
     * 改变自身
     * @param closure 闭包
     * @return 移除的数量个数
     */
    default int filter(DecideTwo<Integer, E> closure) {
        int count = 0;
        int index = 0;
        Iterator<E> iterator = this.iterator();
        while (iterator.hasNext()) {
            E e = iterator.next();
            if (!closure.judge(index++, e)) {
                count++;
                iterator.remove();
            }
        }
        return count;
    }

    /**
     * 所有isEmpty的都会被移除
     * 改变自身
     * @return 移除的数量个数
     */
    default int filter() {
        return filter((index, e) -> !ObjectUtils.isEmpty(e));
    }

    /**
     * 通过给定回调过滤集合，满足条件的数据会被移除
     * 改变自身
     * @param closure 闭包
     * @return 移除的数量个数
     */
    default int reject(DecideTwo<Integer, E> closure) {
        int count = 0;
        int index = 0;
        Iterator<E> iterator = this.iterator();
        while (iterator.hasNext()) {
            E e = iterator.next();
            if (closure.judge(index++, e)) {
                count++;
                iterator.remove();
            }
        }
        return count;
    }

    /**
     * 通过给定回调过滤集合，返回满足条件的第一条
     * @param closure 闭包
     * @return 第一条数据
     */
    @Nullable
    default E first(DecideTwo<Integer, E> closure) {
        int index = 0;
        for (E e : this) {
            if (closure.judge(index++, e)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 返回第一条
     * @return 第一条数据
     */
    @Nullable
    default E first() {
        return first((index, e) -> true);
    }

    /**
     * 通过给回调分组集合数据项
     * @param closure 闭包
     * @return Map<Object, List < E>>
     */
    default Map<Object, List<E>> groupBy(ReturnTwo<Integer, E, Object> closure) {
        int index = 0;
        Map<Object, List<E>> outsideMap = new HashMap<>();
        for (E e : this) {
            Object res = closure.get(index++, e);
            List<E> innerList = outsideMap.computeIfAbsent(res, k -> new ArrayList<>());
            innerList.add(e);
        }
        return outsideMap;
    }

    /**
     * 通过给定键分组集合数据项
     * @param fieldName 属性名
     * @return Map<Object, List < E>>
     */
    default Map<Object, List<E>> groupBy(String fieldName) {
        return groupBy((index, e) -> getValueByFieldName(e, fieldName));
    }

    /**
     * 连接集合中的数据项
     * @param delimiter 分隔符
     * @return 连接后的字符串
     */
    default String implode(CharSequence delimiter) {
        return this.stream().map(e -> ConverterUtils.castNullable(e, String.class)).collect(Collectors.joining(delimiter));
    }

    /**
     * 连接集合中的数据项
     * @param fieldName 属性名
     * @param delimiter 分隔符
     * @return 连接后的字符串
     */
    default String implode(String fieldName, CharSequence delimiter) {
        return this.stream().map(e -> ConverterUtils.castNullable(getValueByFieldName(e, fieldName), String.class)).collect(
            Collectors.joining(delimiter));
    }


    /**
     * 方法将指定属性名的值作为集合的键，如果多个数据项拥有同一个键，只有最后一个会出现在新集合里面
     * @param fieldName 属性名
     * @return 全新的集合
     */
    default Map<Object, E> keyBy(String fieldName) {
        return keyBy((index, e) -> getValueByFieldName(e, fieldName));
    }

    /**
     * 方法将指定回调的结果作为集合的键，如果多个数据项拥有同一个键，只有最后一个会出现在新集合里面
     * @param closure 闭包
     * @return 全新的集合
     */
    default Map<Object, E> keyBy(ReturnTwo<Integer, E, Object> closure) {
        int index = 0;
        Map<Object, E> theMap = new HashMap<>(16);
        for (E e : this) {
            theMap.put(closure.get(index++, e), e);
        }
        return theMap;
    }

    /**
     * 通过给定回调过滤集合，返回满足条件的最后条
     * @param closure 闭包
     * @return 最后条数据
     */
    @Nullable
    default E last(DecideTwo<Integer, E> closure) {
        int index = 0;
        E temp = null;
        for (E e : this) {
            if (closure.judge(index++, e)) {
                temp = e;
            }
        }
        return temp;
    }

    /**
     * 返回最后条
     * @return 最后条数据
     */
    @Nullable
    default E last() {
        return first((index, e) -> true);
    }

    /**
     * 通过给定回调对集合项进行分组
     * @param closureKey   闭包生成key
     * @param closureValue 闭包生成value
     * @return 新的集合
     */
    default Map<Object, List<Object>> mapToGroups(ReturnTwo<Integer, E, Object> closureKey, ReturnTwo<Integer, E, Object> closureValue) {
        int index = 0;
        Map<Object, List<Object>> outsideMap = new HashMap<>(16);

        for (E e : this) {
            Object key = closureKey.get(index, e);
            Object value = closureValue.get(index++, e);

            List<Object> innerList = outsideMap.computeIfAbsent(key, k -> new ArrayList<>());
            innerList.add(value);
        }
        return outsideMap;
    }

    /**
     * 通过给定回调对集合项进行索引
     * @param closureKey   闭包生成key
     * @param closureValue 闭包生成value
     * @return 新的集合
     */
    default Map<Object, Object> mapWithKeys(ReturnTwo<Integer, E, Object> closureKey, ReturnTwo<Integer, E, Object> closureValue) {
        int index = 0;
        Map<Object, Object> outsideMap = new HashMap<>(16);

        for (E e : this) {
            Object key = closureKey.get(index, e);
            Object value = closureValue.get(index++, e);
            outsideMap.put(key, value);
        }
        return outsideMap;
    }


    @FunctionalInterface
    interface DecideOne<A> {

        boolean judge(A a);
    }

    @FunctionalInterface
    interface DecideTwo<A, B> {

        boolean judge(A a, B b);
    }

    @FunctionalInterface
    interface ReturnTwo<A, B, C> {

        @Nullable
        C get(A a, B b);
    }
}
