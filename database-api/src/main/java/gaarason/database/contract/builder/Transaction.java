package gaarason.database.contract.builder;

import gaarason.database.contract.function.TransactionFunctionalInterface;
import gaarason.database.exception.SQLRuntimeException;

import java.util.concurrent.CompletableFuture;

/**
 * 事物
 * @author xt
 */
public interface Transaction {

    /**
     * 标记开启一个事物
     * @throws SQLRuntimeException 数据库异常
     */
    void begin() throws SQLRuntimeException;

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
     * 以闭包开启一个事物
     * @param closure 事物中的处理
     * @return 事物中的处理的结果 (事物失败抛出异常)
     */
    <V> V transaction(TransactionFunctionalInterface<V> closure);

    /**
     * 以闭包异步开启一个事物
     * @param closure 事物中的处理
     * @return 事物中的处理的结果 (事物失败抛出异常)
     */
    <V> CompletableFuture<V> transactionAsync(TransactionFunctionalInterface<V> closure);

    /**
     * 以闭包开启一个事物
     * @param closure 事物中的处理
     * @param maxAttempts 事物死锁重试次数
     * @return 事物中的处理的结果 (事物失败抛出异常)
     */
    <V> V transaction(TransactionFunctionalInterface<V> closure, int maxAttempts);

    /**
     * 以闭包异步开启一个事物
     * @param closure 事物中的处理
     * @param maxAttempts 事物死锁重试次数
     * @return 事物中的处理的结果 (事物失败抛出异常)
     */
    <V> CompletableFuture<V> transactionAsync(TransactionFunctionalInterface<V> closure, int maxAttempts);

    /**
     * 以闭包开启一个事物
     * @param closure 事物中的处理
     */
    void transaction(Runnable closure);

    /**
     * 以闭包异步开启一个事物
     * @param closure 事物中的处理
     */
    CompletableFuture<Boolean> transactionAsync(Runnable closure);

    /**
     * 以闭包开启一个事物
     * @param closure 事物中的处理
     * @param maxAttempts 事物死锁重试次数
     */
    void transaction(Runnable closure, int maxAttempts);

    /**
     * 以闭包异步开启一个事物
     * @param closure 事物中的处理
     * @param maxAttempts 事物死锁重试次数
     */
    CompletableFuture<Boolean> transactionAsync(Runnable closure, int maxAttempts);
}
