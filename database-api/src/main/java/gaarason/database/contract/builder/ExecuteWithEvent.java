package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.lang.Nullable;

/**
 * 执行, 并触发Model的事件
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface ExecuteWithEvent<T, K> {

    /**
     * 删除数据
     * @return 受影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int deleteWithEvent() throws SQLRuntimeException;

    /**
     * 更新数据
     * @return 受影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int updateWithEvent() throws SQLRuntimeException;

    /**
     * 插入数据
     * @return 受影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int insertWithEvent() throws SQLRuntimeException;

    /**
     * 获取第一条数据, 数据为空时返回null
     * @return 数据记录|null
     * @throws SQLRuntimeException 数据库异常
     */
    @Nullable
    Record<T, K> firstWithEvent() throws SQLRuntimeException;

    /**
     * 获取所有数据
     * @return 数剧记录列表
     * @throws SQLRuntimeException 数据库异常
     */
    RecordList<T, K> getWithEvent() throws SQLRuntimeException;
}
