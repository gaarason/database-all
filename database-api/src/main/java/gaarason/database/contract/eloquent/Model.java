package gaarason.database.contract.eloquent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.model.*;
import gaarason.database.core.Container;
import gaarason.database.exception.PrimaryKeyNotFoundException;

/**
 * 数据模型
 * @author xt
 */
public interface Model<T, K>
    extends Query<T, K>, NativeQuery<T, K>, ORMEvent<T, K>, QueryEvent<T, K>, SoftDelete<T, K> {

    /**
     * 返回当前的模型
     * @return 模型
     */
    Model<T, K> getSelf();

    /**
     * Gaarason数据源
     * @return Gaarason数据源
     */
    GaarasonDataSource getGaarasonDataSource();

    /**
     * 主键列名(并非一定是实体的属性名)
     * @return 主键列名(并非一定是实体的属性名)
     * @throws PrimaryKeyNotFoundException 主键未知
     */
    String getPrimaryKeyColumnName() throws PrimaryKeyNotFoundException;

    /**
     * 主键类型
     * @return 主键类型
     */
    Class<K> getPrimaryKeyClass();

    /**
     * 数据库表名
     * @return 数据库表名
     */
    String getTableName();

    /**
     * 实体类型
     * @return 实体类型
     */
    Class<T> getEntityClass();

    /**
     * 获取容器
     * @return 容器
     */
    Container getContainer();

}