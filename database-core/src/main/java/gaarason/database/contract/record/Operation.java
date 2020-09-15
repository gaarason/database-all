package gaarason.database.contract.record;

import gaarason.database.eloquent.Record;

public interface Operation<T, K> {
    /**
     * 新增或者更新
     * 新增情况下: saving -> creating -> created -> saved
     * 更新情况下: saving -> updating -> updated -> saved
     * @return 执行成功
     */
    boolean save();

    /**
     * 删除
     * deleting -> deleted
     * @return 执行成功
     */
    boolean delete();

    /**
     * 恢复(成功恢复后将会刷新record)
     * restoring -> restored
     * @return 执行成功
     */
    boolean restore();

    /**
     * 恢复
     * restoring -> restored
     * @param refresh 是否刷新自身
     * @return 执行成功
     */
    boolean restore(boolean refresh);

    /**
     * 刷新(重新从数据库获取)
     * retrieved
     * @return 执行成功
     */
    Record<T, K> refresh() ;
}
