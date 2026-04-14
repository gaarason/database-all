package gaarason.database.eloquent;

import gaarason.database.contract.eloquent.Builder;

/**
 * 数据模型对象
 * @author xt
 */
abstract class StrictModel<B extends Builder<B, T, K>, T, K> extends ModelOfQuery<B, T, K> {

}
