package gaarason.database.contract.eloquent;

import gaarason.database.contract.model.Event;
import gaarason.database.contract.model.Query;
import gaarason.database.contract.model.SoftDelete;
import gaarason.database.contract.support.IdGenerator;
import gaarason.database.exception.PrimaryKeyNotFoundException;
import gaarason.database.provider.FieldInfo;

import java.io.Serializable;

/**
 * 数据模型
 * @author xt
 */
public interface Model<T extends Serializable, K extends Serializable> extends Query<T, K>, Event<T, K>, SoftDelete<T, K> {

    /**
     * 主键是否存在定义 (约等于数据表中主键是否可以存在)
     * @return bool
     */
    boolean isPrimaryKeyDefinition();

    /**
     * 主键列名(并非一定是实体的属性名)
     * @return 主键列名(并非一定是实体的属性名)
     * @throws PrimaryKeyNotFoundException 主键未知
     */
    String getPrimaryKeyColumnName() throws PrimaryKeyNotFoundException;

    /**
     * 主键名(实体的属性名)
     * @return 主键名(实体的属性名)
     * @throws PrimaryKeyNotFoundException 主键未知
     */
    String getPrimaryKeyName() throws PrimaryKeyNotFoundException;

    /**
     * 主键自增
     * @return 主键自增
     * @throws PrimaryKeyNotFoundException 主键未知
     */
    boolean isPrimaryKeyIncrement() throws PrimaryKeyNotFoundException;

    /**
     * 主键字段信息
     * @return 主键字段信息
     * @throws PrimaryKeyNotFoundException 主键未知
     */
    FieldInfo getPrimaryKeyFieldInfo() throws PrimaryKeyNotFoundException;

    /**
     * 主键生成器
     * @return 主键生成器
     * @throws PrimaryKeyNotFoundException 主键未知
     */
    IdGenerator<K> getPrimaryKeyIdGenerator() throws PrimaryKeyNotFoundException;

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