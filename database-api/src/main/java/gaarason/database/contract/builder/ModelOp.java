package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;

/**
 * 模型级操作
 * 同时被 Model 的接口与 查询构造器(Builder) 的接口继承, 即两端均可调用.
 * 在查询构造器上调用时:
 * 1. 查询条件为 构造器上已有的条件 与 实体中非null属性条件 的并集;
 * 2. 新增/更新所使用的数据仅来自实体本身(不包含构造器上条件的值);
 * 3. 不修改软删除状态(不自动恢复, 也不额外增删软删除列的赋值), 但以`AutoRestore`为后缀的方法除外(其命中已软删除记录时, 会依模型自身的软删除实现自动恢复).
 * @param <T> 实体类
 * @param <K> 主键类型
 * @author xt
 */
public interface ModelOp<B extends Builder<B, T, K>, T, K> {

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

    /**
     * 使用主键进行单个更新, 当查询不到数据时使用ORM insert本条数据
     * 命中已软删除的记录时, 会依模型自身的软删除实现自动恢复(恢复与更新在一条sql中完成)
     * @param entity 实体对象
     * @return 结果集
     */
    Record<T, K> updateByPrimaryKeyOrCreateAutoRestore(T entity);

    /**
     * 单个更新, 当查询不到数据时使用ORM insert本条数据
     * 命中已软删除的记录时, 会依模型自身的软删除实现自动恢复(恢复与更新在一条sql中完成)
     * @param conditionEntity 实体对象(用作查询条件)
     * @param complementEntity 实体对象(用作插入时的补充)
     * @return 结果集
     */
    Record<T, K> updateOrCreateAutoRestore(T conditionEntity, T complementEntity);

}
