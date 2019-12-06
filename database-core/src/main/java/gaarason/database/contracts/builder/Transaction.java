package gaarason.database.contracts.builder;

import gaarason.database.exception.NestedTransactionException;
import gaarason.database.exception.SQLRuntimeException;

public interface Transaction<T> {

    /**
     * 标记开启一个事物
     * @throws SQLRuntimeException 数据库异常
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
     * @param runnable    事物中的处理
     * @param maxAttempts 事物重试次数
     * @return 事物最终执行成功与否
     */
    boolean transaction(Runnable runnable, int maxAttempts);
}
