package gaarason.database.contract.builder;

import gaarason.database.contract.function.TransactionFunctionalInterface;
import gaarason.database.exception.NestedTransactionException;
import gaarason.database.exception.SQLRuntimeException;

/**
 * 事物
 * @param <T>
 * @param <K>
 */
public interface Transaction<T, K> {

    /**
     * 标记开启一个事物
     * @throws SQLRuntimeException        数据库异常
     * @throws NestedTransactionException 事物嵌套异常
     */
    void begin() throws SQLRuntimeException, NestedTransactionException;

    /**
     * 提交事物
     * @throws SQLRuntimeException 数据库异常
     */
    void commit() throws SQLRuntimeException;

    /**
     * 回滚事物
     * @throws SQLRuntimeException 数据库异常
     */
    void rollBack() throws SQLRuntimeException;

    /**
     * 当前是否处于事物中
     * @return 当前是否处于事物中
     */
    boolean inTransaction();

    /**
     * 以闭包开启一个事物
     * @param closure 事物中的处理
     * @return 事物中的处理的结果 (事物失败抛出异常)
     */
    <V> V transaction(TransactionFunctionalInterface<V> closure);

    /**
     * 以闭包开启一个事物
     * @param closure     事物中的处理
     * @param maxAttempts 事物死锁重试次数
     * @return 事物中的处理的结果 (事物失败抛出异常)
     */
    <V> V transaction(TransactionFunctionalInterface<V> closure, int maxAttempts);

    /**
     * 以闭包开启一个事物
     * @param closure 事物中的处理
     */
    void transaction(Runnable closure);

    /**
     * 以闭包开启一个事物
     * @param closure     事物中的处理
     * @param maxAttempts 事物死锁重试次数
     */
    void transaction(Runnable closure, int maxAttempts);
}
