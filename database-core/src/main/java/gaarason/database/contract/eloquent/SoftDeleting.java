package gaarason.database.contract.eloquent;

import gaarason.database.query.Builder;

/**
 * 软删除
 * @param <T> 实体类
 * @param <K> 主键类型
 */
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
