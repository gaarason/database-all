package gaarason.database.eloquent;

import gaarason.database.contracts.eloquent.Repository;
import gaarason.database.eloquent.annotations.Primary;
import gaarason.database.utils.EntityUtil;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

abstract class Initializing<T> implements Repository<T> {

    /**
     * 主键列名(并非一定是实体的属性名)
     */
    @Getter
    protected String primaryKeyName;

    /**
     * 主键自增
     */
    @Getter
    protected boolean primaryKeyIncrement;

    /**
     * 实体类型
     */
    protected Class<T> entityClass;

    Initializing() {
        entityClass = entityClass();
        analysisEntityClass();
    }

    /**
     * 得到实体类型
     * @return 实体类型
     */
    @SuppressWarnings("unchecked")
    private Class<T> entityClass() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * 实体类型分析
     */
    private void analysisEntityClass() {
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Primary.class)) {
                Primary primary = field.getAnnotation(Primary.class);
                primaryKeyIncrement = primary.increment();
                primaryKeyName = EntityUtil.columnName(field);
                break;
            }
        }
    }
}
