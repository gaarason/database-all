package gaarason.database.eloquent.annotation;

import gaarason.database.contract.eloquent.Model;

import java.io.Serializable;
import java.lang.annotation.*;

/**
 * 类属性必须是 复数
 * @author xt
 */
@Documented
@Inherited
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BelongsToMany {

    /**
     * `关系表`model
     * @return `关系表`
     */
    Class<? extends Model<? extends Serializable, ? extends Serializable>> relationModel();

    /**
     * `本表`中的`关联键`
     * @return `本表`中的`关联键`
     */
    String localModelLocalKey();

    /**
     * `关系表`中的`关联本表的外键`
     * @return `关系表`中的`关联本表的外键`
     */
    String foreignKeyForLocalModel();

    /**
     * `关系表`中的`关联目标表的外键`
     * @return `关系表`中的`关联目标表的外键`
     */
    String foreignKeyForTargetModel();

    /**
     * `目标表`中的`关联键`
     * @return `目标表`中的`关联键`
     */
    String targetModelLocalKey();
}
