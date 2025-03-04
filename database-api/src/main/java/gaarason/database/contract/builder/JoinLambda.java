package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;

/**
 * 连接
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface JoinLambda<B extends Builder<B, T, K>, T, K> extends Join<B, T, K>, Support<B, T, K> {

}
