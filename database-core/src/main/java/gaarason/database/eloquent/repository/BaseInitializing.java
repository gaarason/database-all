package gaarason.database.eloquent.repository;

import gaarason.database.contract.eloquent.Repository;
import gaarason.database.eloquent.annotations.Primary;
import gaarason.database.exception.InvalidPrimaryKeyTypeException;
import gaarason.database.util.EntityUtil;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

abstract class BaseInitializing<T, K> implements Repository<T, K> {

    /**
     * 主键列名(并非一定是实体的属性名)
     */
    @Getter
    protected String primaryKeyColumnName;

    /**
     * 主键名(实体的属性名)
     */
    @Getter
    protected String primaryKeyName;

    /**
     * 主键自增
     */
    @Getter
    protected boolean primaryKeyIncrement;

    /**
     * 主键类型
     */
    @Getter
    protected Class<K> primaryKeyClass;

    /**
     * 数据库表名
     */
    @Getter
    protected String tableName;

    /**
     * 实体类型
     */
    @Getter
    protected Class<T> entityClass;

    BaseInitializing() {
        // 泛型初始化
        genericInitialization();
        // entity分析
        analysisEntityClass();
    }

    /**
     * 泛型初始化
     */
    @SuppressWarnings("unchecked")
    private void genericInitialization() {
        entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        primaryKeyClass =
            (Class<K>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    }

    /**
     * 实体类型分析
     */
    private void analysisEntityClass() {
        tableName = EntityUtil.tableName(entityClass);
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Primary.class)) {
                Primary primary = field.getAnnotation(Primary.class);
                primaryKeyIncrement = primary.increment();
                primaryKeyColumnName = EntityUtil.columnName(field);
                primaryKeyName = field.getName();
                // 主键类型检测
                if (!primaryKeyClass.equals(field.getType())) {
                    throw new InvalidPrimaryKeyTypeException(
                        "The primary key type [" + field.getType() + "] of the entity does not match with the " +
                            "generic [" + primaryKeyClass + "]");
                }
                break;
            }
        }
    }
}
