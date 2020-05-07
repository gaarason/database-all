package gaarason.database.contracts.eloquent;

import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.query.Builder;

public interface Repository<T, K> extends Eventing<T, K>, SoftDeleting<T, K> {
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
     * @param column
     * @return
     * @throws SQLRuntimeException
     */
    RecordList<T, K> all(String... column) throws SQLRuntimeException;


    /**
     * 单个查询
     * @param id
     * @return
     * @throws EntityNotFoundException
     * @throws SQLRuntimeException
     */
    Record<T, K> findOrFail(String id) throws EntityNotFoundException, SQLRuntimeException;

    /**
     * 单个查询
     * @param id
     * @return
     */
    @Nullable
    Record<T, K> find(String id);

}
