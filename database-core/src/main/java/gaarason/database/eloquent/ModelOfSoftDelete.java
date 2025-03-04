package gaarason.database.eloquent;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.model.SoftDelete;

/**
 * 数据模型对象
 * @author xt
 */
public abstract class ModelOfSoftDelete<T, K> extends ModelBase<T, K> implements SoftDelete<T, K> {

    private static final String DEFAULT_SOFT_DELETED_COLUMN_NAME = "is_deleted";

    private static final String DEFAULT_SOFT_DELETED_VALUE_YSE = "1";

    private static final String DEFAULT_SOFT_DELETED_VALUE_NO = "0";

    @Override
    public int delete(Builder<?, T, K> builder) {
        return softDeleting() ? softDelete(builder) : builder.forceDelete();
    }

    @Override
    public int restore(Builder<?, T, K> builder) {
        return softDeleteRestore(builder);
    }

    /**
     * 是否启用软删除
     */
    protected boolean softDeleting() {
        return false;
    }

    /**
     * 软删除查询作用域(反)
     * @param builder 查询构造器
     */
    protected void scopeSoftDeleteOnlyTrashed(Builder<?, T, K> builder) {
        builder.where(DEFAULT_SOFT_DELETED_COLUMN_NAME, DEFAULT_SOFT_DELETED_VALUE_YSE);
    }

    /**
     * 软删除查询作用域(全)
     * @param builder 查询构造器
     */
    protected void scopeSoftDeleteWithTrashed(Builder<?, T, K> builder) {

    }

    /**
     * 软删除查询作用域
     * @param builder 查询构造器
     */
    protected void scopeSoftDelete(Builder<?, T, K> builder) {
        builder.where(DEFAULT_SOFT_DELETED_COLUMN_NAME, DEFAULT_SOFT_DELETED_VALUE_NO);
    }

    /**
     * 软删除实现
     * @param builder 查询构造器
     * @return 删除的行数
     */
    protected int softDelete(Builder<?, T, K> builder) {
        return builder.data(DEFAULT_SOFT_DELETED_COLUMN_NAME, DEFAULT_SOFT_DELETED_VALUE_YSE).update();
    }

    /**
     * 恢复软删除实现
     * @param builder 查询构造器
     * @return 恢复的行数
     */
    protected int softDeleteRestore(Builder<?, T, K> builder) {
        return builder.data(DEFAULT_SOFT_DELETED_COLUMN_NAME, DEFAULT_SOFT_DELETED_VALUE_NO).update();
    }


}
