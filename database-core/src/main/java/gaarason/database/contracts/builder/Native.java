package gaarason.database.contracts.builder;

import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.SQLRuntimeException;

import java.util.Collection;

/**
 * 原生查询
 * @param <T>
 */
public interface Native<T> {

    /**
     * 获取第一条数据, 数据为空时返回null
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 数剧记录
     * @throws SQLRuntimeException 数据库异常
     * @throws EntityNotFoundException 没有数据
     */
    Record<T> queryOrFail(String sql, Collection<String> parameters)
        throws SQLRuntimeException, EntityNotFoundException;

    /**
     * 获取第一条数据, 数据为空时抛出异常
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 数剧记录
     * @throws SQLRuntimeException 数据库异常
     */
    @Nullable
    Record<T> query(String sql, Collection<String> parameters) throws SQLRuntimeException;

    /**
     * 获取所有数据
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 数剧记录
     * @throws SQLRuntimeException 数据库异常
     */
    RecordList<T> queryList(String sql, Collection<String> parameters) throws SQLRuntimeException;

    /**
     * 执行语句
     * @param sql 查询语句
     * @param parameters 参数绑定列表
     * @return 受影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int execute(String sql, Collection<String> parameters) throws SQLRuntimeException;

}
