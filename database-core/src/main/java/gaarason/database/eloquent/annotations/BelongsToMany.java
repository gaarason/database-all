package gaarason.database.eloquent.annotations;

import gaarason.database.eloquent.Model;

import java.lang.annotation.*;

@Documented
@Inherited
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BelongsToMany {

    /**
     * 关联model
     * @return
     */
    Class<? extends Model<?, ?>> targetModel();

    /**
     * 关联关系中间model
     * @return
     */
    Class<? extends Model<?, ?>> relationModel();

    String modelLocalKey();

    String targetModelLocalKey();

    String modelForeignKey();

    String targetModelForeignKey();
}
