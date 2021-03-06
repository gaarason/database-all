package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.ChunkFunctionalInterface;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.appointment.SqlType;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.InsertNotSuccessException;
import gaarason.database.exception.SQLRuntimeException;

import java.util.List;

/**
 * 执行
 * @param <T>
 * @param <K>
 */
public interface Execute<T, K> {

    /**
     * 转化为数据库查询语句(不会执行)
     * @param sqlType 查询/更新
     * @return 数据库查询语句
     */
    String toSql(SqlType sqlType);

    /**
     * 单个查询
     * @param id 主键
     * @return 数剧记录|null
     */
    @Nullable
    Record<T, K> find(K id) throws SQLRuntimeException;

    /**
     * 单个查询
     * @param id 主键
     * @return 结果集
     * @throws EntityNotFoundException 未找到对象
     * @throws SQLRuntimeException     数据库异常
     */
    Record<T, K> findOrFail(K id) throws EntityNotFoundException, SQLRuntimeException;

    /**
     * 获取第一条数据, 数据为空时返回null
     * @return 数剧记录|null
     * @throws SQLRuntimeException 数据库异常
     */
    @Nullable
    Record<T, K> first() throws SQLRuntimeException;

    /**
     * 获取第一条数据, 数据为空时抛出异常
     * @return 数剧记录
     * @throws EntityNotFoundException 没有数据
     * @throws SQLRuntimeException     数据库异常
     */
    Record<T, K> firstOrFail() throws EntityNotFoundException, SQLRuntimeException;

    /**
     * 获取所有数据
     * @return 数剧记录列表
     * @throws SQLRuntimeException 数据库异常
     */
    RecordList<T, K> get() throws SQLRuntimeException;

    /**
     * 分块获取所有数据, 并处理
     * @param num                      单次获取的数据量
     * @param chunkFunctionalInterface 对单次获取的数据量的处理
     * @throws SQLRuntimeException 数据库异常
     */
    void dealChunk(int num, ChunkFunctionalInterface<T, K> chunkFunctionalInterface) throws SQLRuntimeException;

    /**
     * 插入数据
     * @return 受影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int insert() throws SQLRuntimeException;


    /**
     * 插入数据
     * @param entity 数据实体对象
     * @return 受影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int insert(T entity) throws SQLRuntimeException;

    /**
     * 批量插入数据
     * @param entityList 数据实体对象列表
     * @return 受影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int insert(List<T> entityList) throws SQLRuntimeException;

    /**
     * 插入数据
     * @return 数据库自增id|null
     * @throws SQLRuntimeException 数据库异常
     */
    @Nullable
    K insertGetId() throws SQLRuntimeException;

    /**
     * 插入数据(会将数据库自增id更新到entity)
     * @param entity 数据实体对象
     * @return 数据库自增id|null
     * @throws SQLRuntimeException 数据库异常
     */
    @Nullable
    K insertGetId(T entity) throws SQLRuntimeException;

    /**
     * 插入数据(会将数据库自增id更新到entity)
     * @return 数据库自增id
     * @throws SQLRuntimeException       数据库异常
     * @throws InsertNotSuccessException 新增失败
     */
    K insertGetIdOrFail() throws SQLRuntimeException, InsertNotSuccessException;

    /**
     * 插入数据(会将数据库自增id更新到entity)
     * @param entity 数据实体对象
     * @return 数据库自增id
     * @throws SQLRuntimeException       数据库异常
     * @throws InsertNotSuccessException 新增失败
     */
    K insertGetIdOrFail(T entity) throws SQLRuntimeException, InsertNotSuccessException;

    /**
     * 批量插入数据
     * @return 数据库自增id列表
     * @throws SQLRuntimeException 数据库异常
     */
    List<K> insertGetIds() throws SQLRuntimeException;

    /**
     * 批量插入数据
     * @param entityList 数据实体对象列表
     * @return 数据库自增id列表
     * @throws SQLRuntimeException 数据库异常
     */
    List<K> insertGetIds(List<T> entityList) throws SQLRuntimeException;

    /**
     * 更新数据
     * @return 受影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int update() throws SQLRuntimeException;

    /**
     * 更新数据
     * @param entity 数据实体对象
     * @return 受影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int update(T entity) throws SQLRuntimeException;

    /**
     * 删除数据(根据模型确定是否使用软删除)
     * @return 受影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int delete() throws SQLRuntimeException;

    /**
     * 恢复软删除
     * @return 受影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int restore() throws SQLRuntimeException;

    /**
     * 删除数据(硬删除)
     * @return 受影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int forceDelete() throws SQLRuntimeException;
}
