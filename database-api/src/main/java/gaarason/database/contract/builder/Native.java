package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.lang.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * 原生查询
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Native<T, K> {

    /**
     * 获取第一条数据, 数据为空时抛出异常
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 数剧记录
     * @throws SQLRuntimeException 数据库异常
     * @throws EntityNotFoundException 没有数据
     */
    Record<T, K> queryOrFail(String sql, @Nullable Collection<?> parameters)
        throws SQLRuntimeException, EntityNotFoundException;

    /**
     * 获取第一条数据, 数据为空时抛出异常
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 数剧记录
     * @throws SQLRuntimeException 数据库异常
     * @throws EntityNotFoundException 没有数据
     */
    Record<T, K> queryOrFail(String sql, Object... parameters) throws SQLRuntimeException, EntityNotFoundException;

    /**
     * 获取第一条数据, 数据为空时返回null
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 数剧记录
     * @throws SQLRuntimeException 数据库异常
     */
    @Nullable
    Record<T, K> query(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException;

    /**
     * 获取第一条数据, 数据为空时返回null
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 数剧记录
     * @throws SQLRuntimeException 数据库异常
     */
    @Nullable
    Record<T, K> query(String sql, Object... parameters) throws SQLRuntimeException;

    /**
     * 获取所有数据
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 数剧记录
     * @throws SQLRuntimeException 数据库异常
     */
    RecordList<T, K> queryList(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException;

    /**
     * 获取所有数据
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 数剧记录
     * @throws SQLRuntimeException 数据库异常
     */
    RecordList<T, K> queryList(String sql, Object... parameters) throws SQLRuntimeException;

    /**
     * 执行语句
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 受影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int execute(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException;

    /**
     * 执行语句
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 受影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int execute(String sql, Object... parameters) throws SQLRuntimeException;

    /**
     * 执行语句
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 自增长主键|null
     * @throws SQLRuntimeException 数据库异常
     */
    @Nullable
    K executeGetId(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException;

    /**
     * 执行语句
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 自增长主键|null
     * @throws SQLRuntimeException 数据库异常
     */
    @Nullable
    K executeGetId(String sql, Object... parameters) throws SQLRuntimeException;

    /**
     * 执行语句
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 自增长主键列表
     * @throws SQLRuntimeException 数据库异常
     */
    List<K> executeGetIds(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException;

    /**
     * 执行语句
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 自增长主键列表
     * @throws SQLRuntimeException 数据库异常
     */
    List<K> executeGetIds(String sql, Object... parameters) throws SQLRuntimeException;

}
