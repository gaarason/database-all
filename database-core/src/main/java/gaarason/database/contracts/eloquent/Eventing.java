package gaarason.database.contracts.eloquent;

import gaarason.database.eloquent.Record;

import java.util.Collection;

/**
 * 事件
 * @param <T> 实体类
 * @param <K> 主键类型
 */
public interface Eventing<T, K> {

    /**
     * sql日志记录
     * @param sql           带占位符的sql
     * @param parameterList 参数
     */
    default void log(String sql, Collection<String> parameterList) {
//        log.debug("SQL with placeholder : {}", sql);
//        log.debug("SQL parameterList    : {}", parameterList);
//        String format = String.format(sql.replace(" ? ", "\"%s\""), parameterList.toArray());
//        log.debug("SQL complete         : {}", format);
    }

    /**
     * 事件会在从数据库中获取已存在模型时触发
     */
    default void retrieved(Record<T, K> record) {

    }

    /**
     * 事件会当一个新模型被首次保存的时候触发
     * @return 继续操作
     */
    default boolean creating(Record<T, K> record) {
        return true;
    }

    /**
     * 事件会当一个新模型被首次保存后触发
     */
    default void created(Record<T, K> record) {

    }

    /**
     * 一个模型已经在数据库中存在并调用save
     * @return 继续操作
     */
    default boolean updating(Record<T, K> record) {
        return true;
    }

    /**
     * 一个模型已经在数据库中存在并调用save
     */
    default void updated(Record<T, K> record) {

    }

    /**
     * 无论是创建还是更新
     * @return 继续操作
     */
    default boolean saving(Record<T, K> record) {
        return true;
    }

    /**
     * 无论是创建还是更新
     */
    default void saved(Record<T, K> record) {

    }

    /**
     * 删除时
     * @return 继续操作
     */
    default boolean deleting(Record<T, K> record) {
        return true;
    }

    /**
     * 删除后
     */
    default void deleted(Record<T, K> record) {

    }

    /**
     * 恢复时
     * @return 继续操作
     */
    default boolean restoring(Record<T, K> record) {
        return true;
    }

    /**
     * 恢复后
     */
    default void restored(Record<T, K> record) {

    }
}
