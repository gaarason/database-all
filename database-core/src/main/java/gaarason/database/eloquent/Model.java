package gaarason.database.eloquent;

import gaarason.database.contract.eloquent.Builder;

/**
 * model
 * @author xt
 * @see StrictModel
 */
public abstract class Model<B extends Builder<B, T, K>, T, K> extends StrictModel<B, T, K> {

}
