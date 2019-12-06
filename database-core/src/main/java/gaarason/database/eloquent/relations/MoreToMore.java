package gaarason.database.eloquent.relations;

import java.lang.annotation.*;

@Documented
@Inherited
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MoreToMore {


    /**
     * 加载策略
     * @return
     */
    String fetch() default "";

    /**
     * 级联策略
     * @return
     */
    String cascade() default  "";

    /**
     * 关联字段
     * @return
     */
    String mappedBy() default "";

    /**
     * 可否为空
     * @return
     */
    boolean optional() default true;
}
