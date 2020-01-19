package gaarason.database.contracts.eloquent;

import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.query.Builder;

public interface Repository<T> extends Eventing<T>, SoftDeleting<T> {
    /**
     * 新的查询构造器
     * @return 查询构造器
     */
    Builder<T> newQuery();

    /**
     * 包含软删除模型
     * @return 查询构造器
     */
    Builder<T> withTrashed();

    /**
     * 只获取软删除模型
     * @return 查询构造器
     */
    Builder<T> onlyTrashed();

    /**
     * 新的记录对象
     * @return 记录对象
     */
    Record<T> newRecord();

    /**
     * 查询全部
     * @param column
     * @return
     * @throws SQLRuntimeException
     */
    RecordList<T> all(String... column) throws SQLRuntimeException;


    /**
     * 单个查询
     * @param id
     * @return
     * @throws EntityNotFoundException
     * @throws SQLRuntimeException
     */
    Record<T> findOrFail(String id) throws EntityNotFoundException, SQLRuntimeException;

    /**
     * 单个查询
     * @param id
     * @return
     */
    @Nullable
    Record<T> find(String id);

}
