package gaarason.database.contract.eloquent;

import gaarason.database.contract.builder.*;
import gaarason.database.contract.support.ExtendedSerializable;

/**
 * 查询构造器
 * @param <T> 实体类型
 * @param <K> 主键类型
 * @author xt
 */
public interface Builder<B extends Builder<B, T, K>, T, K>
    extends Debug, ColumnLambda<B, T, K>, Union<B, T, K>, Support<B, T, K>, From<B, T, K>, ExecuteLambda<B, T, K>,
    WithLambda<B, T, K>, SelectLambda<B, T, K>, OrderLambda<B, T, K>, Limit<B, T, K>, GroupLambda<B, T, K>,
    Value<B, T, K>, DataLambda<B, T, K>, Transaction, AggregatesLambda<B, T, K>, Pager<B, T, K>, Index<B, T, K>, Lock<B, T, K>,
    Native<T, K>, JoinLambda<B, T, K>, AbilityLambda<B, T, K>, When<B, T, K>, WhereLambda<B, T, K>, HavingLambda<B, T, K>,
    ExtendedSerializable {

    /**
     * 反序列化到指定查询构造器
     * @param bytes 序列化byte[]
     * @param <M> 实体类型
     * @param <N> 主键类型
     * @return 查询构造器
     */
    static <L extends Builder<L, M, N>, M, N> Builder<L, M, N> deserialize(byte[] bytes) {
        return ExtendedSerializable.deserialize(bytes);
    }

    /**
     * 反序列化到指定查询构造器
     * @param serializeStr 序列化String
     * @param <M> 实体类型
     * @param <N> 主键类型
     * @return 查询构造器
     */
    static <L extends Builder<L, M, N>, M, N> Builder<L, M, N> deserialize(String serializeStr) {
        return ExtendedSerializable.deserialize(serializeStr);
    }

}
