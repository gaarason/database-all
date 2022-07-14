package gaarason.database.contract.eloquent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.model.Event;
import gaarason.database.contract.model.Query;
import gaarason.database.contract.model.SoftDelete;
import gaarason.database.contract.support.IdGenerator;
import gaarason.database.core.Container;
import gaarason.database.exception.PrimaryKeyNotFoundException;
import gaarason.database.provider.FieldInfo;
import gaarason.database.provider.ModelInfo;

import java.io.Serializable;

/**
 * 数据模型
 * @author xt
 */
public interface Model<T extends Serializable, K extends Serializable> extends Query<T, K>, Event<T, K>, SoftDelete<T, K> {

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