package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.ChunkFunctionalInterface;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.InsertNotSuccessException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.lang.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * 执行
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Execute<B extends Builder<B, T, K>, T, K> {

    /**
     * 单个查询
     * @param id 主键
     * @return 数剧记录|null
     * @throws SQLRuntimeException 数据库异常
     */
    @Nullable
    Record<T, K> find(@Nullable Object id) throws SQLRuntimeException;

    /**
     * 单个查询
     * @param id 主键
     * @return 结果集
     * @throws EntityNotFoundException 未找到对象
     * @throws SQLRuntimeException 数据库异常
     */
    Record<T, K> findOrFail(@Nullable Object id) throws EntityNotFoundException, SQLRuntimeException;

    /**
     * 获取第一条数据, 数据为空时返回null
     * @return 数据记录|null
     * @throws SQLRuntimeException 数据库异常
     */
    @Nullable
    Record<T, K> first() throws SQLRuntimeException;

    /**
     * 获取第一条数据, 数据为空时抛出异常
     * @return 数剧记录
     * @throws EntityNotFoundException 没有数据
     * @throws SQLRuntimeException 数据库异常
     */
    Record<T, K> firstOrFail() throws EntityNotFoundException, SQLRuntimeException;

    /**
     * 获取所有数据
     * @return 数剧记录列表
     * @throws SQLRuntimeException 数据库异常
     */
    RecordList<T, K> get() throws SQLRuntimeException;

    /**
     * 分块获取所有数据(兼容性强), 并处理
     * 使用偏移分页
     * @param num 单次获取的数据量
     * @param chunkFunctionalInterface 对单次获取的数据量的处理
     * @throws SQLRuntimeException 数据库异常
     */
    void dealChunk(int num, ChunkFunctionalInterface<T, K> chunkFunctionalInterface) throws SQLRuntimeException;

    /**
     * 分块获取所有数据(数据库性能好), 并处理
     * 使用索引(游标)分页
     * @param num 单次获取的数据量
     * @param column 分页字段 (字段要求: 数据库唯一约束(索引), 排序稳定 . eg: 单调递增主键)
     * @param chunkFunctionalInterface 对单次获取的数据量的处理
     * @throws SQLRuntimeException 数据库异常
     */
    void dealChunk(int num, String column, ChunkFunctionalInterface<T, K> chunkFunctionalInterface)
        throws SQLRuntimeException;

    /**
     * 单个原子操作中创建记录 or 删除冲突行后再创建记录
     * @return 受影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int replace() throws SQLRuntimeException;

    /**
     * 单个原子操作中更新或创建记录
     * eg : newQuery().column(...).value(...).upsert(...);
     * @param columns 当数据索引冲突后, 更新的列名
     * @return 受影响的行数 (对于mysql而言, 单条新增是1, 单条有变化更新是2, 单条无变化更新是2)
     * @throws SQLRuntimeException 数据库异常
     */
    int upsert(String... columns) throws SQLRuntimeException;

    /**
     * 单个原子操作中更新或创建记录
     * eg : newQuery().column(...).value(...).upsert(...);
     * @param columns 当数据索引冲突后, 更新的列名
     * @return 受影响的行数 (对于mysql而言, 单条新增是1, 单条有变化更新是2, 单条无变化更新是2)
     * @throws SQLRuntimeException 数据库异常
     */
    int upsert(Collection<String> columns) throws SQLRuntimeException;

    /**
     * 插入数据
     * @return 受影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int insert() throws SQLRuntimeException;

    /**
     * 插入数据
     * @return 数据库自增id|null(失败)
     * @throws SQLRuntimeException 数据库异常
     */
    @Nullable
    K insertGetId() throws SQLRuntimeException;

    /**
     * 插入数据
     * @return 数据库自增id
     * @throws SQLRuntimeException 数据库异常
     * @throws InsertNotSuccessException 新增失败
     */
    K insertGetIdOrFail() throws SQLRuntimeException, InsertNotSuccessException;

    /**
     * 批量插入数据
     * @return 数据库自增id列表
     * @throws SQLRuntimeException 数据库异常
     */
    List<K> insertGetIds() throws SQLRuntimeException;

    /**
     * 更新数据
     * @return 受影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int update() throws SQLRuntimeException;

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
