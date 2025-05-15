package gaarason.database.contract.record;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.exception.EntityAttributeInvalidException;
import gaarason.database.lang.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * ORM 操作
 * @author xt
 */
public interface Operation<T, K> extends Support<T, K> {

    /**
     * 新增或者更新
     * 新增情况下: saving -> creating -> created -> saved
     * 更新情况下: saving -> updating -> updated -> saved
     * 注 1 : 新增或者更新, 以当前是否已经存在绑定的数据为依据,
     * 并非以当前的 entity 中是否存在主键,
     * 因为这样的话, 就无法实现更新主键的功能了.
     * 如果是 希望以 entity 中是否存在主键在进行更新或者新增, 那么可以使用 saveByPrimaryKey()
     * @return 执行成功
     */
    boolean save();

    /**
     * 无事件的调研save
     * @return 执行成功
     */
    default boolean saveQuietly() {
        return getModel().newQueryWithoutApply().quiet(this::save);
    }

    /**
     * 新增或者更新
     * 新增情况下: saving -> creating -> created -> saved
     * 更新情况下: saving -> updating -> updated -> saved
     * 注 1 : 新增或者更新, 以当前的 entity 中是否存在主键
     * @return 执行成功
     */
    boolean saveByPrimaryKey();

    /**
     * 无事件的调研saveByPrimaryKey
     * @return 执行成功
     */
    default boolean saveByPrimaryKeyQuietly() {
        return getModel().newQueryWithoutApply().quiet(this::saveByPrimaryKey);
    }

    /**
     * 删除 (根据model情况, 进行软删除或者硬删除)
     * deleting -> deleted
     * @return 执行成功
     */
    boolean delete();

    /**
     * 无事件的调研delete
     * @return 执行成功
     */
    default boolean deleteQuietly() {
        return getModel().newQueryWithoutApply().quiet(this::delete);
    }

    /**
     * 硬删除
     * forceDeleting -> forceDeleted
     * @return 执行成功
     */
    boolean forceDelete();

    /**
     * 无事件的调研forceDelete
     * @return 执行成功
     */
    default boolean forceDeleteQuietly() {
        return getModel().newQueryWithoutApply().quiet(this::forceDelete);
    }

    /**
     * 恢复(成功恢复后将会刷新record)
     * restoring -> restored
     * @return 执行成功
     */
    boolean restore();

    /**
     * 无事件的调研restore
     * @return 执行成功
     */
    default boolean restoreQuietly() {
        return getModel().newQueryWithoutApply().quiet(() -> restore());
    }

    /**
     * 恢复
     * restoring -> restored
     * @param refresh 是否刷新自身
     * @return 执行成功
     */
    boolean restore(boolean refresh);

    /**
     * 无事件的调研restore
     * @return 执行成功
     */
    default boolean restoreQuietly(boolean refresh) {
        return getModel().newQueryWithoutApply().quiet(() -> restore(refresh));
    }

    /**
     * 刷新(重新从数据库查询获取)
     * 现有的模型实例不会受到影响
     * retrieved
     * @return 全新的Record
     */
    Record<T, K> fresh();

    /**
     * 刷新(重新从数据库查询获取)
     * retrieved
     * @return 原Record (刷新后)
     */
    Record<T, K> refresh();

    /**
     * 刷新(重新从指定数据获取)
     * retrieved
     * @param metadataMap 使用的元数据
     * @return 执行成功
     */
    Record<T, K> refresh(Map<String, Object> metadataMap);

    /**
     * 是否有任何属性发生改变, 且未提交到数据库
     * @return bool
     */
    boolean isDirty();

    /**
     * 获取所有变更属性(未提交到数据库)组成的map
     * 不会包含关联关系检测
     * @return map<列名, 值>
     */
    Map<String, Object> getDirtyMap();

    /**
     * 指定属性是否有发生改变, 且未提交到数据库
     * @param fieldNames 实体属性名
     * @return bool
     */
    boolean isDirty(String... fieldNames);

    /**
     * 指定属性是否有发生改变, 且未提交到数据库
     * @param fieldNames 实体属性名
     * @return bool
     */
    boolean isDirty(Collection<String> fieldNames);

    /**
     * 是否没有任何属性发生改变(全部已提交到数据库)
     * @return bool
     */
    boolean isClean();

    /**
     * 指定属性是否没有发生改变(全部已提交到数据库)
     * @param fieldNames 实体属性名
     * @return bool
     */
    boolean isClean(String... fieldNames);

    /**
     * 指定属性是否没有发生改变(全部已提交到数据库)
     * @param fieldNames 实体属性名
     * @return bool
     */
    boolean isClean(Collection<String> fieldNames);

    /**
     * 初始以来, 是否存在已提交到数据库的变化
     * 比较 getOriginal 与 当前已提交数据
     * @return bool
     */
    boolean wasChanged();

    /**
     * 初始以来, 指定的数据是否存在已提交到数据库的变化
     * 比较 getOriginal(field) 与 当前属性的已提交数据
     * @param fieldNames 实体属性名
     * @return bool
     */
    boolean wasChanged(String... fieldNames);

    /**
     * 初始以来, 指定的数据是否存在已提交到数据库的变化
     * 比较 getOriginal(field) 与 当前属性的已提交数据
     * @param fieldNames 实体属性名
     * @return bool
     */
    boolean wasChanged(Collection<String> fieldNames);

    /**
     * 返回一个包含模型初始化属性的实体
     * 不管从检索起模型是否发生了任何变化, 它都不变, 除非调用手动刷新
     * @return 实体对象
     */
    T getOriginal();

    /**
     * 返回模型始化属性属性的值
     * 不管从检索起模型是否发生了任何变化, 它都不变, 除非调用手动刷新
     * @param fieldName 实体属性名
     * @return 实体对象
     * @throws EntityAttributeInvalidException 无效的属性(实体声明中没有的属性)
     */
    @Nullable
    Object getOriginal(String fieldName) throws EntityAttributeInvalidException;

}
