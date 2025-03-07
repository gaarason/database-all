package gaarason.database.eloquent;

import gaarason.database.contract.eloquent.Model;

/**
 * 数据模型对象
 * @author xt
 */
abstract public class StrictModel<T, K> extends ModelOfQuery<T, K> implements Model<T, K> {

}
