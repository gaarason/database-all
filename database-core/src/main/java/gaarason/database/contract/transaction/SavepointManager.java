package gaarason.database.contract.transaction;

import java.sql.Savepoint;

public interface SavepointManager {

    /**
     * 生成回滚点
     * @return 回滚点
     */
    Savepoint createSavepoint();

    /**
     * 回滚到回滚点
     * @param savepoint 回滚点
     */
    void rollbackToSavepoint();

    /**
     * 移除回滚点
     * @param savepoint 回滚点
     */
    void releaseSavepoint();
}
