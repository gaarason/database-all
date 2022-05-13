package gaarason.database.contract.model;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.lang.Nullable;

import java.io.Serializable;
import java.util.Collection;

/**
 * 事件
 * 查询 : retrieved
 * 新增 : saving -> creating -> created -> saved
 * 更新 : saving -> updating -> updated -> saved
 * 删除 : deleting -> deleted
 * 恢复 : restoring -> restored
 * @param <T> 实体类
 * @param <K> 主键类型
 * @author xt
 */
public interface Event<T extends Serializable, K extends Serializable> {

    /**
     * sql日志记录
     * @param sql 带占位符的sql
     * @param parameterList 参数
     */
    default void log(String sql, Collection<?> parameterList) {
//        log.debug("SQL with placeholder : {}", sql);
//        log.debug("SQL parameterList    : {}", parameterList);
//        String format = String.format(sql.replace(" ? ", "\"%s\""), parameterList.toArray());
//        log.debug("SQL complete         : {}", format);
    }

    /**
     * 事件会在从数据库中获取已存在模型时触发
     * @param tkRecord 结果集
     */
    default void retrieved(Record<T, K> tkRecord) {

    }

    /**
     * 事件会当一个新模型被首次保存的时候触发
     * @param tkRecord 结果集
     * @return 继续操作
     */
    default boolean creating(Record<T, K> tkRecord) {
        return true;
    }

    /**
     * 事件会当一个新模型被首次保存后触发
     * @param tkRecord 结果集
     */
    default void created(Record<T, K> tkRecord) {

    }

    /**
     * 一个模型已经在数据库中存在并调用save
     * @param tkRecord 结果集
     * @return 继续操作
     */
    default boolean updating(Record<T, K> tkRecord) {
        return true;
    }

    /**
     * 一个模型已经在数据库中存在并调用save
     * @param tkRecord 结果集
     */
    default void updated(Record<T, K> tkRecord) {

    }

    /**
     * 无论是创建还是更新
     * @param tkRecord 结果集
     * @return 继续操作
     */
    default boolean saving(Record<T, K> tkRecord) {
        return true;
    }

    /**
     * 无论是创建还是更新
     * @param tkRecord 结果集
     */
    default void saved(Record<T, K> tkRecord) {

    }

    /**
     * 删除时
     * @param tkRecord 结果集
     * @return 继续操作
     */
    default boolean deleting(Record<T, K> tkRecord) {
        return true;
    }

    /**
     * 删除后
     * @param tkRecord 结果集
     */
    default void deleted(Record<T, K> tkRecord) {

    }

    /**
     * 恢复时
     * @param tkRecord 结果集
     * @return 继续操作
     */
    default boolean restoring(Record<T, K> tkRecord) {
        return true;
    }

    /**
     * 恢复后
     * @param tkRecord 结果集
     */
    default void restored(Record<T, K> tkRecord) {

    }
}
