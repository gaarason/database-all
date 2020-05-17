package gaarason.database.eloquent.repository;

import gaarason.database.query.Builder;

abstract public class BaseSoftDeleting<T, K> extends BaseInitializing<T, K> {

    /**
     * 是否启用软删除
     */
    protected boolean softDeleting() {
        return false;
    }

    /**
     * 删除(软/硬删除)
     * @param builder 查询构造器
     * @return 删除的行数
     */
    public int delete(Builder<T, K> builder) {
        return softDeleting() ? softDelete(builder) : builder.forceDelete();
    }

    /**
     * 恢复软删除
     * @param builder 查询构造器
     * @return 删除的行数
     */
    public int restore(Builder<T, K> builder) {
        return softDeleteRestore(builder);
    }

    /**
     * 软删除查询作用域(反)
     * @param builder 查询构造器
     */
    protected void scopeSoftDeleteOnlyTrashed(Builder<T, K> builder) {
        builder.where("is_deleted", "1");
    }

    /**
     * 软删除查询作用域(全)
     * @param builder 查询构造器
     */
    protected void scopeSoftDeleteWithTrashed(Builder<T, K> builder) {

    }

    /**
     * 软删除查询作用域
     * @param builder 查询构造器
     */
    protected void scopeSoftDelete(Builder<T, K> builder) {
        builder.where("is_deleted", "0");
    }


    /**
     * 软删除实现
     * @param builder 查询构造器
     * @return 删除的行数
     */
    protected int softDelete(Builder<T, K> builder) {
        return builder.data("is_deleted", "1").update();
    }

    /**
     * 恢复软删除实现
     * @param builder 查询构造器
     * @return 恢复的行数
     */
    protected int softDeleteRestore(Builder<T, K> builder) {
        return builder.data("is_deleted", "0").update();
    }
}
