package gaarason.database.contract.record;

import gaarason.database.core.lang.Nullable;
import gaarason.database.exception.NoSuchAlgorithmException;
import gaarason.database.util.ConverterUtils;
import gaarason.database.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
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
    <W> W getValueByFieldName(E element, String fieldName);

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
        return bigDecimal.divide(new BigDecimal(size()), RoundingMode.HALF_UP);
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
        return bigDecimal.divide(new BigDecimal(size()), RoundingMode.HALF_UP);
    }

    /**
     * 返回集合中所有数据项的和
     * @param fieldName 属性名
     * @return 最大值
     */
    default BigDecimal sum(String fieldName) {
        BigDecimal sum = null;
        for (E e : this) {
            Object valueObj = getValueByFieldName(e, fieldName);
            BigDecimal value = ObjectUtils.isEmpty(valueObj) ? BigDecimal.ZERO : ConverterUtils.castNullable(valueObj, BigDecimal.class);
            sum = sum == null ? value : sum.add(value);
        }
        return sum == null ? BigDecimal.ZERO : sum;
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
            BigDecimal value = ObjectUtils.isEmpty(valueObj) ? BigDecimal.ZERO :
                ConverterUtils.castNullable(valueObj, BigDecimal.class);
            maxValue = maxValue == null ? value : maxValue.max(value);
        }
        return maxValue == null ? BigDecimal.ZERO : maxValue;
    }

    /**
     * 返回集合中给定键的最小值
     * @param fieldName 属性名
     * @return 最小值
     */
    default BigDecimal min(String fieldName) {
        BigDecimal minValue = null;
        for (E e : this) {
            Object valueObj = getValueByFieldName(e, fieldName);
            BigDecimal value = ObjectUtils.isEmpty(valueObj) ? BigDecimal.ZERO : ConverterUtils.castNullable(valueObj, BigDecimal.class);
            minValue = minValue == null ? value : minValue.min(value);
        }
        return minValue == null ? BigDecimal.ZERO : minValue;
    }

    /**
     * 返回集合中给定键的众数
     * @param fieldName 属性名
     * @return 众数列表
     */
    default <W> List<W> mode(String fieldName) {
        // 计数
        Map<String, Integer> countMap = new HashMap<>(16);
        List<W> res = new ArrayList<>();
        int maxCount = 0;
        for (E e : this) {
            W valueObj = getValueByFieldName(e, fieldName);
            Integer count = countMap.computeIfAbsent(ConverterUtils.castNullable(valueObj, String.class), k -> 0);
            count++;
            countMap.put(ConverterUtils.castNullable(valueObj, String.class), count);

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
     * 返回集合中给定键的中位数
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
            Object valueObj = getValueByFieldName(e, fieldName);
            BigDecimal value = ObjectUtils.isEmpty(valueObj) ? BigDecimal.ZERO : ConverterUtils.castNullable(valueObj, BigDecimal.class);

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
        if (!innerList.isEmpty()) {
            outsideList.add(innerList);
        }
        return outsideList;
    }

    /**
     * 判断集合是否包含一个给定值
     * @param fieldName 属性名
     * @param value     给定值
     * @return bool
     */
    default boolean contains(String fieldName, Object value) {
        for (E e : this) {
            if (ObjectUtils.nullSafeEquals(getValueByFieldName(e, fieldName), value)) {
                return true;
            }
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
     * @param closure 闭包
     * @return Map<E, 次数>
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
     * 计算每个元素的出现次数
     * @return Map<E, 次数>
     */
    default <W> Map<W, Integer> countBy(String fieldName) {
        return countBy((index, e) -> getValueByFieldName(e, fieldName));
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
    default <W> Map<W, List<E>> groupBy(ReturnTwo<Integer, E, W> closure) {
        return mapToGroups(closure, (index, e) -> e);
    }

    /**
     * 通过给定键分组集合数据项
     * @param fieldName 属性名
     * @return Map<Object, List < E>>
     */
    default <W> Map<W, List<E>> groupBy(String fieldName) {
        return groupBy((index, e) -> getValueByFieldName(e, fieldName));
    }

    /**
     * 连接集合中的数据项
     * @param fieldName 属性名
     * @param delimiter 分隔符
     * @return 连接后的字符串
     */
    default String implode(String fieldName, CharSequence delimiter) {
        return implode(e -> getValueByFieldName(e, fieldName), delimiter);
    }

    /**
     * 连接集合中的数据项
     * @param closure   闭包
     * @param delimiter 分隔符
     * @return 连接后的字符串
     */
    default String implode(ReturnOne<E, String> closure, CharSequence delimiter) {
        return this.stream().map(e -> ConverterUtils.castNullable(closure.get(e), String.class)).collect(
            Collectors.joining(delimiter));
    }


    /**
     * 方法将指定属性名的值作为集合的键，如果多个数据项拥有同一个键，只有最后一个会出现在新集合里面
     * @param fieldName 属性名
     * @return 全新的集合
     */
    default <W> Map<W, E> keyBy(String fieldName) {
        return keyBy((index, e) -> getValueByFieldName(e, fieldName));
    }

    /**
     * 方法将指定回调的结果作为集合的键，如果多个数据项拥有同一个键，只有最后一个会出现在新集合里面
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
        return get(size() - 1);
    }

    /**
     * 通过给定回调对集合项进行分组
     * @param closureKey   闭包生成key
     * @param closureValue 闭包生成value
     * @return 新的集合
     */
    default <W,Y> Map<W, List<Y>> mapToGroups(ReturnTwo<Integer, E, W> closureKey, ReturnTwo<Integer, E, Y> closureValue) {
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
     * 通过给定回调对集合项进行索引
     * @param closureKey   闭包生成key
     * @param closureValue 闭包生成value
     * @return 新的集合
     */
    default <W,Y> Map<W, Y> mapWithKeys(ReturnTwo<Integer, E, W> closureKey, ReturnTwo<Integer, E, Y> closureValue) {
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
     * 为给定属性获取所有集合值
     * @param fieldName 属性名
     * @return 值的集合
     */
    default <W> List<W> pluck(String fieldName) {
        List<W> res = new ArrayList<>();
        for (E e : this) {
            res.add(getValueByFieldName(e, fieldName));
        }
        return res;
    }

    /**
     * 为给定属性获取所有集合值, 并使用给定的属性进行索引, 如果存在重复索引，最后一个匹配的元素将会插入集合
     * @param fieldNameForValue 属性名
     * @param fieldNameForKey   属性名
     * @return 值的集合
     */
    default <W,Y> Map<W, Y> pluck(String fieldNameForValue, String fieldNameForKey) {
        Map<W, Y> res = new HashMap<>();
        for (E e : this) {
            res.put(getValueByFieldName(e, fieldNameForKey), getValueByFieldName(e, fieldNameForValue));
        }
        return res;
    }

    /**
     * 移除并返回集合中第一个的数据
     * @return 数据项
     * @throws IndexOutOfBoundsException 数据为空
     */
    default E shift() throws IndexOutOfBoundsException {
        return remove(0);
    }

    /**
     * 移除并返回集合中最后面的数据
     * @return 数据项
     * @throws IndexOutOfBoundsException 数据为空
     */
    default E pop() throws IndexOutOfBoundsException {
        return this.remove(size() - 1);
    }

    /**
     * 添加数据项到集合开头, 其他元素后移
     * @param element 元素
     */
    default void prepend(E element) {
        add(0, element);
    }

    /**
     * 添加数据项到集合结尾
     * @param element 元素
     */
    default void push(E element) {
        add(element);
    }

    /**
     * 在集合中设置给定键和值, 原值将被替换
     * @param index   索引
     * @param element 元素
     */
    default void put(int index, E element) {
        set(index, element);
    }

    /**
     * 通过索引从集合中移除并返回数据项, 其后的元素前移
     * @param index 索引
     * @return 元素
     */
    default E pull(int index) {
        return remove(index);
    }

    /**
     * 从集合中返回随机元素
     * @return 元素
     */
    default E random() {
        int randomIndex = new Random().nextInt(size() - 1);
        return get(randomIndex);
    }

    /**
     * 从集合中返回指定个数的随机元素
     * @param count 元素个数
     * @return 随机元素列表
     * @throws NoSuchAlgorithmException 随机算法错误
     */
    default List<E> random(int count) throws NoSuchAlgorithmException {
        try {
            Map<Integer, E> res = new HashMap<>(count);
            final Random random = SecureRandom.getInstanceStrong();
            for (int i = 0; i < count; i++) {
                int index = random.nextInt(size() - 1);
                // 已经存在则跳过
                if (res.containsKey(index)) {
                    i--;
                    continue;
                }
                res.put(index, get(index));
            }
            return new ArrayList<>(res.values());
        } catch (Throwable e) {
            throw new NoSuchAlgorithmException(e);
        }
    }

    /**
     * 将集合数据项的顺序颠倒
     * @return 倒序后的集合
     */
    default List<E> reverse() {
        List<E> list = new ArrayList<>();
        for (int i = size() - 1; i > 0; i--) {
            list.add(get(i));
        }
        return list;
    }

    /**
     * 通过给定键对集合进行排序
     * @param closure 闭包
     * @param ase     正序
     * @return 新的集合
     */
    default List<E> sortBy(ReturnTwo<Integer, E, BigDecimal> closure, boolean ase) {
        PriorityQueue<BigDecimal> heap = new PriorityQueue<>(size(), (o1, o2) -> ase ? o1.compareTo(o2) : o2.compareTo(o1));
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
            list.addAll(map.get(heap.poll()));
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
     * 通过指定属性到的值对集合进行正序排序
     * @param fieldName 属性名
     * @return 新的集合
     */
    default List<E> sortBy(String fieldName) {
        return sortBy((index, e) -> {
            final BigDecimal decimal = ConverterUtils.castNullable(getValueByFieldName(e, fieldName), BigDecimal.class);
            return decimal == null ? BigDecimal.ZERO : decimal;
        }, true);
    }

    /**
     * 通过指定属性到的值对集合进行倒序排序
     * @param fieldName 属性名
     * @return 新的集合
     */
    default List<E> sortByDesc(String fieldName) {
        return sortBy((index, e) -> {
            final BigDecimal decimal = ConverterUtils.castNullable(getValueByFieldName(e, fieldName), BigDecimal.class);
            return decimal == null ? BigDecimal.ZERO : decimal;
        }, false);
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
     * 从给定位置开始移除并返回数据项切片
     * @param offset 平移量
     * @return 新的集合
     */
    default List<E> splice(int offset) {
        return splice(offset, size());
    }

    /**
     * 从给定位置开始移除并返回数据项切片
     * @param offset 平移量
     * @param taken  数据大小
     * @return 新的集合
     */
    default List<E> splice(int offset, int taken) {
        List<E> list = new ArrayList<>();
        int count = 0;
        for (int i = offset; i < size(); i++) {
            if (taken <= count++) {
                break;
            }
            list.add(get(i));
        }
        return list;
    }

    /**
     * 使用指定数目的数据项返回一个新的集合
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
     * 指定自己的回调用于判断数据项唯一性
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
     * 使用属性判断数据项唯一性
     * @param fieldName 属性名
     * @return 去重后的集合
     */
    default List<E> unique(String fieldName) {
        return unique((index, e) -> getValueByFieldName(e, fieldName));
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
