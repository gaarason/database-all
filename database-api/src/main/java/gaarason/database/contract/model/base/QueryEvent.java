package gaarason.database.contract.model.base;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.lang.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * 事件
 * 除了使用原始SQL方式时不会触发, ORM风格以及newQuery风格, 都会触发
 * 查询 : retrieving -> retrieved
 * 新增 : creating -> created
 * 更新 : updating -> updated
 * 删除 : deleting -> deleted
 * 恢复 : restoring -> restored
 * <p>
 * 其中以 eventRecord 为方法名前缀的事件, 仅在使用ORM风格时, 会触发
 * 其中以 eventQuery 为方法名前缀的事件, 除了使用原始SQL方式时不会触发, ORM风格以及newQuery风格, 都会触发
 * log 总是在执行前触发
 * @param <T> 实体类
 * @param <K> 主键类型
 * @see RecordEvent
 * @author xt
 */
public interface QueryEvent<B extends Builder<B, T, K>, T, K> {

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
     * 查询数据时
     * @param builder 查询构造器
     */
    default void eventQueryRetrieving(Builder<B, T, K> builder) {
    }

    /**
     * 查询数据后
     * @param builder 查询构造器
     * @param record 结果集
     */
    default void eventQueryRetrieved(Builder<B, T, K> builder, Record<T, K> record) {

    }

    /**
     * 查询数据后
     * @param builder 查询构造器
     * @param records 结果集合
     */
    default void eventQueryRetrieved(Builder<B, T, K> builder, RecordList<T, K> records) {

    }

    /**
     * 插入数据时
     * @param builder 查询构造器.
     */
    default void eventQueryCreating(Builder<B, T, K> builder) {
    }

    /**
     * 插入数据后
     * 3个created()事件, 相互互补, 不会重复; 具体触发哪一个, 取决于调用的执行方法(所预期的返回值)
     * @param builder 查询构造器
     * @param rows 受影响的行数
     */
    default void eventQueryCreated(Builder<B, T, K> builder, int rows) {

    }

    /**
     * 插入数据后
     * 3个created()事件, 相互互补, 不会重复; 具体触发哪一个, 取决于调用的执行方法(所预期的返回值)
     * @param builder 查询构造器
     * @param primaryKeyValue 主键值
     */
    default void eventQueryCreated(Builder<B, T, K> builder, @Nullable K primaryKeyValue) {

    }

    /**
     * 批量插入数据后
     * 3个created()事件, 相互互补, 不会重复; 具体触发哪一个, 取决于调用的执行方法(所预期的返回值)
     * @param builder 查询构造器
     * @param primaryKeyValues 主键值列表
     */
    default void eventQueryCreated(Builder<B, T, K> builder, List<K> primaryKeyValues) {

    }

    /**
     * 更新数据时
     * @param builder 查询构造器
     */
    default void eventQueryUpdating(Builder<B, T, K> builder) {
    }

    /**
     * 更新数据后
     * @param builder 查询构造器
     * @param rows 受影响的行数
     */
    default void eventQueryUpdated(Builder<B, T, K> builder, int rows) {

    }

    /**
     * 删除数据时
     * @param builder 查询构造器
     */
    default void eventQueryDeleting(Builder<B, T, K> builder) {

    }

    /**
     * 删除数据后
     * @param builder 查询构造器
     * @param rows 受影响的行数
     */
    default void eventQueryDeleted(Builder<B, T, K> builder, int rows) {

    }

    /**
     * 硬删除数据时
     * @param builder 查询构造器
     */
    default void eventQueryForceDeleting(Builder<B, T, K> builder) {

    }

    /**
     * 硬删除数据后
     * @param builder 查询构造器
     * @param rows 受影响的行数
     */
    default void eventQueryForceDeleted(Builder<B, T, K> builder, int rows) {

    }

    /**
     * 恢复数据时
     * @param builder 查询构造器
     */
    default void eventQueryRestoring(Builder<B, T, K> builder) {
    }

    /**
     * 恢复数据后
     * @param builder 查询构造器
     * @param rows 受影响的行数
     */
    default void eventQueryRestored(Builder<B, T, K> builder, int rows) {

    }
}
