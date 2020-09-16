package gaarason.database.eloquent.annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableGaarasonDatabase {
}
