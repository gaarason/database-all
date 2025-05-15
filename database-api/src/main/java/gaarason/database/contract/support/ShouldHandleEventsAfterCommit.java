package gaarason.database.contract.support;

import java.io.Serializable;

/**
 * 标记 数据库事务提交后执行其事件处理程序
 * @see gaarason.database.annotation.ObservedBy
 */
public interface ShouldHandleEventsAfterCommit extends Serializable {
}
