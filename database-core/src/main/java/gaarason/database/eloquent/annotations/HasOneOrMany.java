package gaarason.database.eloquent.annotations;

import gaarason.database.eloquent.Model;

import java.lang.annotation.*;

/**
 * 当前表的当前属性存在一个or多个下级关系
 * 数据库中表结构, 一般为下级表中存在 foreignKey 指向当前表的 id
 * 类属性 可是是单数 or 复数
 */
@Documented
@Inherited
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasOneOrMany {

    /**
     * `子表`model
     * @return
     */
    Class<? extends Model<?, ?>> sonModel();

    /**
     * `子表`中的`关联本表的外键`
     * @return
     */
    String sonModelForeignKey();

    /**
     * `本表`中的`关联键`, 默认值为`本表`的主键(`@Primary()`修饰的键)
     * @return
     */
    String localModelLocalKey() default "";

}
