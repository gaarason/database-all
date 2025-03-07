package gaarason.database.eloquent;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;

/**
 * 数据模型对象
 * @author xt
 */
abstract class StrictModel<B extends Builder<B, T, K>, T, K> extends ModelOfQuery<B, T, K> implements Model<B, T, K> {

}
