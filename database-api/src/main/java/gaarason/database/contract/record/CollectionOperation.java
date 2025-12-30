package gaarason.database.contract.record;

import gaarason.database.config.ConversionConfig;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.exception.NoSuchAlgorithmException;
import gaarason.database.exception.OperationNotSupportedException;
import gaarason.database.lang.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 集合操作类
 * @param <E> 元素类型
 * @author xt
 * @since 2021/11/8 7:04 下午
 */
public interface CollectionOperation<E> extends List<E>, Deque<E> {

    /**
     * 根据结果集列表生成 RecordList
     * @param records 结果集列表
     * @return 结果集集合对象
     * @param <TT> 实体类
     * @param <KK> 主键
     */
    <TT, KK> RecordList<TT, KK> newRecordList(Collection<Record<TT, KK>> records);

    /**
     * 根据元素中的属性名获取值
     * @param element 元素
     * @param fieldName 属性名
     * @return 值
     */
    @Nullable
    <W> W elementGetValueByFieldName(E element, String fieldName);

    /**
     * 元素转简单map
     * @param element 元素
     * @return map
     * @throws OperationNotSupportedException 元素不支持转map的操作
     */
    Map<String, Object> elementToMap(E element) throws OperationNotSupportedException;

    /**
     * 类型转化 Worker
     * @return ConversionWorker
     */
    ConversionConfig getConversionWorkerFromContainer();

    boolean isEmpty(@Nullable Object obj);

    boolean isEmpty(@Nullable Object[] obj);

    /**
     * 返回集合表示的底层元素列表
     * @return 元素列表
     */
    default List<E> all() {
        return new ArrayList<>(this);
    }

    /**
     * 返回集合中的所有元素的指定属性的值的平均值
     * @param fieldName 属性名
     * @return 平均值
     */
    default BigDecimal avg(String fieldName) {
        BigDecimal bigDecimal = BigDecimal.ZERO;
        for (E e : this) {
            Object value = elementGetValueByFieldName(e, fieldName);
            bigDecimal = bigDecimal.add(isEmpty(value) ? BigDecimal.ZERO :
                getConversionWorkerFromContainer().castNullable(value, BigDecimal.class));
        }
        return bigDecimal.divide(new BigDecimal(size()), RoundingMode.HALF_UP);
    }

    /**
     * 返回集合中所有元素的指定属性值的总和
     * @param fieldName 属性名
     * @return 总和
     */
    default BigDecimal sum(String fieldName) {
        BigDecimal sum = null;
        for (E e : this) {
            Object valueObj = elementGetValueByFieldName(e, fieldName);
            BigDecimal value = isEmpty(valueObj) ? BigDecimal.ZERO :
                getConversionWorkerFromContainer().castNullable(valueObj, BigDecimal.class);
            sum = sum == null ? value : sum.add(value);
        }
        return sum == null ? BigDecimal.ZERO : sum;
    }

    /**
     * 返回集合中所有元素的指定属性值的最大值
     * @param fieldName 属性名
     * @return 最大值
     */
    default BigDecimal max(String fieldName) {
        BigDecimal maxValue = null;
        for (E e : this) {
            Object valueObj = elementGetValueByFieldName(e, fieldName);
            BigDecimal value = isEmpty(valueObj) ? BigDecimal.ZERO :
                getConversionWorkerFromContainer().castNullable(valueObj, BigDecimal.class);
            maxValue = maxValue == null ? value : maxValue.max(value);
        }
        return maxValue == null ? BigDecimal.ZERO : maxValue;
    }

    /**
     * 返回集合所有元素的指定属性的值的最小值
     * @param fieldName 属性名
     * @return 最小值
     */
    default BigDecimal min(String fieldName) {
        BigDecimal minValue = null;
        for (E e : this) {
            Object valueObj = elementGetValueByFieldName(e, fieldName);
            BigDecimal value = isEmpty(valueObj) ? BigDecimal.ZERO :
                getConversionWorkerFromContainer().castNullable(valueObj, BigDecimal.class);
            minValue = minValue == null ? value : minValue.min(value);
        }
        return minValue == null ? BigDecimal.ZERO : minValue;
    }

    /**
     * 返回集合所有元素的指定属性的值的众数
     * @param fieldName 属性名
     * @return 众数列表
     */
    default <W> List<W> mode(String fieldName) {
        // 计数
        Map<String, Integer> countMap = new HashMap<>(16);
        List<W> res = new ArrayList<>();
        int maxCount = 0;
        for (E e : this) {
            W valueObj = elementGetValueByFieldName(e, fieldName);
            Integer count = countMap.computeIfAbsent(
                getConversionWorkerFromContainer().castNullable(valueObj, String.class), k -> 0);
            count++;
            countMap.put(getConversionWorkerFromContainer().castNullable(valueObj, String.class), count);

            // 新的众数产生了
            if (count == maxCount) {
                res.add(valueObj);
            } else if (count > maxCount) {
                maxCount = count;
                res.clear();
                res.add(valueObj);
            }
        }
        return res;
    }

    /**
     * 返回集合所有元素的指定属性的值的中位数
     * @param fieldName 属性名
     * @return 中位数
     */
    default BigDecimal median(String fieldName) {
        if (isEmpty()) {
            return BigDecimal.ZERO;
        }
        int count = size() / 2 + 1;
        // 因为元素个数是确定的, 所以一个堆就足够了
        PriorityQueue<BigDecimal> minHeap = new PriorityQueue<>(count + 1, BigDecimal::compareTo);

        for (E e : this) {
            Object valueObj = elementGetValueByFieldName(e, fieldName);
            BigDecimal value = isEmpty(valueObj) ? BigDecimal.ZERO :
                getConversionWorkerFromContainer().castNullable(valueObj, BigDecimal.class);

            minHeap.add(value);
            if (minHeap.size() > count) {
                minHeap.poll();
            }
        }
        // 绝不可能为null, 第一个元素必进heap
        assert minHeap.peek() != null;
        if (count % 2 == 0) {
            return minHeap.poll().add(minHeap.poll()).divide(new BigDecimal("2"), RoundingMode.HALF_UP);
        } else {
            return minHeap.poll();
        }
    }

    /**
     * 将一个集合中的匀速分割成多个小尺寸的小集合, 小集合中类型是自定义的
     * @param closure 闭包
     * @param newSize 每个小集合的尺寸
     * @param <W> 小集合中的类型
     * @return 包含多个小集合的集合
     */
    default <W> List<List<W>> chunk(ReturnTwo<Integer, E, W> closure, int newSize) {
        List<List<W>> outsideList = new ArrayList<>(size() / newSize);
        ArrayList<W> innerList = new ArrayList<>(newSize);
        int index = 0;
        for (E e : this) {
            if (innerList.size() == newSize) {
                outsideList.add(innerList);
                innerList = new ArrayList<>(newSize);
            }
            innerList.add(closure.get(index++, e));
        }
        if (!innerList.isEmpty()) {
            outsideList.add(innerList);
        }
        return outsideList;
    }

    /**
     * 将一个集合中的元素分割成多个小尺寸的小集合
     * @param newSize 每个小集合的尺寸
     * @return 包含多个小集合的集合
     */
    default List<List<E>> chunk(int newSize) {
        return chunk((index, e) -> e, newSize);
    }

    /**
     * 将一个集合分割成多个小尺寸的小集合, 小集合中类型是Map<String, Object>
     * @param newSize 每个小集合的尺寸
     * @return 包含多个小集合的集合
     */
    default List<List<Map<String, Object>>> chunkToMap(int newSize) {
        return chunk((index, e) -> elementToMap(e), newSize);
    }

    /**
     * 判断集合是否存在任何一个元素的属性的值等于给定值
     * @param fieldName 属性名
     * @param value 给定值
     * @return bool
     */
    boolean contains(String fieldName, @Nullable Object value);

    /**
     * 判断集合是否存在任何一个元素满足条件
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
     * 返回集合中所有元素的总数
     * @return 总数
     */
    default int count() {
        return size();
    }

    /**
     * 计算集合中每个元素的自定义维度的出现次数
     * @param closure 闭包
     * @return Map<自定义维度的类型, 次数>
     */
    default <W> Map<W, Integer> countBy(ReturnTwo<Integer, E, W> closure) {
        Map<W, Integer> resMap = new HashMap<>(16);
        int index = 0;
        for (E e : this) {
            W key = closure.get(index++, e);
            Integer count = resMap.computeIfAbsent(key, k -> 0);
            resMap.put(key, ++count);
        }
        return resMap;
    }

    /**
     * 计算集合中每个元素的指定属性的值的出现次数
     * @param fieldName 属性名
     * @return Map<属性的类型, 次数>
     */
    default <W> Map<W, Integer> countBy(String fieldName) {
        return countBy((index, e) -> elementGetValueByFieldName(e, fieldName));
    }

    /**
     * 是否集合的所有元素能够通过给定的真理测试
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
     * 通过给定回调过滤集合，只有通过给定真理测试的元素才会保留下来
     * 改变自身
     * @param closure 闭包
     * @return 移除的元素个数
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
     * 集合中的所有元素为空的都会被移除
     * 改变自身
     * @return 移除的数量个数
     */
    default int filter() {
        return filter((index, e) -> !isEmpty(e));
    }

    /**
     * 集合中的所有元素的指定属性的值为空的都会被移除
     * 改变自身
     * @param fieldName 属性名
     * @return 移除的数量个数
     */
    default int filter(String fieldName) {
        return filter((index, e) -> !isEmpty(
            getConversionWorkerFromContainer().castNullable(elementGetValueByFieldName(e, fieldName), Object.class)));
    }

    /**
     * 通过给定回调过滤集合，只有通过给定真理测试的元素才会被移除
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
     * 通过给定回调过滤集合，返回满足条件的第一个元素
     * @param closure 闭包
     * @return 第一个元素
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
     * 返回第一个元素
     * @return 第一个元素
     */
    default E first() {
        return getFirst();
    }

    /**
     * 通过给定回调对集合中的元素进行分组
     * @param closure 闭包
     * @return Map<Object, List < E>>
     */
    default <W> Map<W, List<E>> groupBy(ReturnTwo<Integer, E, W> closure) {
        return mapToGroups(closure, (index, e) -> e);
    }

    /**
     * 对集合中的元素按照通过给定属性的值进行分组
     * @param fieldName 属性名
     * @return Map<Object, List < E>>
     */
    default <W> Map<W, List<E>> groupBy(String fieldName) {
        return groupBy((index, e) -> elementGetValueByFieldName(e, fieldName));
    }

    /**
     * 将集合中的每一个元素的属性的值, 使用分隔符连接成一个字符串
     * @param fieldName 属性名
     * @param delimiter 分隔符
     * @return 连接后的字符串
     */
    default String implode(String fieldName, CharSequence delimiter) {
        return implode(e -> elementGetValueByFieldName(e, fieldName), delimiter);
    }

    /**
     * 连接集合中的元素
     * @param closure 闭包
     * @param delimiter 分隔符
     * @return 连接后的字符串
     */
    default String implode(ReturnOne<E, String> closure, CharSequence delimiter) {
        return this.stream()
            .map(e -> getConversionWorkerFromContainer().castNullable(closure.get(e), String.class))
            .collect(Collectors.joining(delimiter));
    }


    /**
     * 方法将指定属性名的值作为集合的键，如果多个元素拥有同一个键，只有最后一个会出现在新集合里面
     * @param fieldName 属性名
     * @return 全新的集合
     */
    default <W> Map<W, E> keyBy(String fieldName) {
        return keyBy((index, e) -> elementGetValueByFieldName(e, fieldName));
    }

    /**
     * 方法将指定回调的结果作为集合的键，如果多个元素拥有同一个键，只有最后一个会出现在新集合里面
     * @param closure 闭包
     * @return 全新的集合
     */
    default <W> Map<W, E> keyBy(ReturnTwo<Integer, E, W> closure) {
        int index = 0;
        Map<W, E> theMap = new HashMap<>(16);
        for (E e : this) {
            theMap.put(closure.get(index++, e), e);
        }
        return theMap;
    }

    /**
     * 通过给定回调过滤集合，返回满足条件的最后个元素
     * @param closure 闭包
     * @return 最后个元素
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
     * 返回最后个元素
     * @return 最后个元素
     */
    default E last() {
        return getLast();
    }

    /**
     * 通过给定回调对集合中的元素进行分组
     * @param closureKey 闭包生成key
     * @param closureValue 闭包生成value
     * @return 新的集合
     */
    default <W, Y> Map<W, List<Y>> mapToGroups(ReturnTwo<Integer, E, W> closureKey,
        ReturnTwo<Integer, E, Y> closureValue) {
        int index = 0;
        Map<W, List<Y>> outsideMap = new HashMap<>(16);

        for (E e : this) {
            W key = closureKey.get(index, e);
            Y value = closureValue.get(index++, e);

            List<Y> innerList = outsideMap.computeIfAbsent(key, k -> new ArrayList<>());
            innerList.add(value);
        }
        return outsideMap;
    }

    /**
     * 通过给定回调对集合元素进行索引
     * @param closureKey 闭包生成key
     * @param closureValue 闭包生成value
     * @return 新的集合
     */
    default <W, Y> Map<W, Y> mapWithKeys(ReturnTwo<Integer, E, W> closureKey, ReturnTwo<Integer, E, Y> closureValue) {
        int index = 0;
        Map<W, Y> outsideMap = new HashMap<>(16);

        for (E e : this) {
            W key = closureKey.get(index, e);
            Y value = closureValue.get(index++, e);
            outsideMap.put(key, value);
        }
        return outsideMap;
    }

    /**
     * 将集合中的每个元素的指定属性的值, 组合成新的列表
     * @param fieldName 属性名
     * @return 新的列表
     */
    default <W> List<W> pluck(String fieldName) {
        List<W> res = new ArrayList<>();
        for (E e : this) {
            res.add(elementGetValueByFieldName(e, fieldName));
        }
        return res;
    }

    /**
     * 将集合中的每个元素的指定属性value的值, 使用给定的属性key的值进行索引, 如果存在重复索引，最后一个匹配的元素将会插入集合
     * @param fieldNameForValue 属性名
     * @param fieldNameForKey 属性名
     * @return 值的集合
     */
    default <W, Y> Map<W, Y> pluck(String fieldNameForValue, String fieldNameForKey) {
        Map<W, Y> res = new HashMap<>();
        for (E e : this) {
            res.put(elementGetValueByFieldName(e, fieldNameForKey), elementGetValueByFieldName(e, fieldNameForValue));
        }
        return res;
    }

    /**
     * 移除并返回集合中的第一个元素, 集合为空时返回null
     * 改变自身
     * @return 元素
     */
    @Nullable
    default E shift() {
        return isEmpty() ? null : remove(0);
    }

    /**
     * 在集合中设置给定键和值, 原值将被替换
     * 改变自身
     * @param index 索引
     * @param element 元素
     * @throws IndexOutOfBoundsException 数组越界
     */
    default void put(int index, E element) throws IndexOutOfBoundsException {
        set(index, element);
    }

    /**
     * 通过索引从集合中移除并返回元素, 其后的元素前移
     * 改变自身
     * @param index 索引
     * @return 元素
     * @throws IndexOutOfBoundsException 数组越界
     */
    default E pull(int index) {
        return remove(index);
    }

    /**
     * 从集合中返回随机元素, 集合为空时返回null
     * @return 元素
     */
    @Nullable
    default E random() {
        if (isEmpty()) {
            return null;
        } else if (size() == 1) {
            return get(0);
        } else {
            return random(1).get(0);
        }
    }

    /**
     * 从集合中返回指定个数的随机元素
     * @param count 元素个数
     * @return 随机元素列表
     * @throws NoSuchAlgorithmException 随机算法错误
     */
    List<E> random(int count) throws NoSuchAlgorithmException;

    /**
     * 将集合中元素的顺序颠倒, 不影响原集合
     * @return 倒序后的集合
     */
    default List<E> reverse() {
        List<E> list = new ArrayList<>();
        for (int i = size() - 1; i >= 0; i--) {
            list.add(get(i));
        }
        return list;
    }

    /**
     * 通过给定回调对集合进行排序
     * @param closure 闭包
     * @param ase 正序
     * @return 新的集合
     */
    default List<E> sortBy(ReturnTwo<Integer, E, BigDecimal> closure, boolean ase) {
        PriorityQueue<BigDecimal> heap = new PriorityQueue<>(size(),
            (o1, o2) -> ase ? o1.compareTo(o2) : o2.compareTo(o1));
        List<E> list = new ArrayList<>(size());
        Map<BigDecimal, List<E>> map = new HashMap<>();
        int index = 0;
        for (E e : this) {
            final BigDecimal comparable = closure.get(index++, e);
            heap.add(comparable);

            final List<E> innerList = map.computeIfAbsent(comparable, k -> new ArrayList<>());
            innerList.add(e);
        }
        while (!heap.isEmpty()) {
            BigDecimal key = heap.poll();
            if (map.containsKey(key)) {
                List<E> eList = map.remove(key);
                if (isEmpty(eList)) {
                    break;
                }
                list.addAll(eList);
            }

        }
        return list;
    }

    /**
     * 通过给定回调对集合进行正序排序
     * @param closure 闭包
     * @return 新的集合
     */
    default List<E> sortBy(ReturnTwo<Integer, E, BigDecimal> closure) {
        return sortBy(closure, true);
    }

    /**
     * 通过元素中的指定属性的值，对集合进行正序排序
     * @param fieldName 属性名
     * @return 新的集合
     */
    default List<E> sortBy(String fieldName) {
        return sortBy((index, e) -> {
            final BigDecimal decimal = getConversionWorkerFromContainer().castNullable(
                elementGetValueByFieldName(e, fieldName), BigDecimal.class);
            return decimal == null ? BigDecimal.ZERO : decimal;
        });
    }

    /**
     * 通过元素中的指定属性的值，对集合进行倒序排序
     * @param fieldName 属性名
     * @return 新的集合
     */
    default List<E> sortByDesc(String fieldName) {
        return sortByDesc((index, e) -> {
            final BigDecimal decimal = getConversionWorkerFromContainer().castNullable(
                elementGetValueByFieldName(e, fieldName), BigDecimal.class);
            return decimal == null ? BigDecimal.ZERO : decimal;
        });
    }

    /**
     * 通过给定回调对集合进行倒序排序
     * @param closure 闭包
     * @return 新的集合
     */
    default List<E> sortByDesc(ReturnTwo<Integer, E, BigDecimal> closure) {
        return sortBy(closure, false);
    }

    /**
     * 从给定位置开始移除并返回元素切片
     * 影响自身
     * @param offset 偏移量
     * @return 新的集合
     */
    default List<E> splice(int offset) {
        return splice(offset, size() - offset);
    }

    /**
     * 从给定位置开始移除指定数据大小并返回元素切片
     * 影响自身
     * @param offset 偏移量
     * @param taken 数据大小
     * @return 新的集合
     */
    default List<E> splice(int offset, int taken) {
        int size = Math.min(size() - offset, taken);
        List<E> list = new ArrayList<>(size);
        int count = 0;
        Iterator<E> iterator = iterator();
        int index = 0;
        while (iterator.hasNext()) {
            E element = iterator.next();
            // 未达到开始条件
            if (index++ < offset) {
                continue;
            }
            // 已完成
            if (size <= count++) {
                break;
            }
            list.add(element);
            iterator.remove();
        }
        return list;
    }

    /**
     * 使用指定数目的元素返回一个新的集合
     * 影响自身
     * @param count 指定数目
     * @return 新的集合
     */
    default List<E> take(int count) {
        if (count < 0) {
            return splice(size() + count, -count);
        }
        return splice(0, count);
    }

    /**
     * 使用回调来剔除重复的元素，不影响自身
     * @param closure 闭包
     * @return 去重后的集合
     */
    default List<E> unique(ReturnTwo<Integer, E, Object> closure) {
        List<E> list = new ArrayList<>();
        Set<Object> uniqueSet = new HashSet<>();
        int index = 0;
        for (E e : this) {
            final Object key = closure.get(index++, e);
            if (uniqueSet.contains(key)) {
                continue;
            }
            uniqueSet.add(key);
            list.add(e);
        }
        return list;
    }

    /**
     * 使用元素中的指定属性来剔除重复的元素，不影响自身
     * @param fieldName 属性名
     * @return 去重后的集合
     */
    default List<E> unique(String fieldName) {
        return unique((index, e) -> elementGetValueByFieldName(e, fieldName));
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
    interface ReturnOne<A, C> {

        @Nullable
        C get(A a);
    }

    @FunctionalInterface
    interface ReturnTwo<A, B, C> {

        @Nullable
        C get(A a, B b);
    }
}
