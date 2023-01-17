package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;

/**
 * 锁
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Lock<T, K> {

    /**
     * lock in share mode 不会阻塞其它事务读取被锁定行记录的值
     * 相对适用于,两张以及多张表存在业务关系时的一致性要求,性能稍好
     * @return 查询构造器
     */
    Builder<T, K> sharedLock();

    /**
     * for update 会阻塞其他锁定性读对锁定行的读取
     * 相对适用于,适用于操作同一张表时的一致性要求,性能稍弱
     * @return 查询构造器
     */
    Builder<T, K> lockForUpdate();
}
