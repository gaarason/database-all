package gaarason.database.contracts.eloquent;

import gaarason.database.query.Builder;

public interface SoftDeleting<T, K> {

    /**
     * 删除(软/硬删除)
     * @param builder 查询构造器
     * @return 删除的行数
     */
    int delete(Builder<T, K> builder) ;

    /**
     * 恢复软删除
     * @param builder 查询构造器
     * @return 删除的行数
     */
    int restore(Builder<T, K> builder);
}
