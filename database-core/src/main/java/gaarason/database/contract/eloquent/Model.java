package gaarason.database.contract.eloquent;

import gaarason.database.contract.model.Event;
import gaarason.database.contract.model.Query;
import gaarason.database.contract.model.SoftDelete;

public interface Model<T, K> extends Query<T, K>, Event<T, K>, SoftDelete<T, K> {

    /**
     * 主键列名(并非一定是实体的属性名)
     * @return 主键列名(并非一定是实体的属性名)
     */
    String getPrimaryKeyColumnName();

    /**
     * 主键名(实体的属性名)
     * @return 主键名(实体的属性名)
     */
    String getPrimaryKeyName();

    /**
     * 主键自增
     * @return 主键自增
     */
    boolean isPrimaryKeyIncrement();

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
}