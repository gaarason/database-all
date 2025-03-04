package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.BuilderWrapper;

/**
 * 结果集连接
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Union<B extends Builder<B, T, K>, T, K> {

    /**
     * 结果集连接(去重)
     * @param closure 返回代码片段
     * @return 查询构造器
     */
    B union(BuilderWrapper<B, T, K> closure);

    /**
     * 结果集连接(不去重)
     * @param closure 返回代码片段
     * @return 查询构造器
     */
    B unionAll(BuilderWrapper<B, T, K> closure);

    /**
     * 结果集连接(去重)
     * union 的迭代版本
     * @param builder 查询构造器
     * @return 查询构造器
     * @see Union#unionAll(Builder)
     */
    B union(Builder<?, ?, ?> builder);

    /**
     * unionAll 的迭代版本
     * 之前，在迭代中使用 unionAll 可能会出现如下写法
     * <pre>
     *     Builder newQuery = ....;
     *     Map map = ....;
     *     // 定义标记
     *     AtomicBoolean firstAction = new AtomicBoolean(true);
     *     // 迭代
     *     map.forEach((k , v) -> {
     *                  // 每次迭代的语句
     *                 Builder<?, ?> subBuilder = ....;
     *
     *                  // 判断初次
     *                 if(firstAction.get()){
     *                     newQuery.setAnyBuilder(subBuilder);
     *                     // 设置标记
     *                     firstAction.set(false);
     *                 }else {
     *                     newQuery.unionAll(builder -> builder.setAnyBuilder(subBuilder));
     *                 }
     *             });
     * </pre>
     * <p>
     * 现在可以更方便的实现上述语义
     * <pre>
     *     Builder newQuery = ....;
     *     Map map = ....;
     *     map.forEach((k ,v) -> {
     *         // 每次迭代的语句
     *         Builder subBuilder = ....;
     *         newQuery.unionAll(subBuilder);
     *
     *     });
     * </pre>
     * @param builder 查询构造器
     * @return 查询构造器
     */
    B unionAll(Builder<?, ?, ?> builder);
}
