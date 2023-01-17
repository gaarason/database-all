package gaarason.database.contract.model;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 原始sql执行, 不会做事件触发
 * @param <T> 实体类
 * @param <K> 主键类型
 * @author xt
 * @see gaarason.database.contract.eloquent.Builder
 */
public interface NativeQuery<T, K> {

    /**
     * 异步执行器(一般是线程池)
     * @return 异步执行器
     */
    ExecutorService getExecutorService();

    /**
     * 获取第一条数据, 数据为空时抛出异常
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 数剧记录
     * @throws SQLRuntimeException 数据库异常
     * @throws EntityNotFoundException 没有数据
     */
    Record<T, K> nativeQueryOrFail(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException;

    /**
     * 获取第一条数据, 数据为空时返回null
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 数剧记录
     * @throws SQLRuntimeException 数据库异常
     */
    @Nullable
    Record<T, K> nativeQuery(String sql, @Nullable Collection<?> parameters)
        throws SQLRuntimeException, EntityNotFoundException;

    /**
     * 获取所有数据
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 数剧记录
     * @throws SQLRuntimeException 数据库异常
     */
    RecordList<T, K> nativeQueryList(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException;

    /**
     * 执行语句
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 受影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int nativeExecute(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException;

    /**
     * 执行语句
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 自增长主键列表
     * @throws SQLRuntimeException 数据库异常
     */
    List<K> nativeExecuteGetIds(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException;

    /**
     * 执行语句
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 自增长主键|null
     * @throws SQLRuntimeException 数据库异常
     */
    @Nullable
    K nativeExecuteGetId(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException;

    /**
     * 异步获取第一条数据, 数据为空时抛出异常
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 数剧记录
     * @throws SQLRuntimeException 数据库异常
     * @throws EntityNotFoundException 没有数据
     */
    CompletableFuture<Record<T, K>> nativeQueryOrFailAsync(String sql, @Nullable Collection<?> parameters)
        throws SQLRuntimeException;

    /**
     * 异步获取第一条数据, 数据为空时返回null
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 数剧记录
     * @throws SQLRuntimeException 数据库异常
     */
    CompletableFuture<Record<T, K>> nativeQueryAsync(String sql, @Nullable Collection<?> parameters)
        throws SQLRuntimeException, EntityNotFoundException;

    /**
     * 异步获取所有数据
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 数剧记录
     * @throws SQLRuntimeException 数据库异常
     */
    CompletableFuture<RecordList<T, K>> nativeQueryListAsync(String sql, @Nullable Collection<?> parameters)
        throws SQLRuntimeException;

    /**
     * 异步执行语句
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 受影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    CompletableFuture<Integer> nativeExecuteAsync(String sql, @Nullable Collection<?> parameters)
        throws SQLRuntimeException;

    /**
     * 异步执行语句
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 自增长主键列表
     * @throws SQLRuntimeException 数据库异常
     */
    CompletableFuture<List<K>> nativeExecuteGetIdsAsync(String sql, @Nullable Collection<?> parameters)
        throws SQLRuntimeException;

    /**
     * 异步执行语句
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 自增长主键|null
     * @throws SQLRuntimeException 数据库异常
     */
    CompletableFuture<K> nativeExecuteGetIdAsync(String sql, @Nullable Collection<?> parameters)
        throws SQLRuntimeException;
}
