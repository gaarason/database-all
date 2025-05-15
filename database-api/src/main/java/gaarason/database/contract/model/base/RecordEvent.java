package gaarason.database.contract.model.base;

import gaarason.database.contract.eloquent.Record;

/**
 * 事件
 * 仅在使用ORM风格时, 会触发
 * 新增 : saving -> creating -> created -> saved
 * 更新 : saving -> updating -> updated -> saved
 * 删除 : deleting -> deleted
 * 恢复 : restoring -> restored
 * <p>
 * 其中以 eventRecord 为方法名前缀的事件, 仅在使用ORM风格时, 会触发
 * 其中以 eventQuery 为方法名前缀的事件, 除了使用原始SQL方式时不会触发, ORM风格以及newQuery风格, 都会触发
 * log 总是在执行前触发
 * @param <T> 实体类
 * @param <K> 主键类型
 * @see QueryEvent
 * @author xt
 */
public interface RecordEvent<T, K> {


    /**
     * 查询数据后
     * @param record 结果集
     */
    default void eventRecordRetrieved(Record<T, K> record) {

    }

    /**
     * 事件会当一个新模型被首次保存的时候触发
     * @param record 结果集
     * @return 继续操作
     */
    default boolean eventRecordCreating(Record<T, K> record) {
        return true;
    }

    /**
     * 事件会当一个新模型被首次保存后触发
     * @param record 结果集
     */
    default void eventRecordCreated(Record<T, K> record) {

    }

    /**
     * 一个模型已经在数据库中存在并调用save
     * @param record 结果集
     * @return 继续操作
     */
    default boolean eventRecordUpdating(Record<T, K> record) {
        return true;
    }

    /**
     * 一个模型已经在数据库中存在并调用save
     * @param record 结果集
     */
    default void eventRecordUpdated(Record<T, K> record) {

    }

    /**
     * 无论是创建还是更新
     * @param record 结果集
     * @return 继续操作
     */
    default boolean eventRecordSaving(Record<T, K> record) {
        return true;
    }

    /**
     * 无论是创建还是更新
     * @param record 结果集
     */
    default void eventRecordSaved(Record<T, K> record) {

    }

    /**
     * 删除时
     * @param record 结果集
     * @return 继续操作
     */
    default boolean eventRecordDeleting(Record<T, K> record) {
        return true;
    }

    /**
     * 删除后
     * @param record 结果集
     */
    default void eventRecordDeleted(Record<T, K> record) {

    }

    /**
     * 硬删除时
     * @param record 结果集
     * @return 继续操作
     */
    default boolean eventRecordForceDeleting(Record<T, K> record) {
        return true;
    }

    /**
     * 硬删除后
     * @param record 结果集
     */
    default void eventRecordForceDeleted(Record<T, K> record) {

    }

    /**
     * 恢复时
     * @param record 结果集
     * @return 继续操作
     */
    default boolean eventRecordRestoring(Record<T, K> record) {
        return true;
    }

    /**
     * 恢复后
     * @param record 结果集
     */
    default void eventRecordRestored(Record<T, K> record) {

    }
}
