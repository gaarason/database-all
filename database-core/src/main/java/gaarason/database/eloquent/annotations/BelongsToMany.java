package gaarason.database.eloquent.annotations;

import gaarason.database.contract.eloquent.Model;

import java.lang.annotation.*;

/**
 * 类属性必须是 复数
 */
@Documented
@Inherited
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BelongsToMany {

    /**
     * `目标表`model
     * @return
     */
    Class<? extends Model<?, ?>> targetModel();

    /**
     * `关系表`model
     * @return
     */
    Class<? extends Model<?, ?>> relationModel();

    /**
     * `本表`中的`关联键`
     * @return
     */
    String localModelLocalKey();

    /**
     * `关系表`中的`关联本表的外键`
     * @return
     */
    String foreignKeyForLocalModel();

    /**
     * `关系表`中的`关联目标表的外键`
     * @return
     */
    String foreignKeyForTargetModel();

    /**
     * `目标表`中的`关联键`
     * @return
     */
    String targetModelLocalKey();
}
