package gaarason.database.contract.model;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.lang.Nullable;

import java.util.Collection;

/**
 * model接口
 * @param <T> 实体类
 * @param <K> 主键类型
 * @author xt
 */
public interface Query<T, K> {

    /**
     * 新的查询构造器
     * @return 查询构造器
     */
    Builder<T, K> newQuery();

    /**
     * 包含软删除模型
     * @return 查询构造器
     */
    Builder<T, K> withTrashed();

    /**
     * 只获取软删除模型
     * @return 查询构造器
     */
    Builder<T, K> onlyTrashed();

    /**
     * 新的记录对象
     * @return 记录对象
     */
    Record<T, K> newRecord();

    /**
     * 查询全部
     * @param column 返回列
     * @return 批量结果集
     * @throws SQLRuntimeException SQL异常
     */
    RecordList<T, K> findAll(String... column) throws SQLRuntimeException;

    /**
     * 查询主键列表中的全部
     * @param ids 主键集合
     * @return 批量结果集
     * @throws SQLRuntimeException SQL异常
     */
    RecordList<T, K> findMany(Collection<Object> ids) throws SQLRuntimeException;

    /**
     * 查询主键列表中的全部
     * @param ids 主键集合
     * @return 批量结果集
     * @throws SQLRuntimeException SQL异常
     */
    RecordList<T, K> findMany(Object... ids) throws SQLRuntimeException;

    /**
     * 单个查询
     * @param id 主键
     * @return 结果集
     * @throws EntityNotFoundException 未找到对象
     * @throws SQLRuntimeException 数据库异常
     */
    Record<T, K> findOrFail(@Nullable Object id) throws EntityNotFoundException, SQLRuntimeException;

    /**
     * 单个查询
     * @param id 主键
     * @return 结果集
     */
    @Nullable
    Record<T, K> find(@Nullable Object id);

    /**
     * 单个查询, 当查询不到数据时 构建结果集
     * 实例并没有持久化到数据库中，你还需要调用 save 方法手动持久化
     * @param entity 实体对象
     * @return 结果集
     */
    Record<T, K> findOrNew(T entity);

    /**
     * 使用主键进行单个查询, 当查询不到数据时 构建结果集
     * 实例并没有持久化到数据库中，你还需要调用 save 方法手动持久化
     * @param entity 实体对象
     * @return 结果集
     */
    Record<T, K> findByPrimaryKeyOrNew(T entity);

    /**
     * 单个查询, 当查询不到数据时使用ORM insert本条数据
     * @param entity 实体对象
     * @return 结果集
     */
    Record<T, K> findOrCreate(T entity);

    /**
     * 使用主键进行单个查询, 当查询不到数据时使用ORM insert本条数据
     * @param entity 实体对象
     * @return 结果集
     */
    Record<T, K> findByPrimaryKeyOrCreate(T entity);

    /**
     * 单个查询, 当查询不到数据时 构建结果集
     * 实例并没有持久化到数据库中，你还需要调用 save 方法手动持久化
     * @param conditionEntity 实体对象(用作查询条件)
     * @param complementEntity 实体对象(用作插入时的补充)
     * @return 结果集
     */
    Record<T, K> findOrNew(T conditionEntity, T complementEntity);

    /**
     * 单个查询, 当查询不到数据时使用ORM insert本条数据
     * @param conditionEntity 实体对象(用作查询条件)
     * @param complementEntity 实体对象(用作插入时的补充)
     * @return 结果集
     */
    Record<T, K> findOrCreate(T conditionEntity, T complementEntity);

    /**
     * ORM insert 本条数据
     * @param entity 实体对象
     * @return 结果集
     */
    Record<T, K> create(T entity);

    /**
     * ORM update 本条数据
     * @param entity 实体对象
     * @return 结果集
     */
    Record<T, K> update(T entity);

    /**
     * 使用主键进行单个更新, 当查询不到数据时使用ORM insert本条数据
     * (已存在则更新，否则创建新模型)
     * @param entity 实体对象
     * @return 结果集
     */
    Record<T, K> updateByPrimaryKeyOrCreate(T entity);

    /**
     * 单个更新, 当查询不到数据时使用ORM insert本条数据
     * (已存在则更新，否则创建新模型)
     * @param conditionEntity 实体对象(用作查询条件)
     * @param complementEntity 实体对象(用作插入时的补充)
     * @return 结果集
     */
    Record<T, K> updateOrCreate(T conditionEntity, T complementEntity);

}
