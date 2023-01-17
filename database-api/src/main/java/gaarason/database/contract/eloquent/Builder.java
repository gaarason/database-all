package gaarason.database.contract.eloquent;

import gaarason.database.contract.builder.*;
import gaarason.database.contract.support.ExtendedSerializable;

/**
 * 查询构造器
 * @param <T> 实体类型
 * @param <K> 主键类型
 * @author xt
 */
public interface Builder<T, K>
    extends Cloneable, Debug, ColumnLambda<T, K>, Union<T, K>, Support<T, K>, From<T, K>, ExecuteLambda<T, K>,
    WithLambda<T, K>, SelectLambda<T, K>, OrderLambda<T, K>, Limit<T, K>, GroupLambda<T, K>,
    Value<T, K>, DataLambda<T, K>, Transaction, AggregatesLambda<T, K>, Pager<T, K>, Index<T, K>, Lock<T, K>,
    Native<T, K>, JoinLambda<T, K>, AbilityLambda<T, K>, When<T, K>, WhereLambda<T, K>, HavingLambda<T, K>,
    ExtendedSerializable {

    /**
     * 反序列化到指定查询构造器
     * @param bytes 序列化byte[]
     * @param <M> 实体类型
     * @param <N> 主键类型
     * @return 查询构造器
     */
    static <M, N> Builder<M, N> deserialize(byte[] bytes) {
        return ExtendedSerializable.deserialize(bytes);
    }

    /**
     * 反序列化到指定查询构造器
     * @param serializeStr 序列化String
     * @param <M> 实体类型
     * @param <N> 主键类型
     * @return 查询构造器
     */
    static <M, N> Builder<M, N> deserialize(String serializeStr) {
        return ExtendedSerializable.deserialize(serializeStr);
    }

}
