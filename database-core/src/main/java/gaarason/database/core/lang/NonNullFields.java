package gaarason.database.core.lang;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.*;

/**
 * A common Spring annotation to declare that fields are to be considered as
 * non-nullable by default for a given package.
 *
 * <p>Leverages JSR-305 meta-annotations to indicate nullability in Java to common
 * tools with JSR-305 support and used by Kotlin to infer nullability of Spring API.
 *
 * <p>Should be used at package level in association with {@link Nullable}
 * annotations at field level.
 * @author Sebastien Deleuze
 * @see NonNullApi
 * @see Nullable
 * @see NonNull
 * @since 5.0
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Nonnull
@TypeQualifierDefault(ElementType.FIELD)
public @interface NonNullFields {
}
