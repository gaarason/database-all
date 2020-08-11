package gaarason.database.eloquent.annotations;

import gaarason.database.eloquent.Model;

import java.lang.annotation.*;

/**
 * 当前表的当前属性存在一个or多个下级关系
 * 数据库中表结构, 一般为下级表中存在 foreignKey 指向当前表的 id
 */
@Documented
@Inherited
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasOneOrMany {

    /**
     * 关联model, 下级关系model
     * @return
     */
    Class<? extends Model<?, ?>> sonModel();

    /**
     * 下级关系model的外键
     * @return
     */
    String sonModelForeignKey();

    /**
     * 当前model的关联id, 一般为model主键
     * @return
     */
    String localModelLocalKey() default "";

}
