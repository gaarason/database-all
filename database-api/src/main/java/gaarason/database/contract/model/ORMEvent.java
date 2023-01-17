package gaarason.database.contract.model;

import gaarason.database.contract.eloquent.Record;

/**
 * 事件
 * 查询 : retrieving -> retrieved
 * 新增 : saving -> creating -> created -> saved
 * 更新 : saving -> updating -> updated -> saved
 * 删除 : deleting -> deleted
 * 恢复 : restoring -> restored
 * <p>
 * 其中以 record 为参数的事件, 仅在使用ORM风格时, 会触发
 * 其中以 builder 为参数的事件, 除了使用原始SQL方式时不会触发, ORM风格以及newQuery风格, 都会触发
 * 当同时声明了 record 与 builder 为参数的相同事件时, 先触发 record
 * log 总是在执行前触发
 * @param <T> 实体类
 * @param <K> 主键类型
 * @author xt
 */
public interface ORMEvent<T, K> {


    /**
     * 事件会当一个新模型被首次保存的时候触发
     * 仅ORM场景有效
     * @param record 结果集
     * @return 继续操作
     */
    default boolean creating(Record<T, K> record) {
        return true;
    }

    /**
     * 事件会当一个新模型被首次保存后触发
     * 仅ORM场景有效
     * @param record 结果集
     */
    default void created(Record<T, K> record) {

    }

    /**
     * 一个模型已经在数据库中存在并调用save
     * 仅ORM场景有效
     * @param record 结果集
     * @return 继续操作
     */
    default boolean updating(Record<T, K> record) {
        return true;
    }

    /**
     * 一个模型已经在数据库中存在并调用save
     * 仅ORM场景有效
     * @param record 结果集
     */
    default void updated(Record<T, K> record) {

    }

    /**
     * 无论是创建还是更新
     * 仅ORM场景有效
     * @param record 结果集
     * @return 继续操作
     */
    default boolean saving(Record<T, K> record) {
        return true;
    }

    /**
     * 无论是创建还是更新
     * 仅ORM场景有效
     * @param record 结果集
     */
    default void saved(Record<T, K> record) {

    }

    /**
     * 删除时
     * 仅ORM场景有效
     * @param record 结果集
     * @return 继续操作
     */
    default boolean deleting(Record<T, K> record) {
        return true;
    }

    /**
     * 删除后
     * 仅ORM场景有效
     * @param record 结果集
     */
    default void deleted(Record<T, K> record) {

    }

    /**
     * 恢复时
     * 仅ORM场景有效
     * @param record 结果集
     * @return 继续操作
     */
    default boolean restoring(Record<T, K> record) {
        return true;
    }

    /**
     * 恢复后
     * 仅ORM场景有效
     * @param record 结果集
     */
    default void restored(Record<T, K> record) {

    }
}
