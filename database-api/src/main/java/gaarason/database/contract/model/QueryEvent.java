package gaarason.database.contract.model;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;

import java.util.Collection;
import java.util.List;

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
public interface QueryEvent<T, K> {

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
    default void retrieving(Builder<T, K> builder) {
    }

    /**
     * 查询数据后
     * @param record 结果集
     */
    default void retrieved(Record<T, K> record) {

    }

    /**
     * 查询数据后
     * @param records 结果集合
     */
    default void retrieved(RecordList<T, K> records) {

    }

    /**
     * 插入数据时
     * @param builder 查询构造器.
     */
    default void creating(Builder<T, K> builder) {
    }

    /**
     * 插入数据后
     * 3个created()事件, 相互互补, 不会重复; 具体触发哪一个, 取决于调用的执行方法(所预期的返回值)
     * @param rows 受影响的行数
     */
    default void created(int rows) {

    }

    /**
     * 插入数据后
     * 3个created()事件, 相互互补, 不会重复; 具体触发哪一个, 取决于调用的执行方法(所预期的返回值)
     * @param primaryKeyValue 主键值列表
     */
    default void created(K primaryKeyValue) {

    }

    /**
     * 批量插入数据后
     * 3个created()事件, 相互互补, 不会重复; 具体触发哪一个, 取决于调用的执行方法(所预期的返回值)
     * @param primaryKeyValues 主键值列表
     */
    default void created(List<K> primaryKeyValues) {

    }

    /**
     * 更新数据时
     * @param builder 查询构造器
     */
    default void updating(Builder<T, K> builder) {
    }

    /**
     * 更新数据后
     * @param rows 受影响的行数
     */
    default void updated(int rows) {

    }

    /**
     * 删除数据时
     * @param builder 查询构造器
     */
    default void deleting(Builder<T, K> builder) {
    }

    /**
     * 删除数据后
     * @param rows 受影响的行数
     */
    default void deleted(int rows) {

    }

    /**
     * 回复数据时
     * @param builder 查询构造器
     */
    default void restoring(Builder<T, K> builder) {
    }

    /**
     * 回复数据后
     * @param rows 受影响的行数
     */
    default void restored(int rows) {

    }
}
